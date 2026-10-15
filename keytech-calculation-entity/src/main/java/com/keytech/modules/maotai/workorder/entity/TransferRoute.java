package com.keytech.modules.maotai.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 转酒路径配置:按 车间+工艺+源罐/目标罐/卸车撬 配置可选路径。
 * <p>卸车工艺:撬(源端)+目标罐;装车工艺:源罐+撬(目标端);倒罐/灌装:源罐+目标罐。
 * 工单创建时转酒路径下拉候选即来自本表(无代码兜底,未配置则无候选)。
 *
 * @author workorder
 * @since 2026-09-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mes3_maotai_transfer_route")
@Schema(description = "转酒路径配置")
public class TransferRoute extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField("workshop_code")
    @Schema(description = "车间码(MES3_WORKSHOP 字典 ZZG/XLG/CPG/GWG)")
    private String workshopCode;

    @TableField("process_type")
    @Schema(description = "工艺 卸车工艺/装车工艺/倒罐工艺/灌装工艺")
    private String processType;

    @TableField("source_tank")
    @Schema(description = "源罐(设备台账罐名);卸车工艺为空")
    private String sourceTank;

    @TableField("target_tank")
    @Schema(description = "目标罐(设备台账罐名);装车工艺为空")
    private String targetTank;

    @TableField("skid_code")
    @Schema(description = "卸车撬/鹤位编码(字典 MES3_SKID);仅卸/装车工艺")
    private String skidCode;

    @TableField("route_code")
    @Schema(description = "路径编码,如 Z100FX01;同车间同工艺内唯一")
    private String routeCode;

    @TableField("route_name")
    @Schema(description = "路径名称,如 1#卸车线")
    private String routeName;

    @TableField("enabled")
    @Schema(description = "是否启用 1/0")
    private Integer enabled;

    @TableField("sort_no")
    @Schema(description = "排序(候选下拉展示顺序)")
    private Integer sortNo;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
