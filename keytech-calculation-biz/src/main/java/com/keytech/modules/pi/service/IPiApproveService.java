package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.pi.entity.PiApprove;
import com.keytech.base.service.BaseService;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: pi报表-报表审批
 * @Author: jeecg-boot
 * @Date:   2025-01-09
 * @Version: V1.0
 */
public interface IPiApproveService extends BaseService<PiApprove> {

    /**
     * 修改/剔除申请
     * @param piApprove
     */
    Result<?> approveSave(PiApprove piApprove);

    /**
     * 审批
     * @param piApprove
     */
    void approve(PiApprove piApprove);

    /**
     * 匹配字段
     * @param pageList
     * @return
     */
    IPage<PiApprove> matchData(IPage<PiApprove> pageList);

}
