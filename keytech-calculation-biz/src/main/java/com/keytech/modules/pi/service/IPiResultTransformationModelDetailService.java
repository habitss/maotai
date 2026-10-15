package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiResultTransformationModelDetail;
import com.keytech.base.service.BaseService;

/**
 * @Description: PI-结果转换模型配置
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
public interface IPiResultTransformationModelDetailService extends BaseService<PiResultTransformationModelDetail> {

    void saveOrUpdateData(PiResultTransformationModelDetail piResultTransformationModelDetail);
}
