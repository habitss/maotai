package com.keytech.modules.ems.constants;

import java.math.BigDecimal;

/**
 * EMS通用常量
 * @author yepeng
 * @since 2025.05.05
 */
public class EmsConstantsX {

    private EmsConstantsX() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 采集程序采用了参照值的算法，对于等于 0 的底数不会进行正常采集计算，但是有些异常点位确实底数为 0
     * 为了节约定时任务的开支，将这类点给一个大于 0 的最小值 0.000001 强制让他达到计算条件，得到用量为 0
     */
    public static final BigDecimal ZERO_BASE = BigDecimal.valueOf(0.000001);

    /**
     * 查询Influxdb数据时按此数量分批查询
     */
    public static final int BATCH_TAG_NUM = 10;

    /**
     * 并发采集每个批次的点位数量，根据实际情况动态变化 20 - 5 - 1
     * 因为单个点超时会影响同批次所有点，所以需要自动调整到 1 或 5
     */
    public static final String J_BATCH_TAG_NUM = "BATCH_TAG_NUM";

    /**
     * 是否开启调试模式，1 开启，0 关闭 一旦开启正常采集将暂停，只会读取设置的点位进行采集与计算
     */
    public static final String J_CALCULATE_DEBUG = "CALCULATE_DEBUG";
    /**
     * 调试模式下，采集的点位列表，多个点位用逗号分隔
     */
    public static final String J_CALC_DEBUG_C_TAGS = "CALC_DEBUG_C_TAGS";
    /**
     * 调试模式下，中间点位列表，多个点位用逗号分隔
     */
    public static final String J_CALC_DEBUG_O_TAGS = "CALC_DEBUG_O_TAGS";

    public static final String SYSTEM = "system";
    public static final String TAG_ID = "tag_id";
    public static final String UTIME = "utime";
    public static final String COLLECT_TIME = "collect_time";
    public static final String FACTORY_CODE = "factory_code";
    public static final String SUM_USAGE = " SUM(fusage) AS fusage ";

}
