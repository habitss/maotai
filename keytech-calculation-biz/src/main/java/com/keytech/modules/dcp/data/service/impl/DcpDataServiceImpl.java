package com.keytech.modules.dcp.data.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.dcp.data.mapper.SyncDcpDataMapper;
import com.keytech.modules.dcp.data.service.IDcpDataService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 同步DCP视图数据 业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
@DS("master")
public class DcpDataServiceImpl implements IDcpDataService {

    @Resource
    private SyncDcpDataMapper syncDcpDataMapper;
    @Override
    public void batchInsertData(List<Map<String, Object>> list) {
        syncDcpDataMapper.batchInsertOrUpdate( list);
    }

    @Override
    public int batchUpdateData(List<Map<String, Object>> dataList) {
        return syncDcpDataMapper.batchUpdateData(dataList);
    }

    @Override
    public List<Map<String, Object>> queryData(String thingId, String recipeId, String bbtMtNo, String flagPoint) {
        return syncDcpDataMapper.queryData(thingId, recipeId, bbtMtNo, flagPoint);
    }
}
