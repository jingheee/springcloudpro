package com.atguigu.gulimall.thirdparty.controller;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.atguigu.common.config.S3Config;
import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class OssController {

    private static final AmazonS3 s3 = S3Config.client();
    public static final String BUCKET = "brand";

    @RequestMapping("/oss/policy")
    public R policy() throws IOException {
//        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint

        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // 用户上传文件时指定的前缀。 // 如果想以日期为文件夹
        String dir = format;

        Map<String, String> respMap = new LinkedHashMap<>();
//        try {
//            long expireTime = 30;
//            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
//            Date expiration = new Date(expireEndTime);
//            PolicyConditions policyConds = new PolicyConditions();
//            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
//            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
//
//            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
//            byte[] binaryData = postPolicy.getBytes("utf-8");
//            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
//            String postSignature = ossClient.calculatePostSignature(postPolicy);
//
//            respMap= new LinkedHashMap<String, String>();
//            respMap.put("accessid", accessId);
//            respMap.put("policy", encodedPolicy);
//            respMap.put("signature", postSignature);
//            respMap.put("dir", dir);
//            respMap.put("host", host);
//            respMap.put("expire", String.valueOf(expireEndTime / 1000));
//
//        } catch (Exception e) {
//            // Assert.fail(e.getMessage());
//            System.out.println(e.getMessage());
//        } finally {
//            ossClient.shutdown();
//        }

        return R.ok().put("data", respMap);

    }

    @RequestMapping("/oss/upload")
    public R upload(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectMetadata data = new ObjectMetadata();
        data.setContentType(file.getContentType());
        data.setContentLength(file.getSize());
        String key = IdUtil.fastSimpleUUID();
        if (StrUtil.isNotEmpty(file.getOriginalFilename())) {
            String[] split = file.getOriginalFilename().split("\\.");
            if (ArrayUtil.isNotEmpty(split)) {
                key += "." + split[split.length - 1];
            }
        }
        s3.putObject(BUCKET, key, file.getInputStream(), data);
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("key", key);
        return R.ok().put("data", map);
    }
}