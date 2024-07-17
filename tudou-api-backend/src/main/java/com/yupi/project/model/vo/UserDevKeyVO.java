package com.yupi.project.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDevKeyVO implements Serializable {
    /**
     * 签名 accessKey
     */
    private String accessKey;

    /**
     * 签名 secretKey
     */
    private String secretKey;
    private static final long serialVersionUID = 1L;
}
