package com.keytech.modules.post.check.service;

import com.keytech.modules.post.check.entity.PostCheckRecordConfigProject;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 岗位检查记录配置-检查项目
 * @Author: jeecg-boot
 * @Date:   2025-01-06
 * @Version: V1.0
 */
public interface IPostCheckRecordConfigProjectService extends BaseService<PostCheckRecordConfigProject> {

    /**
     * 保存表单配置
     * @param list
     */
    void savePostCheckPz(List<PostCheckRecordConfigProject> list);
    /**
     * 批量删除
     */
    void deleteBatchData(String ids, String bzIds);
}
