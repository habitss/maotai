package com.keytech.modules.line.efficiency.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.line.efficiency.entity.DbSourceScaleweight;
import com.keytech.modules.line.efficiency.mapper.DbSourceScaleweightMapper;
import com.keytech.modules.line.efficiency.service.IDbSourceScaleweightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: 地磅数据(源数据)
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Service
@Slf4j
public class DbSourceScaleweightServiceImpl extends BaseServiceImpl<DbSourceScaleweightMapper, DbSourceScaleweight> implements IDbSourceScaleweightService {

    @Autowired
    private DbSourceScaleweightMapper dbSourceScaleweightMapper;

    @DS("rmdbsqlservice")
    public List<DbSourceScaleweight> selectScaleweightDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize) {
        return dbSourceScaleweightMapper.selectScaleweightDataWithPaging(lastLoadTime, currentTime, offset, pageSize);
    }

}
