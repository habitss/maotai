package com.keytech.modules.kpi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @FileName KpiFwhyRectifyActionsDto
 * @Description  不达标KPI-整改行动
 * @Author HQ
 * @date 2025/7/8
 **/
@Data
public class KpiFwhyRectifyActionsDto {

    /** 工厂名称 */
    private String sfactory;
    /** 车间名称 */
    private String splant;
    /** 生成线名称 */
    private String sprdline;
    /** 类型id */
    private Integer itype;
    /** 类型 */
    private String stype;
    /** 标题 */
    private String stitle;
    /** 原因 */
    private String sreason;
    /** 行动 */
    private String action;
    /** 提出人工号 */
    private String proposerworkno;
    /** 执行人工号 */
    private String executorworkno;
    /** 计划完成日期 */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dplan;
    /** 实际完成日期 */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date drend;
    /** 备注 */
    private String snote;
    /** 作业班组 (废弃)*/
    private Integer swbz;
    /** 取消原因 */
    private String scancelreason;
    /**完成状态*/
    private Integer istate;
    /**完成状态描述 */
    private String sstate;
    /** 伪删除 (0正常 1删除)*/
    private Integer idel;

    /**生产信息化行动主键*/
    private String isourceid;



}

