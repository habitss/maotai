package com.keytech.modules.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.cop.service.*;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportTargetValueService;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.entity.MdmPost;
import com.keytech.modules.mdm3.entity.MdmShiftType;
import com.keytech.modules.mdm3.service.*;
import com.keytech.modules.opl.service.IOplHomePageService;
import com.keytech.modules.opl.service.IOplReviewPlanFileService;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import com.keytech.modules.period.service.IPeriodVerificationConfigService;
import com.keytech.modules.period.service.IPeriodVerificationDeviceService;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.period.service.IPeriodVerificationSubmitService;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointService;
import com.keytech.modules.pi.service.IPiBasePointService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.plan.service.*;
import com.keytech.modules.post.check.service.*;
import com.keytech.modules.sop.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Description: 组织架构对照处理
 */
@Tag(name="组织架构对照处理")
@RestController
@RequestMapping("/process/dept")
@Slf4j
public class ProcessDeptController {
    private static final List<String> DEPT_CODES = Arrays.asList(
            "A10A08A08A01A04",
            "A10A08A08A05A03",
            "A10A08A08A03A01A13",
            "A10A08A08A03A01A14",
            "A10A08A08A01A04A01",
            "A10A08A08A01A04A02",
            "A10A08A08A03A01",
            "A10A08A08A03A02",
            "A10A08A08A03A01A01",
            "A10A08A08A03A01A12",
            "A10A08A08A03A01A11",
            "A10A08A08A03A01A10",
            "A10A08A08A03A01A15",
            "A10A08A08A02A05A04",
            "A10A08A08A02A05A05",
            "A10A08A08A02A04A02",
            "A10A08A08A01A05A01",
            "A10A08A08A01A05A02",
            "A10A08A08A01A06A01",
            "A10A08A08A01A06A02",
            "A10A08A08A04A06",
            "A10A08A08A04A04",
            "A10A08A08A04A05",
            "A10A08A08A04A02",
            "A10A08A08A04A03",
            "A10A08A08A04A01",
            "A10A08A08A05A02"
    );

    private static final Map<String, String> DEPT_CODE_MAPPING = new HashMap<>() {{
        put("A10A08A08A01A04A01", "A10A08A08A01A03A08");
        put("A10A08A08A01A04A02", "A10A08A08A01A03A09");
        put("A10A08A08A01A04", "A10A08A08A01A03");
        put("A10A08A08A05A03", "A10A08A08A10A02");
        put("A10A08A08A02A05A04", "A10A08A08A02A04A08");
        put("A10A08A08A02A05A05", "A10A08A08A02A04A09");
        put("A10A08A08A02A04A02", "A10A08A08A02A05A03");
        put("A10A08A08A01A05A01", "A10A08A08A01A03A05");
        put("A10A08A08A01A05A02", "A10A08A08A01A03A06");
        put("A10A08A08A01A06A01", "A10A08A08A01A02A05");
        put("A10A08A08A01A06A02", "A10A08A08A01A02A06");
        put("A10A08A08A04A06", "A10A08A08A04A08A01");
        put("A10A08A08A04A04", "A10A08A08A04A08A02");
        put("A10A08A08A04A05", "A10A08A08A04A09A01");
        put("A10A08A08A04A02", "A10A08A08A04A09A02");
        put("A10A08A08A04A03", "A10A08A08A04A09A03");
        put("A10A08A08A04A01", "A10A08A08A04A10A01");
        put("A10A08A08A05A02", "A10A08A08A10A02");
    }};

    private static final Map<String, String> DEPT_CODE_MAPPING3 = new HashMap<>() {{
        put("A10A08A08A03A01A13", "A10A08A08A08A07A10");
        put("A10A08A08A03A01A14", "A10A08A08A08A07A11");
        put("A10A08A08A03A01", "A10A08A08A08A07");
        put("A10A08A08A03A02", "A10A08A08A08A07A01");
        put("A10A08A08A03A01A01", "A10A08A08A08A07A02");
        put("A10A08A08A03A01A12", "A10A08A08A08A07A04");
        put("A10A08A08A03A01A11", "A10A08A08A08A07A05");
        put("A10A08A08A03A01A10", "A10A08A08A08A07A06");
        put("A10A08A08A03A01A15", "A10A08A08A08A07A09");
    }};

