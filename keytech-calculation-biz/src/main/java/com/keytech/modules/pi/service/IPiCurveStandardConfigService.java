package com.keytech.modules.pi.service;

import com.keytech.modules.pi.dto.RecipeExtractDto;
import com.keytech.modules.pi.entity.PiCurveStandardConfig;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 曲线标准配置(多设备)
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
public interface IPiCurveStandardConfigService extends BaseService<PiCurveStandardConfig> {

    /**
     * 保存标准
     * @param piCurveStandardConfig
     */
    void standardSave(PiCurveStandardConfig piCurveStandardConfig);

    /**
     * 编辑标准
     * @param piCurveStandardConfig
     */
    void configEdit(PiCurveStandardConfig piCurveStandardConfig);

    /**
     * 删除标准配置
     * @param id
     */
    void configDelete(String id);

    /**
     * 曲线标准基础数据抽取
     */
    void standardExtractData();

    /**
     * 批量复制标准配置
     * @param ids 配置id列表（按勾选顺序）
     * @param copyNum 复制份数
     */
    void configBatchCopy(List<String> ids, Integer copyNum);

    /**
     * 查询配方/设备/步骤数据
     */
    List<RecipeExtractDto> queryRecipeExtractData();
}
