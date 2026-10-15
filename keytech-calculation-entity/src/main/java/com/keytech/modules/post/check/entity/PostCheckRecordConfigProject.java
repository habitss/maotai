package com.keytech.modules.post.check.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 岗位检查记录配置-检查项目
 * @Author: jeecg-boot
 * @Date:   2025-01-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_post_check_record_config_project")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="岗位检查记录配置-检查项目")
public class PostCheckRecordConfigProject implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "检查项目ID", width = 20, orderNum = "4")
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;

    @Schema(description="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code", orderNum = "1")
    @NotBlank(message = "部门Code为空")
    private String deptCode;

    @Schema(description="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "产线", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code", orderNum = "2")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

    @Schema(description="工段")
    private String section;

	/**报表id*/
    @Schema(description = "报表id")
    @Excel(name = "报表", width = 20, dictTable ="mes3_post_check_record_config_main where del_flag = '0'", dicText = "report_name", dicCode = "id", orderNum = "3")
    private String zbId;

	/**步骤*/
	@Excel(name = "步骤", width = 15, orderNum = "5")
    @Schema(description = "步骤")
    private String step;

    /**检查项目*/
    @Excel(name = "检查项目", width = 15, orderNum = "9")
    @Schema(description = "检查项目")
    private String checkItem;

    /**检查标准*/
    @Excel(name = "检查标准", width = 15, orderNum = "10")
    @Schema(description = "检查标准")
    private String checkStandardDesc;

	/**对照APP任务名称*/
    @Schema(description = "对照APP任务名称")
    private String appItem;

	/**项目检查次数，默认一次检查频次说明，*/
	@Excel(name = "项目检查次数", width = 15, orderNum = "6")
    @Schema(description = "项目检查频次，默认一次")
    private Integer checkFrequency;

	/**标准检查频次说明*/
	@Excel(name = "标准检查频次说明", width = 15, orderNum = "7")
    @Schema(description = "标准检查频次说明")
    private String frequencyIllustrate;

	/**标准检查频次*/
	@Excel(name = "标准检查频次", width = 15, orderNum = "8")
    @Schema(description = "标准检查频次")
    private Double frequencyIllustrateCount;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
    @Schema(description = "备注")
    private String remark;

	/**是否删除 0-正常 1-删除*/
    @Schema(description = "是否删除 0-正常 1-删除")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**检查标准列表**/
    @ExcelCollection(name = "检查数据记录清单", orderNum = "11")
    @TableField(exist = false)
    List<PostCheckRecordConfigStandard> listJcbz = new ArrayList<>();

    private static final long serialVersionUID = 1L;
}
