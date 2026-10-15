package com.keytech.modules.kpi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @FileName KpiFwhyDto
 * @Description 不达标KPI 5WHY分析推送
 * @Author HQ
 * @date 2025/7/8
 **/
@Data
public class KpiFwhyDto {
    /** KPI结果id */
    @JsonProperty(value = "kpi_result_id")
    private Long kpiResultId;
    /** 删除标识(0正常 1删除)*/
    private Integer idel;
    /** 日期 */
    @JsonProperty(value = "datekpi")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dateKpi;
    /**状态 (1夜 2白 3中 4日累 5周累 6月累 7年累)*/
    private Integer state;
    /**工厂名称 */
    private String sfactory;
    /**车间名称*/
    private String splant;
    /**生产线名称*/
    private String sprdline;
    /**指标名称*/
//    private String slibrary;
    @JsonProperty(value = "ilibraryid")
    private String ilibraryId;

    /**标题*/
    private String stitle;
    /**异常描述*/
    private String description;
    /**创建时间*/
    private String dregtStr;
    /**指标名称*/
    private String headline;
    /**人员工号*/
    private String workno;
    /**来源 默认 1*/
    private String itype;
    /**问答分析数据*/
    @JsonProperty(value = "kpi5whyDts")
    private List<KpiFwhyProblemDto> kpiFwhyProblemDto;
    /**整改行动数据*/
    @JsonProperty(value = "kpiRectifyActions")
    private List<KpiFwhyRectifyActionsDto> kpiFwhyRectifyActionsDto;
    /**签名,由生成代码生成,请求时需要携带*/
    @JsonProperty(value = "_signature")
    private String signature;
    /**时间戳*/
    @JsonProperty(value = "_timestamp")
    private Long timestamp;
    /**nonce*/
    @JsonProperty(value = "_nonce")
    private Long nonce;

    /**KpiCode*/
    @JsonIgnore
    private String kpiCode;

    /**5wid*/
    @JsonIgnore
    private String analyseId;

    /**第三方维护ID*/
    @JsonIgnore
    private String thirdPlayId;

}

