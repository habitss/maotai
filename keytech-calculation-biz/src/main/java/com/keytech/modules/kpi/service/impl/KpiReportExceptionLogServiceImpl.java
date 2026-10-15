package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportExceptionLog;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.mapper.KpiReportExceptionLogMapper;
import com.keytech.modules.kpi.service.*;
import com.keytech.modules.kpi.util.BizConstant;
import com.keytech.modules.kpi.util.DateUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Description: KPI数据修改
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Service
public class KpiReportExceptionLogServiceImpl extends BaseServiceImpl<KpiReportExceptionLogMapper, KpiReportExceptionLog> implements IKpiReportExceptionLogService {

    @Autowired
    private IKpiInfoService kpiInfoService;

    @Autowired
    private IKpiTypeService kpiTypeService;

    @Autowired
    private IKpiReportTargetValueService targetValueService;

    @Autowired
    private IKpiReportActualDayValueService dayValueService;

    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private IKpiReportActualWeekValueService kpiReportActualWeekValueService;

    @Override
    public IPage<KpiReportExceptionLog> queryKpiExcList(KpiReportExceptionLog dto, Page<KpiReportExceptionLog> page, QueryWrapper<KpiReportExceptionLog> queryWrapper) {
        IPage<KpiReportExceptionLog> pageList = this.page(page, queryWrapper);
        if (pageList.getTotal() > 0) {
            addObjectType(pageList.getRecords());
        }
        return pageList;
    }

    @Override
    public void batchAddOrEdit(List<KpiReportExceptionLog> kpiReportExceptionLogs) {
        if (null == kpiReportExceptionLogs.get(0).getId()) {
            //修改页面提交
            kpiReportExceptionLogs.forEach(k -> {
                k.setApproveType(3);
                k.setCreateTime(new Date());
                if (k.getYear() == null) {
                    String kpiDataKey = k.getKpiDataKey();
                    k.setYear(NumberUtil.parseInt(kpiDataKey.substring(0, 4)));
                }
                k.setOldColor(calcColour(k));
                k.setNewColor(calcColour(k));
                if(StrUtil.isBlank(k.getWeekId())){
                    k.setWeekId(null);
                }
            });
            saveBatch(kpiReportExceptionLogs);
        } else {
            //修改页面提交
            kpiReportExceptionLogs.forEach(k -> {
                k.setApproveTime(new Date());
            });
            //审核页面提交
            updateBatchById(kpiReportExceptionLogs);
            List<KpiReportExceptionLog> collect = kpiReportExceptionLogs.stream().filter(k ->
                    1 == k.getApproveType()
            ).collect(Collectors.toList());

            //循环更新
            for(KpiReportExceptionLog exceptionLog : collect){
                //统计指标 1:周累实际 2:月累实际 3:周累目标 4:月累目标 5:月年累实际 6:月年累目标
                int type = exceptionLog.getIndicator();

                String kpiCode = exceptionLog.getKpiCode();
                String year = exceptionLog.getYear().toString();
                String key = exceptionLog.getKpiDataKey();
                String value = exceptionLog.getNewValue();
                //月年累目标、月年累实际格式：2024-09-yearAccumulate
                if(type == 5 || type == 6){
                    key = key + "-yearAccumulate";
                }
                //月累目标key需要转换成英文
                if(type == 4){
                    //获取月份
                    int month = Integer.parseInt(key.split("-")[1]);
                    String[] ALIASES = BizConstant.MONTHS;
                    key = ALIASES[month - 1] + "Value";
                }
                if(type == 1 || type == 2 || type == 5){
                    //实际值更新
                    dayValueService.updateActualValue(kpiCode,year,key,value);
                }else{
                    //目标值更新
                    targetValueService.updateTargetValue(kpiCode,year,key,value);
                }
            }
            //审批后刷新数据
            KpiReportValueDto dto = new KpiReportValueDto();
            String year = DateUtils.formatDateByPattern(new Date(),DateUtils.YEAR_FORMAT);
            String month = DateUtils.formatDateByPattern(new Date(),DateUtils.YEAR_MONTH_FORMAT);
            dto.setYear(Integer.parseInt(year));
            //线程 创建一个固定大小的线程池，线程数量为可用处理器核心数量
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            executorService.submit(() -> {
                kpiReportActualWeekValueService.retryReportData(dto);
            });
        }
    }

