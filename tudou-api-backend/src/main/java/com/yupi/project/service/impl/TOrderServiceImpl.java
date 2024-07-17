package com.yupi.project.service.impl;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.TOrderMapper;
import com.yupi.project.model.dto.order.OrderQueryRequest;
import com.yupi.project.model.enums.OrderStatusEnum;
import com.yupi.project.model.vo.OrderVO;
import com.yupi.project.service.TOrderService;
import com.yupi.project.service.UserService;
import com.yupi.yupicommon.model.entity.InterfaceInfo;
import com.yupi.yupicommon.model.entity.TOrder;
import com.yupi.yupicommon.model.entity.User;
import com.yupi.yupicommon.service.ApiBackendService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 86147
* @description 针对表【t_order】的数据库操作Service实现
* @createDate 2024-05-25 14:17:10
*/
@Service
public class TOrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder>
    implements TOrderService {
        @Resource
        private UserService userService;

        @Resource
        private ApiBackendService apiBackendService;
        @Override
        public Page<OrderVO> pageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
            String requestType = orderQueryRequest.getType();
            int current = orderQueryRequest.getCurrent();
            int size = orderQueryRequest.getPageSize();
            if(OrderStatusEnum.getValues().contains(requestType)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User loginUser = userService.getLoginUser(request);
            QueryWrapper<TOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId",loginUser.getId()).eq("status",requestType);
            Page<TOrder> page = new Page<>(current, size);
            Page<TOrder> tOrderPage = page(page, queryWrapper);

            Page<OrderVO> orderVOPage = new Page<>(tOrderPage.getCurrent(), tOrderPage.getSize(), tOrderPage.getTotal());

            List<OrderVO> collect = tOrderPage.getRecords().stream().map(order -> {
                Long interfaceId = order.getInterfaceId();
                InterfaceInfo interfaceInfoById = apiBackendService.getInterfaceInfoById(interfaceId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);

                orderVO.setTotal(order.getCount().longValue());
                orderVO.setTotalAmount(order.getTotalAmount().doubleValue());
                orderVO.setOrderNumber(order.getOrderSn());

                orderVO.setInterfaceName(interfaceInfoById.getName());
                orderVO.setInterfaceDesc(interfaceInfoById.getDescription());
                orderVO.setExpirationTime(DateUtil.offset(order.getCreateTime(), DateField.MINUTE, 30));
                return orderVO;
            }).collect(Collectors.toList());
                orderVOPage.setRecords(collect);
            return orderVOPage;

    }
}




