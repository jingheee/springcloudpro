package com.auguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.auguigu.gulimall.auth.vo.SocialUser;
import com.auguigu.gulimall.auth.vo.UserLoginVo;
import com.auguigu.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>Title: MemberFeignService</p>
 * Description：
 * date：2020/6/25 20:31
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

	@PostMapping("/member/member/register")
	R register(@RequestBody UserRegisterVo userRegisterVo);

	@PostMapping("/member/member/login")
	R login(@RequestBody UserLoginVo vo);

	@PostMapping("/member/member/oauth2/login")
	R login(@RequestBody SocialUser socialUser);
}
