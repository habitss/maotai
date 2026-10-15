package com.keytech.modules.line.efficiency.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.line.efficiency.entity.DbScaleweight;
import com.keytech.modules.line.efficiency.entity.DbSourceScaleweight;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: 地磅数据(源数据)
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
public interface IDbSourceScaleweightService extends BaseService<DbSourceScaleweight> {

    public List<DbSourceScaleweight> selectScaleweightDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize);
}
