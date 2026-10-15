package com.keytech.modules.cpk.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.cpk.entity.CpkConfigCalc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @Description: CPK计算结果
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Mapper
public interface CpkConfigCalcMapper extends BaseMapper<CpkConfigCalc> {
    /**
     * 获取CPK报表数据
     * @param year 年份
     * @param monthList 月份集合
     * @param code 指标编码
     * @param isAll 是否查询所有列数据1：是，0：否
     * @return 统计结果
     */
    List<Map<String, Object>> getCpkReportList(String year, List<String> monthList, String code, Integer isAll);
}
