package com.keytech.modules.opl.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.vo.OplDepartScheduleTreeVo;
import com.keytech.modules.opl.vo.OplManageVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * @Description: Opl 清单
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
public interface IOplHomePageService extends BaseService<OplHomePage> {

    IPage<OplHomePage> getQueryPageData(Page<OplHomePage> page, OplHomePage oplHomePage, HttpServletRequest request);

    void batchAddOrUpdate(List<OplHomePage> oplHomePageList);

    Result<?> importExcelGenCode(HttpServletRequest request, HttpServletResponse response, Class<OplHomePage> oplHomePageClass) throws IOException;

    List<OplDepartScheduleTreeVo> getOplDept(String year, String depts, String dept);

    List<OplManageVo> getOplManage(String year, String month, String depts, String dept);

    void updateByWriteTwoRr(JSONObject input);

    boolean deleteData(String ids);
}
