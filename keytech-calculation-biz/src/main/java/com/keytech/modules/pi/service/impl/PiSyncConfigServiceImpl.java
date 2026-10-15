package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.pi.dto.CalcResultDto;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.entity.PiSyncConfig;
import com.keytech.modules.pi.mapper.PiSyncConfigMapper;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.service.IPiSyncConfigService;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: PI数据同步配置
 * @Author: jeecg-boot
 * @Date:   2026-03-31
 * @Version: V1.0
 */
@Slf4j
@Service
public class PiSyncConfigServiceImpl extends BaseServiceImpl<PiSyncConfigMapper, PiSyncConfig> implements IPiSyncConfigService {

    @Autowired
    private IPiCustomizeTableService piCustomizeTableService;
    /**
     * 数据同步[交叉进罐] (目标表多个字段，对应数据源表多行数据，需按关联键行转列聚合后写入目标表)
     *
     * groupKey  数据类型 1交叉进罐 2.酵母排放
     * fewDay   源几天内数据(默认3天)
     *
     * 1.获取配置表
     * 2.获取目标表数据 3天内时间(创建时间)
     * 3.通过关联字段集合 条件 获取源表数据 按时间排序
     * 4.判断源表数据顺序 按顺序写入 (第1条 对应配置第一条)
     *
     */
    @Override
    public void intersectionData(Map map) {
       // 1.获取配置表（查询启用的配置，按排序字段升序）
       List<PiSyncConfig> configList = this.list(new LambdaQueryWrapper<PiSyncConfig>()
               .eq(PiSyncConfig::getEnabled, 1)
               .eq(PiSyncConfig::getGroupKey, map.get("groupKey"))
               .orderByAsc(PiSyncConfig::getSort)
       );
       if (configList == null || configList.isEmpty()) {
           log.info("PI数据同步配置表无启用数据，跳过同步");
           return;
       }
       log.info("PI数据同步配置表获取到{}条启用配置", configList.size());
       // configList分组得到map key sort val 为 List<PiSyncConfig>
       Map<Double, List<PiSyncConfig>> configMap = configList.stream()
               .collect(Collectors.groupingBy(PiSyncConfig::getSort));


       // 2.获取目标表数据 N天内时间(创建时间)
       PiSyncConfig piSyncConfig =  configList.get(0);
       String tableName = piSyncConfig.getTargetTable();
       String sourceTable = piSyncConfig.getSourceTable();
       //对照 [{"target":"field_0001", "source":"field_0001"}]
       String fieldMapping = piSyncConfig.getFieldMapping();
       String sourceSort = piSyncConfig.getSourceSort();

       //源数据查询条件字段
       String targetCondition = configList.get(0).getTargetCondition();

       String fewDayStr = (String) map.get("fewDay");
       int fewDay = StrUtil.isBlank(fewDayStr) ? 3 : NumberUtil.parseInt(fewDayStr);

       //获取PI N天内数据
       String startTime = LocalDateTime.now().minusDays(fewDay).format(DateTimeFormatter.ofPattern(DateUtils.DATE_TIME_STRING));
       //结束时间
       String endTime = DateUtils.formatDateTime(new Date());

       TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
       tableConditionQueryDto.setTable(tableName);
       tableConditionQueryDto.setCondition(targetCondition + " >= '" + startTime + "' AND " +
                                           targetCondition + " <= '" + endTime +"' ");

       tableConditionQueryDto.setSortField(targetCondition);
       tableConditionQueryDto.setSortMode("ASC");
       // 获取目标表数据
       List<Map<String, Object>> targetDataList = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);
       if (targetDataList == null || targetDataList.isEmpty()) {
           log.info("目标表{}无3天内数据，跳过同步", tableName);
           return;
       }
       log.info("目标表{}获取到{}条3天内数据", tableName, targetDataList.size());

        // 对照 [{"target":"field_0001", "source":"field_0001"}]
        JSONArray mappingArray = JSONUtil.parseArray(fieldMapping);

       // configMap的key排序，用于按顺序匹配源表数据
       List<Double> sortedKeys = configMap.keySet().stream().sorted().collect(Collectors.toList());

