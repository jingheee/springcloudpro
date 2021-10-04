package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>Title: OrderWebConfiguration</p>
 * Description：
 */
@Configuration
public class OrderWebConfiguration implements WebMvcConfigurer {

	@Autowired
	private LoginUserInterceptor loginUserInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 放行支付宝回调请求
		registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**").excludePathPatterns("/payed/notify");
	}
}
