package com.keytech.modules.sop.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.mapper.SopStatisticDataMapper;
import com.keytech.modules.sop.service.ISopHomePageService;
import com.keytech.modules.sop.service.ISopReviewPlanService;
import com.keytech.modules.sop.service.ISopStatisticDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.PmsUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: SOP-诊断计划
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Service
@Slf4j
public class SopStatisticDataServiceImpl extends BaseServiceImpl<SopStatisticDataMapper, SopStatisticData> implements ISopStatisticDataService {

    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private ISopHomePageService sopHomePageService;
    @Autowired
    private ISopStatisticDataService sopStatisticDataService;
    @Autowired
    private ISopReviewPlanService sopReviewPlanService;
    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;
    @Autowired
    private SopStatisticDataMapper sopStatisticDataMapper;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private ISysUserService sysUserService;

    /**
     * 获取分页数据
     * @param page
     * @param req
     * @param sopStatisticData
     * @return
     */
    @Override
    public IPage<SopStatisticData> getData(Page<SopStatisticData> page, SopStatisticData sopStatisticData, HttpServletRequest req) {

        //获取SOP清单
        List<SopHomePage> sopList =  sopHomePageService.list();
        if(CollUtil.isEmpty(sopList)){
            return null;
        }
        //将sop转为map形式
        Map<String, SopHomePage> sopMap = sopList.stream()
                .collect(Collectors.toMap(
                        SopHomePage::getId,
                        sop -> sop,
                        (existing, replacement) -> replacement // 如果有重复 key，保留最后一个
                ));
        //同步清单
        synchronousSop(sopStatisticData,sopList);
        //当前切换的部门
        if(StrUtil.isNotBlank(sopStatisticData.getDeptCode())){
            sopStatisticData.setDeptCode(sopStatisticData.getDeptCode()+"*");
        }
        if(StrUtil.isNotBlank(sopStatisticData.getSopDesc())){
            sopStatisticData.setSopDesc("*"+sopStatisticData.getSopDesc()+"*");
        }
        if(StrUtil.isNotBlank(sopStatisticData.getSopNumber())){
            sopStatisticData.setSopNumber("*"+sopStatisticData.getSopNumber()+"*");
        }

        QueryWrapper<SopStatisticData> queryWrapper = QueryGenerator.initQueryWrapper(sopStatisticData, req.getParameterMap());
        //查询条件部门
        if(StrUtil.isNotBlank(sopStatisticData.getDeptQuery())){
            queryWrapper.in("dept_code",sopStatisticData.getDeptQuery());
        }
        queryWrapper.orderByAsc("create_time");
        IPage<SopStatisticData> pageData = page(page, queryWrapper);
        List<SopStatisticData> sopStatisticDataList = pageData.getRecords();
        //获取周
        // 从请求中获取月份参数
        String[] monthsArray = req.getParameterValues("months");
        List<String> months = null;
        if (monthsArray != null && monthsArray.length > 0) {
            months = Arrays.asList(monthsArray);
        }
        List<MdmWeek> weekList = sopStatisticDataService.getWeek(sopStatisticData.getYear(), months);

        JSONObject s = new JSONObject();
        for (MdmWeek week : weekList) {
            s.put(week.getWeekRangeStr(), 0);
        }

        //查询所有的回顾计划
        List<SopReviewPlan> listPlan =  sopReviewPlanService.lambdaQuery()
                .in(SopReviewPlan::getSopId,
                    sopStatisticDataList.stream()
                    .map(SopStatisticData::getSopId)
                    .collect(Collectors.toList())
                )
                .eq(SopReviewPlan::getYear, sopStatisticData.getYear())
                .list();
        Map<String, SopReviewPlan> sopIdMap = new HashMap<>();
        if(CollUtil.isNotEmpty(listPlan)){
            sopIdMap = listPlan.stream()
                    .collect(Collectors.toMap(
                            SopReviewPlan::getSopId,
                            plan -> plan,
                            (existing, replacement) -> existing
                    ));
        }

        Map<String, JSONObject> rateMap = statisticRate(sopStatisticDataList);
        for (SopStatisticData sopStatistic : sopStatisticDataList) {
            //周赋值
            if(StringUtils.isBlank(sopStatistic.getResultData()) || "{}".equals(sopStatistic.getResultData())){
                sopStatistic.setResultData(s.toJSONString());
            }
            if(sopMap.containsKey(sopStatistic.getSopId())){
                SopHomePage sopHome = sopMap.get(sopStatistic.getSopId());
                sopStatistic.setFactoryCode(sopHome.getFactoryCode());
                sopStatistic.setDeptCode(sopHome.getDeptCode());
                sopStatistic.setLineCode(sopHome.getLineCode());
                sopStatistic.setSopId(sopHome.getId());
                sopStatistic.setSopActivity(sopHome.getSopActivity());
                sopStatistic.setSopName(sopHome.getSopDesc());
            }
            //设置诊断责任人、咨询人、通知人
            if(sopIdMap.containsKey(sopStatistic.getSopId())){
                sopStatistic.setResponsibleUserName(sopIdMap.get(sopStatistic.getSopId()).getResponsibleUserId());
                sopStatistic.setSeekUserName(sopIdMap.get(sopStatistic.getSopId()).getSeekUserId());
                sopStatistic.setNoticeUserName(sopIdMap.get(sopStatistic.getSopId()).getNoticeUserId());
            }
            //设置已完成、计划、延迟统计数据
            if(rateMap.containsKey(sopStatistic.getSopId())){
                JSONObject object = rateMap.get(sopStatistic.getSopId());
                sopStatistic.setCompleted(object.getString("completed"));
                sopStatistic.setUnderWay(object.getString("underWay"));
                sopStatistic.setDelay(object.getString("delay"));
            }
        }
        return pageData;
    }

