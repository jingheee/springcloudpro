/*
 *    Copyright [2022] [lazyd0g]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.atguigu.gulimall.thirdparty.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.atguigu.common.config.S3Config;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: lazyd0g
 * @Date: 2022/9/10
 */
@RestController
public class PicController {
    private static final AmazonS3 s3 = S3Config.client();

    @GetMapping("/oss/{key}")
    public ResponseEntity getPic(@PathVariable("key") String key) {
        S3Object object = s3.getObject(OssController.BUCKET, key);
        val objectMetadata = object.getObjectMetadata();

        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .allow(HttpMethod.GET, HttpMethod.OPTIONS)
                .contentLength(objectMetadata.getContentLength())
                .contentType(MediaType.valueOf(objectMetadata.getContentType()))
                .body(new InputStreamResource(object.getObjectContent()));
    }
}
