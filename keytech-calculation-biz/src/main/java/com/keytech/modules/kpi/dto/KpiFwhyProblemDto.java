package com.keytech.modules.kpi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @FileName KpiFwhyProblemDto
 * @Description 不达标KPI-问答分析数据
 * @Author HQ
 * @date 2025/7/8
 **/
@Data
public class KpiFwhyProblemDto {

    /** 问题ID */
    private String iwhy;
    /** 问题 */
    private String swhy;
    /** 回答ID */
    private String ianswer;
    /** 回答 */
    private String sanswer;
    /** 父节点ID，问题ID拼接回答ID */
    private String iparentid;
    /** 创建人工号 */
    private String sregworkno;
    /** 删除标识 */
    private Integer idel;
    /** 层级 */
    private Integer ilevel;
    /** 临时id */
    @JsonIgnore
    private String tmpId;
    /** 当前是几why */
    @JsonIgnore
    private String several;
}

