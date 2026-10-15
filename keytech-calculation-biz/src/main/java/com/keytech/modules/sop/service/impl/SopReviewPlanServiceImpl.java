package com.keytech.modules.sop.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopReviewPlanFile;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.mapper.SopReviewPlanMapper;
import com.keytech.modules.sop.service.ISopHomePageService;
import com.keytech.modules.sop.service.ISopReviewPlanFileService;
import com.keytech.modules.sop.service.ISopReviewPlanService;
import com.keytech.modules.sop.service.ISopStatisticDataService;
import com.keytech.modules.sop.vo.SopManageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.PmsUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: SOP管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Service
@Slf4j
public class SopReviewPlanServiceImpl extends BaseServiceImpl<SopReviewPlanMapper, SopReviewPlan> implements ISopReviewPlanService {
    @Autowired
    private SopReviewPlanMapper sopReviewPlanMapper;
    @Autowired
    private ISopReviewPlanService sopReviewPlanService;
    @Autowired
    private ISopHomePageService sopHomePageService;
    @Autowired
    private ISopStatisticDataService sopStatisticDataService;
    @Autowired
    private ISopReviewPlanFileService sopReviewPlanFileService;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

    @Override
    public List<SopManageVo> getSoptotal(String deptCode) {
        return sopReviewPlanMapper.getSoptotal(deptCode);
    }

    @Override
    public List<SopStatisticData> getDiagnosisByYear(String year, String deptCode) {
        return sopReviewPlanMapper.getDiagnosisByYear(year, deptCode);
    }

    @Override
    public List<SopHomePage> getSopAll() {
        return sopReviewPlanMapper.getSopAll();
    }

    @Override
    public List<SopManageVo> getSoptotalByDept() {
        return sopReviewPlanMapper.getSoptotalByDept();
    }

    @Override
    public List<SopReviewPlan> getSopReviewByDept(String year) {
        return sopReviewPlanMapper.getSopReviewByDept(year);
    }

    /**
     * 获取SOP回顾数据
     * @param sopReviewPlan
     * @param months
     * @param req
     * @return
     */
    @Override
    public IPage<SopReviewPlan> listData(Integer pageNo,Integer pageSize, SopReviewPlan sopReviewPlan, List<String> months, HttpServletRequest req) {

        //获取SOP清单
        List<SopHomePage> sopList = sopHomePageService.list();
        //获取周
        List<MdmWeek> weekList = sopStatisticDataService.getWeek(sopReviewPlan.getYear(), months);
        //同步清单
        synchronousSop(sopReviewPlan,sopList,weekList);
        //当前切换的部门
        if(StrUtil.isNotBlank(sopReviewPlan.getDeptCode())){
            sopReviewPlan.setDeptCode(sopReviewPlan.getDeptCode()+"*");
        }
        //获取回顾
        QueryWrapper<SopReviewPlan> reviewPlanqueryWrapper = QueryGenerator.initQueryWrapper(sopReviewPlan, req.getParameterMap());
        //查询条件部门
        if(StrUtil.isNotBlank(sopReviewPlan.getDeptQuery())){
            reviewPlanqueryWrapper.in("dept_code",sopReviewPlan.getDeptQuery());
        }
        getExistsSql(sopReviewPlan, reviewPlanqueryWrapper);
        Page<SopReviewPlan> reviewPlanqueryPage = new Page<SopReviewPlan>(pageNo, pageSize);
//        reviewPlanqueryWrapper.orderByAsc("sort");
        IPage<SopReviewPlan> pageData = sopReviewPlanService.page(reviewPlanqueryPage, reviewPlanqueryWrapper);
        List<SopReviewPlan> sopReviewPlanList = pageData.getRecords();


        JSONObject s = new JSONObject();
        for (SopReviewPlan reviewPlan : sopReviewPlanList) {
            //周赋值
            if(StringUtils.isBlank(reviewPlan.getResultData()) || "{}".equals(reviewPlan.getResultData())){
                for (MdmWeek week : weekList) {
                   s.put(week.getWeekRangeStr(), 0);
                }
                reviewPlan.setResultData(s.toJSONString());
            }
            for (SopHomePage sopHome : sopList) {
                if(sopHome.getId().equals(reviewPlan.getSopId()) ){
                    reviewPlan.setFactoryCode(sopHome.getFactoryCode());
                    reviewPlan.setDeptCode(sopHome.getDeptCode());
                    reviewPlan.setLineCode(sopHome.getLineCode());
                    reviewPlan.setSopId(sopHome.getId());
                    reviewPlan.setSopActivity(sopHome.getSopActivity());
                    reviewPlan.setSopType(sopHome.getSopType());
                    reviewPlan.setSopPost(sopHome.getSopPost());
                    reviewPlan.setSopNumber(sopHome.getSopNumber());
                    reviewPlan.setSopDesc(sopHome.getSopDesc());
                    reviewPlan.setSort(sopHome.getSort());
//                    reviewPlan.setYear(sopReviewPlan.getYear());
//                    reviewPlan.setReviewInterval(sopReviewPlan.getReviewInterval());
                }
            }
        }
        return pageData;
    }

