package com.keytech.modules.common;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 步骤管理
 * @Author: HQ
 * @Date:   2024-12-20
 * @Version: V1.0
 */
@Data
public class DataBase {
    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

    /**工厂*/
    @Schema(description="工厂")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private java.lang.String factoryCode;
    /**部门*/
    @Schema(description="部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private java.lang.String deptCode;
    /**产线*/
    @Schema(description="产线")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private java.lang.String lineCode;


    /**排序*/
    @Schema(description="排序")
    private Double sort;
    /**删除状态(0未删,1已删)*/
    @Schema(description="删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="创建时间")
    private Date createTime;
    /**修改时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="修改时间")
    private Date updateTime;
    /**创建人*/
    @Schema(description="创建人")
    private String createBy;
    /**修改人*/
    @Schema(description="修改人")
    private String updateBy;



}
