package com.keytech.modules.sop.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.vo.SopManageVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @Description: SOP管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
public interface ISopReviewPlanService extends BaseService<SopReviewPlan> {

    List<SopManageVo> getSoptotal(String deptCode);

    List<SopStatisticData> getDiagnosisByYear(String year, String deptCode);

    List<SopHomePage> getSopAll();

    List<SopManageVo> getSoptotalByDept();

    List<SopReviewPlan> getSopReviewByDept(String year);

    IPage<SopReviewPlan> listData(Integer pageNo,Integer pageSize, SopReviewPlan sopReviewPlan, List<String> months, HttpServletRequest req);

    void sopSubmit(List<JSONObject> input);

    boolean isUptodate(String id, String week);

    ModelAndView exportXlsAll(HttpServletRequest request, SopReviewPlan sopReviewPlan, Class<SopReviewPlan> sopReviewPlanClass, String title);

    Result<?> sopImportExcel(HttpServletRequest request, HttpServletResponse response, Class<SopReviewPlan> sopReviewPlanClass) throws IOException;
}
