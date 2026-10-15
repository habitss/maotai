package com.keytech.modules.kpi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 *
 */
@Data
public class KpiReportHeadDto {
    private String id;
    /** 字段中文名称*/
    private String title;
    /** 字段英文标识*/
    private String key;
    /** 父id*/
    private String parentId;
    /** 排序 从小到大*/
    private int sort;
    /**
     * 0 否 1 是
     */
    private int keyType;
    /**
     * 扩展字段
     */
    private String ext;

    public KpiReportHeadDto(String id,String title, String key, String parentId, int sort) {
        this.id = id;
        this.title = title;
        this.key = key;
        this.parentId = parentId;
        this.sort = sort;
    }
    public KpiReportHeadDto(){}
}
