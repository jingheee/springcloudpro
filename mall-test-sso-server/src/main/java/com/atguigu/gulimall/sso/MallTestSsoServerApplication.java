package com.atguigu.gulimall.sso;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MallTestSsoServerApplication {

    static {
        try {
            Class.forName("org.burningwave.core.assembler.StaticComponentContainer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MallTestSsoServerApplication.class, args);
    }
}
