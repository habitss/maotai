package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiSpreadjsMain;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiSpreadjsPi;

import java.util.List;

/**
 * @Description: SpreadJS模板配置
 * @Author: jeecg-boot
 * @Date:   2025-12-23
 * @Version: V1.0
 */
public interface IPiSpreadjsMainService extends BaseService<PiSpreadjsMain> {

    /**
     * 存储PI配置
     * @param piSpreadjsPi
     */
    void savePiConfig(PiSpreadjsPi piSpreadjsPi);

    /**
     * 获取PI配置
     * @param spreadjsMainId
     * @return
     */
    List<PiSpreadjsPi> getPiConfig(String spreadjsMainId);
}