    /**
     * 数据保存
     * @param input
     */
    @Override
    public void sopSubmit(List<JSONObject> input) {
        input.forEach(data -> {
                JSONObject hyphenKeys = new JSONObject();
                data.forEach((key, value) -> {
                    if (key.contains("-")) {
                        hyphenKeys.put(key, value);
                    }
                });
//                jsonObject.putAll(hyphenKeys);
                data.put("resultData", hyphenKeys);
//            }
        });
        List<SopStatisticData> sop = new ArrayList<>();
        for (JSONObject jsonObject : input) {
            SopStatisticData data = jsonObject.toJavaObject(SopStatisticData.class);
            sop.add(data);
        }
        saveOrUpdateBatch(sop);
    }



    /**
     * 获取表头周
     * @param year
     * @return
     */
    @Override
    public List<MdmWeek> getWeek(int year) {
        Map<Integer, MdmWeek> weekMap =  mdmWeekService.getWeekDataByWeekNum(year+"");
        List<MdmWeek> list = new ArrayList<>();
        weekMap.forEach((key, value) -> {
            MdmWeek mdmWeek =  (MdmWeek)value;
            mdmWeek.setWeekRangeStr(formatDateRange(mdmWeek.getStartDate(), mdmWeek.getEndDate()));
            list.add(mdmWeek);
        });
        return list;
    }
    
    /**
     * 获取表头周（支持月份筛选）
     * @param year
     * @param months
     * @return
     */
    @Override
    public List<MdmWeek> getWeek(int year, List<String> months) {
        if (months == null || months.isEmpty()) {
            return getWeek(year);
        }
        
        // 转换月份格式，提取月份数字
        List<Integer> monthList = months.stream()
                .map(month -> Integer.parseInt(month.substring(5)))
                .collect(Collectors.toList());
        
        // 封装查询对象
        MdmWeek mdmWeek = new MdmWeek();
        mdmWeek.setYear(year+"");
        mdmWeek.setMonths(monthList);
        
        List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
        weekList.forEach(week -> {
            week.setWeekRangeStr(formatDateRange(week.getStartDate(), week.getEndDate()));
        });
        return weekList;
    }



    /**
     * 时间格式  M.d-M.d
     * @param startDate
     * @param endDate
     * @return
     */
    String formatDateRange(Date startDate, Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat( DateUtils.DATE_MONTH_DAY);
        String start = dateFormat.format(startDate);
        String end = dateFormat.format(endDate);
        return start + "-" + end;
    }


