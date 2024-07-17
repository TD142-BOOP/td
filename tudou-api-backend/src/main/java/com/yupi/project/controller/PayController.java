//package com.yupi.project.controller;
//
//
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.extra.qrcode.QrCodeUtil;
//import cn.hutool.extra.qrcode.QrConfig;
//import com.alipay.api.AlipayApiException;
//import com.alipay.api.AlipayClient;
//import com.alipay.api.DefaultAlipayClient;
//import com.alipay.api.domain.AlipayTradePrecreateModel;
//import com.alipay.api.request.AlipayTradePrecreateRequest;
//import com.alipay.api.response.AlipayTradePrecreateResponse;
//import com.yupi.project.common.BaseResponse;
//import com.yupi.project.common.ResultUtils;
//import com.yupi.project.config.AliPayConfig;
//import com.yupi.project.model.dto.alipay.AlipayInfoRequest;
//import com.yupi.project.service.AlipayInfoService;
//import com.yupi.project.utils.OrderPaySuccessMqUtils;
//import com.yupi.yupicommon.model.entity.AlipayInfo;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import com.alipay.api.internal.util.AlipaySignature;
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import static com.yupi.project.constant.RedisConstant.ALIPAY_TRADE_SUCCESS_RECORD;
//import static com.yupi.project.constant.RedisConstant.EXIST_KEY_VALUE;
//
//@Slf4j
//@RestController
//@RequestMapping("/third/alipay")
//public class PayController {
//
//    @Resource
//    private AliPayConfig aliPayConfig;
//
//    @Resource
//    private OrderPaySuccessMqUtils orderPaySuccessMqUtils;
//    @Resource
//    private AlipayInfoService alipayInfoService;
//    @Resource
//    private StringRedisTemplate stringRedisTemplate;
//
//    @PostMapping("/payCode")
//    public BaseResponse<String> payCode(@RequestBody AlipayInfoRequest alipayInfoRequest) throws AlipayApiException {
//        String subject = alipayInfoRequest.getSubject();
//        Double totalAmount = alipayInfoRequest.getTotalAmount();
//        String tradeNo = alipayInfoRequest.getTradeNo();
//
//        AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.getGatewayUrl(),
//                aliPayConfig.getAppId(),
//                aliPayConfig.getPrivateKey(),
//                "json",
//                aliPayConfig.getCharset(),
//                aliPayConfig.getPublicKey(),
//                aliPayConfig.getSignType());
//        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
//        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
//
//        request.setNotifyUrl("http://localhost:8101/api/third/alipay/notify");
//        request.setBizModel(model);
//        model.setOutTradeNo(tradeNo);
//        model.setSubject(subject);
//        model.setTotalAmount(String.valueOf(totalAmount));
//
//        AlipayTradePrecreateResponse response = alipayClient.execute(request);
//
//        log.info("响应支付二维码详情："+response.getBody());
//
//        String generated = QrCodeUtil.generateAsBase64(response.getQrCode(), new QrConfig(300, 300), "png");
//        return ResultUtils.success(generated);
//    }
//
//
//    @Transactional(rollbackFor = Exception.class)
//    @PostMapping("/notify")
//    public synchronized void payNotify(HttpServletRequest request) throws AlipayApiException {
//        if(request.getParameter("trade_status").equals("TRADE_SUCCESS")){
//            Map<String,String> params = new HashMap<>();
//            Map<String,String[]> requestParams = request.getParameterMap();
//            for(String str:requestParams.keySet()){
//                params.put(str,request.getParameter(str));
//            }
//            if(AlipaySignature.rsaCheckV1(params,aliPayConfig.getPublicKey(),aliPayConfig.getCharset(),aliPayConfig.getSignType())){
//                log.info("支付成功:{}",params);
//                Object outTradeNo = stringRedisTemplate.opsForValue().get(ALIPAY_TRADE_SUCCESS_RECORD + params.get("out_trade_no"));
//                if(null==outTradeNo){
//                    // 验签通过，将订单信息存入数据库
//                    AlipayInfo alipayInfo = new AlipayInfo();
//                    alipayInfo.setSubject(params.get("subject"));
//                    alipayInfo.setTradeStatus(params.get("trade_status"));
//                    alipayInfo.setTradeNo(params.get("trade_no"));
//                    alipayInfo.setOrderNumber(params.get("out_trade_no"));
//                    alipayInfo.setTotalAmount(Double.valueOf(params.get("total_amount")));
//                    alipayInfo.setBuyerId(params.get("buyer_id"));
//                    alipayInfo.setGmtPayment(DateUtil.parse(params.get("gmt_payment")));
//                    alipayInfo.setBuyerPayAmount(Double.valueOf(params.get("buyer_pay_amount")));
//                    alipayInfoService.save(alipayInfo);
//                    //记录处理成功的订单，实现订单幂等性
//                    stringRedisTemplate.opsForValue().set(ALIPAY_TRADE_SUCCESS_RECORD +alipayInfo.getOrderNumber(),EXIST_KEY_VALUE,30, TimeUnit.MINUTES);
//                    //修改数据库，完成整个订单功能
//                    orderPaySuccessMqUtils.sendOrderPaySuccess(params.get("out_trade_no"));
//                }
//
//            }
//        }
//
//    }
//}