    @Autowired
    private IMdmDeviceService mdmDeviceService;
    @Autowired
    private IMdmPostService mdmPostService;
    @Autowired
    private IMdmShiftTypeService mdmShiftTypeService;
    @Autowired
    private IMdmShiftManageService mdmShiftManageService;
    @Autowired
    private IMdmShiftGroupService mdmShiftGroupService;


    @Autowired
    private IMdmItemService mdmItemService;
    @Autowired
    private IMdmTaskService mdmTaskService;
    @Autowired
    private IMdmTaskItemService mdmTaskItemService;
    @Autowired
    private IMdmPostDeviceService mdmPostDeviceService;
    @Autowired
    private IPlanService planService;
    @Autowired
    private IPlanDeviceService planDeviceService;
    @Autowired
    private IPlanOrderService planOrderService;
    @Autowired
    private IPlanOrderResultService planOrderResultService;
    @Autowired
    private IPlanOrderResultConfigService planOrderResultConfigService;
    @Autowired
    private IPostCheckRecordConfigMainService postCheckRecordConfigMainService;
    @Autowired
    private IPostCheckRecordConfigProjectService postCheckRecordConfigProjectService;
    @Autowired
    private IPostCheckRecordConfigStandardService postCheckRecordConfigStandardService;
    @Autowired
    private IPostCheckRecordDatainfoMainService postCheckRecordDatainfoMainService;
    @Autowired
    private IPostCheckRecordDatainfoDetailService postCheckRecordDatainfoDetailService;
    @Autowired
    private ICopDeviceService copDeviceService;
    @Autowired
    private ICopConfigService copConfigService;
    @Autowired
    private ICopPointService copPointService;
    @Autowired
    private ICopScheduleService copScheduleService;
    @Autowired
    private ICopSubmitService copSubmitService;
    @Autowired
    private IPeriodVerificationDeviceService periodVerificationDeviceService;
    @Autowired
    private IPeriodVerificationConfigService periodVerificationConfigService;
    @Autowired
    private IPeriodVerificationScheduleService periodVerificationScheduleService;
    @Autowired
    private IPeriodVerificationSubmitService periodVerificationSubmitService;
    @Autowired
    private ISopHomePageService sopHomePageService;
    @Autowired
    private ISopStatisticDataService sopStatisticDataService;
    @Autowired
    private ISopDiagnoseDataService sopDiagnoseDataService;
    @Autowired
    private ISopReviewPlanFileService sopReviewPlanFileService;
    @Autowired
    private ISopReviewPlanService sopReviewPlanService;
    @Autowired
    private IOplHomePageService oplHomePageService;
    @Autowired
    private IOplReviewPlanFileService oplReviewPlanFileService;
    @Autowired
    private IOplReviewPlanService oplReviewPlanService;
    @Autowired
    private IPiBasePointService piBasePointService;
    @Autowired
    private IPiCustomizeTableService piCustomizeTableService;
    @Autowired
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;
    @Autowired
    private IPiAbnormalAnalysisPointService piAbnormalAnalysisPointService;
    @Autowired
    private IKpiReportTargetValueService targetValueService;
    @Autowired
    private IKpiInfoService kpiInfoService;


