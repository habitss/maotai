package com.keytech.modules.ems.config;

import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jeecg.common.config.TenantContext;
import org.springframework.stereotype.Component;

/**
 * EMS模块@XxlJob注解切面
 * @author yepeng
 * @since 2025.05.23
 */
@Aspect
@Component
public class XxlJobAspect {

    /**
     * XxlJob注解切面设置params参数中的x-tenant-id
     * @param joinPoint 切点
     */
    @Around("@annotation(com.xxl.job.core.handler.annotation.XxlJob) && within(com.keytech.modules.ems.job..*)")
    public Object aroundJob(ProceedingJoinPoint joinPoint) throws Throwable {
        String paramStr = XxlJobHelper.getJobParam();
        if (paramStr != null) {
            JSONObject paramObj = JSONObject.parseObject(paramStr);
            if (paramObj.containsKey("x-tenant-id")) {
                TenantContext.setTenant(paramObj.getString("x-tenant-id"));
            }
        }
        try {
            return joinPoint.proceed();
        } finally {
            TenantContext.clear();
        }
    }
}
