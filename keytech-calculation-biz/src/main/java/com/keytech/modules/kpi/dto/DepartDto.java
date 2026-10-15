package com.keytech.modules.kpi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.keytech.modules.kpi.entity.KpiType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DepartDto {

    private String id;
    private String parentId;
    private String departName;
    private Integer departOrder;
    private String orgCategory;
    private String orgType;
    private String orgCode;

    private List<DepartDto> children = new ArrayList<>();
}
