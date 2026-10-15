package com.keytech.modules.sop.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.sop.entity.SopStatisticData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @Description: SOP-诊断计划
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
public interface ISopStatisticDataService extends BaseService<SopStatisticData> {

    List<MdmWeek> getWeek(int year);
    
    List<MdmWeek> getWeek(int year, List<String> months);

    IPage<SopStatisticData> getData(Page<SopStatisticData> page, SopStatisticData sopStatisticData, HttpServletRequest req);

    void sopSubmit(List<JSONObject> input);

    ModelAndView exportXlsAll(HttpServletRequest request, SopStatisticData sopStatisticData, Class<SopStatisticData> sopStatisticDataClass, String sop);

    Result<?> sopImportExcel(HttpServletRequest request, HttpServletResponse response, Class<SopStatisticData> sopStatisticDataClass) throws IOException;
}
