package com.keytech.modules.kpi.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import com.keytech.modules.kpi.vo.KpiReportCellVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 单元格构建
 */
public class BuildCellUtil {

    /**
     * 构建单元格(判断父节是否为空)
     *
     * @param parentTitle 父级标题
     * @param title       标题
     * @param key         键
     * @param cellVoList  单元格列表
     */
    public static void buildCell(String parentTitle, String title, String key, List<KpiReportCellVo> cellVoList) {
        KpiReportCellVo build = new KpiReportCellVo();
        if(StrUtil.isNotBlank(parentTitle)){
            //有父节点
            build = KpiReportCellVo.builder().title(parentTitle).key("").sortIndex("").build();
            List<KpiReportCellVo> chList = new ArrayList<>();
            //定义key
            KpiReportCellVo ch = KpiReportCellVo.builder().title(title).key(key).sortIndex(build.getKey()).build();
            chList.add(ch);
            build.setChildren(chList);
        }else{
            //武父节点
            build = KpiReportCellVo.builder().title(title).key(key).sortIndex(build.getKey()).build();
        }
        cellVoList.add(build);
    }

    /**
     * 构建单元格 父节点包含多个子节点
     *
     * @param parentTitle 父级标题
     * @param childNodes 多个子节的点
     * @param cellVoList  单元格列表
     */
    public static void buildCell(String parentTitle, List<KpiReportCellVo> childNodes, List<KpiReportCellVo> cellVoList) {
        KpiReportCellVo build = new KpiReportCellVo();
        if(StrUtil.isNotBlank(parentTitle)){
            //有父节点
            build = KpiReportCellVo.builder().title(parentTitle).key("").sortIndex("").build();
            List<KpiReportCellVo> chList = new ArrayList<>();
            //子节点
            build.setChildren(childNodes);
        }
        cellVoList.add(build);
    }


    /**
     * 构建单元格
     *
     * @param parentTitle 父级标题
     * @param title       标题
     * @param key         键
     * @param cellVoList  单元格列表
     */
    public static void buildTableCell(String parentTitle, String title, String key, List<KpiReportCellVo> cellVoList) {
        KpiReportCellVo build = KpiReportCellVo.builder().title(parentTitle).key("").sortIndex("").build();
        //ch
        List<KpiReportCellVo> chList = new ArrayList<>();
        //定义key
        KpiReportCellVo ch = KpiReportCellVo.builder().title(title).key(key).sortIndex(build.getKey()).build();
        chList.add(ch);
        build.setChildren(chList);
        cellVoList.add(build);
    }



    /**
     * 构建周的数据
     *
     * @param currentMonth 当前月份
     * @param cellVoList   单元格数据列表
     * @param weekOfDate   日期对应的周信息
     */
    public void buildWeek(DateTime currentMonth, List<KpiReportCellVo> cellVoList, Map<Integer, String> weekOfDate) {

        int beginIndex = DateUtil.weekOfYear(currentMonth);
        //第一周以外
        if (beginIndex != 1 && !currentMonth.dayOfWeekEnum().equals(Week.MONDAY)) {
            beginIndex += 1;
        }
        DateTime endOfMonth = DateUtil.endOfMonth(currentMonth);
        int endIndex = DateUtil.weekOfYear(endOfMonth);

        int weekOfMonth = DateUtil.weekOfMonth(endOfMonth);
        //最后一个月的最后一周算出来都是1
        if (endIndex == 1 && weekOfMonth != 1) {
            endIndex = 53;
        }
        //周数据
        for (int i = beginIndex; i <= endIndex; i++) {
            String weekScope = weekOfDate.get(i);
            List<String> split = StrUtil.split(weekScope, "|");
            String strT = split.get(0);
            String endT = split.get(1);

            String strTimeStr = (strT.substring(strT.indexOf("-") + 1)).replace("-", ".");
            String endTimeStr = (endT.substring(endT.indexOf("-") + 1)).replace("-", ".");
            //定义key
            String title = strTimeStr + "-" + endTimeStr;
            buildCell("第" + i + "周", title, weekScope, cellVoList);
        }
    }

    /**
     * 递归获取KPI报告单元格的子单元格，并将键值对存储在Map中(title做map的key)
     * @param keyIndexMap 存储键值对的Map，键为单元格标题，值为默认值
     * @param reportCellVo KPI报告单元格的列表
     */
    public static void getChildrenRecursion(Map<String, String> keyIndexMap, List<KpiReportCellVo> reportCellVo) {
        for (KpiReportCellVo kpiReportCellVo : reportCellVo) {
            List<KpiReportCellVo> children = kpiReportCellVo.getChildren();
            if (CollUtil.isEmpty(children)) {
                String key = kpiReportCellVo.getTitle();
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

    /**
     * 递归获取KPI报告单元格的子单元格，并将键值对存储在Map中(key做map的key)
     * @param keyIndexMap 存储键值对的Map，键为单元格标题，值为默认值
     * @param reportCellVo KPI报告单元格的列表
     */
    public static void getChildrenKeyRecursion(Map<String, String> keyIndexMap, List<KpiReportCellVo> reportCellVo) {
        for (KpiReportCellVo kpiReportCellVo : reportCellVo) {
            List<KpiReportCellVo> children = kpiReportCellVo.getChildren();
            if (CollUtil.isEmpty(children)) {
                String key = kpiReportCellVo.getKey();
                String defValue = "";
                if (key.contains("_bz_1")) {
                    defValue = "0.000";
                }
                keyIndexMap.put(key, defValue);
            } else {
                getChildrenKeyRecursion(keyIndexMap, children);
            }
        }
    }
}
