package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiSyncConfig;
import com.keytech.base.service.BaseService;

import java.util.Map;

/**
 * @Description: PI数据同步配置
 * @Author: jeecg-boot
 * @Date:   2026-03-31
 * @Version: V1.0
 */
public interface IPiSyncConfigService extends BaseService<PiSyncConfig> {
    /**
     * pi数据同步（目标表驱动，行转列场景）
     * @param map
     */
    void intersectionData(Map map);

    /**
     * pi数据同步（源表驱动，按配置对照写入目标表）
     * @param map
     */
    void sourceSyncData(Map map);

    /**
     * 数据同步
     * @param map
     */
    void synchronization(Map map);
}
