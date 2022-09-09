package com.atguigu.gulimall.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class KakfaController {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @GetMapping("/send")
    public void send() {
        kafkaTemplate.send("seckill", ThreadLocalRandom.current().nextInt() + "");
    }
}
