package com.keytech.modules.practice.dto;

import com.keytech.modules.practice.entity.BestPracticeReview;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/3/20 18:49
 */
@Data
public class BestPracticeReviewDto {
    //填报id
    String fillId;
    //填报类型
    Integer fillType;
    //填报报表id
    String fillReportId;
    //填报数据
    List<BestPracticeReview> list;
}
