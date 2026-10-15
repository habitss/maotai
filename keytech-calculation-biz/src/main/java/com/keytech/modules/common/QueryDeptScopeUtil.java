package com.keytech.modules.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Map;

public class QueryDeptScopeUtil {
    public static void initDeptScope(QueryWrapper<?> queryWrapper, Map<String, String[]> parameterMap, Object obj) {
        try {
            String[] deptScope = parameterMap.get("deptScope");
            if(null != deptScope){
                queryWrapper.and(queryWrapper1 ->
                        queryWrapper1.
                                likeRight("factory_code", deptScope[0])
                                .or()
                                .likeRight("dept_code", deptScope[0])
                                .or()
                                .likeRight("line_code", deptScope[0])
                );
            }

            String[] orgCode = parameterMap.get("orgCode");
            if(null != orgCode){
                queryWrapper.and(queryWrapper1 ->
                        queryWrapper1.
                                eq("factory_code", orgCode[0])
                                .or()
                                .eq("dept_code", orgCode[0])
                                .or()
                                .eq("line_code", orgCode[0])
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
