package com.yupi.project;

import com.yupi.project.service.impl.UserInterfaceInfoServiceImpl;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class Test {
    @Resource
    private UserInterfaceInfoServiceImpl userInterfaceInfoService;
    @org.junit.jupiter.api.Test
     public void test(){
         boolean invoked = userInterfaceInfoService.invokeCount(1L, 1L);
         System.out.println(invoked);
     }
}
