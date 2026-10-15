package com.keytech.modules.maotai.equipment.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.entity.EquipmentMaint;
import com.keytech.modules.maotai.equipment.mapper.EquipmentMaintMapper;
import com.keytech.modules.maotai.equipment.service.IEquipmentLedgerService;
import com.keytech.modules.maotai.equipment.service.IEquipmentMaintService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Date;

/**
 * 设备保养记录
 * @author maotai
 * @since 2026-06-15
 */
@Service
public class EquipmentMaintServiceImpl extends BaseServiceImpl<EquipmentMaintMapper, EquipmentMaint> implements IEquipmentMaintService {

    @Autowired
    private IEquipmentLedgerService equipmentLedgerService;

    /**
     * 生成工单号。规则集中在此,后期调整只改这里。
     * 当前规则:WO + 秒级时间戳(yyyyMMddHHmmss),如 WO20260615143025
     * 用秒级时间戳保证唯一(到日会因同日多次新增重复,撞唯一索引)
     */
    @Override
    public String generateOrderNo() {
        return "WO" + DateUtil.format(new Date(), "yyyyMMddHHmmss");
    }

    /**
     * 重写 saveBatch:Excel 导入走父类 importExcel → saveBatch,绕过了 controller.add,
     * 此处为每条补:工单号(批量内唯一)、租户ID、工厂、equipmentId(按设备名查台账)。
     */
    @Override
    public boolean saveBatch(Collection<EquipmentMaint> entityList) {
        if (entityList != null && !entityList.isEmpty()) {
            // 取当前请求的租户ID(header X-Tenant-Id)
            int tenantId = 0;
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest req = attrs.getRequest();
                    tenantId = req.getIntHeader("X-Tenant-Id");
                }
            } catch (Exception ignore) {
            }
            // 取当前登录用户的工厂(orgCode)
            String factoryCode = null;
            try {
                LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (loginUser != null) {
                    factoryCode = loginUser.getOrgCode();
                }
            } catch (Exception ignore) {
            }
            // 批量内工单号唯一:毫秒时间戳 + 批内3位序号(秒级在同批多条会重复撞唯一索引)
            String base = "WO" + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");
            int seq = 0;
            for (EquipmentMaint e : entityList) {
                if (e.getOrderNo() == null || e.getOrderNo().isEmpty()) {
                    e.setOrderNo(base + String.format("%03d", ++seq));
                }
                if (e.getTenantId() == null && tenantId > 0) {
                    e.setTenantId(tenantId);
                }
                if ((e.getFactoryCode() == null || e.getFactoryCode().isEmpty())
                        && factoryCode != null && !factoryCode.isEmpty()) {
                    e.setFactoryCode(factoryCode);
                }
                // 按 equipmentName 查台账回填 equipmentId(导入时 Excel 只有设备名,无 id)
                if ((e.getEquipmentId() == null || e.getEquipmentId().isEmpty())
                        && e.getEquipmentName() != null && !e.getEquipmentName().isEmpty()) {
                    QueryWrapper<EquipmentLedger> eqw = new QueryWrapper<>();
                    eqw.eq("equip_name", e.getEquipmentName()).last("limit 1");
                    EquipmentLedger eq = equipmentLedgerService.getOne(eqw, false);
                    if (eq != null) {
                        e.setEquipmentId(eq.getId());
                    }
                }
            }
        }
        return super.saveBatch(entityList);
    }
}
