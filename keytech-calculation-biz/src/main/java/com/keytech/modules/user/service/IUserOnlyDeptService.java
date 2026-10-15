package com.keytech.modules.user.service;

import com.keytech.modules.user.UserOnlyDept;
import com.keytech.base.service.BaseService;

/**
 * @Description: 用户唯一部门
 * @Author: jeecg-boot
 * @Date:   2025-05-16
 * @Version: V1.0
 */
public interface IUserOnlyDeptService extends BaseService<UserOnlyDept> {
    void saveOrUpdateData(UserOnlyDept userOnlyDept);
}
