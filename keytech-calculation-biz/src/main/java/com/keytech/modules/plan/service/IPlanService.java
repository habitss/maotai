package com.keytech.modules.plan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.service.BaseService;
import com.keytech.modules.plan.entity.Plan;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Date;

/**
 * @Description: 工单管理
 * @Author: jeecg-boot
 * @Date:   2025-06-06
 * @Version: V1.0
 */
public interface IPlanService extends BaseService<Plan> {
    Result<IPage<Plan>> queryPageList(Plan plan, Integer pageNo, Integer pageSize, String orderField,
                                      String orderType, String selectFields,
                                      HttpServletRequest req);
    Result<String> savePlan(Plan plan);

    Result<String> deleteData(String ids);

    Result<?> importData(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void startPlan(Plan plan);

    void endPLan(String planId, Date realEndTime, Boolean isDelayClose);

    void pauseOrRenew(Plan plan);
}
