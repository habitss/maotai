package com.keytech.modules.manage.check.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 分层检查-检查清单录入记录表
 * @Author: jeecg-boot
 * @Date:   2025-04-10
 * @Version: V1.0
 */
@Data
@TableName("mes3_manager_checklist_edit_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="分层检查-检查清单录入记录表")
public class ManagerChecklistEditRecord implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户id*/
	@Excel(name = "租户id", width = 15)
    @Schema(description = "租户id")
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

	/**填报主表id*/
	@Excel(name = "填报主表id", width = 15)
    @Schema(description = "填报主表id")
    private String mainId;

	/**配置表id*/
	@Excel(name = "配置表id", width = 15)
    @Schema(description = "配置表id")
    private String configurationId;

	/**检查记录*/
	@Excel(name = "检查记录", width = 15)
    @Schema(description = "检查记录")
    private String record;

	/**检查图片*/
	@Excel(name = "检查图片", width = 15)
    @Schema(description = "检查图片")
    private String picture;

	/**OK/NOK合格不合格*/
	@Excel(name = "OK/NOK合格不合格", width = 15)
    @Schema(description = "OK/NOK合格不合格")
    private String sure;

	/**异常描述*/
	@Excel(name = "异常描述", width = 15)
    @Schema(description = "异常描述")
    private String description;

	/**分层检查id*/
	@Excel(name = "分层检查id", width = 15)
    @Schema(description = "分层检查id")
    private String layeringId;

	/**序号*/
	@Excel(name = "序号", width = 15)
    @Schema(description = "序号")
    private Integer number;

	/**检查项编号*/
	@Excel(name = "检查项编号", width = 15)
    @Schema(description = "检查项编号")
    private String itemNumber;

	/**常规活动*/
	@Excel(name = "常规活动", width = 15)
    @Schema(description = "常规活动")
    private String activities;

	/**检查项来源*/
	@Excel(name = "检查项来源", width = 15)
    @Schema(description = "检查项来源")
    private String source;

	/**频率*/
	@Excel(name = "频率", width = 15)
    @Schema(description = "频率")
    private String frequency;

	/**体系*/
	@Excel(name = "体系", width = 15)
    @Schema(description = "体系")
    private String systemInfo;

	/**检查内容*/
	@Excel(name = "检查内容", width = 15)
    @Schema(description = "检查内容")
    private String content;

	/**是否必须在生产下执行0否，1是*/
	@Excel(name = "是否必须在生产下执行0否，1是", width = 15)
    @Schema(description = "是否必须在生产下执行0否，1是")
    private Integer stopProduction;

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

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;


    /**检查项来源名称*/
    @TableField(exist = false)
    private String sourceName;

    /**频率名称*/
    @TableField(exist = false)
    private String frequencyName;

    /**体系名称*/
    @TableField(exist = false)
    private String systemName;

    /**页面行类型 1检查记录 2检查图片 3检查判定 4异常描述 5异常提交**/
    @TableField(exist = false)
    private Integer rowType;

    @Override
    public Object clone(){
        try{
            return (ManagerChecklistEditRecord) super.clone();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static final long serialVersionUID = 1L;
}
