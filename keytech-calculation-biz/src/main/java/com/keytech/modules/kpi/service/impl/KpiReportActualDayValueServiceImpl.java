package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.dto.KPiConfigDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.kpi.mapper.KpiReportActualDayValueMapper;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportActualDayValueService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.kpi.util.BuildCellUtil;
import com.keytech.modules.kpi.vo.KpiReportCellVo;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: KPI实际值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Service
public class KpiReportActualDayValueServiceImpl extends BaseServiceImpl<KpiReportActualDayValueMapper, KpiReportActualDayValue> implements IKpiReportActualDayValueService {

    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IKpiInfoService kpiInfoService;
    @Autowired
    private IKpiReportActualWeekValueService weekValueService;
    @Autowired
    private KpiReportActualDayValueMapper dayValueMapper;
    /**
     * 获取表头
     * @param kpiReportActualDayValue
     * @return
     */
    @Override
    public List<KpiReportCellVo> queryHeader(KpiReportActualDayValue kpiReportActualDayValue) {

        String monthStr = kpiReportActualDayValue.getMonth();
        DateTime currentMonth = DateUtil.parse(monthStr, "yyyy-MM");
        int year = DateUtil.year(currentMonth);
        int month = DateUtil.month(currentMonth)+1;
        List<KpiReportCellVo> cellVoList = new ArrayList<>();
        //Map<Integer, String> weekOfDate = getWeekOfYear(year);
        //获取周
        MdmWeek mdmWeek = new MdmWeek();
        mdmWeek.setYear(year+"");
        mdmWeek.setMonth(month+"");
        List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);

