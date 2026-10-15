package com.keytech.modules.post.check.service;

import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoMain;
import com.keytech.base.service.BaseService;

/**
 * @Description: 岗位记录填报主表
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
public interface IPostCheckRecordDatainfoMainService extends BaseService<PostCheckRecordDatainfoMain> {
    void saveDataInfo(PostCheckRecordDatainfoMain postCheckRecordDatainfoMain);
}
