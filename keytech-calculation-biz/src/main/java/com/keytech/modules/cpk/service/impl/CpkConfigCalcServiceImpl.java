package com.keytech.modules.cpk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.NumberCalcUtil;
import com.keytech.modules.cpk.entity.CpkConfig;
import com.keytech.modules.cpk.entity.CpkConfigCalc;
import com.keytech.modules.cpk.entity.CpkConfigDetail;
import com.keytech.modules.cpk.mapper.CpkConfigCalcMapper;
import com.keytech.modules.cpk.service.ICpkConfigCalcService;
import com.keytech.modules.cpk.service.ICpkConfigDetailService;
import com.keytech.modules.cpk.service.ICpkConfigService;
import com.keytech.modules.cpk.util.CpkCalculatorUtil;
import com.keytech.modules.custom.report.mapper.CustomReportTemplateMapper;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import groovy.util.logging.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: CPK计算结果
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Service
@Slf4j
public class CpkConfigCalcServiceImpl extends BaseServiceImpl<CpkConfigCalcMapper, CpkConfigCalc> implements ICpkConfigCalcService {
    @Resource
    private ICpkConfigService cpkConfigService;
    @Resource
    private ICpkConfigDetailService cpkConfigDetailService;
    @Resource
    private IPiCustomizeTableService piCustomizeTableService;
    @Resource
    private CustomReportTemplateMapper customReportTemplateMapper;

    @Override
    @Transactional
    public void cpkCalcJob() {
        //获取配置数据
        List<CpkConfig> configList = cpkConfigService.lambdaQuery()
                .isNotNull(CpkConfig::getTableCode)
                .lt(CpkConfig::getEffectiveDate, new Date())
                .list();
        if(CollUtil.isEmpty(configList)){
            return;
        }
        //获取前一天日期
        Date yesterday = DateUtil.yesterday();
        //获取月份第一天
        Date firstDay = DateUtil.beginOfMonth(yesterday);
        //获取月份最后一天
        Date lastDay = DateUtil.endOfMonth(yesterday);
        //获取年月
        String year = DateUtil.format(yesterday, "yyyy");
        String month = DateUtil.format(yesterday, "M");

        //获取cpk条件配置
        List<CpkConfigDetail> detailConfigList = cpkConfigDetailService.list();
        Map<String, List<CpkConfigDetail>> detailConfigMap = detailConfigList.stream()
                .collect(Collectors.groupingBy(CpkConfigDetail::getConfigId));

        //获取所有表名
        List<String> tableCodes = configList.stream()
                .map(CpkConfig::getTableCode)
                .distinct()
                .toList();

        //获取PI报表
        List<PiCustomizeTable> piCustomizeTableList = piCustomizeTableService.lambdaQuery()
                .in(PiCustomizeTable::getTableName, tableCodes)
                .list();
        //获取报表的重算字段集合
        Map<String, PiCustomizeTable> piCustomizeTableMap = piCustomizeTableList.stream()
                .collect(Collectors.toMap(PiCustomizeTable::getTableName, Function.identity()));

        //获取计算数据
        List<CpkConfigCalc> calcList = this.lambdaQuery()
                .eq(CpkConfigCalc::getCalcYear, year)
                .eq(CpkConfigCalc::getCalcMonth, month)
                .list();
        Map<String, CpkConfigCalc> calcMap = calcList.stream()
                .collect(Collectors.toMap(CpkConfigCalc::getConfigId, Function.identity()));

        //计算数据
        List<CpkConfigCalc> saveList = new ArrayList<>();
        for(CpkConfig cpkConfig : configList){
            //历史计算的cpk数据
            CpkConfigCalc oldConfigCalc = calcMap.get(cpkConfig.getId());
            try {
                //获取对应pi报表数据
                PiCustomizeTable piCustomizeTable = piCustomizeTableMap.get(cpkConfig.getTableCode());
                //获取查询sql
                String sql = getCondition(
                        firstDay,
                        lastDay,
                        detailConfigMap.get(cpkConfig.getId()),
                        cpkConfig.getTableCode(),
                        cpkConfig.getFieldCode(),
                        piCustomizeTable.getRecalculateTimePi()
                );
                //查询数据
                List<Double> dataList = customReportTemplateMapper.queryDoubleBySql(sql);
                //新计算的cpk数据
                CpkConfigCalc cpkConfigCalc = CpkCalculatorUtil.calculateCpk(
                        dataList,
                        Double.parseDouble(cpkConfig.getUpperLimit()),
                        Double.parseDouble(cpkConfig.getLowerLimit())
                );
                saveList.add(initObject(cpkConfigCalc, oldConfigCalc, cpkConfig.getId(), year, month));
            } catch (Exception e) {
                saveList.add(initObject(null, oldConfigCalc, cpkConfig.getId(), year, month));
                log.error("cpk计算异常configId："+cpkConfig.getId()+"     tableCode："+cpkConfig.getTableCode()+"   "+e.getMessage());
                e.printStackTrace();
            }
        }
        this.saveOrUpdateBatch(saveList);
    }

