package com.yupi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.project.common.BaseResponse;
import com.yupi.project.common.ResultUtils;
import com.yupi.project.model.dto.order.OrderQueryRequest;
import com.yupi.project.model.vo.OrderVO;
import com.yupi.project.service.TOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    private TOrderService tOrderService;
    @GetMapping("/list")
    public BaseResponse<Page<OrderVO>> listPageOrder(OrderQueryRequest orderQueryRequest,HttpServletRequest request){
        Page<OrderVO> orderVOPage = tOrderService.pageOrder(orderQueryRequest, request);
        return ResultUtils.success(orderVOPage);
    }
}
