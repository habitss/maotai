package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.entity.LineEfficiencySkuDay;
import com.keytech.base.service.BaseService;
import org.jeecg.common.api.vo.Result;

import java.util.List;

/**
 * @Description: 产线-SKU天记录表/拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-20
 * @Version: V1.0
 */
public interface ILineEfficiencySkuDayService extends BaseService<LineEfficiencySkuDay> {

    /**
     * @Description: 产线-SKU天记录拆分
     * @Author: jeecg-boot
     * @Date:   2025-08-21
     * @Version: V1.0
     */
    Result<String> dataSplit(List<LineEfficiencySkuDay> lineEfficiencySkuDayList);
}
