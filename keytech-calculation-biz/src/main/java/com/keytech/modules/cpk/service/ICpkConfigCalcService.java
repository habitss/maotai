package com.keytech.modules.cpk.service;

import com.keytech.modules.cpk.entity.CpkConfigCalc;
import com.keytech.base.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * @Description: CPK计算结果
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
public interface ICpkConfigCalcService extends BaseService<CpkConfigCalc> {
    /**
     * cpk计算定时任务
     */
    void cpkCalcJob();
    /**
     * 获取CPK报表数据
     * @param year 年份
     * @param monthList 月份集合
     * @param code 指标编码
     * @param isAll 是否查询所有列数据1：是，0：否
     * @return 统计结果
     */
    List<Map<String, Object>> queryCpkCalcReport(String year, List<String> monthList, String code, Integer isAll);
}
