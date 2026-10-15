package com.keytech.modules.line.efficiency.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.line.efficiency.entity.TbPkgClassSkuDetailOp;
import com.keytech.modules.line.efficiency.mapper.TbPkgClassSkuDetailOpMapper;
import com.keytech.modules.line.efficiency.service.ITbPkgClassSkuDetailOpService;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: OPI拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-03
 * @Version: V1.0
 */
@Service
@DS("opi")
public class TbPkgClassSkuDetailOpServiceImpl extends BaseServiceImpl<TbPkgClassSkuDetailOpMapper, TbPkgClassSkuDetailOp> implements ITbPkgClassSkuDetailOpService {


    /**
     * 拆分
     * 1.写入拆数据
     * 2.删除历史数据
     * @param tbPkgClassSkuDetailOpList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dataSplit(List<TbPkgClassSkuDetailOp> tbPkgClassSkuDetailOpList) {
        if(tbPkgClassSkuDetailOpList != null && !tbPkgClassSkuDetailOpList.isEmpty()){
            //原数据id
            String mainId = tbPkgClassSkuDetailOpList.get(0).getMainId();
            //写入拆数据
            saveBatch(tbPkgClassSkuDetailOpList);
            //删除历史数据
            removeById(mainId);
        }

    }
}
