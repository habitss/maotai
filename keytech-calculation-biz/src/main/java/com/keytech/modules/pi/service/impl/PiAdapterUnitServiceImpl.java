package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.entity.PiAdapterUnit;
import com.keytech.modules.pi.entity.PiCurveStandard;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.keytech.modules.pi.mapper.PiAdapterUnitMapper;
import com.keytech.modules.pi.service.IPiAdapterUnitService;
import com.keytech.modules.pi.service.IPiSistarRtStepsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: Adapter-设备
 * @Author: jeecg-boot
 * @Date:   2025-09-03
 * @Version: V1.0
 */
@Service
public class PiAdapterUnitServiceImpl extends BaseServiceImpl<PiAdapterUnitMapper, PiAdapterUnit> implements IPiAdapterUnitService {

    @Autowired
    private IMdmDeviceService iMdmDeviceService;
    @Autowired
    private IPiSistarRtStepsService piSistarRtStepsService;

    @Override
    public List<?> getUnitByPcuNo(PiCurveStandard piCurveStandard) {
        //获取设备

        LambdaQueryWrapper<MdmDevice> queryWrapper = new LambdaQueryWrapper<>();
        String areaNo = piCurveStandard.getAreaNo();
        String recipeNo = piCurveStandard.getRecipeNo();
        String recipeName = piCurveStandard.getRecipeName();
        String reccatNo = piCurveStandard.getReccatNo();
        if (areaNo.contains(",")) {
            // pcu_no 查询  and(a or b)
            String[] pcuNoArr = areaNo.split(",");
            queryWrapper.and(wrapper -> {
                Arrays.stream(pcuNoArr)
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .forEach(code -> wrapper.or().likeRight(MdmDevice::getDeviceCode, code + "-"));
            });
        } else {
            //pcu
            queryWrapper.likeLeft(MdmDevice::getDeviceCode,  "-"+areaNo);
        }
        List<MdmDevice> unitList = iMdmDeviceService.list(queryWrapper);

        if(StrUtil.isNotBlank(recipeNo)){
            // 获取 区域和配方对应的 设备集合
            List<PiSistarRtSteps> stepsList = piSistarRtStepsService.getStepsUnitByAreaNo(areaNo, recipeNo, recipeName, reccatNo);
            //stepsList 得到 areaNo-unitNo-pcuNo 集合
            Set<String> distinctSet = stepsList.stream()
                    .map(item -> item.getAreaNo() + "-" + item.getUnitNo() + "-" + item.getPcuNo())
                    .collect(Collectors.toSet());
            //过滤 unitList
            unitList = unitList.stream()
                    .filter(item -> distinctSet.contains(item.getDeviceCode()))
                    .collect(Collectors.toList());
        }

        return unitList;

    }
}
