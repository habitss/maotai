package com.keytech.modules.kpi.util;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hq
 * @version 1.0.0
 * @ClassName DepartUtils
 * @Description TODO
 * @createTime 2025年04月30日
 */
public class WeekUtils {


    /**
     * 将给定的周列表转换为Map。
     *
     * @param weekList 周列表
     * @return 一个Map，其键是由周的起始日期和结束日期组成的字符串，值为"w_" + 年份 + "_" + 周数
     */
    public static Map<String, String> convertToMap(List<MdmWeek> weekList) {
        Map<String, String> map = new HashMap<>();
        SimpleDateFormat sdf = DateUtils.DATE_FORMAT;
        for (MdmWeek week : weekList) {
            String key = sdf.format(week.getStartDate()) + "|" + sdf.format(week.getEndDate());
            String value = "w_" + week.getYear() + "_" + week.getWeekNumber();
            map.put(key, value);
        }
        return map;
    }


    /**
     * 从输入的字符串中提取年份。
     *
     * @param input 输入的字符串，格式应为"w_年份_周数"，如"w_2025_10"。
     * @return 提取出的年份字符串，如果输入的字符串格式不符合预期或年份不存在，则返回null。
     */
    public static String extractYear(String input) {
        // 使用下划线分割字符串
        String[] parts = input.split("_");
        // 检查分割后的数组长度是否符合预期
        if (parts.length == 3) {
            return parts[1];
        }
        return null;
    }

    /**
     * 从输入的字符串中提取周数。
     *
     * @param input 输入的字符串，格式应为"w_年份_周数"，如"w_2025_10"。
     * @return 提取出的年份字符串，如果输入的字符串格式不符合预期或年份不存在，则返回null。
     */
    public static Integer extractWeekNum(String input) {
        // 使用下划线分割字符串
        String[] parts = input.split("_");
        // 检查分割后的数组长度是否符合预期
        if (parts.length == 3) {
            String weekNum = parts[2];
            if(StrUtil.isNotBlank(weekNum)){
                return Integer.parseInt(weekNum);
            }
            return null;
        }
        return null;
    }

    /**
     * 获取周列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public static List<MdmWeek> getWeekList(String startTime, String endTime, IMdmWeekService mdmWeekService) {
        List<MdmWeek> weekList = mdmWeekService.lambdaQuery()
                .ge(MdmWeek::getStartDate, startTime)
                .le(MdmWeek::getEndDate, endTime)
                .list();
        return weekList;
    }
}
