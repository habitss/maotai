package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiResultTransformationModel;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: PI-结果转换模型配置-主表
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
public interface IPiResultTransformationModelService extends BaseService<PiResultTransformationModel> {

    /**
     * 根据结果模型ID查询
     * @param resultModelIds
     * @return
     */
    List<PiResultTransformationModel> queryListByResultModelIds(List<String> resultModelIds);

    /**
     * 批量删除
     * @param asList
     */
    void delete(List<String> asList);

    /** 保存/修改数据 */
    void saveOrUpdateData(PiResultTransformationModel piResultTransformationModel);
}
