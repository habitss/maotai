package com.keytech.modules.plan.enums;

/**
 * @Description: 计划状态枚举
 * @Author: sjw
 * @Date: 2025/6/6 16:00
 */
public enum PlanStatusEnum {
    TODO(1, "未开始"),
    DOING(2, "进行中"),
    FINISHED(3, "已结束"),
    CANCEL(4,"已取消"),
    PAUSE(5,"暂停"),
    RENEW(6,"恢复"),
    DELAY(7,"延迟关闭"),
    TASK_CYCLE(0,"循环任务"),
    TASK_PRODUCTION(3,"生产任务"),;

    public int code;
    public String name;

    PlanStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
