package com.yupi.project.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.InterfaceInfoMapper;
import com.yupi.project.mapper.UserMapper;
import com.yupi.project.service.UserInterfaceInfoService;
import com.yupi.yupicommon.model.entity.InterfaceInfo;
import com.yupi.yupicommon.model.entity.User;
import com.yupi.yupicommon.service.ApiBackendService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
@DubboService
public class ApiBackendServiceImpl implements ApiBackendService {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if(StringUtils.isAnyBlank(url,method)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url",url);
        queryWrapper.eq("method",method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }
    @Override
    public boolean invokeCount(Long interfaceInfoId, Long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }
    @Override
    public int getLeftInvokeCount(Long interfaceInfoId, Long userId) {
        return userInterfaceInfoService.getLeftInvokeCount(interfaceInfoId, userId);
    }
    @Override
    public User getInvokeUser(String accessKey) {
        if(StringUtils.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey",accessKey);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public InterfaceInfo getInterfaceInfoById(Long id) {
        if(id==null||id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }
}
