package com.keytech.modules.practice.entity;

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
 * @Description: 最佳实践推荐表
 * @Author: jeecg-boot
 * @Date:   2025-03-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_best_practice_recommend")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="最佳实践推荐表")
public class BestPracticeRecommend implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**填报ID*/
	@Excel(name = "填报ID", width = 15)
    @Schema(description = "填报ID")
    private String fillId;

	/**改善前描述*/
	@Excel(name = "改善前描述", width = 15)
    @Schema(description = "改善前描述")
    private String improveDescription;

	/**改善措施*/
	@Excel(name = "改善措施", width = 15)
    @Schema(description = "改善措施")
    private String improveMeasure;

	/**推荐人意见*/
	@Excel(name = "推荐人意见", width = 15)
    @Schema(description = "推荐人意见")
    private String opinion;

	/**改善类型文件*/
	@Excel(name = "改善类型文件", width = 15)
    @Schema(description = "改善类型文件")
    private String improveFile;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
