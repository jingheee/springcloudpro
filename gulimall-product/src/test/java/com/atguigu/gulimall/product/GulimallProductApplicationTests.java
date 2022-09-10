package com.atguigu.gulimall.product;


import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.atguigu.common.config.S3Config;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;


    static final String Path = "C:\\Users\\benja\\Documents\\opensource\\gulimallPro\\gulimall-product\\src\\main\\resources\\static\\";
    @Autowired
    CategoryService categoryService;

    @Test
    void excel() throws IOException {
        BigExcelWriter bigWriter = ExcelUtil.getBigWriter();
        List<CategoryEntity> list = categoryService.list();
        for (int i = 0; i < 7; i++) {
            list.addAll(list);
        }
        AmazonS3 client = S3Config.client();
//        Bucket excel = client.createBucket("excel");
        bigWriter.write(list);
        try (FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream()) {
            bigWriter.flush(fastByteArrayOutputStream, true);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fastByteArrayOutputStream.toByteArray());
            client.putObject("excel", "spring.xlsx", inputStream, null);
            System.out.println(inputStream);
        }

    }


    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("修改");
        brandService.updateById(brandEntity);
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功");
//        System.out.println(brandService.getById(1L));
    }


    public void test1() {

        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
        // 扫描的配置类，如@ComponentScan
        // 会去解析配置类然后实例化一个扫描器去扫描
        ac.register(A.class);
        ac.scan("");
        ac.refresh();
        String[] bds = ac.getBeanDefinitionNames();


        Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                A.class.getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object invoke = method.invoke(proxy, args);
                        return invoke;

                    }
                });
    }
}

interface A {
    public int test();
}
