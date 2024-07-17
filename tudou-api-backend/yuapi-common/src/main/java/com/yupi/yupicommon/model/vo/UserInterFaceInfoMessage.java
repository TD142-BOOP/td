package com.yupi.yupicommon.model.vo;

import lombok.Data;

@Data
public class UserInterFaceInfoMessage {
    private Long userId;
    private Long interFaceInfoId;


    public UserInterFaceInfoMessage(Long userId, Long interFaceInfoId) {
        this.userId = userId;
        this.interFaceInfoId = interFaceInfoId;
    }
}
