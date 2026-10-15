package com.keytech.modules.manage.check.service;

import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.manage.check.entity.ManagerCheckConfig;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * @Description: 分层检查-检查识别矩阵
 * @Author: jeecg-boot
 * @Date:   2025-04-08
 * @Version: V1.0
 */
public interface IManagerCheckConfigService extends BaseService<ManagerCheckConfig> {
    /**
     * 导出excel
     * @param request
     * @param managerCheckConfig
     * @param title
     * @return
     */
     ModelAndView exportXls(HttpServletRequest request, ManagerCheckConfig managerCheckConfig, String title);


    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
