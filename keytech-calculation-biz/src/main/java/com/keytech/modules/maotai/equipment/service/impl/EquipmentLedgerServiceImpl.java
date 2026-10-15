package com.keytech.modules.maotai.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.mapper.EquipmentLedgerMapper;
import com.keytech.modules.maotai.equipment.service.IEquipmentLedgerService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 设备台账
 * @author maotai
 * @since 2026-06-15
 */
@Service
public class EquipmentLedgerServiceImpl extends BaseServiceImpl<EquipmentLedgerMapper, EquipmentLedger> implements IEquipmentLedgerService {

    /**
     * 重写 saveBatch:Excel 导入走父类 importExcel → saveBatch,绕过 controller.add,
     * 此处补:工厂(当前登录用户 orgCode)、租户ID(请求 header X-Tenant-Id),
     * 并过滤掉 funloc_code 在同租户内已存在的行(避免重复撞 uk_funloc_tenant 致整批失败)。
     */
    @Override
    public boolean saveBatch(Collection<EquipmentLedger> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return true;
        }
        // 取当前请求的租户ID
        int tenantId = 0;
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                tenantId = req.getIntHeader("X-Tenant-Id");
            }
        } catch (Exception ignore) {
        }
        // 取当前登录用户的工厂
        String factoryCode = null;
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null) {
                factoryCode = loginUser.getOrgCode();
            }
        } catch (Exception ignore) {
        }
        List<EquipmentLedger> toSave = new ArrayList<>();
        for (EquipmentLedger e : entityList) {
            // 工厂:Excel 无则取当前登录用户工厂
            if ((e.getFactoryCode() == null || e.getFactoryCode().isEmpty())
                    && StringUtils.isNotEmpty(factoryCode)) {
                e.setFactoryCode(factoryCode);
            }
            // 租户
            if (e.getTenantId() == null && tenantId > 0) {
                e.setTenantId(tenantId);
            }
            // funloc_code 租户内查重:重复则跳过该行(避免撞唯一索引)
            if (StringUtils.isNotEmpty(e.getFunlocCode())) {
                QueryWrapper<EquipmentLedger> dup = new QueryWrapper<EquipmentLedger>()
                        .eq("funloc_code", e.getFunlocCode());
                if (e.getTenantId() != null) {
                    dup.eq("tenant_id", e.getTenantId());
                }
                long count = this.count(dup);
                if (count > 0) {
                    // 已存在,跳过
                    continue;
                }
            }
            toSave.add(e);
        }
        if (toSave.isEmpty()) {
            return true;
        }
        return super.saveBatch(toSave);
    }
}
