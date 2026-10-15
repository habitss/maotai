package com.keytech.modules.pi.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter-步骤
 * @Author: jeecg-boot
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Mapper
public interface SourceSistarRtStepsMapper extends BaseMapper<SourceSistarRtSteps> {

    // 使用参数分页 ORDER BY insert_timestamp, id
    @Select("SELECT * FROM sistar_rt_steps WITH(NOLOCK) " +
            "WHERE insert_timestamp > #{lastTime} AND insert_timestamp <= #{currentTime} " +
            "ORDER BY insert_timestamp, eop_no " +
            "OFFSET #{offset} ROWS " +
            "FETCH NEXT #{pageSize} ROWS ONLY")
    List<SourceSistarRtSteps> selectIncrementalDataWithPaging(
            @Param("lastTime") Timestamp lastTime,
            @Param("currentTime") Timestamp currentTime,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);

    /**
     *  获取字典表-设备
     */
    @Select("SELECT * FROM sistar_eng_reccats ")
    List<PiSistarEngReccats> getReccats();

    /**
     *  清空数据
     */
    @Update("DELETE FROM ${tableName}")
    void deleteData(@Param("tableName") String tableName);

    /**
     *  获取字典表-pcu
     */
    @Select("SELECT * FROM sistar_eng_pcus ")
    List<PiSistarEngPcus> getPcus();
    /**
     *  获取字典表-设备
     */
    @Select("SELECT * FROM sistar_eng_units ")
    List<PiSistarEngUnits> getUnits();
    /**
     *  获取字典表-步骤
     */
    @Select("SELECT * FROM sistar_eng_eops ")
    List<PiSistarEngEops> getEops();
    /**
     *  获取字典表-配方
     */
    @Select("SELECT * FROM sistar_eng_recipes ")
    List<PiSistarEngRecipes> getRecipes();
}
