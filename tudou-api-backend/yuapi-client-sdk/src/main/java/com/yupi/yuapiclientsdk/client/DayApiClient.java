package com.yupi.yuapiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.yupi.yuapiclientsdk.model.User;

import java.util.Map;

/**
 * DayController-DayApiClient
 */
public class DayApiClient extends CommonApiClient {

    public DayApiClient(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    /**
     * 获取每日壁纸
     * @return
     */
    public String getName(User user){
        String json = JSONUtil.toJsonStr(user);
        return HttpRequest.post(GATEWAY_HOST+"/api/interface/name/user")
                .addHeaders(getHeadMap(json,accessKey,secretKey))
                .body(json)
                .execute().body();
    }

}
