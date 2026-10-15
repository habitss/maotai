package com.keytech.modules.manage.check.service;

import com.keytech.modules.manage.check.entity.ManagerChecklistEditMain;
import com.keytech.base.service.BaseService;

import java.util.Map;

/**
 * @Description: 分层检查-检查清单录入主表
 * @Author: jeecg-boot
 * @Date:   2025-04-10
 * @Version: V1.0
 */
public interface IManagerChecklistEditMainService extends BaseService<ManagerChecklistEditMain> {
    /**
     * 获取检查项目
     * @return
     */
    Map<String, Object> queryInspectData(ManagerChecklistEditMain managerChecklistEditMian);

    /**
     * 数据存储
     * @param managerChecklistEditMian
     */
    void dataSave(ManagerChecklistEditMain managerChecklistEditMian);

    /**
     * 检查清单定时推送
     */
    void managerMonthYearCheckPush();
}
