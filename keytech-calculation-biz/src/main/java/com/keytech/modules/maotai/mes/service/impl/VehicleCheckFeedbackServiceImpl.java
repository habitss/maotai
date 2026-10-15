package com.keytech.modules.maotai.mes.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.VehicleCheckFeedbackRequest;
import com.keytech.modules.maotai.mes.entity.VehicleCheckFeedback;
import com.keytech.modules.maotai.mes.mapper.VehicleCheckFeedbackMapper;
import com.keytech.modules.maotai.mes.service.VehicleCheckFeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 车检反馈实现(API10):落库通知记录 + 回公共响应,rqi 幂等(uk_rqi)。
 * <p>只落通知,不联动 vehicle_check——MES 结果与 E+H 结果是两个来源,vehicle_check 也无
 * passFlag/passReason/allowWeight 列;如需联动后续再加列/映射。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Service
@Slf4j
public class VehicleCheckFeedbackServiceImpl extends BaseServiceImpl<VehicleCheckFeedbackMapper, VehicleCheckFeedback> implements VehicleCheckFeedbackService {

    @Override
    public MesResponse receive(VehicleCheckFeedbackRequest dto) {
        VehicleCheckFeedback rec = new VehicleCheckFeedback();
        BeanUtils.copyProperties(dto, rec);
        try {
            this.save(rec);
        } catch (DuplicateKeyException e) {
            log.info("[API10] 重复请求已忽略 rqi={}", dto.getRqi());
            return MesResponse.ok(dto.getRqi(), "重复请求已忽略");
        } catch (Exception e) {
            log.error("[API10] 落库失败 rqi={} checkNo={}", dto.getRqi(), dto.getCheckNo(), e);
            return MesResponse.fail(dto.getRqi(), "落库失败: " + e.getMessage());
        }
        return MesResponse.ok(dto.getRqi(),
                "已落库车检反馈 checkNo=" + dto.getCheckNo() + " passFlag=" + dto.getPassFlag());
    }
}
