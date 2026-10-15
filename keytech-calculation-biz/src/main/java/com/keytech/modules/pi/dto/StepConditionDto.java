package com.keytech.modules.pi.dto;

import lombok.Data;

/**
 * @FileName ConditionCheckerDto
 * @Description 定义条件对象
 * @Author HQ
 * @date 2026-01-12
 **/
@Data
public class StepConditionDto {

    private String paramName;
    private String calculateType;
    private String calculateValue;
}
