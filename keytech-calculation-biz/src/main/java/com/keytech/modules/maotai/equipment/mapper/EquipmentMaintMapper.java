package com.keytech.modules.maotai.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.equipment.entity.EquipmentMaint;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 设备保养记录
 * @author maotai
 * @since 2026-06-15
 */
public interface EquipmentMaintMapper extends BaseMapper<EquipmentMaint> {

    /**
     * 历史已被保养过的设备数(distinct equipment_id,status=completed),按工厂前缀过滤。
     * 用 COUNT(DISTINCT) 避免 MyBatis-Plus count(wrapper) 退化为 count(*)。
     */
    @Select("SELECT COUNT(DISTINCT equipment_id) FROM mes3_maotai_equipment_maint "
            + "WHERE del_flag = 0 AND status = 'completed' AND equipment_id IS NOT NULL AND equipment_id <> '' "
            + "AND (#{factoryCode} IS NULL OR #{factoryCode} = '' OR factory_code LIKE CONCAT(#{factoryCode}, '%'))")
    int countMaintainedEquipment(@Param("factoryCode") String factoryCode);
}
