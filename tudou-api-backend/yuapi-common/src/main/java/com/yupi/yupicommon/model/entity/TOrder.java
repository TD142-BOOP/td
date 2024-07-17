package com.yupi.yupicommon.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @TableName t_order
 */
@TableName(value ="t_order")
@Data
public class TOrder implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 0-删除 1 正常
     */
    @TableId
    private Integer isDelete;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单应付价格
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态 0-未支付 1 -已支付 2-超时支付
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 单价
     */
    private Double charging;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}