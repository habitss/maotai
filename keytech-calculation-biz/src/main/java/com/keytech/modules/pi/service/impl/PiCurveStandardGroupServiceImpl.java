package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiCurveStandardConfig;
import com.keytech.modules.pi.entity.PiCurveStandardGroup;
import com.keytech.modules.pi.mapper.PiCurveStandardGroupMapper;
import com.keytech.modules.pi.service.IPiCurveStandardConfigService;
import com.keytech.modules.pi.service.IPiCurveStandardGroupService;
import com.keytech.modules.pi.service.IPiCurveStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;

/**
 * @Description: 曲线标准-设备分组
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
@Service
public class PiCurveStandardGroupServiceImpl extends BaseServiceImpl<PiCurveStandardGroupMapper, PiCurveStandardGroup> implements IPiCurveStandardGroupService {

    @Autowired
    private IPiCurveStandardConfigService piCurveStandardConfigService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void groupDelete(List<String> list) {
        //1.查询分组关联的标准配置
        List<PiCurveStandardConfig> configList = piCurveStandardConfigService.list(
                new LambdaQueryWrapper<PiCurveStandardConfig>()
                        .in(PiCurveStandardConfig::getGroupId, list)
        );

        //2.逐个删除标准配置(同时删除关联的拆分数据)
        for (PiCurveStandardConfig config : configList) {
            piCurveStandardConfigService.configDelete(config.getId());
        }

        //3.删除分组
        removeByIds(list);
    }
}
