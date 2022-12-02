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

package com.atguigu.gulimall.cart.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 */
@Configuration
public class GlFeignConfig {

	@Bean("requestInterceptor")
	public RequestInterceptor requestInterceptor(){
		// Feign在远程调用之前都会先经过这个方法
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				// RequestContextHolder拿到刚进来这个请求
				ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
				if(attributes != null){
					HttpServletRequest request = attributes.getRequest();
					if(request != null){
						// 同步请求头数据
						String cookie = request.getHeader("Cookie");
						// 给新请求同步Cookie
						template.header("Cookie", cookie);
					}
				}
			}
		};
	}
}
