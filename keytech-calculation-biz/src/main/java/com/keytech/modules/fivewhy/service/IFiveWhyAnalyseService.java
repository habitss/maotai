package com.keytech.modules.fivewhy.service;

import com.keytech.modules.fivewhy.dto.FiveWhyAnalyseDto;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @Description: 5Why分析
 * @Author: jeecg-boot
 * @Date:   2025-01-20
 * @Version: V1.0
 */
public interface IFiveWhyAnalyseService extends BaseService<FiveWhyAnalyse> {
    /**
     * 保存或更新实体对象
     * @param fiveWhyAnalyseDto 实体对象
     */
    void saveOrUpdateFiveWhy(FiveWhyAnalyseDto fiveWhyAnalyseDto);

    /**
     * 取消分析
     * @param id fiveWhy主键
     */
    void deleteFiveWhy(String id);
    /**
     * 查询数据
     */
    FiveWhyAnalyseDto queryData(FiveWhyAnalyse fiveWhyAnalyse, HttpServletRequest req);

    /**
     * 查询删除数据
     * @param startDateStr
     * @param endDateStr
     * @param kpiCodeList
     * @return
     */
    List<FiveWhyAnalyse> listDel(String startDateStr, String endDateStr, List<String> kpiCodeList);
}
