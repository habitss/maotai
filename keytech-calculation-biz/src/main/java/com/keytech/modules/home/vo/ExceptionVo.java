package com.keytech.modules.home.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 异常清单
 * @author hq
 * @date 2025/05/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionVo {
    // 异常清单id
    private String id;
    // 报表名称
    private String tableName;
    // pi名称
    private String fieldName;
    // 创建时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    // 班组-评价结果
    private String evaluationResult;
    // 班组-评价类型
    private String evaluationType;
    // 车间-评价结果
    private String workshopEvaluationResult;
    // 车间-评价类型
    private String workshopEvaluationType;
}
