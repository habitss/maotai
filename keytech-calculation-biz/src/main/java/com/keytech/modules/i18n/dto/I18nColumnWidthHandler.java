package com.keytech.modules.i18n.dto;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.style.column.AbstractHeadColumnWidthStyleStrategy;

/**
 * 列宽策略
 * @author yepeng
 * @since 2025.12.19
 */
public class I18nColumnWidthHandler extends AbstractHeadColumnWidthStyleStrategy {

    @Override
    protected Integer columnWidth(Head head, Integer columnIndex) {
        // 固定列
        if (columnIndex == 0) {
            // 模块
            return 20;
        }
        if (columnIndex == 1) {
            // 词条 Key
            return 25;
        }
        if (columnIndex == 2) {
            // 描述
            return 40;
        }
        // 动态语言列
        return 40;
    }
}
