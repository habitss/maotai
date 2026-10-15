package com.keytech.modules.plan.dto;

import com.keytech.modules.plan.entity.PlanOrderResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/6/19 13:18
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class PlanOrderResultSaveDto{
    //必填项是否完成填写
    private Boolean isFinish;
    //数据
    private List<PlanOrderResult> list;
}
