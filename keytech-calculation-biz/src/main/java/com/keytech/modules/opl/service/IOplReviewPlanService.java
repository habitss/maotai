package com.keytech.modules.opl.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.service.BaseService;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.vo.OplManageVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @Description: Opl管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
public interface IOplReviewPlanService extends BaseService<OplReviewPlan> {

    List<OplManageVo> getOpltotal(String deptCode);

    List<OplHomePage> getOplAll();

    List<OplManageVo> getOpltotalByDept();

    List<OplReviewPlan> getOplReviewByDept(String year);

    IPage<OplReviewPlan> listData(Integer pageNo,Integer pageSize, OplReviewPlan oplReviewPlan, HttpServletRequest req);

    void oplSubmit(List<JSONObject> input);

    boolean isUptodate(String id, String week);

    ModelAndView exportXlsAll(HttpServletRequest request, OplReviewPlan oplReviewPlan, Class<OplReviewPlan> oplReviewPlanClass, String title);

    Result<?> oplImportExcel(HttpServletRequest request, HttpServletResponse response, Class<OplReviewPlan> oplReviewPlanClass) throws IOException;
}
