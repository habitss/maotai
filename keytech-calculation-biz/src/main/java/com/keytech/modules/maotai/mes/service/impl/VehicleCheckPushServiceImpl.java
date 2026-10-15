package com.keytech.modules.maotai.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.maotai.mes.client.MesClient;
import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.VehicleCheckPushRequest;
import com.keytech.modules.maotai.mes.service.VehicleCheckPushService;
import com.keytech.modules.maotai.eh.vehiclecheck.entity.VehicleCheck;
import com.keytech.modules.maotai.eh.vehiclecheck.service.VehicleCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 车检信息推送实现(API09):按 checkNo 从 mes3_maotai_vehicle_check 取数组装,推勾储MES。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Service
@RequiredArgsConstructor
public class VehicleCheckPushServiceImpl implements VehicleCheckPushService {

    private final MesClient mesClient;
    private final VehicleCheckService vehicleCheckService;

    @Override
    public MesResponse pushVehicleCheck(String checkNo, String rqi) {
        VehicleCheck vc = vehicleCheckService.getOne(new QueryWrapper<VehicleCheck>()
                .eq("check_no", checkNo).last("LIMIT 1"));
        if (vc == null) {
            return MesResponse.fail(rqi, "车检记录不存在: " + checkNo);
        }
        VehicleCheckPushRequest req = new VehicleCheckPushRequest();
        req.setRqi(rqi);
        req.setCheckNo(vc.getCheckNo());
        req.setCarNo(vc.getCarNo());
        req.setIcCardNo(vc.getIcCardNo());
        req.setCheckType(vc.getCheckType());
        req.setCheckTime(vc.getCheckTime());
        return mesClient.vehicleCheckPush(req);
    }
}
