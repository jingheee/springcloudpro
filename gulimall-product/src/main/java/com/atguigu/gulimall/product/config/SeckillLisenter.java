package com.atguigu.gulimall.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillLisenter {

    @KafkaListener(groupId = "product", topics = "seckill")
    public void listener(String message) {
        log.info("receive msg {}", message);
    }
}
