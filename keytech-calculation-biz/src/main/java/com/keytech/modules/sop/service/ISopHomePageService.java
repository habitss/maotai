package com.keytech.modules.sop.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.vo.SopDepartScheduleTreeVo;
import com.keytech.modules.sop.vo.SopManageVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * @Description: SOP 清单
 * @Author: jeecg-boot
 * @Date:   2025-04-11
 * @Version: V1.0
 */
public interface ISopHomePageService extends BaseService<SopHomePage> {

    IPage<SopHomePage> getQueryPageData(Page<SopHomePage> page, SopHomePage sopHomePage, HttpServletRequest request);

    void batchAddOrUpdate(List<SopHomePage> sopHomePageList);

    Result<?> importExcelGenCode(HttpServletRequest request, HttpServletResponse response, Class<SopHomePage> sopHomePageClass) throws IOException;

    List<SopDepartScheduleTreeVo> getSopDept(String year, String depts, String dept);

    List<SopManageVo> getSopManage(String year, String month, String depts, String dept);

    void updateByWriteTwoRr(JSONObject input);

    boolean deleteData(String ids);
}