    @RequestMapping(value = "/processDept", method = RequestMethod.POST)
    public Result<?> processDept(@RequestParam(name="type") String type) {
        Map<String, Object[]> serviceMap = new HashMap<>();
        serviceMap.put("kpiInfoService", new Object[]{kpiInfoService, kpiInfoService.getEntityClass()});
//        serviceMap.put("piAbnormalAnalysisPointService", new Object[]{piAbnormalAnalysisPointService, piAbnormalAnalysisPointService.getEntityClass()});
//        serviceMap.put("piBasePointService", new Object[]{piBasePointService, piBasePointService.getEntityClass()});
//        serviceMap.put("piCustomizeTableService", new Object[]{piCustomizeTableService, piCustomizeTableService.getEntityClass()});
//        serviceMap.put("piCustomizeTableFieldService", new Object[]{piCustomizeTableFieldService, piCustomizeTableFieldService.getEntityClass()});
//        serviceMap.put("oplHomePageService", new Object[]{oplHomePageService, oplHomePageService.getEntityClass()});
//        serviceMap.put("oplReviewPlanFileService", new Object[]{oplReviewPlanFileService, oplReviewPlanFileService.getEntityClass()});
//        serviceMap.put("oplReviewPlanService", new Object[]{oplReviewPlanService, oplReviewPlanService.getEntityClass()});
//        serviceMap.put("sopHomePageService", new Object[]{sopHomePageService, sopHomePageService.getEntityClass()});
//        serviceMap.put("sopStatisticDataService", new Object[]{sopStatisticDataService, sopStatisticDataService.getEntityClass()});
//        serviceMap.put("sopDiagnoseDataService", new Object[]{sopDiagnoseDataService, sopDiagnoseDataService.getEntityClass()});
//        serviceMap.put("sopReviewPlanFileService", new Object[]{sopReviewPlanFileService, sopReviewPlanFileService.getEntityClass()});
//        serviceMap.put("sopReviewPlanService", new Object[]{sopReviewPlanService, sopReviewPlanService.getEntityClass()});
//        serviceMap.put("periodVerificationDeviceService", new Object[]{periodVerificationDeviceService, periodVerificationDeviceService.getEntityClass()});
//        serviceMap.put("periodVerificationConfigService", new Object[]{periodVerificationConfigService, periodVerificationConfigService.getEntityClass()});
//        serviceMap.put("periodVerificationScheduleService", new Object[]{periodVerificationScheduleService, periodVerificationScheduleService.getEntityClass()});
//        serviceMap.put("periodVerificationSubmitService", new Object[]{periodVerificationSubmitService, periodVerificationSubmitService.getEntityClass()});
//        serviceMap.put("copSubmitService", new Object[]{copSubmitService, copSubmitService.getEntityClass()});
//        serviceMap.put("copDeviceService", new Object[]{copDeviceService, copDeviceService.getEntityClass()});
//        serviceMap.put("copConfigService", new Object[]{copConfigService, copConfigService.getEntityClass()});
//        serviceMap.put("copPointService", new Object[]{copPointService, copPointService.getEntityClass()});
//        serviceMap.put("copScheduleService", new Object[]{copScheduleService, copScheduleService.getEntityClass()});
//        serviceMap.put("postCheckRecordConfigMainService", new Object[]{postCheckRecordConfigMainService, postCheckRecordConfigMainService.getEntityClass()});
//        serviceMap.put("postCheckRecordConfigProjectService", new Object[]{postCheckRecordConfigProjectService, postCheckRecordConfigProjectService.getEntityClass()});
//        serviceMap.put("postCheckRecordConfigStandardService", new Object[]{postCheckRecordConfigStandardService, postCheckRecordConfigStandardService.getEntityClass()});
//        serviceMap.put("postCheckRecordDatainfoMainService", new Object[]{postCheckRecordDatainfoMainService, postCheckRecordDatainfoMainService.getEntityClass()});
//        serviceMap.put("postCheckRecordDatainfoDetailService", new Object[]{postCheckRecordDatainfoDetailService, postCheckRecordDatainfoDetailService.getEntityClass()});
//        serviceMap.put("mdmPostDeviceService", new Object[]{mdmPostDeviceService, mdmPostDeviceService.getEntityClass()});
//        serviceMap.put("planService", new Object[]{planService, planService.getEntityClass()});
//        serviceMap.put("planDeviceService", new Object[]{planDeviceService, planDeviceService.getEntityClass()});
//        serviceMap.put("planOrderService", new Object[]{planOrderService, planOrderService.getEntityClass()});
//        serviceMap.put("planOrderResultService", new Object[]{planOrderResultService, planOrderResultService.getEntityClass()});
//        serviceMap.put("planOrderResultConfigService", new Object[]{planOrderResultConfigService, planOrderResultConfigService.getEntityClass()});
//        serviceMap.put("mdmTaskItemService", new Object[]{mdmTaskItemService, mdmTaskItemService.getEntityClass()});
//        serviceMap.put("mdmTaskService", new Object[]{mdmTaskService, mdmTaskService.getEntityClass()});
//        serviceMap.put("mdmItemService", new Object[]{mdmItemService, mdmItemService.getEntityClass()});
//        serviceMap.put("mdmShiftGroupService", new Object[]{mdmShiftGroupService, mdmShiftGroupService.getEntityClass()});
//        serviceMap.put("shiftManage", new Object[]{mdmShiftManageService, mdmShiftManageService.getEntityClass()});
//        serviceMap.put("shiftType", new Object[]{mdmShiftTypeService, MdmShiftType.class});
//        serviceMap.put("post", new Object[]{mdmPostService, MdmPost.class});
//        serviceMap.put("device", new Object[]{mdmDeviceService, MdmDevice.class});


        if (serviceMap.containsKey(type)) {
            Object[] serviceInfo = serviceMap.get(type);
            return processCommon(serviceInfo[0], (Class<?>) serviceInfo[1]);
        }else if("all".equals(type)){
            //循环serviceMap
            for (Map.Entry<String, Object[]> entry : serviceMap.entrySet()) {
                Object[] serviceInfo = entry.getValue();
                processCommon(serviceInfo[0], (Class<?>) serviceInfo[1]);
            }
        }
        return Result.error("没有匹配方法");
    }

