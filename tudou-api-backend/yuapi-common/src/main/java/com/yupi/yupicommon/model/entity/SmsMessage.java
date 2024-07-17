package com.yupi.yupicommon.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@TableName(value = "user")
@Data
@AllArgsConstructor
public class SmsMessage implements Serializable {
    private String email;
    private String code;
}
