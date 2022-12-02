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

package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

import java.util.List;
/**
 * <p>Title: PurchaseDoneVo</p>
 * Description：采购单
 * date：2020/6/6 23:23
 */
@Data
public class PurchaseDoneVo {

	/** 采购单id*/
    @NotNull
    private Long id;

    /** 采购项(需求) */
    private List<PurchaseItemDoneVo> items;
}
