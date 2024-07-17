package com.yupi.yuapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.yuapiclientsdk.model.User;
import com.yupi.yuapiclientsdk.utils.signUtils;

import java.util.HashMap;
import java.util.Map;

public class CommonApiClient {
    protected final static String GATEWAY_HOST = "http://localhost:8090";
    protected final String accessKey;
    protected final String secretKey;

    public CommonApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    protected static Map<String,String> getHeadMap(String body, String accessKey, String secretKey){
        //六个参数
        Map<String,String> headMap = new HashMap<>();
        headMap.put("accessKey",accessKey);
        headMap.put("body",body);
        headMap.put("sign", signUtils.genSign(body,secretKey));
        headMap.put("nonce", RandomUtil.randomNumbers(5));
        //当下时间/1000，时间戳大概10位
        headMap.put("timestamp",String.valueOf(System.currentTimeMillis()/1000));
        return headMap;
    }


}
