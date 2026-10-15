package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.entity.TbPkgClassSkuDetailOp;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: OPI拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-03
 * @Version: V1.0
 */
public interface ITbPkgClassSkuDetailOpService extends BaseService<TbPkgClassSkuDetailOp> {

    /**
     * 拆分
     * @param tbPkgClassSkuDetailOpList
     */
    void dataSplit(List<TbPkgClassSkuDetailOp> tbPkgClassSkuDetailOpList);
}
