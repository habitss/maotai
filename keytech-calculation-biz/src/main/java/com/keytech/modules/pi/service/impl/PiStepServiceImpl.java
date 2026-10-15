package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.entity.PiStep;
import com.keytech.modules.pi.mapper.PiStepMapper;
import com.keytech.modules.pi.service.IPiBasePointService;
import com.keytech.modules.pi.service.IPiStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 步骤管理
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Service
public class PiStepServiceImpl extends BaseServiceImpl<PiStepMapper, PiStep> implements IPiStepService {

    @Autowired
    private IPiBasePointService piBasePointService;


    @Override
    public List<PiStep> getPointStep(String pointCode) {
        List<PiStep> piStepList = new ArrayList<>();
        if(StrUtil.isNotBlank(pointCode)){
            //点位获取设备
            LambdaQueryWrapper<PiBasePoint> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PiBasePoint::getPointCode,pointCode);
            List<PiBasePoint> piBasePointList = piBasePointService.list(queryWrapper);
            //设备获取步骤
            if(null != piBasePointList && piBasePointList.size()>0 && null != piBasePointList.get(0).getDeviceId()
                    && StrUtil.isNotBlank(piBasePointList.get(0).getDeviceId()+"")){
                LambdaQueryWrapper<PiStep> stepQueryWrapper = new LambdaQueryWrapper<>();
                stepQueryWrapper.eq(PiStep::getDeviceId,piBasePointList.get(0).getDeviceId());
                piStepList = list(stepQueryWrapper);
            }
        }
        return piStepList;
    }
}
