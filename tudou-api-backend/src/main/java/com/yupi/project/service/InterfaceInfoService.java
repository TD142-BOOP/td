package com.yupi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.project.model.dto.userinterfaceinfo.UpdateUserInterfaceInfoDTO;
import com.yupi.yupicommon.model.entity.InterfaceInfo;

/**
* @author 86147
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-05-11 17:49:29
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

}
