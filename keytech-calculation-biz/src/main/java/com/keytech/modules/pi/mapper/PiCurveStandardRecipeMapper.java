package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.keytech.modules.pi.entity.PiCurveStandardRecipe;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: 曲线标准-配方
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
@Mapper
public interface PiCurveStandardRecipeMapper extends BaseMapper<PiCurveStandardRecipe> {

    /**
     * 查询所有配方(包含已删除) data_source=2
     */
    @Select("SELECT * FROM mes3_pi_curve_standard_recipe WHERE data_source = 2")
    List<PiCurveStandardRecipe> selectAllByDataSource();
}
