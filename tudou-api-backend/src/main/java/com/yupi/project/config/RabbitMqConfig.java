package com.yupi.project.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.yupi.yupicommon.constant.RabbitmqConstant.*;

@Configuration
@Slf4j
public class RabbitMqConfig {
    @Bean(EXCHANGE_INTERFACE_CONSISTENT)
    public Exchange EXCHANGE_INTERFACE_CONSISTENT(){
        return new DirectExchange(EXCHANGE_INTERFACE_CONSISTENT,true,false);
    }

    @Bean(QUEUE_INTERFACE_CONSISTENT)
    public Queue QUEUE_INTERFACE_CONSISTENT(){
        return new Queue(QUEUE_INTERFACE_CONSISTENT,true,false,false);
    }

    @Bean
    public Binding BINDING_QUEUE_INTERFACE_CONSISTENT(){
        return new Binding(QUEUE_INTERFACE_CONSISTENT,Binding.DestinationType.QUEUE,EXCHANGE_INTERFACE_CONSISTENT,ROUTING_KEY_INTERFACE_CONSISTENT,null);
    }
    //声明交换机
    @Bean
    public Exchange alipayExchange(){
        return new DirectExchange(ORDER_EXCHANGE_NAME,true,false);
    }

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue alipayQueue(){
        return new Queue(ORDER_SUCCESS_QUEUE_NAME,true,false,false ,null);
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding alipayBinding(){
        return new Binding(ORDER_SUCCESS_QUEUE_NAME, Binding.DestinationType.QUEUE,ORDER_EXCHANGE_NAME,ORDER_SUCCESS_EXCHANGE_ROUTING_KEY,null);
    }

}
