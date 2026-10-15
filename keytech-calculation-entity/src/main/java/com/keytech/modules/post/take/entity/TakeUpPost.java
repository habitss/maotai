package com.keytech.modules.post.take.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Description: 上下岗记录
 * @Author: jeecg-boot
 * @Date:   2025-06-16
 * @Version: V1.0
 */
@Data
@TableName("mes3_take_up_post")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="上下岗记录")
public class TakeUpPost implements Serializable {

	/**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

    @Schema(description="部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    @Schema(description="产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**账号*/
    @Schema(description = "账号")
    private String username;

	/**姓名*/
    @Schema(description = "姓名")
    private String realname;

	/**上岗时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "上岗时间")
    private Date postStartTime;

	/**离岗时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "离岗时间")
    private Date postEndTime;

	/**岗位ID*/
    @Schema(description = "岗位ID")
    @Dict(dictTable ="mes3_mdm_post",dicText = "post_name",dicCode = "id")
    private Integer postId;

	/**上岗总时长(分钟)*/
    @Schema(description = "上岗总时长(分钟)")
    private Integer totalTime;

    /**班组*/
    @Schema(description = "班组")
    @Dict(dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    private String team;

    /**班次类别*/
    @Schema(description = "班次类别")
    @Dict(dictTable ="mes3_mdm_shift_type",dicText = "shift_type_name",dicCode = "id")
    private String shiftTypeId;

    /**班次*/
    @Schema(description = "班次")
    @Dict(dictTable ="mes3_mdm_shift_manage",dicText = "shift_name",dicCode = "id")
    private String shiftId;

	/**角色*/
    @Schema(description = "角色")
    private Integer roleId;

	/**班长*/
    @Schema(description = "班长")
    private String monitor;

	/**状态*/
    @Schema(description = "状态")
    private Integer status;

	/**顶岗人 0-在岗 1-正常离岗 2-离岗被顶岗*/
    @Schema(description = "顶岗人 0-在岗 1-正常离岗 2-离岗被顶岗")
    private String field;

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

    @Schema(description = "去重字段")
    @TableField(exist = false)
    private String distinctField;

    @Schema(description = "岗位关联的机台id，key为机台id，value为岗位id")
    @TableField(exist = false)
    private Map<Integer, Integer> devicePostMap;

    private static final long serialVersionUID = 1L;
}
