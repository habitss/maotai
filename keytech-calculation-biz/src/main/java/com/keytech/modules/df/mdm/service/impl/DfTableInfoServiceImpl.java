package com.keytech.modules.df.mdm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.keytech.modules.df.mdm.entity.DfTableInfo;
import com.keytech.modules.df.mdm.mapper.DfTableInfoMapper;
import com.keytech.modules.df.mdm.service.IDfTableFieldInfoService;
import com.keytech.modules.df.mdm.service.IDfTableInfoService;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: DF数据库表基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
@Service
public class DfTableInfoServiceImpl extends BaseServiceImpl<DfTableInfoMapper, DfTableInfo> implements IDfTableInfoService {
    @Autowired
    private IDfTableFieldInfoService dfTableFieldInfoService;

    @Override
    @Transactional
    public void saveData(DfTableInfo dfTableInfo) {
        List<DfTableInfo> list = this.lambdaQuery()
                .eq(DfTableInfo::getDfTableName, dfTableInfo.getDfTableName())
                .list();
        if(CollectionUtil.isNotEmpty(list)){
            throw new JeecgBootException("表已存在，请勿重复添加");
        }
        this.save(dfTableInfo);
    }
}
