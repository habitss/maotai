package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.dto.RaAreaDto;
import com.keytech.modules.pi.vo.RaLineVo;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiRaStep;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: RA-步骤
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Mapper
public interface PiRaStepMapper extends BaseMapper<PiRaStep> {


    /**
     * @Description: 获取步骤数据
     * @param lastTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Select("SELECT InsertDT as insert_dt,EventTime as event_time,AreaID as area_id,AreaName as area_name, " +
            " UnitID as unit_id,UnitName as unit_name,OrderID as order_id,BatchNr as batch_nr,RecipeID as recipe_id, " +
            " RecipeName as recipe_name,BatchID as batch_id,BatchTypeName as batch_type_name,Status as status, " +
            " BopID as bop_id,BopName as bop_name,StepNr as step_nr,BK as bk,UnitStamp as unit_stamp, " +
            " BopStepStamp as bop_step_stamp FROM UnitEventTable WITH(NOLOCK) " +
            " WHERE InsertDT > #{lastTime} AND InsertDT <= #{currentTime} " +
            " and BatchTypeName != 'CIP' and AreaID != 4 AND BopID != 0 and RecipeName !='Unknown Recipe' "+
            " ORDER BY InsertDT,batch_nr " +
            " OFFSET #{offset} ROWS " +
            " FETCH NEXT #{pageSize} ROWS ONLY")
    List<PiRaStep> stepDataWithPaging(
            @Param("lastTime") Timestamp lastTime,
            @Param("currentTime") Timestamp currentTime,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);

    /**
     * @Description: 获取产线线数据
     * @param areaId
     * @return
     */
    @Select("<script>" +
            "select id, area_id as areaId, name_cn as nameCn from mes3_pi_ra_line where del_flag = 0" +
            "<if test='areaId != null'> and area_id = #{areaId}</if>" +
            "</script>")
    List<RaLineVo> getLine(@Param("areaId") Integer areaId);

    @Select("select uid, area_id as areaId, name_cn as nameCn from mes3_pi_ra_area where del_flag = 0")
    List<RaAreaDto> getArea();

    @Select({"<script>",
            "select DISTINCT recipe_id,recipe_name,unit_id from mes3_pi_ra_step where area_id = #{areaId}",
            "<if test='unitId != null and unitId.size() > 0'>",
                "AND unit_id IN",
                "<foreach collection='unitId' item='item' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</if>",
            "</script>"})
    List<PiRaStep> getRecipeByAreaId(@Param("areaId") String areaId, @Param("unitId") List<String> unitId);

    @Select({"<script>",
            "select DISTINCT bop_id,bop_name from mes3_pi_ra_step where area_id = #{areaId}",
            "<if test='unitId != null and unitId.size() > 0'>",
                "AND unit_id IN",
                "<foreach collection='unitId' item='item' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</if>",
            "<if test='recipeId != null and recipeId != \"\"'>",
                "AND recipe_id = #{recipeId}",
            "</if>",
            " and bop_name !='Unknown BOP' ",
            "</script>"})
    List<PiRaStep> getStepByAreaId(@Param("areaId") String areaId, @Param("unitId") List<String> unitId, @Param("recipeId") String recipeId);


    @Select("select top 1 InsertDT as insert_dt,EventTime as event_time,AreaID as area_id,AreaName as area_name, " +
            "   UnitID as unit_id,UnitName as unit_name,OrderID as order_id,BatchNr as batch_nr,RecipeID as recipe_id, " +
            "   RecipeName as recipe_name,BatchID as batch_id,BatchTypeName as batch_type_name,Status as status, " +
            "   BopID as bop_id,BopName as bop_name,StepNr as step_nr,BK as bk,UnitStamp as unit_stamp, " +
            "   BopStepStamp as bop_step_stamp  from UnitEventTable where AreaId = #{areaId} and UnitId = #{unitId} " +
            "  and OrderId = #{orderId} and BatchNr = #{batchNr} and EventTime > #{eventTime}"+
            " and RecipeName !='Unknown Recipe' and BopName != 'Unknown BOP' order by StepNr,EventTime "
    )
    List<PiRaStep> selectNextData(PiRaStep piRaStep);
}
