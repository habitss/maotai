package com.keytech.modules.opl.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.vo.OplManageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: Opl管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Mapper
public interface OplReviewPlanMapper extends BaseMapper<OplReviewPlan> {

    List<OplManageVo> getOplTotal(String deptCode);

    List<OplHomePage> getOplAll();

    List<OplManageVo> getOplTotalByDept();

    List<OplReviewPlan> getOplReviewByDept(String year);

    /**
     * 获取回顾
     *
     * */
    @Select(" select a.*,b.opl_number,b.opl_desc from " +
            " mes3_opl_review_plan a left join " +
            " mes3_opl_home_page b on a.opl_id = b.id "+
            "${ew.customSqlSegment} ")
    List<OplReviewPlan> listAll(@Param("ew") QueryWrapper<OplReviewPlan> queryWrapper);
}
