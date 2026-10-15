package com.keytech.modules.pi.service.impl;

import com.keytech.modules.pi.entity.PiDfAlgorithmModel;
import com.keytech.modules.pi.mapper.PiDfAlgorithmModelMapper;
import com.keytech.modules.pi.service.IPiDfAlgorithmModelService;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: DF算法-计算模型
 * @Author: jeecg-boot
 * @Date:   2026-01-21
 * @Version: V1.0
 */
@Service
public class PiDfAlgorithmModelServiceImpl extends BaseServiceImpl<PiDfAlgorithmModelMapper, PiDfAlgorithmModel> implements IPiDfAlgorithmModelService {

    @Override
    public List<Map<String, String>> queryMdmTableName() {
        return baseMapper.queryMdmTableName();
    }

    @Override
    public List<Map<String, String>> queryMdmTableFieldName(String tableName) {
        return baseMapper.queryMdmTableFieldName(tableName);
    }
}
