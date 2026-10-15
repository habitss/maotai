package com.keytech.modules.opl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.entity.OplReviewPlanFile;
import com.keytech.modules.opl.mapper.OplReviewPlanMapper;
import com.keytech.modules.opl.service.IOplHomePageService;
import com.keytech.modules.opl.service.IOplReviewPlanFileService;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import com.keytech.modules.opl.vo.OplManageVo;
import com.keytech.modules.sop.service.ISopStatisticDataService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: Opl管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Service
@Slf4j
public class OplReviewPlanServiceImpl extends BaseServiceImpl<OplReviewPlanMapper, OplReviewPlan> implements IOplReviewPlanService {
    @Autowired
    private OplReviewPlanMapper oplReviewPlanMapper;
    @Autowired
    private IOplReviewPlanService oplReviewPlanService;
    @Autowired
    private IOplHomePageService oplHomePageService;
    @Autowired
    private IOplReviewPlanFileService oplReviewPlanFileService;
    @Autowired
    private ISopStatisticDataService sopStatisticDataService;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

    @Override
    public List<OplManageVo> getOpltotal(String deptCode) {
        return oplReviewPlanMapper.getOplTotal(deptCode);
    }

    @Override
    public List<OplHomePage> getOplAll() {
        return oplReviewPlanMapper.getOplAll();
    }

    @Override
    public List<OplManageVo> getOpltotalByDept() {
        return oplReviewPlanMapper.getOplTotalByDept();
    }

    @Override
    public List<OplReviewPlan> getOplReviewByDept(String year) {
        return oplReviewPlanMapper.getOplReviewByDept(year);
    }

    /**
     * 获取Opl回顾数据
     * @param oplReviewPlan
     * @param req
     * @return
     */
    @Override
    public IPage<OplReviewPlan> listData(Integer pageNo,Integer pageSize, OplReviewPlan oplReviewPlan, HttpServletRequest req) {

        //获取Opl清单
        List<OplHomePage> oplList = oplHomePageService.list();
        //获取周
        List<MdmWeek> weekList = sopStatisticDataService.getWeek(oplReviewPlan.getYear());
        //同步清单
        synchronousOpl(oplReviewPlan,oplList,weekList);
        //当前切换的部门
        if(StrUtil.isNotBlank(oplReviewPlan.getDeptCode())){
            oplReviewPlan.setDeptCode(oplReviewPlan.getDeptCode()+"*");
        }
        //获取回顾
        QueryWrapper<OplReviewPlan> reviewPlanqueryWrapper = QueryGenerator.initQueryWrapper(oplReviewPlan, req.getParameterMap());
        //查询条件部门
        if(StrUtil.isNotBlank(oplReviewPlan.getDeptQuery())){
            reviewPlanqueryWrapper.in("dept_code",oplReviewPlan.getDeptQuery());
        }
        getExistsSql(oplReviewPlan, reviewPlanqueryWrapper);
        Page<OplReviewPlan> reviewPlanqueryPage = new Page<>(pageNo, pageSize);
        IPage<OplReviewPlan> pageData = oplReviewPlanService.page(reviewPlanqueryPage, reviewPlanqueryWrapper);
        List<OplReviewPlan> oplReviewPlanList = pageData.getRecords();

        JSONObject s = new JSONObject();
        for (OplReviewPlan reviewPlan : oplReviewPlanList) {
            //周赋值
            if(StringUtils.isBlank(reviewPlan.getResultData()) || "{}".equals(reviewPlan.getResultData())){
                for (MdmWeek week : weekList) {
                   s.put(week.getWeekRangeStr(), 0);
                }
                reviewPlan.setResultData(s.toJSONString());
            }
            for (OplHomePage oplHome : oplList) {
                if(oplHome.getId().equals(reviewPlan.getOplId()) ){
                    reviewPlan.setFactoryCode(oplHome.getFactoryCode());
                    reviewPlan.setDeptCode(oplHome.getDeptCode());
                    reviewPlan.setLineCode(oplHome.getLineCode());
                    reviewPlan.setOplId(oplHome.getId());
                    reviewPlan.setOplActivity(oplHome.getOplActivity());
                    reviewPlan.setOplType(oplHome.getOplType());
                    reviewPlan.setOplPost(oplHome.getOplPost());
                    reviewPlan.setOplNumber(oplHome.getOplNumber());
                    reviewPlan.setOplDesc(oplHome.getOplDesc());
                    reviewPlan.setSort(oplHome.getSort());
                }
            }
        }
        return pageData;
    }

