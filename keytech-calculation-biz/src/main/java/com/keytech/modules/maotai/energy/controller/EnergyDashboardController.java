package com.keytech.modules.maotai.energy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 能源看板(Phase 1:假数据,数据表/结构待定后替换)
 * @author maotai
 * @since 2026-06-15
 */
@Tag(name = "能源看板")
@RestController
@RequestMapping("/maotai/energyDashboard")
public class EnergyDashboardController {

    private static final String[] WORKSHOPS = {"中转酒罐车间", "系列酒勾调厂房", "成品酒罐房和包装厂房"};
    private static final String[] WS_COLORS = {"#6366F1", "#0EA5E9", "#F59E0B"};
    /** 三车间产能系数(中转>系列>成品) */
    private static final double[] WS_FACTOR = {1.0, 0.72, 0.55};

    /**
     * 看板数据(假数据,按周期 period 返回)
     * @param period today/week/month/quarter/year
     */
    @Operation(summary = "能源看板-聚合数据(假数据)", description = "能源看板-聚合数据(假数据)")
    @GetMapping("/data")
    public Result<Map<String, Object>> data(@RequestParam(name = "period", defaultValue = "month") String period) {
        Map<String, Object> result = new HashMap<>();

        int points = pointsOf(period);
        List<String> labels = labelsOf(period);
        String desc = descOf(period);

        // 趋势数据:3 能源 × 3 车间
        Map<String, Object> electric = buildSeries("electric", "电耗", "kWh", 90, points);
        Map<String, Object> water = buildSeries("water", "水耗", "m³", 4.2, points);
        Map<String, Object> air = buildSeries("air", "压缩空气", "Nm³", 13, points);
        Map<String, Object> series = new HashMap<>();
        series.put("electric", electric);
        series.put("water", water);
        series.put("air", air);

        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", labels);
        trend.put("series", series);

        // 统计卡片:电/水/气 总量(从 series 求和)+ 能效(固定)
        double totalElec = sumAll(electric);
        double totalWater = sumAll(water);
        double totalAir = sumAll(air);
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(stat("electric", "总电耗", "kWh", "⚡", "#F59E0B", round(totalElec), desc));
        stats.add(stat("water", "总水耗", "m³", "💧", "#0EA5E9", round(totalWater), desc));
        stats.add(stat("air", "压缩空气消耗", "Nm³", "💨", "#14B8A6", round(totalAir), desc));
        stats.add(stat("eff", "综合能效", "%", "📊", "#10B981", 87.6, "达标"));

        // 车间对比:每车间 3 能源 total + 占比(相对该能源最大车间)
        List<Map<String, Object>> workshops = buildWorkshops(electric, water, air);

        // 异常提醒(固定假数据)
        List<Map<String, Object>> alarms = buildAlarms();

        result.put("stats", stats);
        result.put("trend", trend);
        result.put("workshops", workshops);
        result.put("alarms", alarms);
        return Result.OK(result);
    }

    // ===== 私有构造方法 =====

    private int pointsOf(String period) {
        switch (period == null ? "" : period) {
            case "today": return 24;
            case "week": return 7;
            case "quarter": return 3;
            case "year": return 12;
            default: return 30;
        }
    }

    private List<String> labelsOf(String period) {
        switch (period == null ? "" : period) {
            case "today":
                List<String> h = new ArrayList<>();
                for (int i = 0; i < 24; i++) h.add(String.format("%02d:00", i));
                return h;
            case "week":
                return Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
            case "quarter":
                return Arrays.asList("4月", "5月", "6月");
            case "year":
                List<String> m = new ArrayList<>();
                for (int i = 1; i <= 12; i++) m.add(i + "月");
                return m;
            default:
                List<String> d = new ArrayList<>();
                for (int i = 1; i <= 30; i++) d.add(i + "日");
                return d;
        }
    }

    private String descOf(String period) {
        switch (period == null ? "" : period) {
            case "today": return "今日累计";
            case "week": return "本周累计";
            case "quarter": return "本季度累计";
            case "year": return "本年累计";
            default: return "本月累计";
        }
    }

