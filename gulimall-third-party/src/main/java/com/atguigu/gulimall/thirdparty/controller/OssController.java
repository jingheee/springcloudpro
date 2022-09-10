package com.atguigu.gulimall.thirdparty.controller;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.atguigu.common.config.S3Config;
import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class OssController {

    private static final AmazonS3 s3 = S3Config.client();
    private static final String BUCKET = "brand";

    @RequestMapping("/oss/policy")
    public R policy() throws IOException {

        return R.ok().put("data", "ok");
    }

    @RequestMapping("/oss/upload")
    public R upload(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectMetadata data = new ObjectMetadata();
        data.setContentType(file.getContentType());
        data.setContentLength(file.getSize());
        PutObjectResult putObjectResult = s3.putObject(BUCKET, file.getOriginalFilename(), file.getInputStream(), data);
        return R.ok().put("data", putObjectResult.getContentMd5());
    }
}