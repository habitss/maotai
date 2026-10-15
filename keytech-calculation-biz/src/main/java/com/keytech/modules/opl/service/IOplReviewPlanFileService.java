package com.keytech.modules.opl.service;

import com.alibaba.fastjson.JSONObject;
import com.keytech.base.service.BaseService;
import com.keytech.modules.opl.entity.OplReviewPlanFile;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: Opl管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
public interface IOplReviewPlanFileService extends BaseService<OplReviewPlanFile> {

    Result<?> fileSave(JSONObject input);
}
