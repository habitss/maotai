package com.keytech.modules.kpi.vo;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 表头数据集
 * @version v1.0.0
 * @date 2025/2/24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiReportCellVo {

    //名称
    String title;
    //字段key
    String key;
    //排序
    String sortIndex;
    //父节点
    String parentId;
    //是否固定
    String fixed;
    //子节点
    List<KpiReportCellVo> children;

    JSONObject extraConfig;
    //Map scopedSlots;
    public void initChildren() {
        children =new ArrayList<>();
    }

}
