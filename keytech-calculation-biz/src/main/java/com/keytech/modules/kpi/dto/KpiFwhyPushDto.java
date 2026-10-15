package com.keytech.modules.kpi.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @FileName KpiFwhyPushDto
 * @Description  不达标KPI-整改行动
 * @Author HQ
 * @date 2025/7/8
 **/
@Data
public class KpiFwhyPushDto {

    /**签名,由生成代码生成,请求时需要携带*/
    @JsonProperty(value = "_signature")
    private String signature;
    /**时间戳*/
    @JsonProperty(value = "_timestamp")
    private Long timestamp;
    /**nonce*/
    @JsonProperty(value = "_nonce")
    private Long nonce;

    /**数据集合**/
    private List<KpiFwhyDto> dataList;

}

