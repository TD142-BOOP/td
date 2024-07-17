package com.yupi.project.listener;


import com.yupi.project.service.UserInterfaceInfoService;
import com.yupi.yupicommon.model.vo.UserInterFaceInfoMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import com.rabbitmq.client.Channel;

import java.io.IOException;

import static com.yupi.yupicommon.constant.RabbitmqConstant.QUEUE_INTERFACE_CONSISTENT;

@Slf4j
@Component
public class InterfaceInvokeListener {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @RabbitListener(queuesToDeclare = {@Queue(QUEUE_INTERFACE_CONSISTENT)})
    private void receive(UserInterFaceInfoMessage userInterfaceInfoMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息啦，内容是："+userInterfaceInfoMessage);
        Long interFaceInfoId = userInterfaceInfoMessage.getInterFaceInfoId();
        Long userId = userInterfaceInfoMessage.getUserId();
        boolean result = false;
        try{
            result=userInterfaceInfoService.recoverInvokeCount(interFaceInfoId,userId);
        }catch (Exception e){
            log.info("回滚失败");
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            return;
        }
        if(!result){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
