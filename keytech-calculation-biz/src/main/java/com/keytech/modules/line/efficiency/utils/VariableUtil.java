package com.keytech.modules.line.efficiency.utils;

/**
 * 线效率公共变量
 * @author aolei
 */
public class VariableUtil {
    /**
     * 配置类型-防清零
     */
    public final static Integer HANDLE_TYPE_FQL = 0;

    /**
     * 配置类型-线效率开机
     */
    public final static Integer HANDLE_TYPE_LINE_EFFICIENCY_STATE_OPEN = 1;

    /**
     * 配置类型-线效率关机
     */
    public final static Integer HANDLE_TYPE_LINE_EFFICIENCY_STATE_CLOSE = 2;

    /**
     * 配置类型-开关机信号
     */
    public final static Integer HANDLE_TYPE_LINE_POWER_SIGNAL = 3;

    /**
     * 配置类型-报警
     */
    public final static Integer HANDLE_TYPE_ALARM = 4;

    /**
     * 配置类型-故障状态
     */
    public final static Integer OPERATING_DURATION_POINT = 6;

    /**
     * 线效状态-0
     */
    public final static String LINE_EFFICIENCY_STATE_ZERO = "0";

    /**
     * 线效状态-1
     */
    public final static String LINE_EFFICIENCY_STATE_ONE = "1";


    /**
     * 设备运行状态-未运行
     */
    public final static String RUNNING_STATE_NOT_RUNNING = "0";

    /**
     * 设备运行状态-运行
     */
    public final static String RUNNING_STATE_RUNNING = "1";

    /**
     * 初始产量
     */
    public final static Double PRODUCTION_INITIAL = 1000.0;
}
