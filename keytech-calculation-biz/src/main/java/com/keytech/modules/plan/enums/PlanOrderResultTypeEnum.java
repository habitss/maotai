package com.keytech.modules.plan.enums;

/**
 * @Description: 任务条目类型
 * @Author: sjw
 * @Date: 2025/6/6 16:00
 */
public enum PlanOrderResultTypeEnum {
    TYPE_1("1","OK/NOK"),
    TYPE_2("2","时间"),
    TYPE_3("3","输入框"),
    TYPE_5("5","多文字输入框"),
    TYPE_6("6","图片上传"),
    TYPE_8("8","单选下拉"),
    TYPE_9("9","多选下拉");;

    public String code;
    public String name;

    PlanOrderResultTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
