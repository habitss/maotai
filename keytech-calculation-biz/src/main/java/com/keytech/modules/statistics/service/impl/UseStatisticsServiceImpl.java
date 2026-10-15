package com.keytech.modules.statistics.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.service.IKpiReportActualDayValueService;
import com.keytech.modules.kpi.service.IKpiTypeService;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.statistics.dto.UseStatisticsDto;
import com.keytech.modules.statistics.mapper.UseStatisticsMapper;
import com.keytech.modules.statistics.service.IUseStatisticsService;
import com.keytech.modules.statistics.vo.TableChildrenVo;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description:
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Service
@Slf4j
public class UseStatisticsServiceImpl extends BaseServiceImpl<UseStatisticsMapper, KpiInfo> implements IUseStatisticsService {
    @Autowired
    private UseStatisticsMapper useStatisticsMapper;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private IKpiTypeService kpiTypeService;
    @Autowired
    private IKpiReportActualDayValueService kpiReportActualDayValueService;
    @Autowired
    private IMdmWeekService mdmWeekService;

    @Override
    public List<Map> getStatistics(String checkDateBegin, String checkDateEnd, Boolean week) {
        //获取部门
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        query.in(SysDepart::getOrgType,"3","4");
        query.orderByAsc(SysDepart::getOrgCode);
        List<SysDepart> departList = sysDepartService.list(query);
        //获取KPI类型(第一层)
        List<KpiType> kpiTypes = kpiTypeService.getRootKpiType();

        //行动计划(提出日期/提出部门)[工厂]
        List<UseStatisticsDto> moveList = useStatisticsMapper.move(checkDateBegin, checkDateEnd);
        Map<String, Integer> moveMap = moveList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //分层检查 [工厂]
        List<UseStatisticsDto> layeringList = useStatisticsMapper.layering(checkDateBegin, checkDateEnd);
        Map<String, Integer> layeringMap = layeringList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //有效会议 [工厂]
        //有效会议-工厂
        List<UseStatisticsDto> factoryMeetingList = useStatisticsMapper.factoryMeeting(checkDateBegin, checkDateEnd);
        //有效会议-部门
        List<UseStatisticsDto> deptMeetingList = useStatisticsMapper.deptMeeting(checkDateBegin, checkDateEnd);
        //有效会议-车间
        List<UseStatisticsDto> workshopMeetingList = useStatisticsMapper.workshopMeeting(checkDateBegin, checkDateEnd);
        //合并
        List<UseStatisticsDto> deptList = mergeLists(deptMeetingList,workshopMeetingList);

        factoryMeetingList.addAll(deptList);
//        factoryMeetingList.addAll(workshopMeetingList);
        Map<String, Integer> meetingMap = factoryMeetingList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //异常报告
        List<UseStatisticsDto> abnormalList = useStatisticsMapper.abnormal(checkDateBegin, checkDateEnd);
        Map<String, Integer> abnormalMap = abnormalList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));


        //期间核查 (包装 酿造 技术服务部)  [*****]
        List<UseStatisticsDto> periodList = useStatisticsMapper.period(checkDateBegin, checkDateEnd);
        Map<String, Integer> periodMap = periodList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //COP报表 (包装 酿造 技术服务部) [*****]
        List<UseStatisticsDto> copList = useStatisticsMapper.cop(checkDateBegin, checkDateEnd);
        Map<String, Integer> copMap = copList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //通用岗位记录
        List<UseStatisticsDto> postList = useStatisticsMapper.post(checkDateBegin, checkDateEnd);
        Map<String, Integer> postMap = postList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //岗位检查清单
        List<UseStatisticsDto> postInspectList = useStatisticsMapper.postInspect(checkDateBegin, checkDateEnd);
        Map<String, Integer> postInspectMap = postInspectList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));

        //管理者检查清单
        List<UseStatisticsDto> managerInspectList = useStatisticsMapper.managerInspect(checkDateBegin, checkDateEnd+" 23:59:59");
        Map<String, Integer> managerInspectMap = managerInspectList.stream()
                .collect(Collectors.toMap(item -> item.getDeptCode(),item -> item.getNum()));


        //KPI [工厂]
        //获取年
        String year = checkDateBegin.substring(0, 4);;
        //年月集合
        List<String> yearMonthList = new ArrayList<>();
        //周集合
        List<String> weekList = new ArrayList<>();
        if(week){
            weekList = getWeek(checkDateBegin, checkDateEnd);
        }else {
            yearMonthList =  DateUtils.getMonthsBetween(checkDateBegin, checkDateEnd);
        }

        //获取KPI分类
        List<KpiType> kpiTypeList = kpiTypeService.list();
        //获取层级对照 (子节点-->根节点)
        Map<String, String> kpiRootMap = buildIdToRootIdMap(kpiTypeList);

        //获取KPI实际值
        List<KpiReportActualDayValue> kpiActualList = useStatisticsMapper.kpiActual(year);
        //KPI分类合计
        Map<String, Integer> kpiTypeSumMap = new HashMap<>();
        for (KpiType kpiType : kpiTypes) {
            kpiTypeSumMap.put(kpiType.getId(),0);
        }
        //统计数量 ({部门,{分类1,分类2...}})
        Map<String, Map> kpiMap = new HashMap<>();
        for (KpiReportActualDayValue kpiReportActualDayValue : kpiActualList) {
          String value = kpiReportActualDayValue.getValue();
          String deptCode = kpiReportActualDayValue.getDeptCode();
          String parentId = kpiReportActualDayValue.getPerentId();
          Map<String, Integer> typeMap = kpiMap.getOrDefault(deptCode,new HashMap<>());
          int num = 0;
          JSONObject newJson = JSONUtil.parseObj(value);
          if(week){
              //周
              for (String weekStr : weekList) {
                  if(StrUtil.isNotBlank(newJson.getStr(weekStr))){num++; break;}
              }
          }else{
              //月
              for (String yearMonth : yearMonthList) {
                  if(StrUtil.isNotBlank(newJson.getStr(yearMonth))){num++; break;}
              }
          }
          //部门合计[kpi-合计]
          typeMap.put("kpiAmount", typeMap.getOrDefault("kpiAmount",0)+num);
          //类别数量
          typeMap.put(kpiRootMap.get(parentId), typeMap.getOrDefault(kpiRootMap.get(parentId),0)+num);
          //类别合计[部门-合计]
          kpiTypeSumMap.put(kpiRootMap.get(parentId),kpiTypeSumMap.getOrDefault(kpiRootMap.get(parentId),0)+num);
          kpiMap.put(deptCode,typeMap);
        }
        //初始化KPI类型合计
