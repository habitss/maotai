package com.keytech.modules.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 获取部门子级id
 * @Author: sjw
 * @Date: 2025/5/29 17:58
 */
@Component
public class GetDeptChildUtil{
    @Autowired
    private ISysDepartService sysDepartService;

    /**
     * 根据部门id获取所有子级id
     * @param deptId 部门id
     * @return 包含传入部门及子级id
     */
    public List<String> getListByDeptId(String deptId){
        List<String> depts = new ArrayList<>(0);
        if(StrUtil.isNotBlank(deptId)){
            List<SysDepart> list = sysDepartService.lambdaQuery().eq(SysDepart::getId, deptId).list();
            if(CollUtil.isNotEmpty(list)){
                SysDepart sysDepart = list.get(0);
                list = sysDepartService.lambdaQuery().likeRight(SysDepart::getOrgCode, sysDepart.getOrgCode()).list();
                depts = list.stream()
                        .map(SysDepart::getId)
                        .collect(Collectors.toList());
            }
        }
        return depts;
    }

    /**
     * 根据部门id获取所有子级id
     * @param deptId 部门id
     * @return 包含传入部门及子级id的sql字符串,例："'a','b','c'"
     */
    public String getSqlStrByDeptId(String deptId){
        if(StrUtil.isBlank(deptId)){
            return null;
        }
        List<String> deptList = new ArrayList<>();
        String[] deptArr = deptId.split(",");
        for (String string : deptArr) {
            deptList.addAll(this.getListByDeptId(string));
        }
        if(CollUtil.isEmpty(deptList)){
            return null;
        }
        String sss = String.join(", ", deptList.stream().map(s -> "'" + s + "'").toArray(String[]::new));
        return String.join(", ", deptList.stream().map(s -> "'" + s + "'").toArray(String[]::new));
    }

    /**
     * 根据参数按逗号拆分
     * @param value
     * @return 包含逗号拼接字符串
     */
    public String getConversionToSqlStr(String value){
        if(StrUtil.isBlank(value)){
            return null;
        }
        StringBuilder result = new StringBuilder();
        String[] valArr = value.split(",");
        for(int i = 0; i < valArr.length; i++){
            SqlInjectionUtil.filterContent(valArr[i]);
            result.append("'").append(valArr[i]).append("'");
            if(i < valArr.length - 1){
                result.append(",");
            }
        }
        return result.toString();
    }
}
