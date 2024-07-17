package com.yupi.yuapiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;


public class signUtils {
    public static String genSign(String body,String secretKey){
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        String secret=body+"."+secretKey;
        return digester.digestHex(secret);
    }
}
