package com.keytech.modules.post.check.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 岗位记录填报明细表
 * @Author: jeecg-boot
 * @Date:   2025-02-11
 * @Version: V1.0
 */
@Data
@TableName("mes3_post_check_record_datainfo_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="岗位记录填报明细表")
public class PostCheckRecordDatainfoDetail implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deptCode;

    /**产线*/
    @Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String lineCode;

    /**工段*/
    @Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**填报主表ID*/
	@Excel(name = "填报主表ID", width = 15)
    @Schema(description = "填报主表ID")
    private String datainfoId;

	/**配置主表id*/
	@Excel(name = "配置主表id", width = 15)
    @Schema(description = "配置主表id")
    private String configMainId;

	/**检查项目id*/
	@Excel(name = "检查项目id", width = 15)
    @Schema(description = "检查项目id")
    private String jcxmId;

	/**检查项目循环数*/
	@Excel(name = "检查项目循环数", width = 15)
    @Schema(description = "检查项目循环数")
    private Integer jcxmXhs;

	/**检查时间*/
	@Excel(name = "检查时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "检查时间")
    private Date checkTime;

	/**检查数据记录*/
	@Excel(name = "检查数据记录", width = 15)
    @Schema(description = "检查数据记录")
    private String checkData;

	/**检查照片*/
	@Excel(name = "检查照片", width = 15)
    @Schema(description = "检查照片")
    private String checkPhoto;

	/**检查标准循环数*/
	@Excel(name = "检查标准循环数", width = 15)
    @Schema(description = "检查标准循环数")
    private Integer jcbzXhs;

	/**检查项目id(组合id 检查项目id_序号)*/
	@Excel(name = "检查项目id(组合id 检查项目id_序号)", width = 15)
    @Schema(description = "检查项目id(组合id 检查项目id_序号)")
    private String checkItemId;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**是否删除 0-正常 1-删除*/
	@Excel(name = "是否删除 0-正常 1-删除", width = 15)
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