    @Override
    public List<Map<String, Object>> queryCpkCalcReport(String year, List<String> monthList, String code, Integer isAll) {
        //节点层级含义，1层为筛选条件，2、3层为表格前置合并列，4层为表格统计列统计5层总和显示在列表，5层为表格数据列
        //返回结果
        List<Map<String, Object>> resultList = new ArrayList<>(0);
        //获取计算数据
        List<Map<String, Object>> calcList = baseMapper.getCpkReportList(year, monthList, code, isAll);
        if(CollUtil.isEmpty(calcList)){
            return resultList;
        }
        List<String> fieldList = new ArrayList<>();
        fieldList.add("cpk_");
        if(1 == isAll){
            fieldList.add("cp_");
            fieldList.add("cpku_");
            fieldList.add("cpkl_");
            fieldList.add("record_num_");
            fieldList.add("record_mean_");
        }
        List<String> newFieldList = new ArrayList<>();
        for(String monStr : monthList){
            for(String keyStr : fieldList){
                newFieldList.add(keyStr+monStr);
            }
        }
        //用于记录的配置id
        String towLevelId = "none";
        String threeLevelId = "none";
        String fourLevelId = "none";
        //用于记录配置改变时候的下标
        int towFlagIndex = 0;
        int threeFlagIndex = 0;
        int fourFlagIndex = 0;
        //处理数据
        for(int i = 0; i < calcList.size(); i++){
            //当前数据
            Map<String, Object> itemMap = calcList.get(i);

            //获取统计数据
            Map<String, Object> totalTemp = i == 0 ? null : resultList.get(fourFlagIndex);
            //获取当前配置id
            String currentTowLevelId = itemMap.get("tow_level_id").toString();
            String currentThreeLevelId = itemMap.get("three_level_id").toString();
            String currentFourLevelId = itemMap.get("four_level_id").toString();

            //当前配置id不同的时候，初始化一条统计数据
            if(!fourLevelId.equals(currentFourLevelId) || i == 0){
                if(i != 0){
                    int totalIndex = resultList.size() - fourFlagIndex - 1;
                    //如果配置不同计算CPK平均数
                    for(String key : newFieldList){
                        if(key.startsWith("record_num_")){
                            continue;
                        }
                        //总数除以记录数
                        totalTemp.put(key, NumberCalcUtil.divideWithScale(totalTemp.get(key).toString(), totalIndex+"",  2));
                    }
                    //改变时的下标
                    fourFlagIndex = resultList.size();
                }
                //改变时的第4层id
                fourLevelId = currentFourLevelId;
                //添加CPK累加后的平均数数据
                Map<String, Object> temp = new HashMap<>();
                BeanUtil.copyProperties(itemMap, temp);
                temp.put("five_level_name", temp.get("four_level_name"));
                resultList.add(temp);
            } else{
                //如果配置相同则循环月份累加CPK数据
                for(String key : newFieldList){
                    double totalNum = Double.parseDouble(totalTemp.get(key).toString()) + Double.parseDouble(itemMap.get(key).toString());
                    totalTemp.put(key, totalNum);
                }
            }
            if(!threeLevelId.equals(currentThreeLevelId) || i == calcList.size() - 1){
                if(!threeLevelId.equals(currentThreeLevelId) && i == (calcList.size() - 1)){
                    //如果id不同，且是最后一行数据则需要多处理一次，将指定位置添加合并行
                    int tempIndex = resultList.size()-1;
                    resultList.get(tempIndex).put("three_level_merge_num", 2);
                    resultList.get(tempIndex).put("three_level_index", tempIndex);
                }
                if(i != 0 ){
                    //需要合并的行数
                    int mergeNum = resultList.size() - threeFlagIndex - 1;
                    if(threeLevelId.equals(currentThreeLevelId)){
                        //如果id相同且到了最后一行，则+2
                        mergeNum = mergeNum + 2;
                    }
                    resultList.get(threeFlagIndex).put("three_level_merge_num", mergeNum);
                    resultList.get(threeFlagIndex).put("three_level_index", threeFlagIndex);
                    //改变时的下标
                    threeFlagIndex = resultList.size();
                }
                //改变时的第3层id
                threeLevelId = currentThreeLevelId;
            }

            if(!towLevelId.equals(currentTowLevelId) || i == calcList.size() - 1){
                if(!towLevelId.equals(currentTowLevelId) && i == (calcList.size() - 1)){
                    //如果id不同，且是最后一行数据则需要多处理一次，将指定位置添加合并行
                    int tempIndex = resultList.size()-1;
                    resultList.get(tempIndex).put("tow_level_merge_num", 2);
                    resultList.get(tempIndex).put("tow_level_index", tempIndex);
                }
                if(i != 0 ){
                    //需要合并的行数
                    int mergeNum = resultList.size() - towFlagIndex - 1;
                    if(towLevelId.equals(currentTowLevelId)){
                        //如果id相同且到了最后一行，则+2
                        mergeNum = mergeNum + 2;
                    }
                    resultList.get(towFlagIndex).put("tow_level_merge_num", mergeNum);
                    resultList.get(towFlagIndex).put("tow_level_index", towFlagIndex);
                    //改变时的下标
                    towFlagIndex = resultList.size();
                }
                //改变时的第2层id
                towLevelId = currentTowLevelId;
            }
            resultList.add(itemMap);
        }
        return resultList;
    }

