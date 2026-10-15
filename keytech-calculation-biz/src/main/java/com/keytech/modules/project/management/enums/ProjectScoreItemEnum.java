package com.keytech.modules.project.management.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum ProjectScoreItemEnum {
    //数组中依次为，分类对象名称，计划分字段，加分项字段
    PROJECT_APPROVAL_KEY("PROJECT_APPROVAL_KEY", new String[]{"立项", "planApprovalScore", "addApprovalScore"}),
    MAIN_TARGET_KEY("MAIN_TARGET_KEY", new String[]{"主目标", "planTargetScore", "addTargetScore"}),
    ACTION_OBJECTIVES_KEY("ACTION_OBJECTIVES_KEY", new String[]{"举措目标", "planActScore", "addActScore"}),
    CLOSING_ITEM_KEY("CLOSING_ITEM_KEY", new String[]{"结项", "planEndScore", "addEndScore"}),
    TOTAL_KEY("TOTAL_KEY", new String[]{"合计", "planTotalScore", "addTotalScore"});

    private String key;

    private String[] values;

    public static String[] getValuesByKey(String key) {
        for (ProjectScoreItemEnum item : ProjectScoreItemEnum.values()) {
            if (item.getKey().equals(key)) {
                return item.getValues();
            }
        }
        return null;
    }
}