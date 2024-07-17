package com.yupi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.project.model.dto.userinterfaceinfo.UpdateUserInterfaceInfoDTO;
import com.yupi.project.model.vo.InterfaceInfoVo;
import com.yupi.project.model.vo.UserInterfaceInfoVO;
import com.yupi.yupicommon.model.entity.InterfaceInfo;
import com.yupi.yupicommon.model.entity.UserInterfaceInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86147
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2024-05-13 15:33:00
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    boolean updateUserInterfaceInfo(UpdateUserInterfaceInfoDTO updateUserInterfaceInfoDTO);


    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    @Transactional
    boolean invokeCount(long userId, long interfaceInfoId);

    boolean recoverInvokeCount(long userId, long interfaceInfoId);

    int getLeftInvokeCount(long userId, long interfaceInfoId);

    List<UserInterfaceInfoVO> getInterfaceInfoByUserId(Long userId, HttpServletRequest request);

    List<InterfaceInfoVo> interfaceInvokeTopAnalysis(int limit);
}
