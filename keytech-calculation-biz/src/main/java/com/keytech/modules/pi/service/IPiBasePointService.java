package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiBasePoint;

/**
 * @Description: 基础点位维护
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface IPiBasePointService extends BaseService<PiBasePoint> {

    /**
     * 获取点位最新值
     * @param list
     */
    void getPointData(Page<PiBasePoint> list);
}
