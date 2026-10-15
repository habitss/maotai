package com.keytech.modules.alarm.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 报警点位
 * @author liu
 * @since 2026.01.12
 * */
@Data
@TableName("alarm_point")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="报警点位")
public class AlarmPoint implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
    /**工厂Code*/
    @Excel(name = "工厂", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;
    /**部门Code*/
    @Excel(name = "部门", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;
    /**产线Code*/
    @Excel(name = "产线", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;
    /**设备ID*/
    @Excel(name = "设备", width = 15)
    @Schema(description = "设备")
    private Integer deviceId;
    /**设备编码*/
    @Excel(name = "设备编码", width = 15)
    @Schema(description = "设备编码")
    private String deviceCode;
	/**PI编码*/
	@Excel(name = "PI编码", width = 15)
    @Schema(description = "PI编码")
    private String alarmPiCode;
	/**报警名称*/
	@Excel(name = "报警名称", width = 15)
    @Schema(description = "报警名称")
    private String alarmName;
	/**报警大类*/
	@Excel(name = "报警大类", width = 15)
    @Schema(description = "报警大类")
    private String alarmCategory;
	/**报警小类*/
	@Excel(name = "报警小类", width = 15)
    @Schema(description = "报警小类")
    private String alarmSubCategory;
	/**默认报警等级*/
	@Excel(name = "默认报警等级", width = 15)
    @Schema(description = "默认报警等级")
    private Integer alarmLevelDefault;
	/**功能位置编码*/
	@Excel(name = "功能位置编码", width = 15)
    @Schema(description = "功能位置编码")
    private String funcLocCode;
	/**所属系统*/
	@Excel(name = "所属系统", width = 15)
    @Schema(description = "所属系统")
    private String sourceSystem;
	/**是否启用*/
	@Excel(name = "是否启用", width = 15)
    @Schema(description = "是否启用")
    private Integer enableFlag;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @Schema(description = "排序号")
    private Integer sort;
	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
	/**删除标记*/
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;
    /**是否推TSM(0-否 1-是)*/
    @Excel(name = "是否推TSM", width = 15)
    @Schema(description = "是否推TSM(0-否 1-是)")
    private Integer pushTsm;
}
