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

package com.atguigu.common.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Title: ListValue</p>
 * Description：JSR303自定义注解 必须有前三个方法
 * date：2020/6/1 23:25
 */
@Documented
// 指定校验器   这里可以指定多个不同的校验器
@Constraint(validatedBy = { ListValueConstraintValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ListValue {
	// 1 使用该属性去Validation.properties中取
	String message() default "{com.atguigu.common.valid.ListValue.message}";

	// 2
	Class<?>[] groups() default { };

	// 3
	Class<? extends Payload>[] payload() default { };

	int[] vals() default { };
}
