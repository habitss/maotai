package com.keytech.modules.serviceAgreement.dto;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

@Data
public class ServiceMonthVO implements Serializable {

    @Excel(name = "目标/实际", width = 15)
    private String title;

    @Excel(name = "1月", width = 15)
    private int jan ;
    @Excel(name = "2月", width = 15)
    private int feb ;
    @Excel(name = "3月", width = 15)
    private int mar ;
    @Excel(name = "4月", width = 15)
    private int apr ;
    @Excel(name = "5月", width = 15)
    private int may ;
    @Excel(name = "6月", width = 15)
    private int jun ;
    @Excel(name = "7月", width = 15)
    private int jul ;
    @Excel(name = "8月", width = 15)
    private int aug ;
    @Excel(name = "9月", width = 15)
    private int sep ;
    @Excel(name = "10月", width = 15)
    private int oct ;
    @Excel(name = "11月", width = 15)
    private int nov ;
    @Excel(name = "12月", width = 15)
    private int dec ;

    private static final long serialVersionUID = 1L;

}
