package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.dto.RaAreaDto;
import com.keytech.modules.pi.entity.PiAdapterUnit;
import com.keytech.modules.pi.entity.PiCurveStandard;
import com.keytech.modules.pi.entity.PiRaUnit;
import com.keytech.modules.pi.mapper.PiRaUnitMapper;
import com.keytech.modules.pi.service.IPiRaBopService;
import com.keytech.modules.pi.service.IPiRaProcedureService;
import com.keytech.modules.pi.service.IPiRaStepService;
import com.keytech.modules.pi.service.IPiRaUnitService;
import com.keytech.modules.pi.vo.RaLineVo;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: RA-设备字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiRaUnitServiceImpl extends BaseServiceImpl<PiRaUnitMapper, PiRaUnit> implements IPiRaUnitService {
    private static final Logger log = LoggerFactory.getLogger(PiRaUnitServiceImpl.class);
    @Autowired
    private IMdmDeviceService iMdmDeviceService;
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IPiRaBopService iPiRaBopService;
    @Autowired
    private IPiRaProcedureService iPiRaProcedureService;
    @Autowired
    private IPiRaUnitService iPiRaUnitService;
    @Autowired
    private IPiRaStepService iPiRaStepService;

    /**
     * RA抽取数据
     */
    @Override
    public void extractData() {
        // 抽取设备字典
        deviceExtract();
        // 抽取步骤字典
        iPiRaBopService.stepExtract();
        // 抽取配方
        iPiRaProcedureService.recipeExtract();
    }

    /**
     * 设备字典数据抽取
     */
    private void deviceExtract() {
        // 抽取设备字典
        log.info("开始RA设备分页增量同步");
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;
        //获取区域
        List<RaAreaDto> areas = iPiRaStepService.getArea();
        //遍历区域 uid 与 areaId对照
        Map<Integer, RaAreaDto> areaMap = areas.stream()
                .collect(Collectors.toMap(
                        area -> area.getUid(),
                        area -> area
                ));

        //获取产线
        List<RaLineVo> lines = iPiRaStepService.getLine(null);
        //遍历产线 得到map key为 areaId_1 value为RaLineVO
        Map<String, RaLineVo> lineMap = lines.stream()
            .collect(Collectors.toMap(
                    line -> line.getAreaId() + "_" + getLineNumber(line.getNameCn()),
                    line -> line
            ));

        while (hasMoreData) {
            long pageSize = PiSistarRtStepsServiceImpl.BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<PiRaUnit> dataList = iPiRaUnitService.deviceDataExtract(offset, pageSize);
            if (dataList.isEmpty()) {
                hasMoreData = false;
                break;
            }
            // 匹配区域/设备
            for (PiRaUnit piRaUnit : dataList) {
                try {
                    // 区域 - 增加空值检查
                    RaAreaDto areaVO = areaMap.get(piRaUnit.getAreaUid());
                    if (areaVO == null) {
                        // 记录日志或处理区域不存在的情况
                        continue; // 或者设置默认值
                    }
                    Integer areaId = areaVO.getAreaId();
                    piRaUnit.setAreaId(areaId);

                    // 设备
                    String lineKey = areaId + "_" + getLineNumberByUnitName(piRaUnit.getNameCn());
                    RaLineVo lineVO = lineMap.get(lineKey);
                    if (lineVO != null && lineVO.getId() != null) {
                        piRaUnit.setLineId(lineVO.getId());
                    } else {
                        // 处理线路不存在的情况
                        piRaUnit.setLineId(null); // 或者设置默认线路ID
                    }
                } catch (Exception e) {
                    // 根据业务需求决定是否设置默认值
                    piRaUnit.setLineId(null);
                }
            }

            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processed, totalProcessed);
            page++;
            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < pageSize) {
                hasMoreData = false;
            }
        }
        // 更新抽取记录
        if (totalProcessed > 0) {
            log.info("RA设备分页同步完成，共处理 {} 条数据", totalProcessed);
        }

    }

    /**
     * 设备字典数据抽取
     * @param offset
     * @param pageSize
     * @return
     */
    @Override
    @DS("rabasesqlservice")
    public List<PiRaUnit> deviceDataExtract(long offset, long pageSize) {
        return baseMapper.deviceDataExtract(offset,  pageSize);
    }


    /**
     * 处理数据
     */
    private int processBatchData(List<PiRaUnit> dataList) {
        log.info("RA数据设备处理1--------------{}", dataList.size());
        if (dataList.isEmpty()) {
            return 0;
        }
        // 设置租户
        dataList.forEach(data -> data.setTenantId(tenantId));

        // 批量插入
        saveOrUpdateBatch(dataList);

        return dataList.size();
    }


    // 辅助方法，根据 nameCn 返回对应的数字
    private static String getLineNumber(String nameCn) {
        if (nameCn == null) {
            return "0"; // 或者返回默认值
        }
        switch (nameCn) {
            case "一线":
                return "1";
            case "二线":
                return "2";
            default:
                return "0"; // 或者根据实际需求处理未知线路
        }
    }

    // 根据名称判断一线/二线
    private String getLineNumberByUnitName(String nameCn) {
        if (nameCn == null) {
            return "-1"; // 默认无
        }
        if (nameCn.contains("2期")) {
            return "2"; // 二线
        } else if (nameCn.contains("1期")) {
            return "1"; // 一线
        } else {
            // 其他情况默认一线，或者根据业务需求调整
            return "0";
        }
    }

    /**
     * 根据编号获取设备信息
     * @param areaId
     * @return
     */
    @Override
    public List<?> getUnitByAreaId(String areaId) {
//        LambdaQueryWrapper<PiRaUnit> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(PiRaUnit::getAreaId,areaId);
//        List<PiRaUnit> unitList = list(queryWrapper);
//
//        List<PiCurveStandard> list = new ArrayList<>();
//        for (PiRaUnit piRaUnit : unitList) {
//            PiCurveStandard piCurveStandard = new PiCurveStandard();
//            piCurveStandard.setUnitNo(String.valueOf(piRaUnit.getUnitId()));
//            piCurveStandard.setUnitName(piRaUnit.getNameCn());
//            list.add(piCurveStandard);
//        }
        LambdaQueryWrapper<MdmDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MdmDevice::getDeviceCode, "-");
        if (areaId.contains(",")) {
            // pcu_no 查询  and(a or b)
            String[] pcuNoArr = areaId.split(",");
            queryWrapper.and(wrapper -> {
                Arrays.stream(pcuNoArr)
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .forEach(code -> wrapper.or().likeLeft(MdmDevice::getDeviceCode, "-" + code));
            });
        } else {
            queryWrapper.like(MdmDevice::getDeviceCode,  areaId+"-");
        }
        List<MdmDevice> unitList = iMdmDeviceService.list(queryWrapper);
        return unitList;
    }
}
