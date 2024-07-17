//package com.yupi.yuapiinterface;
//
//
//
//
//
//import com.yupi.yuapiclientsdk.client.YuApiClient;
//
//import com.yupi.yuapiclientsdk.model.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//
//
//@SpringBootTest
//class YuapiInterfaceApplicationTests {
//    @Resource
//    private YuApiClient yuApiClient;
//
//    @Test
//    void contextLoads() {
//        String result = yuApiClient.getNameByGet("tudou");
//        User user = new User();
//        user.setUserName("tudou");
//        String usernameByPost = yuApiClient.getUserNameByPost(user);
//        System.out.println(result);
//        System.out.println(usernameByPost);
//
//    }
//
//}
