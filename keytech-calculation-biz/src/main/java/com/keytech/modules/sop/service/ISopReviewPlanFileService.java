package com.keytech.modules.sop.service;

import com.alibaba.fastjson.JSONObject;
import com.keytech.base.service.BaseService;
import com.keytech.modules.sop.entity.SopReviewPlanFile;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: SOP管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
public interface ISopReviewPlanFileService extends BaseService<SopReviewPlanFile> {

    Result<?> fileSave(JSONObject input);
}
