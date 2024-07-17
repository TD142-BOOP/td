package com.yupi.project.model.dto.alipay;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName alipay_info
 */
@Data
public class AlipayInfoRequest implements Serializable {
    /**
     * 订单id
     */
    @TableId
    private String orderNumber;

    /**
     * 交易名称
     */
    private String subject;

    /**
     * 交易金额
     */
    private Double totalAmount;

    /**
     * 支付宝交易凭证号
     */
    private String tradeNo;
}