    /**
     * 最简化的通用处理方法
     */
    private <T> Result<?> processCommon(Object service, Class<T> entityClass) {
        try {
            // 直接使用BaseService的list方法配合QueryWrapper
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

            // 构建查询条件
            buildDynamicQuery(queryWrapper, entityClass);

            // 通过反射调用service的list方法
            List<T> list = (List<T>) service.getClass()
                    .getMethod("list", com.baomidou.mybatisplus.core.conditions.Wrapper.class)
                    .invoke(service, queryWrapper);

            log.info("查询到 {} 条符合条件的数据", list.size());

            List<T> saveList = new ArrayList<>();
            // 更新字段
            list.forEach(item -> {
                this.updateCommonFields(item);
                saveList.add(item);
                if(saveList.size()>500){
                    update(service, saveList);
                    saveList.clear();
                }
            });
            if(!saveList.isEmpty()){
                update(service, saveList);
            }

            return Result.ok("处理完成，共更新 " + list.size() + " 条记录");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理失败: " + e.getMessage());
        }
    }

    private <T> void update(Object service, List<T> list){
        // 批量更新 - 多种方式尝试
        boolean updated = false;

        // 方式1: 直接类型转换
        if (service instanceof com.baomidou.mybatisplus.extension.service.IService) {
            try {
                com.baomidou.mybatisplus.extension.service.IService<T> baseService =
                        (com.baomidou.mybatisplus.extension.service.IService<T>) service;
                updated = baseService.updateBatchById(list);
                log.info("通过IService直接转换成功执行批量更新: {}", updated);
            } catch (Exception e) {
                log.warn("IService直接转换执行失败: {}", e.getMessage());
            }
        }

        // 方式2: 通过代理对象调用IService方法
        if (!updated) {
            try {
                // 获取IService接口的Class对象
                Class<?> iServiceClass = Class.forName("com.baomidou.mybatisplus.extension.service.IService");
                // 使用代理对象调用接口方法
                java.lang.reflect.Method updateMethod = iServiceClass.getMethod("updateBatchById", java.util.Collection.class);
                updated = (boolean) updateMethod.invoke(service, list);
                log.info("通过IService接口反射调用成功: {}", updated);
            } catch (Exception e) {
                log.warn("IService接口反射调用失败: {}", e.getMessage());
            }
        }
    }

    private <T> void buildDynamicQuery(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> queryWrapper, Class<T> entityClass) {
        // 检查实体类是否包含相应字段
        boolean hasDeptCode = hasField(entityClass, "deptCode");
        boolean hasLineCode = hasField(entityClass, "lineCode");
        if (hasDeptCode && hasLineCode) {
            queryWrapper.in("dept_code", DEPT_CODES)
                    .or()
                    .in("line_code", DEPT_CODES);
        }
    }

