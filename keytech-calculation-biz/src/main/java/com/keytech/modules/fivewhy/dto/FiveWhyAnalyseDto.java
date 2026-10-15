package com.keytech.modules.fivewhy.dto;

import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;
import java.util.Map;

/**
 * @Description: 5why 分析DTO
 * @Author: sjw
 * @Date: 2025/1/20 15:08
 */
@Data
public class FiveWhyAnalyseDto {
    private String id;
    private String title;
    private String fieldName;
    private String fieldCode;
    private String tableName;
    private String tableCode;
    private String kpiCode;
    private String kpiColKey;
    private String kpiYear;
    private String dataId;
    private String color;
    private String type;
    private String reason;
    private String problem;
    private String problemDesc;
    private String mergeRule;
    private Integer whyNum;
    private List<FiveWhyResonDto> resonDtoList;
    private List<FiveWhyDetail> fiveWhyDetailList;
}
