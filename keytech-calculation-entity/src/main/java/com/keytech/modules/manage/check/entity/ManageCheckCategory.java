package com.keytech.modules.manage.check.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 管理者检查清单-分层检查分类
 * @Author: jeecg-boot
 * @Date:   2025-03-30
 * @Version: V1.0
 */
@Data
@TableName("mes3_manage_check_category")
@Schema(description="管理者检查清单-分层检查分类")
public class ManageCheckCategory implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;
    /**租户ID*/
    @Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**工厂*/
    @Schema(description = "工厂")
    private String factoryCode;
	/**部门*/
    @Schema(description = "部门")
    private String deptCode;
	/**产线*/
    @Schema(description = "产线")
    private String lineCode;
	/**code*/
    @Schema(description = "code")
    private String code;
	/**分类名称*/
    @Schema(description = "分类名称")
    private String categoryName;
	/**上级code*/
    @Schema(description = "上级code")
    private String parentCode;
	/**是否可用： 0否 1是*/
    @Schema(description = "是否可用： 0否 1是")
    private Integer active;
    /**级别*/
    @Schema(description = "级别")
    private Integer level;
    /**是否启用1：是；0：否*/
    @Schema(description = "是否启用1：是；0：否")
    private Integer ifEnable;
	/**排序*/
    @Schema(description = "排序")
    private Integer sort;
	/**父级节点*/
    @Schema(description = "父级节点")
    private String pid;
	/**扩展配置*/
    @Schema(description = "扩展配置")
    private String extJson;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "创建时间")
    private Date createTime;
	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;
	/**创建人*/
    @Schema(description = "创建人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**删除状态(0未删,1已删)*/
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;
	/**是否有子节点*/
	@Dict(dicCode = "yn")
    @Schema(description = "是否有子节点")
    private String hasChild;

    @TableField(exist = false)
    private List<ManageCheckCategory> childList = new ArrayList<>();
    @TableField(exist = false)
    private Boolean disabled;
}
