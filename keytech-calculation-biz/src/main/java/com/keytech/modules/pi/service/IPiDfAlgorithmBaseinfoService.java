package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.PiDfAlgorithmDto;
import com.keytech.modules.pi.entity.PiDfAlgorithmBaseinfo;

/**
 * @Description: DF算法-基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-20
 * @Version: V1.0
 */
public interface IPiDfAlgorithmBaseinfoService extends BaseService<PiDfAlgorithmBaseinfo> {
    /**
     * 保存或更新算法配置
     * @param piDfAlgorithmDto 数据
     */
    void saveOrUpdateData(PiDfAlgorithmDto piDfAlgorithmDto);
    /**
     * 删除算法配置
     * @param ids 数据id
     */
    void removeData(String ids);
    /**
     * DF数据抽取定时任务
     */
    void syncDfDataJob();
}