    /** 构造某能源类型的趋势 series:label/unit/workshops[{name,color,data}] */
    private Map<String, Object> buildSeries(String type, String label, String unit, double baseVal, int points) {
        // 周期缩放(月/年的单点量级更大)
        double scale;
        if (points == 24) scale = 1;
        else if (points == 7) scale = 22;
        else if (points == 3) scale = 620;
        else if (points == 12) scale = 230;
        else scale = 21;

        List<Map<String, Object>> workshops = new ArrayList<>();
        for (int w = 0; w < 3; w++) {
            List<Number> data = new ArrayList<>();
            for (int i = 0; i < points; i++) {
                // 波形:中间高、两端低(模拟生产时段高峰)
                double wave = Math.sin((i + 1) * Math.PI / (points + 1));
                if (wave < 0.15) wave = 0.15;
                double val = baseVal * WS_FACTOR[w] * scale * wave;
                data.add(round(val));
            }
            Map<String, Object> ws = new HashMap<>();
            ws.put("name", WORKSHOPS[w]);
            ws.put("color", WS_COLORS[w]);
            ws.put("data", data);
            workshops.add(ws);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("unit", unit);
        m.put("workshops", workshops);
        return m;
    }

    private double sumAll(Map<String, Object> series) {
        double sum = 0;
        List<Map<String, Object>> ws = (List<Map<String, Object>>) series.get("workshops");
        for (Map<String, Object> w : ws) {
            List<Number> data = (List<Number>) w.get("data");
            for (Number n : data) sum += n.doubleValue();
        }
        return sum;
    }

    private Map<String, Object> stat(String key, String label, String unit, String icon, String color, double value, String desc) {
        Map<String, Object> m = new HashMap<>();
        m.put("key", key);
        m.put("label", label);
        m.put("unit", unit);
        m.put("icon", icon);
        m.put("color", color);
        m.put("value", value);
        m.put("desc", desc);
        return m;
    }

    /** 3 车间对比:每车间 3 能源 total + 占比(相对该能源全车间的最大值) */
    private List<Map<String, Object>> buildWorkshops(Map<String, Object> elec, Map<String, Object> water, Map<String, Object> air) {
        double[] maxByType = new double[3];
        double[][] totals = new double[3][3]; // [车间][能源]
        for (int t = 0; t < 3; t++) {
            Map<String, Object> series = t == 0 ? elec : (t == 1 ? water : air);
            String unit = t == 0 ? "kWh" : (t == 1 ? "m³" : "Nm³");
            List<Map<String, Object>> ws = (List<Map<String, Object>>) series.get("workshops");
            for (int w = 0; w < 3; w++) {
                double total = 0;
                for (Number n : (List<Number>) ws.get(w).get("data")) total += n.doubleValue();
                totals[w][t] = round(total);
                if (total > maxByType[t]) maxByType[t] = total;
            }
        }
        String[] units = {"kWh", "m³", "Nm³"};
        String[] keys = {"electric", "water", "air"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (int w = 0; w < 3; w++) {
            Map<String, Object> card = new HashMap<>();
            card.put("name", WORKSHOPS[w]);
            card.put("color", WS_COLORS[w]);
            for (int t = 0; t < 3; t++) {
                Map<String, Object> e = new HashMap<>();
                e.put("total", totals[w][t]);
                e.put("unit", units[t]);
                e.put("pct", maxByType[t] > 0 ? Math.round(totals[w][t] / maxByType[t] * 100) : 0);
                card.put(keys[t], e);
            }
            list.add(card);
        }
        return list;
    }

    private List<Map<String, Object>> buildAlarms() {
        Object[][] rows = {
                {"2026-05-21 09:15", "中转酒罐车间", "electric", "⚡", "电耗", "瞬时功率超过额定值 120%", "critical", "紧急", "alarm", "未处理"},
                {"2026-05-20 14:30", "成品酒罐房和包装厂房", "water", "💧", "水耗", "小时水耗超出均值 30%", "warning", "警告", "online", "已处理"},
                {"2026-05-19 22:00", "系列酒勾调厂房", "air", "💨", "压缩空气", "管道压力下降速率异常,疑似泄漏", "warning", "警告", "online", "已处理"},
                {"2026-05-18 08:45", "中转酒罐车间", "air", "💨", "压缩空气", "空压机出口温度偏高 92°C(限值 85°C)", "info", "提示", "online", "已处理"},
                {"2026-05-17 16:20", "系列酒勾调厂房", "electric", "⚡", "电耗", "非生产时段电耗异常偏高", "info", "提示", "online", "已处理"},
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("time", r[0]);
            m.put("workshop", r[1]);
            m.put("energyKey", r[2]);
            m.put("energyIcon", r[3]);
            m.put("energyLabel", r[4]);
            m.put("detail", r[5]);
            m.put("levelKey", r[6]);
            m.put("levelLabel", r[7]);
            m.put("statusKey", r[8]);
            m.put("statusLabel", r[9]);
            list.add(m);
        }
        return list;
    }

    private double round(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