        //KPI
        KpiReportCellVo build =KpiReportCellVo.builder().title("KPI").key("kpiName").sortIndex("").build();
        cellVoList.add(build);
        BuildCellUtil.buildCell("年累", monthStr + "", monthStr + "-yearAccumulate", cellVoList);
        //周数据
        for (MdmWeek week : weekList) {
            String startDate = DateUtil.format(week.getStartDate(), "MM.dd");
            String endDate = DateUtil.format(week.getEndDate(), "MM.dd");
            BuildCellUtil.buildCell("第"+week.getWeekNumber()+"周", startDate+"-"+endDate, "w_"+week.getYear()+"_"+week.getWeekNumber(), cellVoList);
        }
        BuildCellUtil.buildCell("月累", monthStr, monthStr, cellVoList);
        return cellVoList;
    }



    /**
     * 获取数据(实际值)
     * @param dto
     * @return
     */
    @Override
    public List<Map<String, Object>>queryData(KpiReportValueDto dto) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获取表头
        KpiReportActualDayValue kpiReportActualDayValue = new KpiReportActualDayValue().setMonth(dto.getMonth());
        //获取表头数据
        List<KpiReportCellVo> listHeader = queryHeader(kpiReportActualDayValue);
        //String dept = dto.getDeptCode();
        String factory = dto.getFactory();

        //获取年
        DateTime currentMonth = DateUtil.parse(dto.getMonth(), "yyyy-MM");
        int year = DateUtil.year(currentMonth);
        dto.setYear(year);

        //获取KPI集合 (带上年份)
        dto.setShowChildrenKpi(true);
        dto.setVisibleWrite(1);
        List<KPiConfigDto> configDtos = kpiInfoService.listKpi(dto);
        if(null != configDtos && configDtos.size() > 0){
            //获取KPICode集合
            List<String> kpiCodes = configDtos.stream().map(KPiConfigDto::getKpiCode).collect(Collectors.toList());

            //找到对应的key
            Map<String, String> keyIndexMap = new HashMap<>();
            //
            BuildCellUtil.getChildrenRecursion(keyIndexMap, listHeader);

            //获取实际值
//        QueryWrapper<KpiReportActualDayValue> qw = new QueryWrapper<>();
//        qw.likeRight(StrUtil.isNotBlank(factory), "v.factory", factory);
//        qw.in(CollUtil.isNotEmpty(kpiCodes), "v.kpi_code", kpiCodes);
//        LambdaQueryWrapper<KpiReportActualDayValue> queryWrapper = qw.lambda();
//        //查询数据
//        queryWrapper.eq(KpiReportActualDayValue::getYear, year);
//        //填报值
//        List<KpiReportActualDayValue> mdmKpiReportActualDayValues = dayValueMapper.listJoinKpiInfo(queryWrapper);

            //获取实际值
            LambdaQueryWrapper<KpiReportActualDayValue> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(KpiReportActualDayValue::getFactoryCode,factory);
            lambdaQueryWrapper.in(KpiReportActualDayValue::getKpiCode,kpiCodes);
            lambdaQueryWrapper.eq(KpiReportActualDayValue::getYear,year);
            List<KpiReportActualDayValue> mdmKpiReportActualDayValues = list(lambdaQueryWrapper);

            //转map <KPICode, KpiReportActualDayValue>
            Map<String, KpiReportActualDayValue> actualDayValueMap = CollStreamUtil.toMap(mdmKpiReportActualDayValues, KpiReportActualDayValue::getKpiCode, bean -> bean);

            //数据融合,排序
            resultList = configDtos.stream()
                .map(bean -> getActualValueMap(dto, year, keyIndexMap, actualDayValueMap, bean))
                .sorted(Comparator.comparing(map -> (Comparable) map.get("inputSort")!=null?Double.parseDouble(map.get("inputSort")+""):null, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        }

        return resultList;
    }



    /**
     * 获取实际值映射
     * @param dto KPI报告实际值日数据传输对象
     * @param year 年份
     * @param keyIndexMap 键值对映射
     * @param actualDayValueMap 实际值日值映射
     * @param bean KPI配置数据传输对象
     * @return 实际值映射
     */
    private Map<String, Object> getActualValueMap(KpiReportValueDto dto, int year, Map<String, String> keyIndexMap,
                                                  Map<String, KpiReportActualDayValue> actualDayValueMap, KPiConfigDto bean) {
        String kpiCode = bean.getKpiCode();
        Map<String, Object> valueMap = new HashMap<>();
        if (actualDayValueMap.containsKey(kpiCode)) {
            KpiReportActualDayValue actualDayValue = actualDayValueMap.get(kpiCode);
            BeanUtil.copyProperties(actualDayValue, bean);
        }
        //名称加单位拼接
        String unit = bean.getUnit();
        if (StrUtil.isNotBlank(unit)) {
            unit = "(" + unit + ")";
        } else {
            unit = "";
        }
        String intPut = bean.getInputPermission();
        if (StrUtil.isNotEmpty(intPut) && StrUtil.isNotBlank(dto.getReportId())) {
            valueMap.put("inputPermission", intPut.contains(dto.getReportId()) ? 1 : 0);
        }
        valueMap.put("kpiName", bean.getKpiName() + unit);
        valueMap.put("reportId", dto.getReportId());
        valueMap.put("kpiCode", kpiCode);
        valueMap.put("id", bean.getId());
        valueMap.put("year", year);

        String dataValueStr = bean.getValue();
        JSONObject parse = null;
        if (JSONUtil.isTypeJSON(dataValueStr)) {
            parse = JSONUtil.parseObj(dataValueStr);
        }
        JSONObject finalParse = parse;
        keyIndexMap.forEach((key, value) -> {
            if ("kpiName".equals(key)) {
                return;
            }
            if (Objects.nonNull(finalParse)) {
                value = finalParse.getByPath(key, String.class);
                finalParse.forEach((keyStr, valueStr) -> {
                    if (!keyStr.equals(key) && !"kpiName".equals(keyStr)) {
                        valueMap.put(keyStr, valueStr);
                    }
                });
            }
            valueMap.put(key, value);
        });
        //排序
        valueMap.put("inputSort", bean.getInputSort());
        return valueMap;
    }

    /**
     * KPI实际值保存
     * @param mapList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<KpiReportActualDayValue> saveData(List<Map<String, Object>> mapList) {
        if(CollUtil.isEmpty(mapList)){
            return null;
        }
        Map<String, PropertyDescriptor> propertyDescriptorMap = BeanUtil.getPropertyDescriptorMap(KpiReportActualDayValue.class, false);
        //年份
        String year = mapList.get(0).get("year")+"";
        Map<Integer, MdmWeek> weekMapNew = mdmWeekService.getWeekDataByWeekNum(year);
        List<KpiReportActualDayValue> collect = mapList.stream().map(bean -> {

            Map<String, Object> dynamicMap = new HashMap<>(16);
            Map<String, Object> fixedMap = new HashMap<>(16);
            Map<String, Object> newBean = new HashMap<>(bean);
            bean.forEach((k, v) -> {
                //空字符可以改
                if (StrUtil.isNullOrUndefined(v + "")) {
                    return;
                }
                if (propertyDescriptorMap.containsKey(k)) {
                    // 固定字段
                    fixedMap.put(k, v);
                } else {
                    if(!k.contains("|")){
                        //处理比值带是中文符号的问题
                        String value = v + "";
                        if (value.contains("：")) {
                            v = value.replace("：", ":");
                        }
                        // 动态字段 周累 月累
                        dynamicMap.put(k, v);
                        // 同步到周表和月表数据


                        if (k.startsWith("w_")) {
                            //获取周数 (w_2025_42)
                            //获取时段
                            String[] keys = k.split("_");
                            int weekNumber = Integer.parseInt(keys[2]);
                            //设置周数
                            newBean.put("weekNumber",weekNumber);
                            newBean.put("newKey",k); //存储key w_2025_42
                            //更新key 格式 (2023-11-20|2023-11-26)
                            //newBean.put("monthStr",)
                            MdmWeek mdmWeek = weekMapNew.get(weekNumber);
                            String month = mdmWeek.getMonth();
                            if(StrUtil.isNotBlank(month)){
                                month = Integer.parseInt(month)<10?"0"+month:month;
                            }
                            newBean.put("monthStr",mdmWeek.getYear()+month);
                            k = mdmWeek.getWeekRangeStr();

                        }
                        //生成周月
                        weekValueService.addWeekAndMonth(newBean, k, v);
                    }

                }
            });
            KpiReportActualDayValue actualDayValue = BeanUtil.mapToBean(fixedMap, KpiReportActualDayValue.class, false, CopyOptions.create());
            actualDayValue.setIndicator(1);
            actualDayValue.setEndTime(actualDayValue.getStartTime());
            actualDayValue.setValue(JSONUtil.toJsonStr(dynamicMap));

            return actualDayValue;
        }).collect(Collectors.toList());
        saveOrUpdateBatch(collect, 500);
        return collect;
    }


    /**
     * 更新实际值 年累  周
     * kpiCode KPICode
     * year 年
     * key 需要更新的key 如月(2024-09) 年累(2024-09-yearAccumulate) 周(2024-08-19|2024-08-25)
     * value 值
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateActualValue(String kpiCode, String year, String key, String value) {
        Boolean isOk = false;
        if(StrUtil.isNotBlank(kpiCode) && StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)){
            //查询实际值
            KpiReportActualDayValue actualDayValue = lambdaQuery()
                    .eq(KpiReportActualDayValue::getYear,year)
                    .eq(KpiReportActualDayValue::getKpiCode,kpiCode).one();
            if(null != actualDayValue && StrUtil.isNotBlank(actualDayValue.getId())) {
                String valueJson = actualDayValue.getValue();
                if(StrUtil.isNotBlank(valueJson)){
                    Map<String, Object> dtoMap = BeanUtil.beanToMap(actualDayValue);
                    JSONObject object = JSONUtil.parseObj(valueJson);
                    object.set(key,value);
                    //把 object 加入 dtoMap 中
                    object.forEach((k, v) -> {
                        dtoMap.put(k, v);
                    });
                    // 同步到周表和月表数据
                    weekValueService.addWeekAndMonth(dtoMap, key, value);
                    //存储
                    actualDayValue.setIndicator(1);
                    actualDayValue.setEndTime(actualDayValue.getStartTime());
                    actualDayValue.setValue(JSONUtil.toJsonStr(object));
                    updateById(actualDayValue);
                    isOk = true;
                }
            }
        }
        return isOk;
    }

    /**
     * KPI实际值查询(录入查询)
     * @param dto
     * @return
     */
    @Override
    public List<Map<String, Object>> actualDataList(KpiReportValueDto dto) {
        //获取表头
        KpiReportActualDayValue kpiReportActualDayValue = new KpiReportActualDayValue().setMonth(dto.getMonth());
        List<KpiReportCellVo> listHeader = queryHeader(kpiReportActualDayValue);
        String dept = dto.getDept();
        String factory = dto.getFactory();
        dto.setShowChildrenKpi(true);
        //是否在填录入中显示
        dto.setVisibleWrite(1);
        //获取kpi
        List<KPiConfigDto> configDtos = kpiInfoService.listKpi(dto);
        List<String> kpiCodes = configDtos.stream().map(KPiConfigDto::getKpiCode).collect(Collectors.toList());
        DateTime currentMonth = DateUtil.parse(dto.getMonth(), "yyyy-MM");
        int year = DateUtil.year(currentMonth);
        //找到对应的key
        Map<String, String> keyIndexMap = new HashMap<>();
        BuildCellUtil.getChildrenRecursion(keyIndexMap, listHeader);
        QueryWrapper<KpiReportActualDayValue> qw = new QueryWrapper<>();
        qw.likeRight(StrUtil.isNotBlank(factory), "v.factory_code", factory);
//        qw.eq(StrUtil.isNotBlank(dept), "v.dept", dept);
        qw.in(CollUtil.isNotEmpty(kpiCodes), "v.kpi_code", kpiCodes);
        qw.eq("v.year", year);
        qw.like("k.year", year);
        qw.eq("v.del_flag", "0");
        qw.eq("k.del_flag", "0");
        LambdaQueryWrapper<KpiReportActualDayValue> queryWrapper = qw.lambda();
        //查询数据
        //queryWrapper.eq(StrUtil.isNotBlank(dto.getReportId()), MdmKpiReportActualDayValue::getReportId, dto.getReportId());
//        queryWrapper.eq(MdmKpiReportActualDayValue::getActualType, isomerism);
//        queryWrapper.eq(KpiReportActualDayValue::getYear, year);
        //填报值
        List<KpiReportActualDayValue> mdmKpiReportActualDayValues = dayValueMapper.listJoinKpiInfo(queryWrapper);
        Map<String, KpiReportActualDayValue> actualDayValueMap = CollStreamUtil.toMap(mdmKpiReportActualDayValues, KpiReportActualDayValue::getKpiCode, bean -> bean);
        //处理多余的 todo排序
        Map<String, Map<String, Object>> kpiCode = configDtos.stream().map(bean -> getActualValueMap(dto, year, keyIndexMap, actualDayValueMap, bean)).collect(Collectors.toMap(bean -> (String) bean.get("kpiCode"), bean -> bean));

        //排序
        KpiTypeDto kpiTypeDto = new KpiTypeDto();
        kpiTypeDto.setOrgCode(dto.getReportId());
        kpiTypeDto.setEnableSort(true);
        List<Map<String, Object>> list = kpiInfoService.kpiSortList(kpiTypeDto, kpiCode);

        List<Map<String, Object>> sortedList = list.stream()
                // 自定义比较器，先处理非空值的比较，null值放到最后
                .sorted(Comparator.comparing(map -> (Comparable) map.get("inputSort")!=null?Double.parseDouble(map.get("inputSort")+""):null, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        return sortedList;
    }

    /**
     * 数据更新
     *
     * @param listData 实际数据列表，实际值集合
     * @param year           年份，用于标识数据的年份
     */
    @Override
    public void updateByImport(List<Map<String, Object>> listData, String year) {
        if (CollUtil.isEmpty(listData)) {
            return;
        }
        //kpiCode集合
        Set<String> kpiCodeList = listData.stream().map(bena -> (String) bena.get("kpiCode")).collect(Collectors.toSet());
        //查询所有的kpi
        List<KpiInfo> list = kpiInfoService.listReprot(kpiCodeList);
        //转化成map kpiCode-->kpi对象
        Map<String, KpiInfo> kpiMap = CollStreamUtil.toMap(list, KpiInfo::getKpiCode, bean -> bean);
        //查询所有的kpi实际值填写记录
        List<KpiReportActualDayValue> actualDayValueList = lambdaQuery().in(KpiReportActualDayValue::getKpiCode, kpiCodeList).eq(KpiReportActualDayValue::getYear, year).list();
        //实际值 的 kpiCode-->实际值对象
        Map<String, KpiReportActualDayValue> dayValueMap = CollStreamUtil.toMap(actualDayValueList, KpiReportActualDayValue::getKpiCode, bean -> bean);
        //处理实际值表
        List<JSONObject> weekMonthDataList = new ArrayList<>();
        List<KpiReportActualDayValue> collect = listData.stream().filter(bean -> kpiMap.containsKey((String) bean.get("kpiCode"))).map(bean -> {
            //数据类型
            String kpiCode = (String) bean.get("kpiCode");
            //kpi对象
            KpiInfo kpiInfo = kpiMap.get(kpiCode);
            KpiReportActualDayValue actualDayValue = dayValueMap.getOrDefault(kpiCode, new KpiReportActualDayValue());
            //实际值json
            String valueJson = actualDayValue.getValue();
            if (!JSONUtil.isTypeJSONObject(valueJson)) {
                valueJson = "{}";
            }
            JSONObject valueObj = JSONUtil.parseObj(valueJson);
            String orgCode = kpiInfo.getOrgCode();

            JSONObject weekAndMonth = new JSONObject();
            weekAndMonth.putOpt("reportId", orgCode).putOpt("year", year)
                    .putOpt("kpiCode", kpiCode).putOpt("factory", kpiInfo.getFactoryCode()).putOpt("kpiInfo", kpiInfo);

            bean.forEach((k, v) -> {
                //空字符
                if (StrUtil.isNullOrUndefined(v + "")) {
                    return;
                }
                //if (!(k.contains("|") || k.contains("-") || k.matches("\\d{4}"))) {
                if (!(k.contains("w_") || k.contains("-") || k.matches("\\d{4}"))) {
                    return;
                }
                //数据处理
                weekAndMonth.putOpt(k, v);

                //处理动态key 值替换
                valueObj.putOpt(k, v);

            });
            weekMonthDataList.add(weekAndMonth);

            //actualDayValue.setActualType(kpiInfo.getIsomerism());
            actualDayValue.setTenantId(kpiInfo.getTenantId());
            actualDayValue.setFactoryCode(kpiInfo.getFactoryCode());
            actualDayValue.setValue(valueObj.toString());
            actualDayValue.setYear(NumberUtil.parseInt(year));
            actualDayValue.setKpiCode(kpiCode);
            actualDayValue.setIndicator(1);
            actualDayValue.setReportId(orgCode);

            return actualDayValue;
        }).collect(Collectors.toList());
        //更新实际值(json)
        saveOrUpdateBatch(collect, 500);
        //处理周和月表 算颜色
        weekValueService.updateWeekAndMonth(weekMonthDataList, year);
    }

    /**
     * 递归获取子节点
     *
     * @param keyIndexMap 存储键和默认值的映射关系的 Map
     * @param listHeader  KpiReportCellVo 列表头
     */
    @Override
    public void getChildrenRecursion(Map<String, String> keyIndexMap, List<KpiReportCellVo> listHeader) {
        for (KpiReportCellVo kpiReportCellVo : listHeader) {
            List<KpiReportCellVo> children = kpiReportCellVo.getChildren();
            if (CollUtil.isEmpty(children)) {
                String key = kpiReportCellVo.getKey();
                String defValue = "";
                if (key.contains("_bz_1")) {
                    defValue = "0.000";
                }
                keyIndexMap.put(key, defValue);
            } else {
                getChildrenRecursion(keyIndexMap, children);
            }
        }
    }
}