    /**
     * 同步opl
     * @param oplReviewPlan
     * @param oplList
     */
    private void synchronousOpl(OplReviewPlan oplReviewPlan, List<OplHomePage> oplList, List<MdmWeek> weekList) {
        //获取id集合
        List<String> homeIds = oplList.stream()
                .map(OplHomePage::getId)
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(homeIds)){
            //获取Opl回顾
            LambdaQueryWrapper<OplReviewPlan> queryWrapper = Wrappers.lambdaQuery(OplReviewPlan.class);
            queryWrapper.in(OplReviewPlan::getOplId,homeIds);
            queryWrapper.eq(OplReviewPlan::getYear,oplReviewPlan.getYear());
            List<OplReviewPlan> list = oplReviewPlanService.list(queryWrapper);
            Map<String, OplReviewPlan> map = list.stream()
                    .collect(Collectors.toMap(
                            OplReviewPlan::getOplId,
                            Function.identity(),
                            (existing, replacement) -> existing,
                            HashMap::new
                    ));

            JSONObject s = new JSONObject();
            //周赋值
            for (MdmWeek week : weekList) {
                s.put(week.getWeekRangeStr(), 0);
            }

            //匹配没有的Opl
            List<OplReviewPlan> addList = new ArrayList<>();
            List<OplReviewPlan> updateList = new ArrayList<>();
            for (OplHomePage oplHome : oplList) {
                OplReviewPlan reviewPlan = map.get(oplHome.getId()); // 或其他默认值
                boolean isSave = false;
                if(map.isEmpty() || reviewPlan == null || doubleJudge(reviewPlan.getSort(), oplHome.getSort())){
                    if(reviewPlan == null){
                        reviewPlan  = new OplReviewPlan();
                        isSave = true;
                    }
                    reviewPlan.setFactoryCode(oplHome.getFactoryCode());
                    reviewPlan.setDeptCode(oplHome.getDeptCode());
                    reviewPlan.setLineCode(oplHome.getLineCode());
                    reviewPlan.setOplId(oplHome.getId());

                    reviewPlan.setOplActivity(oplHome.getOplActivity());
                    reviewPlan.setOplType(oplHome.getOplType());
                    reviewPlan.setOplPost(oplHome.getOplPost());
                    reviewPlan.setOplNumber(oplHome.getOplNumber());
                    reviewPlan.setOplDesc(oplHome.getOplDesc());
                    reviewPlan.setYear(oplReviewPlan.getYear());
                    reviewPlan.setSort(oplHome.getSort());
                    reviewPlan.setReviewInterval(oplReviewPlan.getReviewInterval());
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
            if(CollUtil.isNotEmpty(addList)){
                //同步opl
                oplReviewPlanService.saveBatch(addList);
            }
            if(CollUtil.isNotEmpty(updateList)){
                //同步opl
                oplReviewPlanService.updateBatchById(updateList);
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
            return reviewSort == null || Double.compare(reviewSort, homeSort) != 0;
        }
        return false;
    }

    /**
     * 保存
     * @param jsonObjectList 保存对象
     */
    @Override
    @Transactional
    public void oplSubmit(List<JSONObject> jsonObjectList) {
        //周
        List<MdmWeek> weekList = sopStatisticDataService.getWeek(2025);
        jsonObjectList.forEach(data -> {
            String id = data.getString("id");
            //判断是否为新增数据
            if (StrUtil.isEmpty(id)) {
                Integer year = data.getInteger("year");
                JSONObject resultData = new JSONObject();
                for (MdmWeek week : weekList) {
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
        LambdaQueryWrapper<OplReviewPlan> query = new LambdaQueryWrapper<>();
        query.lt(OplReviewPlan::getYear, jsonObjectList.get(0).getInteger("year"));
        query.orderByDesc(OplReviewPlan::getYear);
        List<OplReviewPlan> planListopl = list(query);

        List<OplReviewPlan> opl = new ArrayList<>();
        for (JSONObject jsonObject : jsonObjectList) {
            OplReviewPlan data = jsonObject.toJavaObject(OplReviewPlan.class);
            //上一次回顾年份
            for (OplReviewPlan mdmOplReviewPlan : planListopl) {
                if(data.getOplId().equals(mdmOplReviewPlan.getOplId())){
                    data.setLastYear(mdmOplReviewPlan.getYear()+"");
                    break;
                }
            }
            opl.add(data);
        }
        System.out.println(opl.get(0).getReviewInterval()+"=================================");
        this.saveOrUpdateBatch(opl);
    }

    /**
     * 判断是否更新版本号和时间
     * @param id 主键
     * @param week 周
     * @return 结果
     */
    @Override
    public boolean isUptodate(String id, String week) {
        boolean isUp = false;
        LambdaQueryWrapper<OplReviewPlanFile> query = new LambdaQueryWrapper<>();
        query.eq(OplReviewPlanFile::getReviewPlanId,id);
        query.eq(OplReviewPlanFile::getDelFlag,"0");
        List<OplReviewPlanFile> oplReviewPlanFileList = oplReviewPlanFileService.list(query);

        //排序获取最新文件
        oplReviewPlanFileList.sort((o1, o2) -> {
            String[] dates1 = o1.getWeek().split("-");
            String[] dates2 = o2.getWeek().split("-");
            int startDate1 = parseDate(dates1[0]);
            int startDate2 = parseDate(dates2[0]);
            return startDate1 - startDate2;
        });
        //倒序取最新一条
        if(CollUtil.isNotEmpty(oplReviewPlanFileList)){
            Collections.reverse(oplReviewPlanFileList);
            String weekOld = oplReviewPlanFileList.get(0).getWeek();
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
     * @param request 请求参数
     * @param oplReviewPlan 回顾对象
     * @param oplReviewPlanClass 类
     * @param title 标题
     * @return 结果
     */
    @Override
    public ModelAndView exportXlsAll(HttpServletRequest request, OplReviewPlan oplReviewPlan, Class<OplReviewPlan> oplReviewPlanClass, String title) {
        String deptCode = oplReviewPlan.getDeptCode();
        oplReviewPlan.setDeptCode(null);
        request.setAttribute("column","sort");
        request.setAttribute("order","asc");
        QueryWrapper<OplReviewPlan> queryWrapper = QueryGenerator.initQueryWrapper(oplReviewPlan, request.getParameterMap());
        //查询条件部门
        if(StrUtil.isNotBlank(oplReviewPlan.getDeptQuery())){
            queryWrapper.in("a.dept_code",oplReviewPlan.getDeptQuery());
        }
        //当前切换的部门
        if(StrUtil.isNotBlank(deptCode)){
            queryWrapper.likeRight("a.dept_code", deptCode);
        }
        getExistsSql(oplReviewPlan, queryWrapper);
        queryWrapper.eq("a.del_flag",0);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String selections = request.getParameter("selections");
        List exportList;
        if (oConvertUtils.isNotEmpty(selections)) {
            exportList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", exportList);
        }

        exportList = oplReviewPlanMapper.listAll(queryWrapper);
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject("fileName", title);
        mv.addObject("entity", oplReviewPlanClass);
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
    @Override
    public Result<?> oplImportExcel(HttpServletRequest request, HttpServletResponse response, Class<OplReviewPlan> oplReviewPlanClass) throws IOException{
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
                List<OplReviewPlan> list = ExcelImportUtil.importExcel(file.getInputStream(), oplReviewPlanClass, params);
                List<OplReviewPlan> resultUpdate = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    OplReviewPlan oplReviewPlanEntry = list.get(i);

                    if(oplReviewPlanEntry.getId() == null){
                        errorLines++;
                        int lineNumber = i + 4;
                        errorMessage.add("第" + lineNumber + "行，Opl管理回顾不存在，忽略导入。");
                        continue;
                    }

                    OplReviewPlan oplReviewPlan = new OplReviewPlan();
                    oplReviewPlan.setId(oplReviewPlanEntry.getId());
                    oplReviewPlan.setOplState(oplReviewPlanEntry.getOplState());
                    //回顾频次
                    oplReviewPlan.setFrequency(oplReviewPlanEntry.getFrequency());
                    //回顾间隔
                    oplReviewPlan.setReviewInterval(oplReviewPlanEntry.getReviewInterval());
                    //回顾负责人
                    oplReviewPlan.setHeadUserId(oplReviewPlanEntry.getHeadUserId());
                    //回顾责任人
                    oplReviewPlan.setResponsibleUserId(oplReviewPlanEntry.getResponsibleUserId());
                    //回顾咨询人
                    oplReviewPlan.setSeekUserId(oplReviewPlanEntry.getSeekUserId());
                    //回顾通知人
                    oplReviewPlan.setNoticeUserId(oplReviewPlanEntry.getNoticeUserId());

                    resultUpdate.add(oplReviewPlan);
                    successLines++;
                }

                long start = System.currentTimeMillis();
                //修改
                if(CollUtil.isNotEmpty(resultUpdate)){
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

    private void getExistsSql(OplReviewPlan oplReviewPlan, QueryWrapper<OplReviewPlan> reviewPlanqueryWrapper){
        boolean flag = false;
        String sql = " select 1 from mes3_opl_home_page opl where opl.id = opl_id ";
        if(StrUtil.isNotBlank(oplReviewPlan.getOplDesc())){
            flag = true;
            sql += " and opl.opl_desc like '%"+oplReviewPlan.getOplDesc()+"%' ";
        }
        if(StrUtil.isNotBlank(oplReviewPlan.getOplNumber())){
            flag = true;
            sql += " and opl.opl_number like '%"+oplReviewPlan.getOplNumber()+"%' ";
        }
        if(StrUtil.isNotBlank(oplReviewPlan.getDataType())){
            flag = true;
            sql += " and opl.data_type = '"+oplReviewPlan.getDataType()+"' ";
        }
        if(flag){
            reviewPlanqueryWrapper.exists(sql);
        }
    }
}
