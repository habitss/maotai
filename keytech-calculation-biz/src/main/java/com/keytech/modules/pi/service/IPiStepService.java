package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiStep;

import java.util.List;

/**
 * @Description: 步骤管理
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface IPiStepService extends BaseService<PiStep> {

    //获取点位对应的步骤
    List<PiStep> getPointStep(String pointCode);
}
