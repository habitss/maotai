package com.keytech.modules.maotai.scada.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 处理工艺报警请求(前端"工艺报警"页"处理报警"弹窗提交)。
 * <p>status 必传(2-处理中/3-已处理),由前端按当前报警状态动态给出选项
 * (未处理→处理中/已处理;处理中→仅已处理)。处理方式(handleWay)只是记录性字段,与状态解耦。
 *
 * @author maotai
 * @since 2026-08-13
 */
@Data
@Schema(description = "处理工艺报警请求")
public class AlarmHandleRequest {

    @Schema(description = "报警事件主键")
    @NotNull(message = "报警id不能为空")
    private Integer id;

    @Schema(description = "处理方式(现场处理/远程复位/转派工单/标记误报)")
    @NotBlank(message = "处理方式不能为空")
    @Size(max = 32, message = "处理方式长度不能超过32")
    private String handleWay;

    @Schema(description = "处理说明(可选)")
    @Size(max = 500, message = "处理说明长度不能超过500")
    private String handleDesc;

    @Schema(description = "处理状态(必传):2-处理中 3-已处理;与处理方式解耦")
    @NotNull(message = "处理状态不能为空")
    @Min(value = 2, message = "处理状态只能为2(处理中)或3(已处理)")
    @Max(value = 3, message = "处理状态只能为2(处理中)或3(已处理)")
    private Integer status;
}
