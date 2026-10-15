package com.keytech.modules.project.management.dto;

import cn.hutool.json.JSONObject;
import com.keytech.modules.project.management.entity.ProjectManagement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description: 项目清单列表
 * @Author: sjw
 * @Date: 2025/4/21 15:11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectManagementDocumentDto {
    /**
     * 年份
     */
    private String year;
    /**
     * 月份集合
     */
    private List<String> months;
    /**
     * 数据集合-动态数据，用json对象接受
     */
    private List<JSONObject> dataList;
}
