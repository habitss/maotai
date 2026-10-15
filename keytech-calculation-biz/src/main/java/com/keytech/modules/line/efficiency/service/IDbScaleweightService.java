package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.entity.DbScaleweight;
import com.keytech.base.service.BaseService;

/**
 * @Description: 地磅数据
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
public interface IDbScaleweightService extends BaseService<DbScaleweight> {

    /**
     * 定时任务接口-地磅数据抽取
     */
    void extractData();
}
