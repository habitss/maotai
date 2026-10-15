package com.keytech.modules.practice.dto;

import com.keytech.modules.practice.entity.BestPracticeRecommend;
import com.keytech.modules.practice.entity.BestPracticeRecommendDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BestPracticeRecommendDto extends BestPracticeRecommend {

    //明细列表
    private List<BestPracticeRecommendDetail> detailList;
}
