package com.atguigu.gulimall.product.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 怎么拿categoryDao？
        /*
        * 继承了ServiceImpl<CategoryDao, CategoryEntity>
        有个属性baseMapper，自动注入
        * */


        // 1 查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2 组装成父子的树型结构
        // 2.1 找到所有一级分类
        List<CategoryEntity> level1Menus = categoryEntities.stream().filter(
                categoryEntity -> categoryEntity.getParentCid() == 0
        ).collect(Collectors.toList());

        return level1Menus;
//        return categoryEntities;
    }

}