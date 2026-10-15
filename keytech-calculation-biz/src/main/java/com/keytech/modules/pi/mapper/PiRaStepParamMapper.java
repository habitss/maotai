package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.SourceSistarRtSteps;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiRaStepParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: RA-步骤参数
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Mapper
public interface PiRaStepParamMapper extends BaseMapper<PiRaStepParam> {

    /**
     * 查询参数分页数据
     * @param lastTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Select(" select InsertDT as insert_dt,EventTime as event_time,AreaID as area_id,UnitID as unit_id,OrderID as order_id, " +
            " BatchNr as batch_nr,RecipeID as recipe_id,BopID as bop_id,StepNr as step_nr,ParaID as para_id, " +
            " ParaName as para_name,RcpValue as rcp_value,RptValue as rpt_value,BK as bk,UnitStamp as unit_stamp, " +
            " BopStepStamp as bop_step_stam FROM ParaUploadTable WITH(NOLOCK) " +
            " WHERE InsertDT > #{lastTime} AND InsertDT <= #{currentTime} " +
            " AND ParaID != '0' and AreaID !=4 "+
            " ORDER BY InsertDT,batch_nr " +
            " OFFSET #{offset} ROWS " +
            " FETCH NEXT #{pageSize} ROWS ONLY")
    List<PiRaStepParam> stepParamDataWithPaging(
            @Param("lastTime") Timestamp lastTime,
            @Param("currentTime") Timestamp currentTime,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);


    /**
     * 根据条件查询数据 参数
     * @param dataList
     * @return
     */
    @Select("<script>" +
            " select InsertDT as insert_dt,EventTime as event_time,AreaID as area_id,UnitID as unit_id,OrderID as order_id, " +
            " BatchNr as batch_nr,RecipeID as recipe_id,BopID as bop_id,StepNr as step_nr,ParaID as para_id, " +
            " ParaName as para_name,RcpValue as rcp_value,RptValue as rpt_value,BK as bk,UnitStamp as unit_stamp, " +
            " BopStepStamp as bop_step_stam FROM ParaUploadTable WHERE ParaID != '0' " +
            " AND ("+
            "<foreach collection='list' item='condition' open='(' separator=') OR (' close=')'>" +
            "AreaId = #{condition.areaId} AND UnitId = #{condition.unitId} " +
            "AND OrderId = #{condition.orderId} AND BatchNr = #{condition.batchNr} " +
            "AND BopID = #{condition.bopId} AND StepNr = #{condition.stepNr} AND RecipeID = #{condition.recipeId} "+
            //"AND BopID = #{condition.bopId} AND RecipeID = #{condition.recipeId} "+
            "</foreach>" +
            ")"+
            "</script>")
    List<PiRaStepParam> selectByConditions(@Param("list") List<PiRaStep> dataList);


    @Select("<script>" +
            " select InsertDT as insert_dt,EventTime as event_time,AreaID as area_id,UnitID as unit_id,OrderID as order_id, " +
            " BatchNr as batch_nr,RecipeID as recipe_id,BopID as bop_id,StepNr as step_nr,ParaID as para_id, " +
            " ParaName as para_name,RcpValue as rcp_value,RptValue as rpt_value,BK as bk,UnitStamp as unit_stamp, " +
            " BopStepStamp as bop_step_stam FROM ParaUploadTable WHERE ParaID = '1' " +
            " AND ("+
            "<foreach collection='list' item='condition' open='(' separator=') OR (' close=')'>" +
            "AreaId = #{condition.areaId} AND UnitId = #{condition.unitId} " +
            "AND OrderId = #{condition.orderId} AND BatchNr = #{condition.batchNr} " +
            "AND BopID = #{condition.bopId} AND StepNr = #{condition.stepNr} AND RecipeID = #{condition.recipeId} "+
            "</foreach>" +
            ")"+
            "</script>")
    List<PiRaStepParam> selectByTimeConditions(List<PiRaStep> dataList);
}
