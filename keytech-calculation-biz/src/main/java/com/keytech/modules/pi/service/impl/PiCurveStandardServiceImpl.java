package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiCurveStandardMapper;
import com.keytech.modules.pi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 曲线标准配置
 * @Author: jeecg-boot
 * @Date:   2026-01-22
 * @Version: V1.0
 */
@Service
public class PiCurveStandardServiceImpl extends BaseServiceImpl<PiCurveStandardMapper, PiCurveStandard> implements IPiCurveStandardService {

    @Autowired
    private IPiAdapterUnitService iPiAdapterUnitService;
    @Autowired
    private IPiRaUnitService iPiRaUnitService;
    @Autowired
    private IPiSistarRtStepsService iPiSistarRtStepsService;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private IPiRaStepService iPiRaStepService;
    @Autowired
    private IMdmDeviceService iMdmDeviceService;

    /**
     * 获取设备
     * @param piCurveStandard
     * @return
     */
    @Override
    public Object getDevice(PiCurveStandard piCurveStandard) {
        //判断adapter 还是 ra
        if(piCurveStandard.getDataSource() == PiCommonConstants.ONE){
            // adapter
            return iPiAdapterUnitService.getUnitByPcuNo(piCurveStandard);
        }else{
            // ra
            return iPiRaUnitService.getUnitByAreaId(piCurveStandard.getAreaNo());
        }
        //return null;
    }
    /**
     * 获取配方
     * @param piCurveStandard
     * @return
     */
    @Override
    public Object getRecipe(PiCurveStandard piCurveStandard) {
        if(piCurveStandard.getDataSource() == PiCommonConstants.ONE){
            // adapter
            return iPiSistarRtStepsService.getRecipeByPcuNo(piCurveStandard);
        }else{
            // ra
            return iPiRaStepService.getRecipeByAreaId(piCurveStandard.getAreaNo(), piCurveStandard.getUnitNo());
        }
        //return null;
    }

    /**
     * 获取步骤
     * @param piCurveStandard
     * @return
     */
    @Override
    public Object getStep(PiCurveStandard piCurveStandard) {
        if(piCurveStandard.getDataSource() == PiCommonConstants.ONE){
            // adapter
            return iPiSistarRtStepsService.getStepByPcuNo(piCurveStandard.getAreaNo(), piCurveStandard.getUnitNo(), piCurveStandard.getRecipeNo(), piCurveStandard.getReccatNo());
        }else{
            // ra
            return iPiRaStepService.getStepByAreaId(piCurveStandard.getAreaNo(), piCurveStandard.getUnitNo(), piCurveStandard.getRecipeNo());
        }
        //return null;
    }