    /**
     * 获取查询sql
     * @param firstDay 月份第一天
     * @param lastDay 月份最后一天
     * @param cpkConfigDetailList 条件集合
     * @param tableCode 表名
     * @param queryField 字段名
     * @param calcField 重算字段
     * @return 结果
     */
    private String getCondition(
            Date firstDay,
            Date lastDay,
            List<CpkConfigDetail> cpkConfigDetailList,
            String tableCode,
            String queryField,
            String calcField){
        SqlInjectionUtil.customFilterContent(tableCode, false);
        SqlInjectionUtil.customFilterContent(queryField, false);
        SqlInjectionUtil.customFilterContent(calcField, false);
        //初始化返回sql
        StringBuilder result = new StringBuilder();
        result.append("select ").append(queryField).append(" as num from ").append(tableCode)
                .append(" where del_flag = 0 ")
                .append(" and ").append(calcField).append(" >= '").append(firstDay).append("'")
                .append(" and ").append(calcField).append(" <= '").append(lastDay).append("'");
        if(CollUtil.isEmpty(cpkConfigDetailList)){
            return result.toString();
        }

        //获取cpk条件配置
        Map<String, List<CpkConfigDetail>> detailConfigMap = cpkConfigDetailList.stream()
                .collect(Collectors.groupingBy(CpkConfigDetail::getConditionField));
        for (Map.Entry<String, List<CpkConfigDetail>> entry : detailConfigMap.entrySet()) {
            String fieldCode = entry.getKey();
            List<CpkConfigDetail> details = entry.getValue();
            SqlInjectionUtil.customFilterContent(fieldCode, false);
            if(details.size() > 1){
                result.append(" and ").append(fieldCode).append(" in (");
                boolean isFirst = true;
                for(CpkConfigDetail detail : details){
                    if(isFirst){
                        isFirst = false;
                    }else{
                        result.append(",");
                    }
                    result.append("'").append(detail.getConditionValue()).append("'");
                }
                result.append(")");
            }else{
                result.append(" and ").append(fieldCode).append("='").append(details.get(0).getConditionValue()).append("'");
            }
        }
        return result.toString();
    }

    /**
     * 初始化对象
     * @param cpkConfigCalc 新计算的cpk数据
     * @param oldConfigCalc 历史计算的cpk数据
     * @param cpkConfigId cpk配置id
     * @param year 年份
     * @param month 月份
     */
    private CpkConfigCalc initObject(CpkConfigCalc cpkConfigCalc,
                            CpkConfigCalc oldConfigCalc,
                            String cpkConfigId,
                            String year,
                            String month
                            ){
        //历史计算为空的话初始化必要参数
        if(null == oldConfigCalc){
            oldConfigCalc = new CpkConfigCalc();
            oldConfigCalc.setConfigId(cpkConfigId);
            oldConfigCalc.setCalcYear(year);
            oldConfigCalc.setCalcMonth(month);
        }
        //没有计算数据设置为0
        if(null == cpkConfigCalc){
            oldConfigCalc.setRecordNum("0");
            oldConfigCalc.setRecordSum("0");
            oldConfigCalc.setRecordMean("0");
            oldConfigCalc.setCp("0");
            oldConfigCalc.setCpk("0");
            oldConfigCalc.setCpku("0");
            oldConfigCalc.setCpkl("0");
        }else{
            oldConfigCalc.setRecordNum(cpkConfigCalc.getRecordNum());
            oldConfigCalc.setRecordSum(cpkConfigCalc.getRecordSum());
            oldConfigCalc.setRecordMean(cpkConfigCalc.getRecordMean());
            oldConfigCalc.setCp(cpkConfigCalc.getCp());
            oldConfigCalc.setCpk(cpkConfigCalc.getCpk());
            oldConfigCalc.setCpku(cpkConfigCalc.getCpku());
            oldConfigCalc.setCpkl(cpkConfigCalc.getCpkl());
        }
        return oldConfigCalc;
    }
}