    /**
     * 同步sop
     * @param sopReviewPlan
     * @param sopList
     */
    private void synchronousSop(SopReviewPlan sopReviewPlan, List<SopHomePage> sopList, List<MdmWeek> weekList) {
        //获取id集合
        List<String> homeIds = sopList.stream()
                .map(SopHomePage::getId)
                .collect(Collectors.toList());
        if(homeIds != null && !homeIds.isEmpty()){
            //获取SOP回顾
            LambdaQueryWrapper<SopReviewPlan> queryWrapper = Wrappers.lambdaQuery(SopReviewPlan.class);
            queryWrapper.in(SopReviewPlan::getSopId,homeIds);
            queryWrapper.eq(SopReviewPlan::getYear,sopReviewPlan.getYear());
            List<SopReviewPlan> list = sopReviewPlanService.list(queryWrapper);
//        Map<String, Boolean> map = list.stream()
//            .collect(Collectors.toMap(
//                SopReviewPlan::getSopId,
//                plan -> true,
//                (existing, replacement) -> existing,
//                HashMap::new
//        ));
            Map<String, SopReviewPlan> map = list.stream()
                    .collect(Collectors.toMap(
                            SopReviewPlan::getSopId,
                            Function.identity(),
                            (existing, replacement) -> existing,
                            HashMap::new
                    ));

            JSONObject s = new JSONObject();
            //周赋值
            for (MdmWeek week : weekList) {
                s.put(week.getWeekRangeStr(), 0);
            }

            //匹配没有的SOP
            List<SopReviewPlan> addList = new ArrayList<>();
            List<SopReviewPlan> updateList = new ArrayList<>();
            for (SopHomePage sopHome : sopList) {
                //boolean result = !Optional.ofNullable(map.get(sopHome.getId())).orElse(false);
                SopReviewPlan reviewPlan = Optional.ofNullable(map.get(sopHome.getId())).orElse(null); // 或其他默认值
                Boolean isSave = false;
                if((map != null && map.isEmpty()) || reviewPlan == null || doubleJudge(reviewPlan.getSort(),sopHome.getSort())){
                    if(reviewPlan == null){
                        reviewPlan  = new SopReviewPlan();
                        isSave = true;
                    }
                    reviewPlan.setFactoryCode(sopHome.getFactoryCode());
                    reviewPlan.setDeptCode(sopHome.getDeptCode());
                    reviewPlan.setLineCode(sopHome.getLineCode());
                    reviewPlan.setSopId(sopHome.getId());

                    reviewPlan.setSopActivity(sopHome.getSopActivity());
                    reviewPlan.setSopType(sopHome.getSopType());
                    reviewPlan.setSopPost(sopHome.getSopPost());
                    reviewPlan.setSopNumber(sopHome.getSopNumber());
                    reviewPlan.setSopDesc(sopHome.getSopDesc());
                    reviewPlan.setYear(sopReviewPlan.getYear());
                    reviewPlan.setSort(sopHome.getSort());
                    reviewPlan.setReviewInterval(sopReviewPlan.getReviewInterval());
                    if(StringUtils.isBlank(reviewPlan.getResultData()) || "{}".equals(reviewPlan.getResultData())){
                        reviewPlan.setResultData(s.toJSONString());
                    }
                    if(isSave){
                        addList.add(reviewPlan);
                    }else {
                        updateList.add(reviewPlan);
                    }

                }
            }
            if(addList != null && !addList.isEmpty()){
                //同步sop
                sopReviewPlanService.saveBatch(addList);
            }
            if(updateList != null && !updateList.isEmpty()){
                //同步sop
                sopReviewPlanService.updateBatchById(updateList);
            }
        }
    }

    /**
     * 比较两个双精度浮点数是否相等或homeSort不为null且reviewSort为null或二者相等。
     *
     * @param reviewSort 需要比较的第一个双精度浮点数
     * @param homeSort 需要比较的第二个双精度浮点数
     * @return 如果homeSort不为null且reviewSort为null或二者相等，则返回true；否则返回false
     */
    private Boolean doubleJudge (Double reviewSort, Double homeSort) {
        if(homeSort != null){
            if(reviewSort == null || Double.compare(reviewSort, homeSort) != 0){
                return true;
            }
        }
        return false;
    }

