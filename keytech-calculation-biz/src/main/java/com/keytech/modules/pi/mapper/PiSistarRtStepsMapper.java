package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter数据抽取-步骤
 * @Author: jeecg-boot
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Mapper
public interface PiSistarRtStepsMapper extends BaseMapper<PiSistarRtSteps> {



    /**
     * 根据PCU编号获取步骤信息
     * @param pcuNo
     * @param unitNo
     * @param recipeNo
     * @param recipeName
     * @return
     */
    @Select({"<script>",
            "select DISTINCT recipe_no,recipe_name,reccat_no,reccat_name from mes3_pi_sistar_rt_steps where pcu_no IN",
            "<foreach collection='pcuNo' item='item' open='(' separator=',' close=')'>",
                "#{item}",
            "</foreach>",
            "<if test='unitNo != null and unitNo.size() > 0'>",
                "AND unit_no IN",
                "<foreach collection='unitNo' item='item' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</if>",
            "<if test='recipeNo != null and recipeNo != \"\"'>",
                "AND recipe_no = #{recipeNo}",
            "</if>",
            "<if test='recipeName != null and recipeName != \"\"'>",
                "AND recipe_name LIKE CONCAT('%', #{recipeName}, '%')",
            "</if>",
            "</script>"})
    List<PiSistarRtSteps> getRecipeByPcuNo(@Param("pcuNo") List<String> pcuNo,
                                           @Param("unitNo") List<String> unitNo,
                                           @Param("recipeNo") String recipeNo,
                                           @Param("recipeName") String recipeName);


    /**
     * 根据PCU编号获取步骤信息
     * @param pcuNo
     * @param unitNo
     * @param recipeNo
     * @return
     */
    @Select({"<script>",
            "select DISTINCT eop_no,eop_name,rop_id from mes3_pi_sistar_rt_steps where pcu_no = #{pcuNo}",
            "<if test='unitNo != null and unitNo.size() > 0'>",
                "AND unit_no IN",
                "<foreach collection='unitNo' item='item' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</if>",
            "<if test='recipeNo != null and recipeNo != \"\"'>",
                "AND recipe_no = #{recipeNo}",
            "</if>",
            "<if test='reccatNo != null and reccatNo != \"\"'>",
                "AND reccat_no = #{reccatNo}",
            "</if>",
            "</script>"})
    List<PiSistarRtSteps> getStepByPcuNo(@Param("pcuNo") String pcuNo, @Param("unitNo") List<String> unitNo, @Param("recipeNo") String recipeNo, @Param("reccatNo") String reccatNo);

    /**
     * 根据PCU编号获取步骤信息
     * @param areaNo
     * @param recipeNo
     * @param recipeName
     * @return
     */
    @Select({"<script>",
            "select DISTINCT unit_no,unit_name,pcu_no,area_no from mes3_pi_sistar_rt_steps where pcu_no = #{areaNo} and recipe_no = #{recipeNo}",
            "<if test='recipeName != null and recipeName != \"\"'>",
                "AND recipe_name = #{recipeName}",
            "</if>",
            "<if test='reccatNo != null and reccatNo != \"\"'>",
                "AND reccat_no = #{reccatNo}",
            "</if>",
            "</script>"})
    List<PiSistarRtSteps> getStepsUnitByAreaNo(@Param("areaNo") String areaNo, @Param("recipeNo") String recipeNo, @Param("recipeName") String recipeName, @Param("reccatNo") String reccatNo);
}
