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

package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * 品牌
 * 
 * @author hh
 * @email 55333@qq.com
 * @date 2020-06-23 20:09:14
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 * POSTman：{"name":"aaa","logo":"abc","brandId":1}
	 */
	@NotNull(message = "修改必须定制品牌id", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定id", groups = {AddGroup.class})
	@TableId
	private Long brandId;

	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;

	/**
	 * 品牌logo地址 修改可以不带上logoURL
	 */
	@NotBlank(groups = {AddGroup.class})
	@URL(message = "logo必须是一个合法的URL地址", groups={AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(vals = {0,1}, groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;

	/**
	 * 检索首字母  修改可以不带, 不管是新增还是修改都必须是一个字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须是一个正整数" , groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