    /**
     * 同步sop
     * @param sopStatisticData
     * @param sopList
     */
    private void synchronousSop(SopStatisticData sopStatisticData, List<SopHomePage> sopList) {
        //获取id集合
        List<String> homeIds = sopList.stream()
                .map(SopHomePage::getId)
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(homeIds)){
            //获取SOP回顾
            LambdaQueryWrapper<SopStatisticData> queryWrapper = Wrappers.lambdaQuery(SopStatisticData.class);
            queryWrapper.in(SopStatisticData::getSopId,homeIds);
            queryWrapper.eq(SopStatisticData::getYear,sopStatisticData.getYear());
            List<SopStatisticData> list = sopStatisticDataService.list(queryWrapper);
//        Map<String, Boolean> map = list.stream()
//                .collect(Collectors.toMap(
//                        SopStatisticData::getSopId,
//                        plan -> true,
//                        (existing, replacement) -> existing,
//                        HashMap::new
//                ));
            Map<String, SopStatisticData> map = list.stream()
                    .collect(Collectors.toMap(
                            SopStatisticData::getSopId,
                            Function.identity(),
                            (existing, replacement) -> existing,
                            HashMap::new
                    ));
            //匹配没有的SOP
            List<SopStatisticData> addList = new ArrayList<>();
            List<SopStatisticData> updateList = new ArrayList<>();
            for (SopHomePage sopHome : sopList) {
//            boolean result = !Optional.ofNullable(map.get(sopHome.getId())).orElse(false);
                SopStatisticData statisticData = Optional.ofNullable(map.get(sopHome.getId())).orElse(null); // 或其他默认值
                Boolean isSave = false;
                if((map != null && map.isEmpty()) || statisticData == null || doubleJudge(map,sopHome)){
                    if(statisticData == null){
                        statisticData  = new SopStatisticData();
                        isSave = true;
                    }

                    statisticData.setFactoryCode(sopHome.getFactoryCode());
                    statisticData.setDeptCode(sopHome.getDeptCode());
                    statisticData.setLineCode(sopHome.getLineCode());
                    statisticData.setSopId(sopHome.getId());
                    statisticData.setYear(sopStatisticData.getYear());
                    statisticData.setSopActivity(sopHome.getSopActivity());
                    statisticData.setSopNumber(sopHome.getSopNumber());
                    statisticData.setSopType(sopHome.getSopType());
                    statisticData.setSopDesc(sopHome.getSopDesc());
                    if(isSave){
                        addList.add(statisticData);
                    }else {
                        updateList.add(statisticData);
                    }

                }
            }
            if(addList != null && !addList.isEmpty()){
                //同步sop
                sopStatisticDataService.saveBatch(addList);
            }
            if(updateList != null && !updateList.isEmpty()){
                //同步sop
                sopStatisticDataService.updateBatchById(updateList);
            }
        }
    }

    private Boolean doubleJudge(Map<String, SopStatisticData> statisticDataMap, SopHomePage sopHome) {
        Boolean isTheSame = true;
        SopStatisticData sopStatisticData = statisticDataMap.get(sopHome.getId());
        if(sopStatisticData != null){
            if(sopStatisticData.getSopNumber() != null && sopStatisticData.getSopNumber().equals(sopHome.getSopNumber()) &&
                sopStatisticData.getSopDesc() != null && sopStatisticData.getSopDesc().equals(sopHome.getSopDesc()) &&
                sopStatisticData.getSopType() != null && sopStatisticData.getSopType().equals(sopHome.getSopType())){
                isTheSame = false;
            }
        }
        return isTheSame;
    }


    /**
     * 导出
     * @param request
     * @param sopStatisticData
     * @param sopStatisticDataClass
     * @param title
     * @return
     */
    public ModelAndView exportXlsAll(HttpServletRequest request, SopStatisticData sopStatisticData, Class<SopStatisticData> sopStatisticDataClass, String title) {
        String sopNumber = sopStatisticData.getSopNumber();
        String sopDesc = sopStatisticData.getSopDesc();
        String deptCode = sopStatisticData.getDeptCode();
        sopStatisticData.setSopNumber(null);
        sopStatisticData.setSopDesc(null);
        sopStatisticData.setDeptCode(null);
        //当前切换的部门
        if(StrUtil.isNotBlank(sopStatisticData.getDeptCode())){
            sopStatisticData.setDeptCode(sopStatisticData.getDeptCode()+"*");
        }
        request.setAttribute("column","sort");
        request.setAttribute("order","asc");
        QueryWrapper<SopStatisticData> queryWrapper = QueryGenerator.initQueryWrapper(sopStatisticData, request.getParameterMap());
        //查询条件部门
        if(StrUtil.isNotBlank(sopStatisticData.getDeptQuery())){
            queryWrapper.in("a.dept_code",sopStatisticData.getDeptQuery());
        }
        queryWrapper.like(StrUtil.isNotBlank(sopNumber), "b.sop_number", sopNumber);
        queryWrapper.like(StrUtil.isNotBlank(sopDesc), "b.sop_desc", sopDesc);
        queryWrapper.likeRight(StrUtil.isNotBlank(deptCode), "b.dept_code", deptCode);
        queryWrapper.eq("a.del_flag",0);
        queryWrapper.orderByAsc("b.sop_number");

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String selections = request.getParameter("selections");
        List exportList;
        if (oConvertUtils.isNotEmpty(selections)) {
            exportList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", exportList);
        }

        exportList = sopStatisticDataMapper.listAll(queryWrapper);
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject("fileName", title);
        mv.addObject("entity", sopStatisticDataClass);
        ExportParams exportParams = new ExportParams(title + "报表", "导出人:" + sysUser.getRealname(), title);
        exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
        mv.addObject("params", exportParams);
        mv.addObject("data", exportList);
        return mv;
    }

    /**
     * 解析日期字符串，将其转换为整型值。
     *
     * @param dateStr 日期字符串，格式为 "月.日"，例如 "01.01"
     * @return 日期的整型表示，格式为 "月月日日"，例如 0101
     * @throws NumberFormatException 如果解析过程中发生数字格式错误
     */
    private int parseDate(String dateStr) {
        String[] parts = dateStr.split("\\.");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        return month * 100 + day;
    }


    /**
     * 导入
     */
    @Transactional
    public Result<?> sopImportExcel(HttpServletRequest request, HttpServletResponse response, Class<SopStatisticData> sopStatisticDataClass) throws IOException {
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(2);
            params.setNeedSave(true);
            try {
                //获取所有用户信息，并按id做key转换成map
                List<SysUser> allUserList = sysUserService.list();
                Map<String, SysUser> allUserMap =CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean);

                //获取诊断频率字典
                List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                        List.of(
                                "MES3_SOP_FREQUENCY"
                        )
                );

                Map<String, MdmDict> frequenyMap = mdmDictList.stream()
                        .collect(Collectors.toMap(
                                MdmDict::getItemVal, // 键是由groupCode和val组成的字符串
                                item -> item // 值是对象本身
                        ));

                //获取上传文件中的数据
                List<SopStatisticData> list = ExcelImportUtil.importExcel(file.getInputStream(), sopStatisticDataClass, params);
                //获取sop清单信息
                List<String> sopIdList = list.stream()
                        .map(SopStatisticData::getSopId)
                        .filter(Objects::nonNull)
                        .toList();
                List<SopHomePage> sopList = sopHomePageService.listByIds(sopIdList);
                Map<String, SopHomePage> sopMap = CollStreamUtil.toMap(sopList, SopHomePage::getId, bean -> bean);

                //操作数据
                List<SopStatisticData> saveList = new ArrayList<>();
                int lineNumber = 3;
                for(SopStatisticData sopStatisticData : list){
                    lineNumber++;

                    if(StrUtil.isBlank(sopStatisticData.getSopId()) || !sopMap.containsKey(sopStatisticData.getSopId())){
                        errorMessage.add("第" + lineNumber + "行，SOP不存在");
                        errorLines++;
                        continue;
                    } else {
                        //设置值
                        SopHomePage sopHomePage = sopMap.get(sopStatisticData.getSopId());
                        sopStatisticData.setSopDesc(sopHomePage.getSopDesc());
                        sopStatisticData.setSopNumber(sopHomePage.getSopNumber());
                        sopStatisticData.setFactoryCode(sopHomePage.getFactoryCode());
                        sopStatisticData.setDeptCode(sopStatisticData.getDeptCode());
                        sopStatisticData.setLineCode(sopHomePage.getLineCode());
                    }

                    if(StrUtil.isNotBlank(sopStatisticData.getCheckInterval()) && !frequenyMap.containsKey(sopStatisticData.getCheckInterval())){
                        errorMessage.add("第" + lineNumber + "行，诊断频率不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isNotBlank(sopStatisticData.getCheckUserId()) && !allUserMap.containsKey(sopStatisticData.getCheckUserId())){
                        errorMessage.add("第" + lineNumber + "行，诊断负责人不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isNotBlank(sopStatisticData.getByCheckUserId()) && !allUserMap.containsKey(sopStatisticData.getByCheckUserId())){
                        errorMessage.add("第" + lineNumber + "行，被诊断负责人不存在");
                        errorLines++;
                        continue;
                    }
                    successLines++;
                    saveList.add(sopStatisticData);
                }
                this.saveOrUpdateBatch(saveList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }

    private Map<String, JSONObject> statisticRate(List<SopStatisticData> records) {
        Map<String, JSONObject> resultMap = new HashMap<>();
        if (CollUtil.isEmpty(records)) {
            return resultMap;
        }

        Map<String, List<SopStatisticData>> groupedBySopId = records.stream()
                .collect(Collectors.groupingBy(SopStatisticData::getSopId));

        for (Map.Entry<String, List<SopStatisticData>> entry : groupedBySopId.entrySet()) {
            String sopId = entry.getKey();
            List<SopStatisticData> sopRecords = entry.getValue();

            List<String> collect = sopRecords.stream()
                    .map(SopStatisticData::getResultData)
                    .toList();
            int size = collect.size();
            if (size == 0) {
                JSONObject stats = new JSONObject();
                stats.put("completed", "/");
                stats.put("underWay", "/");
                stats.put("delay", "/");
                resultMap.put(sopId, stats);
                continue;
            }
            //未填写的算计划中（getAllLatestNonZeroEntries返回的仅仅是填写的状态，未填写状态不返回，所以此处的差值全部归纳到计划中）
            //1计划 2进行中 3已完成 4延迟 5取消 6调整计划诊断 7未通过 0未填写
            //按照最新一周的状态进行判断，如果全部都未填写，按照计划处理
            //获取cop存储的json值
            long completedCount = 0;
            long underWayCount = 0;
            long delayCount = 0;

            for(String jsonExt : collect){
                JSONObject extJsonObject = JSONObject.parseObject(jsonExt);

                String startWeekDate = "";//存储当前最新周
                String lastState = "";//存储当前最新状态

                //循环json对象，获取最新任务状态
                // 获取所有键
                if(null != extJsonObject){
                    for(String key : extJsonObject.keySet()){
                        String keyValue = null != extJsonObject.get(key) ? extJsonObject.get(key).toString() : "";
                        //查找不等于0的最新一周
                        if(!keyValue.equals("0") && StrUtil.isNotEmpty(keyValue)){
                            //将最新一周的数据全局存储，用于比较
                            if(StrUtil.isEmpty(startWeekDate)){
                                startWeekDate = key.split("-")[0];
                                lastState = keyValue;
                            }
                            if(!key.split("-")[0].equals(startWeekDate)){
                                int comparison = compareDateRanges(key.split("-")[0],startWeekDate);
                                if(comparison > 0){
                                    startWeekDate = key.split("-")[0];
                                    lastState = keyValue;
                                }
                            }
                        }
                    }
                }
                if(StrUtil.isEmpty(lastState) || lastState.equals("1") || lastState.equals("2") || lastState.equals("5") || lastState.equals("6") || lastState.equals("7")){
                    underWayCount ++;
                }
                if(lastState.equals("3")){
                    completedCount ++;
                }
                if(lastState.equals("4")){
                    delayCount ++;
                }
            }
            double completedRate = ((double) completedCount / size) * 100;
            double underWayRate = ((double) underWayCount / size) * 100;
            double delayRate = ((double) delayCount / size) * 100;

            JSONObject stats = new JSONObject();
            stats.put("completed", Math.round(completedRate)+"%");
            stats.put("underWay", Math.round(underWayRate)+"%");
            stats.put("delay", Math.round(delayRate)+"%");

            resultMap.put(sopId, stats);
        }
        return resultMap;
    }

    public static int compareDateRanges(String range1, String range2) {
        if (range2.isEmpty()) {
            return 1;
        }

        String[] dates1 = range1.split("-");
        String[] dates2 = range2.split("-");

        String[] startDate1 = dates1[0].split("\\.");
        String[] startDate2 = dates2[0].split("\\.");

        // 比较月
        int month1 = Integer.parseInt(startDate1[0]);
        int month2 = Integer.parseInt(startDate2[0]);
        if (month1 != month2) {
            return month1 - month2;
        }

        // 比较日
        int day1 = Integer.parseInt(startDate1[1]);
        int day2 = Integer.parseInt(startDate2[1]);
        return day1 - day2;
    }
}
