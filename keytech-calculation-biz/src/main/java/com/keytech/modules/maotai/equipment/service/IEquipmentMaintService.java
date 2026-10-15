package com.keytech.modules.maotai.equipment.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.maotai.equipment.entity.EquipmentMaint;

/**
 * 设备保养记录
 * @author maotai
 * @since 2026-06-15
 */
public interface IEquipmentMaintService extends BaseService<EquipmentMaint> {

    /**
     * 生成工单号(规则集中在此,后期调整只改这里)
     * 当前规则:WO + yyyyMMdd + "-" + 当日4位序号(如 WO20260615-0001)
     * @return 工单号
     */
    String generateOrderNo();
}