    /**
     * 保存
     * @param input
     */
    @Override
    public void sopSubmit(List<JSONObject> input) {
        //周
        List<MdmWeek>[] weekListArray = new List[]{null};
        input.forEach(data -> {
            String id = data.getString("id");
            if (StrUtil.isEmpty(id)) {
                Integer year = data.getInteger("year");
                JSONObject resultData = new JSONObject();
                if(weekListArray[0] == null || weekListArray[0].isEmpty()){
                    weekListArray[0] = sopStatisticDataService.getWeek(year);
                }
                for (MdmWeek week : weekListArray[0]) {
                    resultData.put(week.getWeekRangeStr(), 0);
                }
                data.put("resultData", resultData);
            } else {
                JSONObject jsonObject = data.getJSONObject("resultData");
                JSONObject hyphenKeys = new JSONObject();
                data.forEach((key, value) -> {
                    if (key.contains("-")) {
                        hyphenKeys.put(key, value);
                    }
                });
                jsonObject.putAll(hyphenKeys);
                data.put("resultData", jsonObject);
            }
        });

        //获取当前年份之前的回顾(如2024年之前)
        LambdaQueryWrapper<SopReviewPlan> query = new LambdaQueryWrapper<>();
        query.lt(SopReviewPlan::getYear,input.get(0).getInteger("year"));
        query.orderByDesc(SopReviewPlan::getYear);
        List<SopReviewPlan> planListsop = list(query);

        List<SopReviewPlan> sop = new ArrayList<>();
        for (JSONObject jsonObject : input) {
            SopReviewPlan data = jsonObject.toJavaObject(SopReviewPlan.class);
            //上一次回顾年份
            for (SopReviewPlan mdmSopReviewPlan : planListsop) {
                if(data.getSopId().equals(mdmSopReviewPlan.getSopId())){
                    data.setLastYear(mdmSopReviewPlan.getYear()+"");
                    break;
                }
            }
            sop.add(data);
        }
        System.out.println(sop.get(0).getReviewInterval()+"=================================");
        saveOrUpdateBatch(sop);
    }

    /**
     * 判断是否更新版本号和时间
     * @param id
     * @param week
     * @return
     */
    @Override
    public boolean isUptodate(String id, String week) {
        boolean isUp = false;
        LambdaQueryWrapper<SopReviewPlanFile> query = new LambdaQueryWrapper<>();
        query.eq(SopReviewPlanFile::getReviewPlanId,id);
        query.eq(SopReviewPlanFile::getDelFlag,"0");
        List<SopReviewPlanFile> fileListsop = sopReviewPlanFileService.list(query);

        //排序获取最新文件
        fileListsop.sort((o1, o2) -> {
            String[] dates1 = o1.getWeek().split("-");
            String[] dates2 = o2.getWeek().split("-");
            int startDate1 = parseDate(dates1[0]);
            int startDate2 = parseDate(dates2[0]);
            return startDate1 - startDate2;
        });
        //倒序取最新一条
        if(null != fileListsop && fileListsop.size()>0){
            Collections.reverse(fileListsop);
            String weekOld = fileListsop.get(0).getWeek();
            //比较那条最新
            //周 1.1-1.7  9.9-9.15 比较大小
            String[] datesOld = weekOld.split("-");
            String[] dates = week.split("-");
            if (Double.parseDouble(datesOld[0]) < Double.parseDouble(dates[0])) {
                //更新
                isUp = true;
            }
        }else{
            //更新
            isUp = true;
        }
        return isUp;
    }

