package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiCurveStandardRecipe;
import com.keytech.base.service.BaseService;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: 曲线标准-配方
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
public interface IPiCurveStandardRecipeService extends BaseService<PiCurveStandardRecipe> {

    /**
     * 配方保存或更新
     * @param piCurveStandardRecipe
     * @return
     */
    Result<String> recipeSaveOrUpdate(PiCurveStandardRecipe piCurveStandardRecipe);

    /**
     * 删除配方
     * @param id
     */
    void recipeDelete(String id);
}
