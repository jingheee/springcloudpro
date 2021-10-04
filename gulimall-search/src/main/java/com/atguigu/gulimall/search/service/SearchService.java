package com.atguigu.gulimall.search.service;


import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * <p>Title: MallService</p>
 * Description：
 * date：2020/6/12 23:05
 */
public interface SearchService {

	/**
	 * 检索所有参数
	 */
	SearchResult search(SearchParam Param);
}
