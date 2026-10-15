package com.keytech.modules.maotai.mes.service;

import com.keytech.modules.maotai.mes.dto.MesResponse;

/**
 * 工单查询服务(API02: 平台 → 勾储MES /GZMES/workOrders/list)。
 * empNo 动态传参(查哪个员工的工单);响应解析 services[].properties.workOrders[] 落库,
 * 同工单号重复校验(一致幂等/不一致记异常表+返回异常+不落库)。
 *
 * @author maotai
 * @since 2026-08-04
 */
public interface WorkOrderListQueryService {

    /**
     * 工单查询(API02):按员工工号查勾储MES,响应工单解析落库 + 重复校验。
     * @param rqi   请求流水Id,空则自动生成
     * @param empNo 员工工号(动态查询条件,必填)
     * @return MesResponse(resultCode 1=成功;rqi 回传;resultDesp 含落库/不一致汇总)
     */
    MesResponse queryWorkOrderList(String rqi, String empNo);
}
