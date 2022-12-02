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

package com.auguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * <p>Title: UserRegisterVo</p>
 * Description：
 * date：2020/6/25 17:09
 */
@Data
public class UserRegisterVo {

	// JSR303校验
	@Length(min = 6,max = 20,message = "用户名长度必须在6-20之间")
	@NotEmpty(message = "用户名必须提交")
	private String userName;

	@Length(min = 6,max = 20,message = "用户名长度必须在6-20之间")
	@NotEmpty(message = "密码必须提交")
	private String password;

	@NotEmpty(message = "手机号不能为空")
	@Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
	private String phone;

	@NotEmpty(message = "验证码必须填写")
	private String code;
}