    /**
     * 检查类是否包含指定字段
     */
    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * 更新通用字段（deptCode和lineCode）
     */
    private <T> void updateCommonFields(T data) {
        try {
            // 通过反射获取字段值
            String deptCode = (String) data.getClass().getMethod("getDeptCode").invoke(data);
            String lineCode = (String) data.getClass().getMethod("getLineCode").invoke(data);

            // 更新deptCode
            if (DEPT_CODE_MAPPING.containsKey(deptCode)) {
                data.getClass().getMethod("setDeptCode", String.class)
                        .invoke(data, DEPT_CODE_MAPPING.get(deptCode));
            }else if (DEPT_CODE_MAPPING3.containsKey(deptCode)) {
                data.getClass().getMethod("setDeptCode", String.class)
                        .invoke(data, DEPT_CODE_MAPPING3.get(deptCode));
            }

//            // 更新lineCode
//            if (DEPT_CODE_MAPPING.containsKey(lineCode)) {
//                data.getClass().getMethod("setLineCode", String.class)
//                        .invoke(data, DEPT_CODE_MAPPING.get(lineCode));
//            }
//            // 更新lineCode
//            if (DEPT_CODE_MAPPING3.containsKey(lineCode)) {
//                String lineCodeTemp = DEPT_CODE_MAPPING3.get(lineCode);
//                data.getClass().getMethod("setLineCode", String.class)
//                        .invoke(data, lineCodeTemp);
//                data.getClass().getMethod("setDeptCode", String.class)
//                        .invoke(data, lineCodeTemp.substring(0, 12));
//            }
        } catch (Exception e) {
            log.error("更新实体字段失败", e);
        }
    }


    @RequestMapping(value = "/processKpiDept", method = RequestMethod.POST)
    public Result<?> processKpiDept() {
        QueryWrapper<KpiInfo> queryWrapper = new QueryWrapper<>();
//        queryWrapper.in("dept_code", DEPT_CODES);
        queryWrapper.in("input_permission", DEPT_CODES);
        int pageNo = 1;
        boolean hasNext = true;
        while (hasNext){
//            Page<KpiReportTargetValue> page = new Page<>(pageNo, 500);
//            IPage<KpiReportTargetValue> pageList = targetValueService.page(page, queryWrapper);
            Page<KpiInfo> page = new Page<>(pageNo, 500);
            IPage<KpiInfo> pageList = kpiInfoService.page(page, queryWrapper);
            if(!pageList.getRecords().isEmpty()){
//                for(KpiReportTargetValue item:pageList.getRecords()){
//                    String oldDeptCode = item.getDeptCode();
//                    // 更新deptCode
//                    if (DEPT_CODE_MAPPING.containsKey(item.getDeptCode())) {
//                        item.setDeptCode(DEPT_CODE_MAPPING.get(item.getDeptCode()));
//                    }else if (DEPT_CODE_MAPPING3.containsKey(item.getDeptCode())) {
//                        item.setDeptCode(DEPT_CODE_MAPPING3.get(item.getDeptCode()));
//                    }
//                    if(item.getReportId().equals(oldDeptCode)){
//                        item.setReportId(item.getDeptCode());
//                    }
//                }
//                targetValueService.saveOrUpdateBatch(pageList.getRecords());

//                for(KpiReportTargetValue item : pageList.getRecords()){
//                    // 更新deptCode
//                    if (DEPT_CODE_MAPPING.containsKey(item.getReportId())) {
//                        item.setReportId(DEPT_CODE_MAPPING.get(item.getReportId()));
//                    }else if (DEPT_CODE_MAPPING3.containsKey(item.getReportId())) {
//                        item.setReportId(DEPT_CODE_MAPPING3.get(item.getReportId()));
//                    }
//                }
//                targetValueService.saveOrUpdateBatch(pageList.getRecords());


                for(KpiInfo item:pageList.getRecords()){
                    // 更新deptCode
                    if (DEPT_CODE_MAPPING.containsKey(item.getInputPermission())) {
                        item.setInputPermission(DEPT_CODE_MAPPING.get(item.getInputPermission()));
                    }else if (DEPT_CODE_MAPPING3.containsKey(item.getInputPermission())) {
                        item.setInputPermission(DEPT_CODE_MAPPING3.get(item.getInputPermission()));
                    }
                }
                kpiInfoService.saveOrUpdateBatch(pageList.getRecords());
                pageNo++;
            }else{
                hasNext = false;
            }
        }
        return Result.ok();
    }
}
