package com.keytech.modules.statistics.vo;

import lombok.Data;

/**
 * 使用统计表头
 */
@Data
public class TableChildrenVo {
    //标题
    private String title;
    //
    private String align;
    //字段名
    private String dataIndex;
    //是否省略
    private Boolean ellipsis;
    //宽度
    private String width;
}