    private List<KpiReportExceptionLog> addObjectType(List<KpiReportExceptionLog> logs) {

        //指标类型转换
        HashMap<Integer,String> indicatorMap = new HashMap<>();
        indicatorMap.put(1,"周累实际值");
        indicatorMap.put(2,"月累实际值");
        indicatorMap.put(3,"周累目标值");
        indicatorMap.put(4,"月累目标值");
        indicatorMap.put(5,"月年累实际值");
        indicatorMap.put(6,"月年累目标值");

        //获取所有的KPI code
        List<String> kpiCodes = CollStreamUtil.toList(logs,KpiReportExceptionLog::getKpiCode).stream().distinct().collect(Collectors.toList());
        if(null != kpiCodes && kpiCodes.size() > 0){
            //所有kpi基本信息
            List<KpiInfo> kpiInfoList = kpiInfoService.lambdaQuery().in(KpiInfo::getKpiCode,kpiCodes).list();

            //机构信息
            Map<String, String> idDeparMap = CollStreamUtil.toMap(sysDepartService.list(), SysDepart::getOrgCode, SysDepart::getDepartName);
            //KPI类型信息
            Map<String, String> idTypeMap = CollStreamUtil.toMap(kpiTypeService.list(), KpiType::getId, KpiType::getKpiType);
            logs.forEach(kpiReportExceptionLog -> {
//                String s = kpiReportExceptionLog.getKpiParent().toString();
                Optional<KpiInfo> kpiInfoOptional = kpiInfoList.stream().filter(e->e.getKpiCode().equals(kpiReportExceptionLog.getKpiCode())).findFirst();
                if(kpiInfoOptional.isPresent()){
//                    String objectName = idDeparMap.getOrDefault(Long.parseLong(s.substring(0, 3)), "暂无");
                    String objectName = "";
                    if(StrUtil.isNotBlank(kpiInfoOptional.get().getDeptCode())){
                        objectName = StrUtil.isNotBlank(idDeparMap.get(kpiInfoOptional.get().getDeptCode())) ? idDeparMap.get(kpiInfoOptional.get().getDeptCode()) : "";
                    }

                    String typeName = "";
                    if(Objects.nonNull(kpiInfoOptional.get().getPerentId())){
                        typeName = StrUtil.isNotBlank(idTypeMap.get(kpiInfoOptional.get().getPerentId())) ? idTypeMap.get(kpiInfoOptional.get().getPerentId()) : "";
                    }

                    kpiReportExceptionLog.setObjectName(objectName);
                    kpiReportExceptionLog.setTypeName(typeName);

                    kpiReportExceptionLog.setIndicatorName(indicatorMap.get(kpiReportExceptionLog.getIndicator()));
                }
            });
        }
        return logs;
    }

    private int calcColour(KpiReportExceptionLog k) {
        String kpiCode = k.getKpiCode();
        KpiInfo one = kpiInfoService.lambdaQuery().eq(KpiInfo::getKpiCode, kpiCode).one();
        if (one == null) {
            throw new JeecgBootException("KPI 数据不存在");
        }

        LambdaQueryWrapper<KpiReportTargetValue> query = new LambdaQueryWrapper<>();
        query.likeRight(KpiReportTargetValue::getFactoryCode, one.getFactoryCode());
        query.likeRight(null != k.getYear(), KpiReportTargetValue::getYear, k.getYear());
        query.eq(KpiReportTargetValue::getKpiCode, kpiCode);
        KpiReportTargetValue targetValue = new KpiReportTargetValue();
        List<KpiReportTargetValue> kpiReportTargetValues = targetValueService.list(query);
        if(CollUtil.isNotEmpty(kpiReportTargetValues)){
            targetValue = kpiReportTargetValues.get(0);
        }

        if (targetValue == null) {
            throw new JeecgBootException("KPI 目标数据不存在");
        }
        String kpiTypeWeekValue = targetValue.getKpiTypeWeekValue();
        JSONObject targetBean = new JSONObject();
        if (JSONUtil.isTypeJSON(targetValue.getKpiTypeWeekValue())) {
            targetBean.putAll(JSONUtil.parseObj(kpiTypeWeekValue));
        }
        if (JSONUtil.isTypeJSON(targetValue.getKpiTypeMonthValue())) {
            targetBean.putAll(JSONUtil.parseObj(targetValue.getKpiTypeMonthValue()));
        }
        Integer indicator = k.getIndicator();
        String kpiDataKey = k.getKpiDataKey();
        if (Objects.equals(2,indicator)){
            Map<String, String> map = targetValueService.monthMap(false);
            kpiDataKey =map.get(kpiDataKey.substring(5));
        }
        String targetValueS = targetBean.getStr(kpiDataKey, "");
        //计算颜色
        return targetValueService.getColour(targetBean, k.getOldValue(), targetValueS, one.getCompareValue());
    }
}
