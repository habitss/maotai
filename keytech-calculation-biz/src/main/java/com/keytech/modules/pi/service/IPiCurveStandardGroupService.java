package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiCurveStandardGroup;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 曲线标准-设备分组
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
public interface IPiCurveStandardGroupService extends BaseService<PiCurveStandardGroup> {

    void groupDelete(List<String> list);
}
