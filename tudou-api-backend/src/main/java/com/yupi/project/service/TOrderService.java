package com.yupi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.project.model.dto.order.OrderQueryRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.project.model.vo.OrderVO;
import com.yupi.yupicommon.model.entity.TOrder;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86147
* @description 针对表【t_order】的数据库操作Service
* @createDate 2024-05-25 13:30:44
*/
public interface TOrderService extends IService<TOrder> {

    Page<OrderVO> pageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request);

}
