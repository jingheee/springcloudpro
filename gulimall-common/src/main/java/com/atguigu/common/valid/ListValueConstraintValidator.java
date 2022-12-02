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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;
/**
 * 校验器：规定ListValue这个注解 用于校验 Integer 类型的数据
 * 	POSTman :{"name":"aaa","logo":"https://github.com/1046762075","sort":0,"firstLetter":"d","showStatus":0}
 */
public class ListValueConstraintValidator
        implements ConstraintValidator<ListValue,Integer> {//泛型参数<校验注解,标注字段类型>

	/**
	 * set 里面就是使用注解时规定的值, 例如: @ListValue(vals = {0,1})  set= {0,1}
	 */
    private Set<Integer> set = new HashSet<>();

    //初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        // 获取java后端写好的限制
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * 判断是否校验成功
     * @param value 需要校验的值
	 *              判断这个值再set里面没
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // 每次请求传过来的值是否在java后端限制的值里
        return set.contains(value);
    }
}
