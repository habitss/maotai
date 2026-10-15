package com.keytech.modules.post.check.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 岗位检查配置-检查标准
 * @Author: jeecg-boot
 * @Date:   2025-01-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_post_check_record_config_standard")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="岗位检查配置-检查标准")
public class PostCheckRecordConfigStandard implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "检查数据记录ID", width = 20)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
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

	/**检查项目id*/
    @Schema(description = "检查项目id")
    private String jcxmId;

	/**检查标准名称*/
	@Excel(name = "检查数据记录描述", width = 15)
    @Schema(description = "检查标准名称")
    private String checkname;

	/**对照APP条目名称*/
    @Schema(description = "对照APP条目名称")
    private String appCheck;

	/**检查时间是否必填*/
    @Schema(description = "检查时间是否必填")
    private Integer checktimeIfmust;

	/**检查数据记录格式*/
    @Excel(name = "检查数据记录格式", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_POST_CHECK_FIELD_TYPE'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "检查数据记录格式")
    private String checknameFormat;

	/**检查数据记录是否必填,0否，1是*/
	@Excel(name = "检查数据记录是否必填", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "检查数据记录是否必填,0否，1是")
    private Integer checknameIfmust;

	/**检查数据记录对应的基础数据代码*/
    @Schema(description = "检查数据记录对应的基础数据代码")
    private String piCode;

	/**检查数据记录对应的基础数据名称*/
    @Schema(description = "检查数据记录对应的基础数据名称")
    private String piName;

	/**检查数据记录对应的基础数据名称1自动采集，其他手动*/
    @Schema(description = "检查数据记录对应的基础数据名称1自动采集，其他手动")
    private String piType;

	/**检查照片是否必填，0否，1是*/
    @Schema(description = "检查照片是否必填，0否，1是")
    private Integer checkphotoIfmust;

	/**排序*/
	@Excel(name = "排序", width = 15)
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

    private static final long serialVersionUID = 1L;
}
