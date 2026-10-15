package com.keytech.modules.pi.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 曲线步骤配置
 * @Author: jeecg-boot
 * @Date:   2026-01-06
 * @Version: V1.0
 */
@Mapper
public interface PiCurveStepMapper extends BaseMapper<PiCurveStep> {

    @Select("<script>" +
            "SELECT * FROM mes3_pi_curve_step WHERE del_flag = 0 AND " +
            "<foreach collection='stepList' item='step' separator=' OR '>" +
            "   (area_no = #{step.areaNo} " +
            //"    AND year_no = #{step.yearNo} " +
            "    AND reccat_no = #{step.reccatNo} " +
            "    AND pcu_no = #{step.pcuNo} " +
            "    AND unit_no = #{step.unitNo} " +
            "    AND recipe_no = #{step.recipeNo})" +
            "</foreach>" +
            "</script>")
    List<PiCurveStep> selectByStepList(@Param("stepList") List<PiSistarRtSteps> stepList);



    @Select("<script>" +
            "SELECT * FROM mes3_pi_curve_step WHERE del_flag = 0 AND " +
            "<foreach collection='stepList' item='step' separator=' OR '>" +
            "   (area_no = #{step.areaId} " +
            "    AND reccat_no = #{step.lineId} " +
            "    AND unit_no = CONCAT(#{step.areaId}, '-', #{step.unitId}) " +
            "    AND recipe_no = #{step.recipeId})" +
            "</foreach>" +
            "</script>")
    List<PiCurveStep> selectRaByStepList(@Param("stepList") List<PiRaStep> stepList);

    @Select("<script>" +
            "SELECT * FROM mes3_pi_curve_step WHERE del_flag = 0 AND " +
            "<foreach collection='stepList' item='step' separator=' OR '>" +
            //"   (area_no = #{step.AreaNo} " +
            "    ( unit_no = #{step.unitNo} )" +
            //"    AND recipe_no = #{step.recipeId})" +
            "</foreach>" +
            "</script>")
    List<PiCurveStep> selectPiByStepList(@Param("stepList") List<PiCurveStepDto> stepList);
}
