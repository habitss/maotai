package com.keytech.modules.serviceAgreement.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;

import java.io.Serializable;
import java.util.List;

@Data
public class ServiceAgreementExportVO implements Serializable {

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

//    /**工厂Code*/
//    @Excel(name = "工厂Code", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
//    private String factoryCode;
//
//    /**部门Code*/
//    @Excel(name = "部门(*)", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
//    private String deptCode;
//
//    /**产线Code*/
//    @Excel(name = "产线Code", width = 15 ,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
//    private String lineCode;

    /**提供方部门*/
    @Excel(name = "提供方部门(*)", width = 15, needMerge = true,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String provideDept;

    /**产品/服务*/
    @Excel(name = "产品/服务(*)", needMerge = true,width = 15)
    private String product;

    /**活动*/
    @Excel(name = "活动(*)", needMerge = true,width = 15)
    private String activity;

    /**接收方部门*/
    @Excel(name = "接收方部门(*)", needMerge = true,width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String receiveDept;

    /**服务度量指标*/
    @Excel(name = "服务度量指标", needMerge = true,width = 30)
    private String serviceIndicator;

    /**单位*/
    @Excel(name = "单位", needMerge = true,width = 15)
    private String unit;

    @ExcelCollection(name = "月份", type = ServiceMonthVO.class)
    private List<ServiceMonthVO> serviceMonthList;


//    @Excel(name = "提供方部门(*)", width = 15)
//    private String provideDept_text;
//
//    @Excel(name = "接收方部门(*)", width = 15)
//    private String receiveDept_text;

    private static final long serialVersionUID = 1L;
}
