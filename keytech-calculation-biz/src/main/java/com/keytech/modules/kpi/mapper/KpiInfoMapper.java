package com.keytech.modules.kpi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.kpi.dto.KPiConfigDto;
import com.keytech.modules.kpi.vo.ControlConfigurationVo;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: KPI清单
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Mapper
public interface KpiInfoMapper extends BaseMapper<KpiInfo> {


    /**
     * 获取KPI最大id
     * @return
     */
    @Select("select max(id) from mes3_kpi_info")
    Long kpiInfomaxId();


    /**
     * 获取KPI及KPI分类
     * @param queryWrapper
     * @return
     */
    @Select("select '' org_code,'' orgName,y.kpi_type,y.parent_id as typeParentId,c.kpi_code,c.kpi_name,c.perent_id as kpiPerentId" +
            ",c.unit,c.parent_id AS kpiParentId,c.decimal_num,y.id AS typeId ,c.input_permission AS inputPermission " +
            ",c.id as kpiId,c.input_sort "+
            ",c.provide_by,c.possess_by,c.consulted_by,c.informed_by,c.deadline "+
            ",c.duty_provide_by,c.duty_possess_by,c.duty_consulted_by,c.duty_informed_by "+
            "from mes3_kpi_type y " +
            "INNER JOIN mes3_kpi_info c on y.id=c.perent_id "+
            "${ew.customSqlSegment} ")
    List<KPiConfigDto> listKpi( @Param("ew") Wrapper<KpiInfo> queryWrapper);

    /**
     * 获取与柏拉图
     * @param factoryCode
     * @param kpiCode
     * @return
     */
    @Select("SELECT  mki.dept_code AS deptCode," +
            "mki.perent_id AS perentId, " +
            "mki.kpi_code AS kpiCode, " +
            "mki.kpi_name AS kpiName, " +
            "mko.`code` AS platoOneId, " +
            "mko.process_name AS platoOneName, " +
            "mkt.`code` AS platoTwoId, " +
            "mkt.controller_name AS platoTwoName, " +
            "mke.`id` AS platoThreeKey, " +
            "mke.`control_activity` AS controlActivity, " +
            "mke.`standard` AS standard, " +
            "mke.`unit` AS unit, " +
            "mke.`code` AS platoThreeId, " +
            "mke.pi_code AS  piCode, " +
            "mke.pi_name AS platoThreeName " +
            "FROM mes3_kpi_info mki " +
            "LEFT JOIN mes3_kpi_plato_one mko ON mki.kpi_code = mko.parent_code " +
            "LEFT JOIN mes3_kpi_plato_two mkt ON mko.`code` = mkt.parent_code " +
            "LEFT JOIN mes3_kpi_plato_three mke ON mkt.`code`= mke.parent_code " +
            "WHERE mki.kpi_code = #{kpiCode}  " +
            "AND mki.plato = 1 " +
            "AND mki.factory_code LIKE CONCAT(#{factoryCode},'%') AND  mke.`id` IS NOT NULL " +
            "ORDER BY mki.perent_id,mki.kpi_code,mko.`code`,mkt.`code`,mke.`code`")
    List<ControlConfigurationVo> kpiControlConfiguration(String factoryCode, String kpiCode);



    @Select("select t.depart_name deptName,t1.depart_name inputDeptName,y.kpi_type kpiTypeName,c.*   " +
            "from mes3_kpi_info c  " +
            "left join mes3_kpi_type y on c.perent_id = y.id " +
            "left join sys_depart t on c.dept_code = t.org_code " +
            "left join sys_depart t1 on c.input_permission = t1.org_code " +
            "${ew.customSqlSegment} ")
    List<KpiInfo> listKPiInfoJoin(@Param("ew") Wrapper<KpiInfo> queryWrapper);
}