//        if(kpiActualList != null && kpiActualList.isEmpty()){
//        for (KpiType kpiType : kpiTypes) {
//            kpiTypeSumMap.put(kpiType.getId(),0);
//        }
//        }

        List<Map> list = new ArrayList<>();
        for (SysDepart sysDepart : departList) {
            String deCode = sysDepart.getOrgCode();
            String deName = sysDepart.getDepartName();
            Map<String,Object> map = new HashMap<>();
            map.put("dept",deName);
            map.put("deptCode",deCode);
            map.put("move",0);
            map.put("layering",0);
            map.put("meeting",0);
            map.put("abnormal",deCode.length() == 9?"/": 0);
            map.put("period",deCode.length() == 9?"/": 0);
            map.put("cop",deCode.length() == 9?"/": 0);
            map.put("post",deCode.length() == 9?"/": 0);
            map.put("postInspect",deCode.length() == 9?"/": 0);
            map.put("managerInspect",deCode.length() == 9?"/": 0);
            map.put("kpiAmount",0);

            for (KpiType kpiType : kpiTypes) {
                map.put(kpiType.getId(),0);
            }

            //行动计划
            map.put("move",moveMap.getOrDefault(deCode,0));
            //分层检查
            map.put("layering",layeringMap.getOrDefault(deCode,0));
            //有效会议
            map.put("meeting",meetingMap.getOrDefault(deCode,0));
            //异常报告
            if(deCode.length() != 9){
                map.put("abnormal",abnormalMap.getOrDefault(deCode,0));
            }

            //期间核查
            if(deName.startsWith("包装部") || deName.startsWith("酿造部") || deName.startsWith("技术服务部")){
                map.put("period",periodMap.getOrDefault(deCode,0));
            }else{
                map.put("period","/");
            }
            //COP报表
            if(deName.startsWith("包装部") || deName.startsWith("酿造部") || deName.startsWith("技术服务部")) {
                map.put("cop", copMap.getOrDefault(deCode, 0));
            }else{
                map.put("cop","/");
            }
            //岗位记录
            if(deCode.length() != 9) {
                map.put("post", postMap.getOrDefault(deCode, 0));
            }
            //岗位检查清单
            if(deCode.length() != 9) {
                map.put("postInspect", postInspectMap.getOrDefault(deCode, 0));
            }
            //管理者检查清单
            if(deCode.length() != 9) {
                map.put("managerInspect", managerInspectMap.getOrDefault(deCode, 0));
            }
            Map<String,Object> ktMap = kpiMap.get(deCode);
            if(ktMap != null){
                map.putAll(ktMap);
            }
            list.add(map);
        }
        Map<String,Object> map = new HashMap<>();
        // 计算合计 排查/
        int moveSum = 0;
        int layeringSum = 0;
        int meetingSum = 0;
        int abnormalSum = 0;
        int periodSum = 0;
        int copSum = 0;
        int postSum = 0;
        int postInspectSum = 0;
        int managerInspectSum = 0;
        int kpiAmountSum = 0;

        for (Map<String, Object> mapS : list) {
            Object moveValue = mapS.get("move");
            Object layeringValue = mapS.get("layering");
            Object meetingValue = mapS.get("meeting");

            Object abnormalValue = mapS.get("abnormal");
            Object periodValue = mapS.get("period");
            Object copValue = mapS.get("cop");
            Object postValue = mapS.get("post");
            Object postInspectValue = mapS.get("postInspect");
            Object managerInspectValue = mapS.get("managerInspect");
            Object kpiAmountValue = mapS.get("kpiAmount");


            if (moveValue != null && !"/".equals(moveValue)) {
                moveSum += (Integer) moveValue;
            }
            if (layeringValue != null && !"/".equals(layeringValue)) {
                layeringSum += (Integer) layeringValue;
            }
            if (meetingValue != null && !"/".equals(meetingValue)) {
                meetingSum += (Integer) meetingValue;
            }
            if (abnormalValue != null && !"/".equals(abnormalValue)) {
                abnormalSum += (Integer) abnormalValue;
            }
            if (periodValue != null && !"/".equals(periodValue)) {
                periodSum += (Integer) periodValue;
            }
            if (copValue != null && !"/".equals(copValue)) {
                copSum += (Integer) copValue;
            }
            if (postValue != null && !"/".equals(postValue)) {
                postSum += (Integer) postValue;
            }
            if (postInspectValue != null && !"/".equals(postInspectValue)) {
                postInspectSum += (Integer) postInspectValue;
            }
            if (managerInspectValue != null && !"/".equals(managerInspectValue)) {
                managerInspectSum += (Integer) managerInspectValue;
            }
            if (kpiAmountValue != null && !"/".equals(kpiAmountValue)) {
                kpiAmountSum += (Integer) kpiAmountValue;
            }
        }
        // 创建总和行
        Map<String, Object> sumMap = new HashMap<>();
        sumMap.put("dept", "合计");
        sumMap.put("move", moveSum);
        sumMap.put("layering", layeringSum);
        sumMap.put("meeting", meetingSum);
        sumMap.put("abnormal", abnormalSum);
        sumMap.put("period", periodSum);
        sumMap.put("cop", copSum);
        sumMap.put("post", postSum);
        sumMap.put("postInspect", postInspectSum);
        sumMap.put("managerInspect", managerInspectSum);
        sumMap.put("kpiAmount", kpiAmountSum);
        sumMap.putAll(kpiTypeSumMap);
        map.put("dept","合计");
        list.add(sumMap);
        return list;
    }

    /**
     * 数据合并
     */
    public static List<UseStatisticsDto> mergeLists(
            List<UseStatisticsDto> deptMeetingList,
            List<UseStatisticsDto> workshopMeetingList) {

        // 合并两个列表并分组求和
        Map<String, Integer> mergedMap = Stream.concat(
            deptMeetingList.stream(),
            workshopMeetingList.stream()
        ).collect(Collectors.groupingBy(
            UseStatisticsDto::getDeptCode,
            Collectors.summingInt(UseStatisticsDto::getNum)
        ));

        // 转换回UseStatisticsDto列表
        return mergedMap.entrySet().stream()
                .map(entry -> new UseStatisticsDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 表头(kpi分类)
     * @return
     */
    @Override
    public List<TableChildrenVo> getHead() {
        //获取KPI类型(第一层)
        List<KpiType> kpiTypes = kpiTypeService.getRootKpiType();

        List<TableChildrenVo> list = new ArrayList<>();
        for (KpiType kpiType : kpiTypes) {
            TableChildrenVo tableChildrenVo = new TableChildrenVo();
            tableChildrenVo.setTitle(kpiType.getKpiType());
            tableChildrenVo.setAlign("center");
            tableChildrenVo.setDataIndex(kpiType.getId());
            tableChildrenVo.setEllipsis(false);
            tableChildrenVo.setWidth(tableWidth(kpiType.getKpiType()));
            list.add(tableChildrenVo);
        }
        return list;
    }
    /**
     * 通过内容计算表头宽度
     */
    public static String tableWidth(String title){
        String str = "50px";
        if(StrUtil.isNotBlank(title)){
            str = title.length()*25+"px";
        }
        return str;
    }


    /**
     * KPI分类找根节点
     * @param list
     * @return
     */
    public static Map<String, String> buildIdToRootIdMap(List<KpiType> list) {
        Map<String, String> idToRootId = new HashMap<>();
        Map<String, KpiType> idToNode = list.stream()
                .collect(Collectors.toMap(a -> a.getId(), a -> a));

        for (KpiType node : list) {
            String rootId = findRootId(node, idToNode);
            idToRootId.put(node.getId(), rootId);
        }
        return idToRootId;
    }

    /**
     * 查找给定节点的根节点ID
     *
     * @param node  当前节点
     * @param idToNode  ID到节点的映射关系
     * @return 根节点的ID
     */
    private static String findRootId(KpiType node, Map<String, KpiType> idToNode) {
        if(node == null){
            return null;
        }
        if (node.getParentId() == null || StrUtil.isBlank(node.getParentId())) {
            return node.getId(); // 自己就是根节点
        }
        System.out.println(node.getId()+"=====================");
        return findRootId(idToNode.get(node.getParentId()), idToNode); // 递归查找
    }

    /**
     * 获取周集合
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    private List<String> getWeek(String checkDateBegin, String checkDateEnd){
        LambdaQueryWrapper<MdmWeek> query = new LambdaQueryWrapper<MdmWeek>();
        List<String> list = new ArrayList<>();
        try {
            query.ge(MdmWeek::getStartDate, DateUtils.DATE_FORMAT.parse(checkDateBegin));
            query.le(MdmWeek::getEndDate, DateUtils.DATE_FORMAT.parse(checkDateEnd));
            query.orderByAsc(MdmWeek::getWeekNumber);
            List<MdmWeek> weekList = mdmWeekService.list(query);
            for (MdmWeek week : weekList) {
                list.add("w_"+week.getYear()+"_"+week.getWeekNumber());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}
