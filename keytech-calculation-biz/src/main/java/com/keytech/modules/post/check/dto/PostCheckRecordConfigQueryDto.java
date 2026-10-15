package com.keytech.modules.post.check.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 岗位检查配置-检查标准查询DTO
 */
@Data
public class PostCheckRecordConfigQueryDto implements Serializable {
    /**主键*/
    @Excel(name = "ID", width = 15)
    @Schema(description = "主键")
    private String id;

    /**租户ID*/
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

    /**报表id*/
    @Excel(name = "配置ID", width = 15)
    @Schema(description = "报表id")
    private Integer zbId;

    /**步骤*/
    @Excel(name = "步骤", width = 15)
    @Schema(description = "步骤")
    private String step;

    /**检查项目*/
    @Excel(name = "检查项目", width = 15)
    @Schema(description = "检查项目")
    private String checkItem;

    /**项目检查次数，默认一次检查频次说明，*/
    @Excel(name = "项目检查频次", width = 15)
    @Schema(description = "项目检查频次，默认一次")
    private Integer checkFrequency;

    /**标准检查频次说明*/
    @Excel(name = "标准检查频次说明", width = 15)
    @Schema(description = "标准检查频次说明")
    private String frequencyIllustrate;

    /**标准检查频次*/
    @Excel(name = "标准检查频次", width = 15)
    @Schema(description = "标准检查频次")
    private Double frequencyIllustrateCount;

    /**检查项目id*/
    @Schema(description = "检查项目id")
    private String jcxmId;

    /**检查标准名称*/
    @Excel(name = "检查标准名称", width = 15)
    @Schema(description = "检查标准名称")
    private String checkname;

    /**检查标准描述*/
    @Excel(name = "检查标准描述", width = 15)
    @Schema(description = "检查标准描述")
    private String checkStandardDesc;

    /**检查数据记录格式*/
    @Excel(name = "检查数据记录格式", width = 15)
    @Schema(description = "检查数据记录格式")
    private String checknameFormat;

    /**检查数据记录是否必填,0否，1是*/
    @Excel(name = "检查数据记录是否必填,0否，1是", width = 15)
    @Schema(description = "检查数据记录是否必填,0否，1是")
    private Integer checknameIfmust;

    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

    /**标准id*/
    private String bzid;

    /**合并行的数量*/
    private Integer rowSpan;

    private static final long serialVersionUID = 1L;
}
