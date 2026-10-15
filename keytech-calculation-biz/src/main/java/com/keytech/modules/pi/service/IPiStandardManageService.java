package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiStandardManage;

/**
* @Description: 针对表【mes3_pi_standard_manage(PI标准管理)】的数据库操作Service
* @Author: sjw
* @CreateDate: 2024/12/11 14:37:06
*/
public interface IPiStandardManageService extends BaseService<PiStandardManage> {

    /**
     * 获取最大排序
     * @param customizeId
     * @return
     */
    Double getMaxSort(String customizeId);

    /**
     * 复制标准
     * @param id
     */
    Boolean standardCopy(String id);
}
