package com.keytech.modules.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.keytech.modules.user.UserOnlyDept;
import com.keytech.modules.user.mapper.UserOnlyDeptMapper;
import com.keytech.modules.user.service.IUserOnlyDeptService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;

/**
 * @Description: 用户唯一部门
 * @Author: jeecg-boot
 * @Date:   2025-05-16
 * @Version: V1.0
 */
@Service
public class UserOnlyDeptServiceImpl extends BaseServiceImpl<UserOnlyDeptMapper, UserOnlyDept> implements IUserOnlyDeptService {

    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public void saveOrUpdateData(UserOnlyDept userOnlyDept) {
        List<SysDepart> list = sysDepartService.lambdaQuery().eq(SysDepart::getId, userOnlyDept.getUserDeptId()).list();
        if(CollectionUtil.isEmpty(list)){
            throw new JeecgBootException("部门不存在");
        }
        SysDepart sysDepart = list.get(0);
        userOnlyDept.setUserDeptCode(sysDepart.getOrgCode());
        userOnlyDept.setUserDeptName(sysDepart.getDepartName());
        this.saveOrUpdate(userOnlyDept);
    }
}