       // 3.通过关联字段集合 条件 获取源表数据 按时间排序
       for (Map<String, Object> stringObjectMap : targetDataList) {
           //构建源表查询条件
           StringBuilder stb = new StringBuilder();
           stb.append(" del_flag = 0 ");
           for (int i = 0; i < mappingArray.size(); i++) {
               JSONObject mappingItem = mappingArray.getJSONObject(i);
               String targetField = mappingItem.getStr("target");
               String sourceField = mappingItem.getStr("source");
               stb.append(" AND ").append(sourceField).append(" = '").append(stringObjectMap.get(targetField)).append("'");
           }

           TableConditionQueryDto sourceCondition = new TableConditionQueryDto();
           sourceCondition.setTable(sourceTable);
           sourceCondition.setCondition(stb.toString());
           sourceCondition.setSortField(sourceSort);
           sourceCondition.setSortMode("ASC");
           // 获取源数据
           List<Map<String, Object>> sourceDataList = piCustomizeTableService.getTableByCondition(sourceCondition);

           //4.判断源表数据顺序 按顺序写入 (如 第1条 对应配置第一条)
           List<TableFieldValueDto> fieldList = new ArrayList<>();
           //计算分组结果Map (key: calcGroup, value: 计算结果对象)
           Map<String, CalcResultDto> calcResultMap = new HashMap<>();

           for (int i = 0; i < sourceDataList.size() && i < sortedKeys.size(); i++) {
               Map<String, Object> valMap = sourceDataList.get(i);
               List<PiSyncConfig> piSyncConfigList = configMap.get(sortedKeys.get(i));
               for (PiSyncConfig syncConfig : piSyncConfigList) {
                   // 判断是否包含","  包含源字段有多个值拼接 分别取出合并写入val 如 值1值2
                   Object val = "";
                   if(syncConfig.getSourceField().contains(",")){
                       // 多个值拼接
                       StringBuilder sb = new StringBuilder();
                       for (String field : syncConfig.getSourceField().split(",")) {
                           Object v = valMap.get(field.trim());
                           if (v != null) {
                               sb.append(v);
                           }
                       }
                       val = sb.toString();
                   }else{
                       // 单个值
                       val = valMap.get(syncConfig.getSourceField());
                   }

                   String strVal = formatValue(val);
                   fieldList.add(new TableFieldValueDto(syncConfig.getTargetField(), strVal));

                   //配置化计算逻辑：根据calcType和calcGroup进行计算
                   String calcType = syncConfig.getCalcType();
                   String calcGroup = syncConfig.getCalcGroup();
                   String calcResultField = syncConfig.getCalcResultField();

                   if (calcType != null && !"NONE".equals(calcType) && calcGroup != null && calcResultField != null) {
                       CalcResultDto calcResult = calcResultMap.computeIfAbsent(calcGroup,
                           k -> new CalcResultDto(calcResultField, calcType));
                       calcResult.addValue(strVal);
                   }
               }
           }

           //将计算结果添加到字段列表
           for (CalcResultDto calcResult : calcResultMap.values()) {
               fieldList.add(new TableFieldValueDto(calcResult.getResultField(), calcResult.calculate()));
           }


           //更新目标表数据
           if (!fieldList.isEmpty()) {
               TableSaveDto tableSaveDto = new TableSaveDto();
               tableSaveDto.setTable(tableName);
               tableSaveDto.setFieldList(fieldList);
               List<TableConditionDto> conditionList = new ArrayList<>();
               conditionList.add(new TableConditionDto("id", stringObjectMap.get("id") + ""));
               tableSaveDto.setConditionList(conditionList);

               System.out.println(tableSaveDto.getTable());
               piCustomizeTableService.tableSave(tableSaveDto);
           }
       }

    }

    /**
     * 数据同步[废醇母排放]（源表驱动查询，按配置对照写入目标表）
     *
     * groupKey  数据类型
     * fewDay    源几天内数据(默认3天)
     *
     * 1.获取配置表
     * 2.查询源表N天内数据,将数据按条件分组(将同一组数据合并成一个集合)
     * 3.遍历源表数据，按配置字段对照拼接
     * 4.写入目标表
     *
     */
    @Override
    public void sourceSyncData(Map map) {
       // 1.获取配置表（查询启用的配置，按排序字段升序）
       List<PiSyncConfig> configList = this.list(new LambdaQueryWrapper<PiSyncConfig>()
               .eq(PiSyncConfig::getEnabled, 1)
               .eq(PiSyncConfig::getGroupKey, map.get("groupKey"))
               .orderByAsc(PiSyncConfig::getSort)
       );
       if (configList == null || configList.isEmpty()) {
           log.info("PI数据同步配置表无启用数据，跳过同步");
           return;
       }
       log.info("PI数据同步配置表获取到{}条启用配置", configList.size());

       // 2.获取表信息
       PiSyncConfig piSyncConfig = configList.get(0);
       String tableName = piSyncConfig.getTargetTable();
       // 源表
       String sourceTable = piSyncConfig.getSourceTable();
       String sourceSort = piSyncConfig.getSourceSort();
       String targetCondition = piSyncConfig.getTargetCondition();
       String fieldMapping = piSyncConfig.getFieldMapping();
       String fewDayStr = (String) map.get("fewDay");
       int fewDay = StrUtil.isBlank(fewDayStr) ? 3 : NumberUtil.parseInt(fewDayStr);

       //获取源表N天内数据
       String startTime = LocalDateTime.now().minusDays(fewDay).format(DateTimeFormatter.ofPattern(DateUtils.DATE_TIME_STRING));
       String endTime = DateUtils.formatDateTime(new Date());

       //查询源表数据
       TableConditionQueryDto sourceQueryDto = new TableConditionQueryDto();
       sourceQueryDto.setTable(sourceTable);
       sourceQueryDto.setCondition(" del_flag = 0 AND " + targetCondition + " >= '" + startTime + "' AND " +
                                   targetCondition + " <= '" + endTime + "' ");
       sourceQueryDto.setSortField(sourceSort);
       sourceQueryDto.setSortMode("ASC");

       List<Map<String, Object>> sourceDataList = piCustomizeTableService.getTableByCondition(sourceQueryDto);
       if (sourceDataList == null || sourceDataList.isEmpty()) {
           log.info("源表{}无{}天内数据，跳过同步", sourceTable, fewDay);
           return;
       }
       log.info("源表{}获取到{}条{}天内数据", sourceTable, sourceDataList.size(), fewDay);

       JSONArray mappingArray = JSONUtil.parseArray(fieldMapping);

       // 3.从fewDay查询结果中提取分组键[唯一约束]（确定哪些分组有新数据）
       Set<String> activeGroupKeys = new LinkedHashSet<>();
       Map<String, Map<String, Object>> groupKeySampleMap = new LinkedHashMap<>();
       for (Map<String, Object> sourceData : sourceDataList) {
           StringBuilder keyBuilder = new StringBuilder();
           for (int i = 0; i < mappingArray.size(); i++) {
               JSONObject mappingItem = mappingArray.getJSONObject(i);
               String sourceField = mappingItem.getStr("source");
               Object val = sourceData.get(sourceField);
               if (i > 0) {
                   keyBuilder.append("-");
               }
               keyBuilder.append(val != null ? val.toString() : "");
           }
           String groupKey = keyBuilder.toString();
           //排查source中 field_0005(罐号) 值为0的，提前跳过，避免不必要的全量查询
           Object tankNo = sourceData.get("field_0005");
           if (tankNo != null && "0".equals(tankNo.toString().trim())) {
               continue;
           }
           if (activeGroupKeys.add(groupKey)) {
               groupKeySampleMap.put(groupKey, sourceData);
           }
       }

       // 4.对每个分组，单独查源表全量数据（不限时间），确保数据完整
       int totalWriteCount = 0;
       int configCount = configList.size() / 3;
       for (Map.Entry<String, Map<String, Object>> entry : groupKeySampleMap.entrySet()) {
           String groupKey = entry.getKey();
           Map<String, Object> sampleData = entry.getValue();

           // 构建该分组的查询条件：按fieldMapping字段精确匹配，不限定时间范围
           StringBuilder groupConditionBuilder = new StringBuilder();
           groupConditionBuilder.append(" del_flag = 0 ");
           for (int i = 0; i < mappingArray.size(); i++) {
               JSONObject mappingItem = mappingArray.getJSONObject(i);
               String sourceField = mappingItem.getStr("source");
               Object val = sampleData.get(sourceField);
               groupConditionBuilder.append(" AND ").append(sourceField).append(" = '").append(val).append("'");
           }

           TableConditionQueryDto groupQueryDto = new TableConditionQueryDto();
           groupQueryDto.setTable(sourceTable);
           groupQueryDto.setCondition(groupConditionBuilder.toString());
           groupQueryDto.setSortField(sourceSort);
           groupQueryDto.setSortMode("ASC");
           List<Map<String, Object>> groupDataList = piCustomizeTableService.getTableByCondition(groupQueryDto);

           if (groupDataList == null || groupDataList.isEmpty()) {
               log.info("分组{}查源表无数据，跳过", groupKey);
               continue;
           }
           log.info("分组{}查源表获取到{}条全量数据", groupKey, groupDataList.size());

           // 5.按配置顺序匹配源数据，第i条源数据对应第i组配置
           List<TableFieldValueDto> fieldList = new ArrayList<>();
           for (int i = 0; i < groupDataList.size() && i < configCount; i++) {
               Map<String, Object> sourceData = groupDataList.get(i);
               //for 3次 当i=0 得到k 0,1,2  当i=1 得到k 3,4,5
               for (int j = 0; j < 3; j++) {
                   int k = i * 3 + j;
                   PiSyncConfig syncConfig = configList.get(k);
                   Object val = sourceData.get(syncConfig.getSourceField());
                   String strVal = formatValue(val);
                   fieldList.add(new TableFieldValueDto(syncConfig.getTargetField(), strVal));
               }
           }

           //写入目标表
           if (!fieldList.isEmpty()) {
               TableSaveDto tableSaveDto = new TableSaveDto();
               tableSaveDto.setTable(tableName);
               tableSaveDto.setFieldList(fieldList);
               // 添加fieldMapping中的字段作为条件
               List<TableConditionDto> conditionList = new ArrayList<>();
               for (int j = 0; j < mappingArray.size(); j++) {
                   JSONObject mappingItem = mappingArray.getJSONObject(j);
                   String targetField = mappingItem.getStr("target");
                   String sourceField = mappingItem.getStr("source");
                   // 从分组第一条数据获取条件值
                   Object targetValue = groupDataList.get(0).get(sourceField);
                   if (targetValue != null) {
                       conditionList.add(new TableConditionDto(targetField, targetValue + ""));
                   }
               }
               tableSaveDto.setConditionList(conditionList);
               piCustomizeTableService.tableSave(tableSaveDto);
               totalWriteCount++;
           }
       }
       log.info("源表{}数据同步完成，共写入{}条", sourceTable, totalWriteCount);
    }

    /**
     * 数据同步 (一对一写入)
     * 1.获取配置表数据
     * 2.获取目标表数据(查询时间段(多少天内)[条件字段,排序字段,排序方式])
     * 3.通过关联字段集合 条件 获取源表数据 按指定字段排序
     * 4.匹配数据
     * 5.写入数据
     * @param map
     */
    @Override
    public void synchronization(Map map) {
        // 1.获取配置表（查询启用的配置，按排序字段升序）
        List<PiSyncConfig> configList = this.list(new LambdaQueryWrapper<PiSyncConfig>()
                .eq(PiSyncConfig::getEnabled, 1)
                .eq(PiSyncConfig::getGroupKey, map.get("groupKey"))
                .orderByAsc(PiSyncConfig::getSort)
        );
        if (configList == null || configList.isEmpty()) {
            log.info("PI数据同步配置表无启用数据，跳过同步");
            return;
        }
        log.info("PI数据同步配置表获取到{}条启用配置", configList.size());

        // 2.获取目标表数据 N天内时间(创建时间)
        PiSyncConfig piSyncConfig =  configList.get(0);
        String tableName = piSyncConfig.getTargetTable();
        String sourceTable = piSyncConfig.getSourceTable();
        //对照 [{"target":"field_0001", "source":"field_0001"}]
        String fieldMapping = piSyncConfig.getFieldMapping();
        String sourceSort = piSyncConfig.getSourceSort();

        //源数据查询条件字段
        String targetCondition = configList.get(0).getTargetCondition();

        String fewDayStr = (String) map.get("fewDay");
        int fewDay = StrUtil.isBlank(fewDayStr) ? 3 : NumberUtil.parseInt(fewDayStr);

        //获取PI N天内数据
        String startTime = LocalDateTime.now().minusDays(fewDay).format(DateTimeFormatter.ofPattern(DateUtils.DATE_TIME_STRING));
        //结束时间
        String endTime = DateUtils.formatDateTime(new Date());

        TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
        tableConditionQueryDto.setTable(tableName);
        tableConditionQueryDto.setCondition(targetCondition + " >= '" + startTime + "' AND " +
                targetCondition + " <= '" + endTime +"' ");

        tableConditionQueryDto.setSortField(targetCondition);
        tableConditionQueryDto.setSortMode("ASC");
        // 获取目标表数据
        List<Map<String, Object>> targetDataList = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);
        if (targetDataList == null || targetDataList.isEmpty()) {
            log.info("目标表{}无3天内数据，跳过同步", tableName);
            return;
        }
        log.info("目标表{}获取到{}条3天内数据", tableName, targetDataList.size());

        // 对照 [{"target":"field_0001", "source":"field_0001"}]
        JSONArray mappingArray = JSONUtil.parseArray(fieldMapping);

        // 3.通过关联字段集合 条件 获取源表数据 按时间排序
        for (Map<String, Object> stringObjectMap : targetDataList) {
            //构建源表查询条件
            StringBuilder stb = new StringBuilder();
            stb.append(" del_flag = 0 ");
            for (int i = 0; i < mappingArray.size(); i++) {
                JSONObject mappingItem = mappingArray.getJSONObject(i);
                String targetField = mappingItem.getStr("target");
                String sourceField = mappingItem.getStr("source");
                //去除目标值# stringObjectMap.get(targetField)
                String targetValue = String.valueOf(stringObjectMap.get(targetField));
                if (targetValue != null) {
                    targetValue = targetValue.replace("#", "");
                }
                stb.append(" AND ").append(sourceField).append(" = '").append(targetValue).append("'");
            }

            TableConditionQueryDto sourceCondition = new TableConditionQueryDto();
            sourceCondition.setTable(sourceTable);
            sourceCondition.setCondition(stb.toString());
            sourceCondition.setSortField(sourceSort);
            sourceCondition.setSortMode("ASC");
            // 获取源数据
            List<Map<String, Object>> sourceDataList = piCustomizeTableService.getTableByCondition(sourceCondition);
            // 取最新一条 sourceDataList
            if (sourceDataList == null || sourceDataList.isEmpty()) {
                continue;
            }
            Map<String, Object> lastSourceData = sourceDataList.get(sourceDataList.size() - 1);
            List<TableFieldValueDto> fieldList = new ArrayList<>();
            // 计算分组结果Map (key: calcGroup, value: 计算结果对象)
            Map<String, CalcResultDto> calcResultMap = new HashMap<>();
            for (PiSyncConfig syncConfig : configList) {
                // 判断是否包含","  包含源字段有多个值拼接 分别取出合并写入val 如 值1值2
                Object val = lastSourceData.get(syncConfig.getSourceField());

                String strVal = formatValue(val);
                fieldList.add(new TableFieldValueDto(syncConfig.getTargetField(), strVal));

                //配置化计算逻辑：根据calcType和calcGroup进行计算
                String calcType = syncConfig.getCalcType();
                String calcGroup = syncConfig.getCalcGroup();
                String calcResultField = syncConfig.getCalcResultField();

                if (calcType != null && !"NONE".equals(calcType) && calcGroup != null && calcResultField != null) {
                    CalcResultDto calcResult = calcResultMap.computeIfAbsent(calcGroup,
                            k -> new CalcResultDto(calcResultField, calcType));
                    calcResult.addValue(strVal);
                }
            }

            //将计算结果添加到字段列表
            for (CalcResultDto calcResult : calcResultMap.values()) {
                fieldList.add(new TableFieldValueDto(calcResult.getResultField(), calcResult.calculate()));
            }

            //更新目标表数据
            if (!fieldList.isEmpty()) {
                TableSaveDto tableSaveDto = new TableSaveDto();
                tableSaveDto.setTable(tableName);
                tableSaveDto.setFieldList(fieldList);
                List<TableConditionDto> conditionList = new ArrayList<>();
                conditionList.add(new TableConditionDto("id", stringObjectMap.get("id") + ""));
                tableSaveDto.setConditionList(conditionList);

                System.out.println(tableSaveDto.getTable());
                piCustomizeTableService.tableSave(tableSaveDto);
            }
        }
    }

    /**
     * 格式化值，如果为ISO时间格式(如2026-03-29T10:31:21)则转为yyyy-MM-dd HH:mm:ss
     */
    private String formatValue(Object val) {
        if (val == null) {
            return "";
        }
        String str = val.toString();
        // 匹配ISO格式: 2026-03-29T10:31:21
        if (str.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ignored) {
            }
        }
        return str;
    }
}
