package com.yupi.yupicommon.service;

import com.yupi.yupicommon.model.entity.InterfaceInfo;
import com.yupi.yupicommon.model.entity.User;

public interface ApiBackendService {
    int getLeftInvokeCount(Long interfaceInfoId,Long userId);
    InterfaceInfo getInterfaceInfo(String path, String method);
    boolean invokeCount(Long interfaceInfoId,Long userId);
    User getInvokeUser(String accessKey);
    InterfaceInfo getInterfaceInfoById(Long id);
}
