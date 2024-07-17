package com.yupi.project.service.impl;

import static com.yupi.project.constant.UserConstant.ADMIN_ROLE;
import static com.yupi.project.constant.UserConstant.USER_LOGIN_STATE;
import static com.yupi.yupicommon.constant.RabbitmqConstant.*;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.constant.CommonConstant;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.exception.ThrowUtils;
import com.yupi.project.mapper.UserMapper;
import com.yupi.project.model.dto.user.UserQueryRequest;
import com.yupi.project.model.dto.user.UserUpdateRequest;
import com.yupi.project.model.enums.UserRoleEnum;
import com.yupi.project.model.vo.LoginUserVO;
import com.yupi.project.model.vo.UserDevKeyVO;
import com.yupi.project.model.vo.UserVO;
import com.yupi.project.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yupi.project.utils.FileUploadUtil;
import com.yupi.project.utils.LeakyBucket;
import com.yupi.project.utils.SqlUtils;
import com.yupi.yupicommon.Utils.JwtUtils;
import com.yupi.yupicommon.model.entity.SmsMessage;
import com.yupi.yupicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //登录和注册的标识，方便切换不同的令牌桶来限制验证码发送
    @Resource
    private RabbitTemplate rabbitTemplate;
    private static final String LOGIN_SIGN = "login";

    private static final String REGISTER_SIGN="register";
    /**
     * 图片验证码 redis 前缀
     */
    private static final String CAPTCHA_PREFIX = "api:captchaId:";
    public static final String USER_LOGIN_EMAIL_CODE ="user:login:phone:code:";
    public static final String USER_REGISTER_EMAIL_CODE ="user:register:phone:code:";
    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "tudou";

    @Resource
    private Gson gson;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 3. 插入数据
            User user = new User();
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setUserName(accessKey);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        return setLoginUser(response,user);
    }

    @Override
    public void sendCode(String phone, String captchaType) {
        if(StringUtils.isBlank(captchaType)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"验证码类型为空");
        }
        synchronized (phone.intern()){
            Boolean exits = stringRedisTemplate.hasKey(USER_LOGIN_EMAIL_CODE + phone);
            if(exits!=null&&exits){
                LeakyBucket leakyBucket = null;
                Long lastTime=0L;
                if(captchaType.equals(REGISTER_SIGN)){
                    String strLastTime = stringRedisTemplate.opsForValue().get(USER_REGISTER_EMAIL_CODE + phone);
                    if(strLastTime!=null){
                        lastTime = Long.parseLong(strLastTime);
                    }
                    leakyBucket=LeakyBucket.registerLeakyBucket;
                }else {
                    String strLastTime = stringRedisTemplate.opsForValue().get(USER_LOGIN_EMAIL_CODE + phone);
                    if(strLastTime!=null){
                        lastTime = Long.parseLong(strLastTime);
                    }
                    leakyBucket=LeakyBucket.loginLeakyBucket;
                }
                if(!leakyBucket.control(lastTime)){
                    log.info("邮箱发送太频繁了");
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱发送太频繁了");
                }
            }
            String code = RandomUtil.randomNumbers(4);
            SmsMessage smsMessage = new SmsMessage(phone, code);

            rabbitTemplate.convertAndSend(EXCHANGE_SMS_INFORM,ROUTINGKEY_SMS,smsMessage);

            if(captchaType.equals(REGISTER_SIGN)){
                stringRedisTemplate.opsForValue().set(USER_REGISTER_EMAIL_CODE+phone,""+System.currentTimeMillis()/1000);
            }else {
                stringRedisTemplate.opsForValue().set(USER_LOGIN_EMAIL_CODE+phone,""+System.currentTimeMillis()/1000);
            }
        }
    }

    private LoginUserVO setLoginUser(HttpServletResponse response,User user){
        String token = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        Cookie cookie = new Cookie("token",token);
        cookie.setPath("/");
        response.addCookie(cookie);
        String gsonJson = gson.toJson(user);
        stringRedisTemplate.opsForValue().set(LOGIN_SIGN+user.getId(),gsonJson,JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Long userId = JwtUtils.getUserIdByToken(request);
        if (userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_STATE+userId);
        User user = gson.fromJson(userJson, User.class);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request,HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie:cookies){
            if(cookie.getName().equals("token")){
                Long userId = JwtUtils.getUserIdByToken(request);
                stringRedisTemplate.delete(USER_LOGIN_STATE+userId);
                Cookie timeOutCookie = new Cookie(cookie.getName(), cookie.getValue());
                timeOutCookie.setMaxAge(0);
                response.addCookie(timeOutCookie);
                return true;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String signature = request.getHeader("signature");
        if(StringUtils.isEmpty(signature)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
            lineCaptcha.setGenerator(randomGenerator);
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中,2分钟过期
            stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX+signature,lineCaptcha.getCode(),2,TimeUnit.MINUTES);
            response.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateUserAvatar(MultipartFile file, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);

        User user = new User();
        user.setId(loginUser.getId());
        String url = FileUploadUtil.uploadFileAvatar(file);
        user.setUserAvatar(url);
        boolean result = this.updateById(user);

        loginUser.setUserAvatar(url);
        Gson gson = new Gson();
        String gsonJson = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE+loginUser.getId(),gsonJson,JwtUtils.EXPIRE,TimeUnit.MILLISECONDS);
        return result;
    }

    @Override
    public UserDevKeyVO genKey(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_FOUND_ERROR);

        UserDevKeyVO userDevKeyVO = genKey(loginUser.getUserAccount());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userAccount",loginUser.getUserAccount());
        updateWrapper.eq("id",loginUser.getId());
        updateWrapper.set("accessKey",userDevKeyVO.getAccessKey());
        updateWrapper.set("secretKey",userDevKeyVO.getSecretKey());
        this.update(updateWrapper);
        loginUser.setSecretKey(userDevKeyVO.getSecretKey());
        loginUser.setAccessKey(userDevKeyVO.getAccessKey());

        String gsonJson = gson.toJson(userDevKeyVO);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE+loginUser.getId(),gsonJson,JwtUtils.EXPIRE,TimeUnit.MILLISECONDS);
        return userDevKeyVO;


    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        Long updateRequestId = userUpdateRequest.getId();
        if(!loginUser.getId().equals(updateRequestId)){
            if(!loginUser.getUserRole().equals(ADMIN_ROLE)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);

        user.setUserName(userUpdateRequest.getUserName());
        user.setGender(userUpdateRequest.getGender());
        String gsonJson = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE+loginUser.getId(),gsonJson,JwtUtils.EXPIRE,TimeUnit.MILLISECONDS);
        return true;
    }


    public UserDevKeyVO genKey(String account) {
        String accessKey = DigestUtil.md5Hex(SALT + account + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + account + RandomUtil.randomNumbers(8));
        UserDevKeyVO userDevKeyVO = new UserDevKeyVO();
        userDevKeyVO.setAccessKey(accessKey);
        userDevKeyVO.setSecretKey(secretKey);
        return userDevKeyVO;
    }


}
