package com.keytech.modules.maotai.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.workorder.entity.TransferRoute;
import com.keytech.modules.maotai.workorder.mapper.TransferRouteMapper;
import com.keytech.modules.maotai.workorder.service.TransferRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 转酒路径配置服务实现
 *
 * @author workorder
 * @since 2026-09-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransferRouteServiceImpl extends BaseServiceImpl<TransferRouteMapper, TransferRoute> implements TransferRouteService {

    private static final String PROCESS_UNLOAD = "卸车工艺";
    private static final String PROCESS_LOAD = "装车工艺";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferRoute saveOrUpdateRoute(TransferRoute route, String operator) {
        validate(route);
        Date now = new Date();
        if (route.getId() == null || route.getId().isEmpty()) {
            // DataBase 审计列为 java.util.Date 且 setter 非链式,逐个赋值
            route.setDelFlag(0);
            route.setCreateTime(now);
            route.setCreateBy(operator);
            this.save(route);
        } else {
            TransferRoute db = this.getById(route.getId());
            if (db == null || db.getDelFlag() != 0) {
                throw new IllegalArgumentException("路径配置不存在");
            }
            db.setWorkshopCode(route.getWorkshopCode()).setProcessType(route.getProcessType())
                    .setSourceTank(route.getSourceTank()).setTargetTank(route.getTargetTank())
                    .setSkidCode(route.getSkidCode()).setRouteCode(route.getRouteCode())
                    .setRouteName(route.getRouteName()).setEnabled(route.getEnabled() == null ? 1 : route.getEnabled())
                    .setSortNo(route.getSortNo()).setRemark(route.getRemark());
            db.setUpdateTime(now);
            db.setUpdateBy(operator);
            this.updateById(db);
        }
        return route;
    }

    private void validate(TransferRoute route) {
        if (isEmpty(route.getWorkshopCode()) || isEmpty(route.getProcessType())) {
            throw new IllegalArgumentException("请选择车间和工艺");
        }
        if (isEmpty(route.getRouteCode()) || isEmpty(route.getRouteName())) {
            throw new IllegalArgumentException("请填写路径编码和路径名称");
        }
        boolean unload = PROCESS_UNLOAD.equals(route.getProcessType());
        boolean load = PROCESS_LOAD.equals(route.getProcessType());
        if (unload) {
            if (isEmpty(route.getSkidCode()) || isEmpty(route.getTargetTank())) {
                throw new IllegalArgumentException("卸车工艺需配置卸车撬和目标罐");
            }
            route.setSourceTank(null);
        } else if (load) {
            if (isEmpty(route.getSkidCode()) || isEmpty(route.getSourceTank())) {
                throw new IllegalArgumentException("装车工艺需配置源罐和卸车撬");
            }
            route.setTargetTank(null);
        } else {
            if (isEmpty(route.getSourceTank()) || isEmpty(route.getTargetTank())) {
                throw new IllegalArgumentException("该工艺需配置源罐和目标罐");
            }
            route.setSkidCode(null);
        }
        if (route.getEnabled() == null) {
            route.setEnabled(1);
        }
        // route_code 同车间同工艺唯一
        QueryWrapper<TransferRoute> dup = new QueryWrapper<TransferRoute>()
                .eq("workshop_code", route.getWorkshopCode())
                .eq("process_type", route.getProcessType())
                .eq("route_code", route.getRouteCode())
                .eq("del_flag", 0);
        if (route.getId() != null && !route.getId().isEmpty()) {
            dup.ne("id", route.getId());
        }
        if (this.count(dup) > 0) {
            throw new IllegalArgumentException("路径编码已存在: " + route.getRouteCode());
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