    /**
     * 导出
     * @param request
     * @param sopReviewPlan
     * @param sopReviewPlanClass
     * @param title
     * @return
     */
    @Override
    public ModelAndView exportXlsAll(HttpServletRequest request, SopReviewPlan sopReviewPlan, Class<SopReviewPlan> sopReviewPlanClass, String title) {
        String deptCode = sopReviewPlan.getDeptCode();
        sopReviewPlan.setDeptCode(null);
        request.setAttribute("column","sort");
        request.setAttribute("order","asc");
        QueryWrapper<SopReviewPlan> queryWrapper = QueryGenerator.initQueryWrapper(sopReviewPlan, request.getParameterMap());
        //查询条件部门
        if(StrUtil.isNotBlank(sopReviewPlan.getDeptQuery())){
            queryWrapper.in("a.dept_code",sopReviewPlan.getDeptQuery());
        }
        //当前切换的部门
        if(StrUtil.isNotBlank(deptCode)){
            queryWrapper.likeRight("a.dept_code", deptCode);
        }
        getExistsSql(sopReviewPlan, queryWrapper);
        queryWrapper.eq("a.del_flag",0);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String selections = request.getParameter("selections");
        List exportList;
        if (oConvertUtils.isNotEmpty(selections)) {
            exportList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", exportList);
        }

        exportList = sopReviewPlanMapper.listAll(queryWrapper);
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject("fileName", title);
        mv.addObject("entity", sopReviewPlanClass);
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
     * @param request
     * @param response
     * @param sopReviewPlanClass
     * @return
     */
    @Override
    public Result<?> sopImportExcel(HttpServletRequest request, HttpServletResponse response, Class<SopReviewPlan> sopReviewPlanClass) throws IOException{
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SopReviewPlan> list = ExcelImportUtil.importExcel(file.getInputStream(), sopReviewPlanClass, params);
                List<SopReviewPlan> resultSave = new ArrayList<>();
                List<SopReviewPlan> resultUpdate = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    SopReviewPlan sopReviewPlanEntry = list.get(i);

                    if(sopReviewPlanEntry.getId() == null){
                        errorLines++;
                        int lineNumber = i + 1;
                        errorMessage.add("第 " + lineNumber + "SOP管理回顾不存在，忽略导入。");
                        continue;
                    }

                    SopReviewPlan sopReviewPlan = new SopReviewPlan();
                    sopReviewPlan.setId(sopReviewPlanEntry.getId());
                    //BeanUtil.copyProperties(sopReviewPlanEntry, sopReviewPlan);
                    sopReviewPlan.setSopState(sopReviewPlanEntry.getSopState());
                    //回顾频次
                    sopReviewPlan.setFrequency(sopReviewPlanEntry.getFrequency());
                    //回顾间隔
                    sopReviewPlan.setReviewInterval(sopReviewPlanEntry.getReviewInterval());
                    //回顾负责人
                    sopReviewPlan.setHeadUserId(sopReviewPlanEntry.getHeadUserId());
                    //回顾责任人
                    sopReviewPlan.setResponsibleUserId(sopReviewPlanEntry.getResponsibleUserId());
                    //回顾咨询人
                    sopReviewPlan.setSeekUserId(sopReviewPlanEntry.getSeekUserId());
                    //回顾通知人
                    sopReviewPlan.setNoticeUserId(sopReviewPlanEntry.getNoticeUserId());

                    resultUpdate.add(sopReviewPlan);
                    successLines++;
                }

                long start = System.currentTimeMillis();
                //修改
                if(resultUpdate.size() > 0){
                    updateBatchById(resultUpdate);
                }
                log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
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
        return imporReturnRes(errorLines, successLines, errorMessage);
    }


    /**
     * 导入数据并返回结果
     *
     * @param errorLines 错误行数
     * @param successLines 成功行数
     * @param errorMessage 错误信息列表
     * @return Result<?> 包含导入结果的封装类
     * @throws IOException 如果文件操作出现异常则抛出此异常
     */
    public Result<?> imporReturnRes(int errorLines,int successLines,List<String> errorMessage) throws IOException {
        if (errorLines == 0) {
            return Result.ok("共" + successLines + "行数据全部导入成功！");
        } else {
            JSONObject result = new JSONObject(5);
            int totalCount = successLines + errorLines;
            result.put("totalCount", totalCount);
            result.put("errorCount", errorLines);
            result.put("successCount", successLines);
            result.put("msg", "总上传行数：" + totalCount + "，已导入行数：" + successLines + "，错误行数：" + errorLines);
            String fileUrl = PmsUtil.saveErrorTxtByList(errorMessage, "userImportExcelErrorLog");
            int lastIndex = fileUrl.lastIndexOf(File.separator);
            String fileName = fileUrl.substring(lastIndex + 1);
            result.put("fileUrl", "/sys/common/static/" + fileUrl);
            result.put("fileName", fileName);
            Result res = Result.ok(result);
            res.setCode(201);
            res.setMessage("文件导入成功，但有错误。");
            return res;
        }
    }

    private void getExistsSql(SopReviewPlan sopReviewPlan, QueryWrapper<SopReviewPlan> reviewPlanqueryWrapper){
        boolean flag = false;
        String sql = " select 1 from mes3_sop_home_page sop where sop.id = sop_id ";
        if(StrUtil.isNotBlank(sopReviewPlan.getSopDesc())){
            flag = true;
            sql += " and sop.sop_desc like '%"+sopReviewPlan.getSopDesc()+"%' ";
        }
        if(StrUtil.isNotBlank(sopReviewPlan.getSopNumber())){
            flag = true;
            sql += " and sop.sop_number like '%"+sopReviewPlan.getSopNumber()+"%' ";
        }
        if(flag){
            reviewPlanqueryWrapper.exists(sql);
        }
    }
}
