package com.keytech.modules.home.vo;

import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hq
 * @date 2025/05/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpRaFromVo {

    private int actionOk;// 已完成
    private int actionIng;// 正在进行
    private List<ActionPlanTrack> tracks;
    private int deferCount;//延期数
    private int closeCount;//取消数
    private int deferOkCount;//延期完成数
}
