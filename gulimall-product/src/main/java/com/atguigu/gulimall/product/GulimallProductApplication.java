package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.product.dao")
@EnableFeignClients(basePackages="com.atguigu.gulimall.product.feign")// 扫描远程调用接口
@SpringBootApplication
public class GulimallProductApplication {


    public static void main(String[] args) {

        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
