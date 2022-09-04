package com.atguigu.gulimall.malltestssoclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MallTestSsoClientApplicationA {

    //	http://client1.com/
    static {
        try {
            Class.forName("org.burningwave.core.assembler.StaticComponentContainer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MallTestSsoClientApplicationA.class, args);
    }

}
