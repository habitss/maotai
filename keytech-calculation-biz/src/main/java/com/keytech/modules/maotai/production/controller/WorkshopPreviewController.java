package com.keytech.modules.maotai.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.mapper.EquipmentLedgerMapper;
import com.keytech.modules.maotai.tank.entity.TankConfig;
import com.keytech.modules.maotai.tank.mapper.TankConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车间预览(罐区/位置来自原型 workshops.json)
 * <p>
 * 罐体展示信息以设备台账为唯一事实源(2026-09-10 起联动):
 * JSON 骨架中每个罐的 title 在返回前按 funloc_code 覆盖为台账 equip_name,
 * 并注入 funlocCode 字段——台账改名后车间预览/驾驶舱罐名同源跟随;
 * 无台账记录的罐保留 JSON 原名。
 *
 * @author maotai
 * @since 2026-06-15
 */
@Tag(name = "车间预览")
@RestController
@RequestMapping("/maotai/workshopPreview")
@Slf4j
@RequiredArgsConstructor
public class WorkshopPreviewController {

    private final TankConfigMapper tankConfigMapper;
    private final EquipmentLedgerMapper equipmentLedgerMapper;

    private List<Object> workshopsCache;

    /** 从 classpath 读取原型 workshops.json(4 车间完整数据) */
    private List<Object> loadWorkshops() {
        if (workshopsCache != null) return workshopsCache;
        try {
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream is = getClass().getResourceAsStream("/workshops.json")) {
                if (is == null) {
                    log.error("workshops.json 未在 classpath 找到");
                    return new ArrayList<>();
                }
                workshopsCache = mapper.readValue(is, new TypeReference<List<Object>>() {});
            }
        } catch (Exception e) {
            log.error("加载 workshops.json 失败", e);
            return new ArrayList<>();
        }
        return workshopsCache;
    }

    /** 4 车间完整数据(罐名/位置编码按台账覆盖) */
    @Operation(summary = "车间预览-车间列表", description = "4 车间完整数据,罐名/位置编码以设备台账为准")
    @GetMapping("/list")
    public Result<List<Object>> list() {
        return Result.OK(applyLedgerNames(loadWorkshops()));
    }

    /** 单个车间数据(罐名/位置编码按台账覆盖) */
    @Operation(summary = "车间预览-单车间数据", description = "单车间数据,罐名/位置编码以设备台账为准")
    @GetMapping("/data")
    public Result<Object> data(@RequestParam(name = "workshopIndex", defaultValue = "0") Integer workshopIndex) {
        List<Object> list = applyLedgerNames(loadWorkshops());
        int idx = workshopIndex == null || workshopIndex < 0 || workshopIndex >= list.size() ? 0 : workshopIndex;
        return Result.OK(list.get(idx));
    }

    /**
     * 深度遍历 JSON,把含 title 的罐对象覆盖为台账名并注入 funlocCode。
     * <p>链:json.title ↔ tank_config.tank_id → funloc_code → 台账.equip_name。
     * 台账查询失败时静默返回原 JSON(降级不阻塞页面)。
     */
    @SuppressWarnings("unchecked")
    private List<Object> applyLedgerNames(List<Object> workshops) {
        try {
            // 关联链:json.title ↔ tank_config.tank_id → tank_config.id →(稳定键)→ 台账行
            Map<String, TankConfig> tankByTankId = new HashMap<>();
            for (TankConfig cfg : tankConfigMapper.selectList(new LambdaQueryWrapper<TankConfig>()
                    .isNotNull(TankConfig::getTankId))) {
                tankByTankId.put(cfg.getTankId(), cfg);
            }
            Map<String, EquipmentLedger> ledgerByTankConfigId = new HashMap<>();
            for (EquipmentLedger row : equipmentLedgerMapper.selectList(
                    new LambdaQueryWrapper<EquipmentLedger>().isNotNull(EquipmentLedger::getTankConfigId))) {
                ledgerByTankConfigId.put(row.getTankConfigId(), row);
            }
            if (tankByTankId.isEmpty() || ledgerByTankConfigId.isEmpty()) {
                return workshops;
            }
            int renamed = overlayTankTitles(workshops, tankByTankId, ledgerByTankConfigId);
            log.debug("[车间预览] 台账罐名/位置编码覆盖 {} 个", renamed);
        } catch (Exception e) {
            log.warn("[车间预览] 台账罐名联动失败,返回原 JSON: {}", e.getMessage());
        }
        return workshops;
    }

    /** 递归遍历 List/Map,对含 title 键的 Map 执行覆盖,返回覆盖数量 */
    @SuppressWarnings("unchecked")
    private int overlayTankTitles(Object node, Map<String, TankConfig> tankByTankId, Map<String, EquipmentLedger> ledgerByTankConfigId) {
        int count = 0;
        if (node instanceof List) {
            for (Object item : (List<Object>) node) {
                count += overlayTankTitles(item, tankByTankId, ledgerByTankConfigId);
            }
        } else if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;
            // 已注入过 tankConfigId 的罐(缓存被上次覆盖过)按稳定键找台账——台账改编码/改名持续跟随
            Object existingTankConfigId = map.get("tankConfigId");
            EquipmentLedger row = null;
            if (existingTankConfigId instanceof String) {
                row = ledgerByTankConfigId.get(existingTankConfigId);
            } else {
                Object title = map.get("title");
                if (title instanceof String) {
                    TankConfig cfg = tankByTankId.get(title);
                    if (cfg != null) {
                        row = ledgerByTankConfigId.get(cfg.getId());
                        if (row != null) {
                            map.put("tankConfigId", cfg.getId());
                        }
                    }
                }
            }
            if (row != null) {
                boolean touched = false;
                if (row.getEquipName() != null && !row.getEquipName().isEmpty() && !row.getEquipName().equals(map.get("title"))) {
                    map.put("title", row.getEquipName());
                    touched = true;
                }
                if (row.getFunlocCode() != null && !row.getFunlocCode().isEmpty() && !row.getFunlocCode().equals(map.get("funlocCode"))) {
                    map.put("funlocCode", row.getFunlocCode());
                    touched = true;
                }
                if (touched) {
                    count++;
                }
            }
            for (Object value : map.values()) {
                count += overlayTankTitles(value, tankByTankId, ledgerByTankConfigId);
            }
        }
        return count;
    }
}