    @Override
    public void convert(IPage<PiCurveStandard> pageList) {
        //获取区域字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of("MES3_PI_CURVE_AREA")
        );
        Map<String, String> dictMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getItemVal(),
                        item -> item.getItemKey()
                ));

        for (PiCurveStandard curveStandard : pageList.getRecords()) {
            if(dictMap != null){
                curveStandard.setAreaName(dictMap.get(curveStandard.getAreaNo()));
            }

        }

    }
    /**
     * 获取曲线标准
     * @param stepEopGroup
     * @param pointList
     * @return
     */
    @Override
    public List<PiCurveStandard> getCurveStandard(Map<String, List<PiSistarRtSteps>> stepEopGroup, List<PiBasePoint> pointList) {
        LambdaQueryWrapper<PiCurveStandard> queryWrapper = new LambdaQueryWrapper<>();
        //遍历stepEopGroup
        queryWrapper.and(main -> {
            for (Map.Entry<String, List<PiSistarRtSteps>> entry : stepEopGroup.entrySet()) {
                String key = entry.getKey();
                String[] unitIdArr = key.split("_");
                main.or(group -> {
                    for (String unitIdStr : unitIdArr) {
                        String[] parts = unitIdStr.split("-");
                        group.or(w -> w.eq(PiCurveStandard::getAreaNo, parts[0])
                            .eq(PiCurveStandard::getUnitNo, parts[1])
                            .eq(PiCurveStandard::getRecipeNo, parts[2])
                            .eq(PiCurveStandard::getEopNo, parts[3])
                            .eq(PiCurveStandard::getRopId, parts[4]));
                    }
                });
            }
        });
        // 点位
//        queryWrapper.and(wr -> {
//            for (PiBasePoint point : pointList) {
//                wr.or(w -> w.eq(PiCurveStandard::getPointCode, point.getPointCode()));
//            }
//        });
        //


        List<PiCurveStandard> curveStandardList = list(queryWrapper);


        if(curveStandardList != null && !curveStandardList.isEmpty()){
            //得到设备code和点位code map集合 key为设备code, value为点位code
            List<Integer> deviceIds = pointList.stream()
                    .map(PiBasePoint::getDeviceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            //获取设备
            Map<Integer, String> deviceIdToCodeMap = new HashMap<>();
            if (deviceIds != null && !deviceIds.isEmpty()) {
                LambdaQueryWrapper<MdmDevice> deviceWrapper = new LambdaQueryWrapper<>();
                deviceWrapper.in(MdmDevice::getId, deviceIds);
                List<MdmDevice> deviceList = iMdmDeviceService.list(deviceWrapper);
                deviceIdToCodeMap = deviceList.stream()
                        .collect(Collectors.toMap(
                                MdmDevice::getId,
                                MdmDevice::getDeviceCode,
                                (existing, replacement) -> existing
                        ));
            }
            //设备-点位对照
            Map<String, String> devicePointMap = new HashMap<>();
            for (PiBasePoint point : pointList) {
                if (point.getDeviceId() != null && point.getPointCode() != null) {
                    String deviceCode = deviceIdToCodeMap.get(point.getDeviceId());
                    if (deviceCode != null) {
                        // 设备编码去掉区域 如2-28-7  区域只保留 28-7
                        int firstHyphen = deviceCode.indexOf("-");
                        if (firstHyphen > 0) {
                            deviceCode = deviceCode.substring(firstHyphen + 1);
                        }
                        devicePointMap.putIfAbsent(deviceCode, point.getPointCode());
                    }
                }
            }

            // 排除list中设备编码在devicePointMap中匹配不上的, 匹配上的把pointCode同步上
            curveStandardList = curveStandardList.stream()
                    .filter(standard -> StrUtil.isNotBlank(standard.getUnitNo()) &&
                            StrUtil.isNotBlank(standard.getAreaNo()) && devicePointMap.containsKey(standard.getUnitNo()+"-"+standard.getAreaNo()))
                    .peek(standard -> standard.setPointCode(devicePointMap.get(standard.getUnitNo()+"-"+standard.getAreaNo())))
                    .collect(Collectors.toList());
        }

        return curveStandardList;
    }

    @Override
    public void recipeConvert(IPage<PiCurveStandardRecipe> pageList) {
        //获取区域字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of("MES3_PI_CURVE_AREA")
        );
        Map<String, String> dictMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getItemVal(),
                        item -> item.getItemKey()
                ));

        for (PiCurveStandardRecipe curveStandardRecipe : pageList.getRecords()) {
            if(dictMap != null){
                curveStandardRecipe.setAreaName(dictMap.get(curveStandardRecipe.getAreaNo()));
            }

        }
    }


    /**
     * 获取曲线标准
     * @param stepEopGroup
     * @param pointList
     * @return
     */
    @Override
    public List<PiCurveStandard> getCurveStandardByRa(Map<String, List<PiRaStep>> stepEopGroup, List<PiBasePoint> pointList) {
        LambdaQueryWrapper<PiCurveStandard> queryWrapper = new LambdaQueryWrapper<>();
        //遍历stepEopGroup
        queryWrapper.and(main -> {
            for (Map.Entry<String, List<PiRaStep>> entry : stepEopGroup.entrySet()) {
                String key = entry.getKey();
                String[] unitIdArr = key.split("_");
                main.or(group -> {
                    for (String unitIdStr : unitIdArr) {
                        String[] parts = unitIdStr.split("-");
                        group.or(w -> w.eq(PiCurveStandard::getAreaNo, parts[0])
                                .eq(PiCurveStandard::getUnitNo, parts[1])
                                .eq(PiCurveStandard::getRecipeNo, parts[2])
                                //.eq(PiCurveStandard::getEopNo, parts[3])
                                //.eq(PiCurveStandard::getRopId, parts[4])
                        );
                    }
                });
            }
        });
        // 点位
//        queryWrapper.and(wr -> {
//            for (PiBasePoint point : pointList) {
//                wr.or(w -> w.eq(PiCurveStandard::getPointCode, point.getPointCode()));
//            }
//        });
        // 排序
        queryWrapper.orderByAsc(PiCurveStandard::getSort);
        List<PiCurveStandard> curveStandardList = list(queryWrapper);


        if(curveStandardList != null && !curveStandardList.isEmpty()){
            //得到设备code和点位code map集合 key为设备code, value为点位code
            List<Integer> deviceIds = pointList.stream()
                    .map(PiBasePoint::getDeviceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            //获取设备
            Map<Integer, String> deviceIdToCodeMap = new HashMap<>();
            if (deviceIds != null && !deviceIds.isEmpty()) {
                LambdaQueryWrapper<MdmDevice> deviceWrapper = new LambdaQueryWrapper<>();
                deviceWrapper.in(MdmDevice::getId, deviceIds);
                List<MdmDevice> deviceList = iMdmDeviceService.list(deviceWrapper);
                deviceIdToCodeMap = deviceList.stream()
                        .collect(Collectors.toMap(
                                MdmDevice::getId,
                                MdmDevice::getDeviceCode,
                                (existing, replacement) -> existing
                        ));
            }
            //设备-点位对照
            Map<String, String> devicePointMap = new HashMap<>();
            for (PiBasePoint point : pointList) {
                if (point.getDeviceId() != null && point.getPointCode() != null) {
                    String deviceCode = deviceIdToCodeMap.get(point.getDeviceId());
                    if (deviceCode != null) {
                        // 设备编码去掉区域 如2-28-7  区域只保留 28-7
//                        int firstHyphen = deviceCode.indexOf("-");
//                        if (firstHyphen > 0) {
//                            deviceCode = deviceCode.substring(firstHyphen + 1);
//                        }
                        devicePointMap.putIfAbsent(deviceCode, point.getPointCode());
                    }
                }
            }

            // 排除list中设备编码在devicePointMap中匹配不上的, 匹配上的把pointCode同步上
            curveStandardList = curveStandardList.stream()
                    .filter(standard -> StrUtil.isNotBlank(standard.getUnitNo()) &&
                            StrUtil.isNotBlank(standard.getAreaNo()) && devicePointMap.containsKey(standard.getAreaNo()+"-"+standard.getUnitNo()))
                    .peek(standard -> standard.setPointCode(devicePointMap.get(standard.getAreaNo()+"-"+standard.getUnitNo())))
                    .collect(Collectors.toList());
        }

        return curveStandardList;
    }

}
