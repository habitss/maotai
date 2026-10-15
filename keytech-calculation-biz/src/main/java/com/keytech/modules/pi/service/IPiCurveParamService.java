package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.pi.entity.PiCurveParam;
import com.keytech.base.service.BaseService;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: PI曲线参数配置
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
public interface IPiCurveParamService extends BaseService<PiCurveParam> {

    //保存/修改数据
    Result<?> saveDate(PiCurveParam piCurveParam);

    void convert(IPage<PiCurveParam> pageList);
}
