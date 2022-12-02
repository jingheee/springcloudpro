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

package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.SearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>Title: SearchController</p>
 * Description：
 */
@Controller
public class SearchController {

	@Autowired
	private SearchService searchService;

	@GetMapping("/list.html")
	public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){

		// 获取路径原生的查询属性
		searchParam.set_queryString(request.getQueryString());
		// ES中检索到的结果 传递给页面
		SearchResult result = searchService.search(searchParam);
		model.addAttribute("result", result);
		return "list";
	}
}
