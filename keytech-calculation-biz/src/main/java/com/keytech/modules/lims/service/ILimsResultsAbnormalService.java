package com.keytech.modules.lims.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.lims.dto.LimsResultsAbnormalDto;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: 超标不合格台账(LIMS)
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
public interface ILimsResultsAbnormalService extends BaseService<LimsResultsAbnormal> {

    /**
     * 更新5why
     * @param limsResultsAbnormal
     */
    void updateFwhy(LimsResultsAbnormal limsResultsAbnormal);

    /**
     * 审批
     * @param limsResultsAbnormal
     * @return
     */
    Result approval(LimsResultsAbnormal limsResultsAbnormal);

    /**
     * 接收LIMS推送的数据
     * @param limsResultsAbnormalDto
     */
    void abnormalSave(LimsResultsAbnormalDto limsResultsAbnormalDto);

    /**
     * 获取超标不合数据
     * @param page
     * @param limsResultsAbnormal
     * @return
     */
    IPage<LimsResultsAbnormal> queryPageList(Page<LimsResultsAbnormal> page, LimsResultsAbnormal limsResultsAbnormal, HttpServletRequest req);

    /**
     * 超标不合格定时推送
     */
    void limsMessageSendJob();
}
