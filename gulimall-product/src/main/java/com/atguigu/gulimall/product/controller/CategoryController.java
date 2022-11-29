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

package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



/**
 * 商品三级分类
 *
 * @author hh
 * @email 55333@qq.com
 * @date 2020-06-23 21:00:59
 */
@RestController
@RequestMapping("product/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    CategoryDao categoryDao;

    @PostMapping("/userinfo")
    public String userinfo() {
        return """
                { "$id": "user-info", "username": "法外狂徒", "name": "野兽先辈", "sex": "男", "_isOptionsAPI": true }""";
    }

    @PostMapping("/list")
    public R list(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;
        PageHelper.startPage(page, size);
        List<CategoryEntity> categoryEntities = categoryDao.selectList(null);
        PageInfo<CategoryEntity> res = PageInfo.of(categoryEntities);
        return R.ok().put("data", res);
    }


    /**
     * 查出所有分类 以及子分类，以树形结构组装起来
     */

    private static int ZERO = 0;
    private static int CNT = 0;

    @Value("${server.port}")
    private String port;

    @RequestMapping("/list/tree")
//    @PreAuthorize("hasAnyAuthority('admin')")
    public R list() throws InterruptedException {
        log.info("第{}访问", CNT++);
//        Thread.sleep(200);
//        if (ThreadLocalRandom.current().nextBoolean())
//        {
//            throw new RuntimeException();
//        }
//        if (ZERO == 1) {
//            ZERO = 0;
//            throw new RuntimeException();
//        }
        ZERO++;

        List<CategoryEntity> entities = categoryService.listWithTree();
        // 筛选出所有一级分类
//        List<CategoryEntity> level1Menus = entities.stream().
//                filter((categoryEntity) -> categoryEntity.getParentCid() == 0)
//                .map((menu) -> {
//                    menu.setChildren(getChildrens(menu, entities));
//                    return menu;
//                }).sorted((menu1, menu2) -> {
//                    return (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null? 0 : menu2.getSort());
//                })
//                .collect(Collectors.toList());
        return R.ok().put("data", port);
    }

    /**
     * 递归找所有的子菜单、中途要排序
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == root.getCatId()
        ).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1,menu2) -> {
            return (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
        categoryService.save(category);
        return R.ok();
    }

    /**
     * 批量修改层级
     */
    @RequestMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category){
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     * 必须发送POST请求
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
        categoryService.removeByIds(Arrays.asList(catIds));
        // 检查当前节点是否被别的地方引用
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
