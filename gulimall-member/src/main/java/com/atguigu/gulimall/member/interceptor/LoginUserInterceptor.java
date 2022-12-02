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

package com.atguigu.gulimall.member.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>Title: LoginUserInterceptor</p>
 * Description：
 * date：2020/7/4 22:35
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

	public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String uri = request.getRequestURI();
		// 这个请求直接放行
		boolean match = new AntPathMatcher().match("/member/**", uri);
		if(match){
			return true;
		}
		HttpSession session = request.getSession();
		MemberRespVo MemberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
		if(MemberRespVo != null){
			threadLocal.set(MemberRespVo);
			return true;
		}else{
			// 没登陆就去登录
			session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
			response.sendRedirect("http://auth.gulimall.com/login.html");
			return false;
		}
	}
}
