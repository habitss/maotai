package com.keytech.modules.maotai.workorder.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.maotai.workorder.entity.TransferRoute;

/**
 * 转酒路径配置服务
 *
 * @author workorder
 * @since 2026-09-15
 */
public interface TransferRouteService extends BaseService<TransferRoute> {

    /**
     * 新增/编辑统一校验+保存。
     * 卸车=撬+目标罐必填源罐空;装车=撬+源罐必填目标罐空;倒罐/灌装=双罐必填撬空。
     * route_code 同车间同工艺内唯一(启用与停用均占号,防切换启用后撞唯一索引)。
     */
    TransferRoute saveOrUpdateRoute(TransferRoute route, String operator);
}
