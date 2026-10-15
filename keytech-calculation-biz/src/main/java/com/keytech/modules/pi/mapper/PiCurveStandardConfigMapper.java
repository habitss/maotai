package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.dto.RecipeExtractDto;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiCurveStandardConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 曲线标准配置(多设备)
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
@Mapper
public interface PiCurveStandardConfigMapper extends BaseMapper<PiCurveStandardConfig> {

    /**
     * 获取配方/设备/步骤数据
     */
    @Select("SELECT " +
            "CASE WHEN rp.AreaUID = 6 THEN 1 WHEN rp.AreaUID = 7 THEN 2 WHEN rp.AreaUID = 8 THEN 3 ELSE rp.AreaUID END AS areaNo, " +
            "rp.UID AS recipeNo, " +
            "rp.NameCN AS recipeName, " +
            "mu.UnitID AS unitNo, " +
            "rup.NameCN AS unitName, " +
            "mb.BopID AS eopNo, " +
            "ro.StepInProcedure AS stepInProcedure, " +
            "mb.NameCN AS eopName " +
            "FROM Recipe_Procedure rp " +
            "LEFT JOIN Recipe_UnitProcedure rup ON rp.UID = rup.ProcedureUID " +
            "LEFT JOIN Recipe_Operation ro ON rup.UID = ro.UPUID " +
            "LEFT JOIN Model_Bop mb ON ro.BopUID = mb.UID " +
            "LEFT JOIN Model_Unit mu ON rup.UnitUID = mu.UID " +
            "WHERE rp.UID IN ( " +
            "  SELECT UID FROM Recipe_Procedure " +
            "  WHERE AreaUID IN (6,7,8) " +
            "  AND NameCN NOT LIKE N'%洗%' AND NameCN NOT LIKE N'%去发酵%' " +
            "  AND NameCN NOT LIKE N'%备用%' AND NameCN NOT LIKE N'%服务单元%' " +
            "  AND NameCN NOT LIKE N'%试验%' AND NameCN NOT LIKE N'%杀菌%' " +
            "  AND NameCN NOT LIKE N'%酵母%' AND NameCN NOT LIKE N'%钝化%' " +
            "  AND NameCN NOT LIKE N'%传送%' AND NameCN NOT LIKE N'%CIP%' " +
            "  AND NameCN NOT LIKE N'%单独%' AND NameCN NOT LIKE N'%进%' " +
            ") " +
            "AND rup.NameCN NOT LIKE N'%麦汁管道%' AND rup.NameCN NOT LIKE N'%耕糟%' " +
            "AND rup.NameCN NOT LIKE N'%排糟%' AND rup.NameCN NOT LIKE N'%酒花罐%' " +
            "AND rup.NameCN NOT LIKE N'%耕刀%' AND rup.NameCN NOT LIKE N'%暂存罐%' " +
            "ORDER BY rp.UID DESC, mu.UnitID ASC, ro.StepInProcedure ASC")
    List<RecipeExtractDto> selectRecipeExtractData();

    /**
     * 查询所有步骤配置(包含已删除) data_source=2
     */
    @Select("SELECT * FROM mes3_pi_curve_standard_config WHERE data_source = 2")
    List<PiCurveStandardConfig> selectAllByDataSource2();
}
