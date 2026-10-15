package com.keytech.modules.dataSync.config;


/**
 * 历史补全任务状态
 */
public enum HistoryFillStatus {
    /** 关闭（默认） */
    CLOSE,
    /** 开启（需要补全） */
    OPEN,
    /** 执行中（防止重复执行） */
    RUNNING
}
