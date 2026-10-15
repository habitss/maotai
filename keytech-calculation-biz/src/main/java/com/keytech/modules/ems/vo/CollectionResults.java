package com.keytech.modules.ems.vo;

import lombok.Getter;
import java.util.List;

/**
 * 采集结果封装类
 * 用于封装采集的成功和失败结果列表
 * @author yepeng
 */
@Getter
public class CollectionResults {
    // 成功结果列表
    private final List<CollectResultVo> successList;
    // 失败结果列表
    private final List<CollectResultVo> failList;

    public CollectionResults(List<CollectResultVo> successList, List<CollectResultVo> failList) {
        this.successList = successList;
        this.failList = failList;
    }

    public int getSuccessCount() {
        return successList.size();
    }

    public int getFailCount() {
        return failList.size();
    }

}