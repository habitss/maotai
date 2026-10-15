package com.keytech.modules.pi.dto;

import com.keytech.modules.pi.entity.PiDfAlgorithmBaseinfo;
import com.keytech.modules.pi.entity.PiDfAlgorithmModel;
import com.keytech.modules.pi.entity.PiDfAlgorithmUnique;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * DF算法配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PiDfAlgorithmDto extends PiDfAlgorithmBaseinfo {
    //是否改变取数表
    private Boolean isChangeDfTable;
    //唯一约束
    private List<PiDfAlgorithmUnique> piDfAlgorithmUniqueList;
    //计算模型
    private List<PiDfAlgorithmModel> piDfAlgorithmModelList;
}
