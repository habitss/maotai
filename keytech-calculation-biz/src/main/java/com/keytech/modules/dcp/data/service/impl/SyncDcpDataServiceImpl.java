package com.keytech.modules.dcp.data.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.DbType;
import com.keytech.modules.common.DataBaseTypeUtil;
import com.keytech.modules.dcp.data.service.IDcpDataService;
import com.keytech.modules.dcp.data.service.ISyncDcpDataService;
import com.keytech.modules.df.mdm.entity.DfMdmDeptMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步DCP视图数据 业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
@DS("ccrDg")
public class SyncDcpDataServiceImpl implements ISyncDcpDataService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IDcpDataService dcpDataService;

    //产线，通过thingid获取
    static final Map<String, String> LINE_MAP;
    static {
        LINE_MAP = Map.ofEntries(
                Map.entry("CN54225001-0301", "BL01"),
                Map.entry("CN54225001-0302", "BL02"),
                Map.entry("CN54225001-0303", "BL03"),
                Map.entry("CN54225001-0304", "BL04"),
                Map.entry("CN54225001-0305", "BL05"),
                Map.entry("CN54225001-0306", "BL06"),
                Map.entry("CN54225001-0307", "CL01"),
                Map.entry("CN54225001-0308", "CL02"),
                Map.entry("CN54225001-0309", "CL03"),
                Map.entry("CN54225001-0310", "KL01"),
                Map.entry("CN54225001-0311", "KL02"));
    }
    //配方类型，通过recipeid获取
    static final Map<String, String> RECIPEID_MAP;
    static {
        RECIPEID_MAP = Map.ofEntries(
                Map.entry("1", "Budweiser"),
                Map.entry("2", "WBIB"),
                Map.entry("3", "HBJZ"),
                Map.entry("4", "WHCR"),
                Map.entry("5", "WHBZ"),
                Map.entry("6", "EHR"),
                Map.entry("7", "Bud Magnum"),
                Map.entry("8", "BGD"),
                Map.entry("9", "MHHB 8%"),
                Map.entry("10", "HabinWheatKing"),
                Map.entry("11", "BGD MCS"),
                Map.entry("12", "HBJS 10%"),
                Map.entry("13", "HSB"),
                Map.entry("14", "Bud Lime"),
                Map.entry("15", "BingShuang"),
                Map.entry("16", "WH8B"),
                Map.entry("17", "WHB5"),
                Map.entry("18", "HBQS"),
                Map.entry("19", "Ultra 6.4"),
                Map.entry("20", "WHSZ"),
                Map.entry("21", "BUD MCS"),
                Map.entry("22", "WL8P"),
                Map.entry("23", "Ultra 7.6"),
                Map.entry("24", "WheatBeer"),
                Map.entry("25", "Hoegaarden"),
                Map.entry("26", "WHFR"),
                Map.entry("27", "WHPC"),
                Map.entry("28", "GI IPA"),
                Map.entry("29", "xiaoboluo"),
                Map.entry("30", "RROR"),
                Map.entry("31", "Eden"),
                Map.entry("32", "Moito"),
                Map.entry("33", "Blue Fantasy"),
                Map.entry("34", "Maitai"),
                Map.entry("35", "Long Island Ice"),
                Map.entry("36", "Love Margarita")
            );
    }
    //需要触发的时间点 // 定义初始List
    private static final List<Integer> TRIGGER_HOUR_LIST = new ArrayList<>(Arrays.asList(7, 16, 23));


    @DS("ccrDg")
    public List<Map<String, Object>> selectDcpList(String startDate, String endDate) {
        String sql = "select * from ccr_dg.cn54_wuh_common " +
                " where event_timestamp >= '"+startDate+"' " +
                " and event_timestamp < '"+endDate+"' "+
                " and end_time is not null " +
                " order by event_timestamp ";
        return  jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取DWS_PI_NON_BATCH_RESULT数据
     * @param code 工厂、部门、工序、产线拼接的code
     * @param num 罐号
     * @param mgTime 满罐时间
     * @return 结果
     */
    @DS("ccrDg")
    public List<Map<String, Object>> selectDwsPiNonBatchResult(String code, String num, String mgTime) {
        String sql = "SELECT * " +
                "FROM ( " +
                "    SELECT *, " +
                "           ROW_NUMBER() OVER (PARTITION BY ind_stand_code ORDER BY mg_time DESC) AS rn " +
                "    FROM ccr_dg.dws_pi_non_batch_result " +
                "       where mg_time <= '"+mgTime+"' " +
                "       and equip_num = '"+num+"' " +
                "       and funloc_code like '"+code+"-%' " +
                ") t " +
                "WHERE rn = 1";
        return  jdbcTemplate.queryForList(sql);
    }

    /**
     * 同步DCP视图数据到本地日志表
     * @param tenantId 租户ID
     */
    @Override
    public void syncDcpData(Integer tenantId, String sDate, String eDate) {
        if(StrUtil.isBlank(sDate)){
            sDate = DateUtil.format(DateUtil.offsetHour(new Date(), -1), "yyyy-MM-dd HH:mm:ss");
        }
        if(StrUtil.isBlank(eDate)){
            eDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
        log.info("-------DCP数据抽取开始：{}至{} ", sDate, eDate);
        List<Map<String, Object>> dcpList = this.selectDcpList(sDate, eDate);
        if (CollectionUtils.isEmpty(dcpList)) {
            log.info("-------本次获取 {} DCP数据为空", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            return;
        }
        //需要新增的数据
        List<Map<String, Object>> insertList = new ArrayList<>();
        //需要更新的数据
        List<Map<String, Object>> updateList = new ArrayList<>();

        //将抽取的DCP数据转换为自己系统需要的数据
        for (Map<String, Object> data : dcpList) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("tenantId", tenantId);
            newMap.put("field_0014", data.get("start_vol"));
            newMap.put("field_0015", data.get("end_vol"));
            newMap.put("field_0003", data.get("bbt_mt_no"));
            newMap.put("field_0004", data.get("bbt_mt2_no"));
            newMap.put("field_0005", data.get("start_bbt_mt_ll"));
            newMap.put("field_0006", data.get("end_bbt_mt_ll"));
            newMap.put("field_0011", null != data.get("start_time") ? DateUtil.date(MapUtil.getLong(data, "start_time")) : null);
            newMap.put("field_0012", null != data.get("end_time") ? DateUtil.date(MapUtil.getLong(data, "end_time")) : null);
            newMap.put("event_timestamp", data.get("event_timestamp"));
            newMap.put("field_0016", NumberUtil.sub(MapUtil.getStr(data, "end_vol"), MapUtil.getStr(data, "start_vol")));
            newMap.put("field_0017", NumberUtil.sub(MapUtil.getStr(data, "start_bbt_mt_ll"), MapUtil.getStr(data, "end_bbt_mt_ll")));
            newMap.put("field_0021", data.get("start_time"));
            newMap.put("field_0022", data.get("picode"));
            newMap.put("field_0023", data.get("piname"));
            newMap.put("field_0024", data.get("pitype"));
            newMap.put("field_0018", data.get("productstatus"));

            //获取包装线
            String thingid = MapUtil.getStr(data, "thingid");
            if(StrUtil.isNotBlank(thingid)){
                newMap.put("field_0020", thingid);
                newMap.put("field_0001", LINE_MAP.get(thingid.replace("APAC-", "")));
            }
            //获取品牌
            String recipeid = MapUtil.getStr(data, "recipeid");
            if(StrUtil.isNotBlank(recipeid)){
                newMap.put("field_0002", RECIPEID_MAP.get(recipeid));
                newMap.put("field_0025", recipeid);
            }

            //获取开始时间、小时、秒
            Date dateTime = (Date) newMap.get("field_0012");
            int hour = DateUtil.hour(dateTime, true);
            int minute = DateUtil.minute(dateTime);
            int seconds = DateUtil.second(dateTime);
            String flagPointId = null;
            String flagPoint = null;
            if(minute == 0 && seconds < 10){
                //整点且在15秒以内添加记录点标记为开始时间的整点
                if(TRIGGER_HOUR_LIST.contains(hour)){
                    flagPoint = String.format("%02d", hour)+":00";
                    flagPointId = DateUtil.format(dateTime, "yyyyMMddHHmmss");
                }
            } else {
                //如果bbt液位止大于等于20为余酒小于为空罐
                int end_bbt_mt_ll = MapUtil.getInt(newMap, "field_0006");
                flagPoint = end_bbt_mt_ll >= 20 ? "余酒" : "空罐";
                flagPointId = DateUtil.format(dateTime, "yyyyMMddHHmmss")+flagPoint;
            }
            newMap.put("field_0013", flagPoint);
            newMap.put("field_0026", flagPointId);

            //查询数据库是否已经存在该数据
            List<Map<String, Object>> queryDataList = dcpDataService.queryData(
                    newMap.get("field_0020").toString(),
                    newMap.get("field_0025").toString(),
                    newMap.get("field_0003").toString(),
                    null
            );
            if(CollectionUtils.isEmpty(queryDataList) && null != flagPointId){
                //因为每次查询两小时前的数据，可能会出现多查询数据的情况，所以这里需要带上flagPointId再查一遍
                queryDataList = dcpDataService.queryData(
                        newMap.get("field_0020").toString(),
                        newMap.get("field_0025").toString(),
                        newMap.get("field_0003").toString(),
                        flagPointId
                );
            }

            //如果有数据则修改否则新增
            if(CollectionUtils.isNotEmpty(queryDataList)){
                //旧数据
                Map<String, Object> oldData = queryDataList.get(0);
                //处理数据，只更改停止、结束字段的的数据
                oldData.put("field_0013", flagPoint);
                oldData.put("field_0026", flagPointId);
                oldData.put("field_0015", newMap.get("field_0015"));
                oldData.put("field_0006", newMap.get("field_0006"));
                oldData.put("field_0012", null != newMap.get("field_0012") ? DateUtil.date(MapUtil.getLong(newMap, "field_0012")) : null);
                oldData.put("event_timestamp", newMap.get("event_timestamp"));
                oldData.put("field_0016", NumberUtil.sub(MapUtil.getStr(newMap, "field_0015"), MapUtil.getStr(oldData, "field_0014")));
                oldData.put("field_0017", NumberUtil.sub(MapUtil.getStr(oldData, "field_0005"), MapUtil.getStr(newMap, "field_0006")));
                //送酒酒损
                oldData.put("field_0019", sjjs(oldData.get("field_0007"), oldData.get("field_0016"), oldData.get("field_0017")));
                updateList.add(oldData);
                dcpDataService.batchUpdateData(updateList);
                updateList.clear();
            } else {
                //拆分获取工厂、部门、工序、产线拼接的code
                String code = newMap.get("field_0020").toString();
                code = code.substring(code.indexOf("-")+1, code.lastIndexOf("-"));
                //罐号
                String num = String.format("%02d", MapUtil.getInt(newMap, "field_0003"));
                //获取非批次数据
                List<Map<String, Object>> nonBatchList = this.selectDwsPiNonBatchResult(
                        code,
                        num,
                        DateUtil.format((Date) newMap.get("field_0011"), "yyyy-MM-dd HH:mm:ss")
                );
                for(Map<String, Object> nonBatchData : nonBatchList){
                    if(null != nonBatchData.get("pi_value") && null != nonBatchData.get("ind_stand_code")){
                        String indStandCode = nonBatchData.get("ind_stand_code").toString();
                        BigDecimal value = (BigDecimal) nonBatchData.get("pi_value");
                        if("QA-05305".equals(indStandCode)){
                            //DO
                            newMap.put("field_0009", value);
                        } else if("QA-05307".equals(indStandCode)){
                            //CO2
                            newMap.put("field_0008", value);
                        } else if("QA-05318".equals(indStandCode)){
                            //OG
                            newMap.put("field_0007", value);
                            //送酒酒损
                            newMap.put("field_0019", sjjs(value, newMap.get("field_0016"), newMap.get("field_0017")));
                        }
                    }
                }
                //处理数据，只更改停止、结束字段的的数据
                insertList.add(newMap);
                dcpDataService.batchInsertData(insertList);
                insertList.clear();
            }
        }
        if(CollectionUtils.isNotEmpty(insertList)){
            dcpDataService.batchInsertData(insertList);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            dcpDataService.batchUpdateData(updateList);
        }
    }

    @Override
    public List<Map<String, Object>> queryDcpData(String tableName, String timeField,
                                                  String factoryField, String factoryCodes,
                                                  String startDate, String endDate,
                                                  String indicatorField, String indicator) {
        //取数表和查询时间字段不能为空
        if(StrUtil.isBlank(tableName) || StrUtil.isBlank(timeField) || StrUtil.isBlank(startDate) || StrUtil.isBlank(endDate)){
            return null;
        }
        tableName = addSymbol(tableName);
        timeField = addSymbol(timeField);
        //拼接sql
        String sql = "select * from " + tableName +
                " where "+timeField+" >= '"+startDate+"' " +
                " and "+timeField+" < '"+endDate+"' ";

        if(StrUtil.isNotBlank(factoryCodes)){
            factoryField = addSymbol(factoryField);
            sql += " and "+factoryField +" in ("+factoryCodes+") ";
        }

        if(StrUtil.isNotBlank(indicatorField)){
            indicatorField = addSymbol(indicatorField);
            sql += " and "+indicatorField +" in ("+indicator+") ";
        }
        sql += " order by "+timeField;
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 送酒酒损
     * @param og og
     * @param field16 累计送酒量（酿造流量计）
     * @param field17 累计送酒量（BBT液位）
     * @return
     */
    private String  sjjs(Object og, Object field16, Object field17){
        // s1: field_0016 * og * (1.6332 - SQRT(2.6634 - 0.004911 * (460.234 + og)))
        // s2: field_0017 * og * (1.6332 - SQRT(2.6634 - 0.004911 * (460.234 + og)))
        if (og == null || field16 == null || field17 == null) {
            return null;
        }

        try {
            double ogValue = Double.parseDouble(og.toString());
            double field16Value = Double.parseDouble(field16.toString());
            double field17Value = Double.parseDouble(field17.toString());

            // 计算公共部分
            double commonPart = 1.6332 - Math.sqrt(2.6634 - 0.004911 * (460.234 + ogValue));

            // S1: field_0016 * og * (1.6332 - SQRT(2.6634 - 0.004911 * (460.234 + og)))
            double s1 = field16Value * ogValue * commonPart;

            // S2: field_0017 * og * (1.6332 - SQRT(2.6634 - 0.004911 * (460.234 + og)))
            double s2 = field17Value * ogValue * commonPart;

            // 如果s2为0，避免除零错误
            if (Math.abs(s2) < 0.000001) {
                return null; // 或者返回一个特殊值表示无法计算
            }

            return String.valueOf((s1 / s2) * 100); // 根据业务逻辑，可能是差值百分比
        } catch (NumberFormatException e) {
            log.error("数值转换错误", e);
            return null;
        }
    }

    private String addSymbol(String str){
        String strSymbol = "";
        if (DataBaseTypeUtil.getDbType() == DbType.MYSQL) {
            strSymbol = "`";
        } else if (DataBaseTypeUtil.getDbType() == DbType.POSTGRE_SQL) {
            strSymbol = "\"";
        }
        return strSymbol + str + strSymbol;
    }
}
