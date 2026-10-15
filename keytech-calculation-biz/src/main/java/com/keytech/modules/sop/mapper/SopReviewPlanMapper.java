package com.keytech.modules.sop.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.kpi.dto.KPiConfigDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.vo.SopManageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: SOP管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Mapper
public interface SopReviewPlanMapper extends BaseMapper<SopReviewPlan> {

    List<SopManageVo> getSoptotal(String deptCode);

    List<SopStatisticData> getDiagnosisByYear(String year, String deptCode);

    List<SopHomePage> getSopAll();

    List<SopManageVo> getSoptotalByDept();

    List<SopReviewPlan> getSopReviewByDept(String year);

    /**
     * 获取回顾
     *
     * */
    @Select(" select a.*,b.sop_number,b.sop_desc from " +
            " mes3_sop_review_plan a left join " +
            " mes3_sop_home_page b on a.sop_id = b.id "+
            "${ew.customSqlSegment} ")
    List<SopReviewPlan> listAll(@Param("ew") QueryWrapper<SopReviewPlan> queryWrapper);
//    List<KPiConfigDto> listKpi(@Param("ew") Wrapper<KpiInfo> queryWrapper);
}
