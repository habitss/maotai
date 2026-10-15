package com.keytech.modules.sop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.sop.dto.UserStatisticsDto;
import com.keytech.modules.sop.dto.WriteTwoRrDto;
import com.keytech.modules.sop.entity.*;
import com.keytech.modules.sop.mapper.SopHomePageMapper;
import com.keytech.modules.sop.service.*;
import com.keytech.modules.sop.vo.SopDepartScheduleTreeVo;
import com.keytech.modules.sop.vo.SopManageVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: SOP 清单
 * @Author: jeecg-boot
 * @Date:   2025-04-11
 * @Version: V1.0
 */
@Service
@Slf4j
public class SopHomePageServiceImpl extends BaseServiceImpl<SopHomePageMapper, SopHomePage> implements ISopHomePageService {

    @Autowired
    private ISopStatisticDataService sopStatisticDataService;
    @Autowired
    private ISysDepartService departService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private ISopReviewPlanService iSopReviewPlanService;
    @Autowired
    private IMdmDictService dicService;
    @Autowired
    private ISopReviewPlanFileService sopReviewPlanFileService;
    @Autowired
    private IMdmWeekService mdmWeekService;
    /**
     * 查询
     * @param page
     * @param spage
     * @param req
     * @return
     */
    @Override
    public IPage<SopHomePage> getQueryPageData(Page<SopHomePage> page, SopHomePage spage, HttpServletRequest req) {
        //根据SOP名称添加模糊查询
        if(StrUtil.isNotBlank(spage.getSopDesc())){
            spage.setSopDesc("*"+spage.getSopDesc()+"*");
        }
        if(StrUtil.isNotBlank(spage.getDeptCode())){
            spage.setDeptCode(spage.getDeptCode()+"*");
        }
        QueryWrapper<SopHomePage> queryWrapper = QueryGenerator.initQueryWrapper(spage, req.getParameterMap());

        //根据查询条件的部门做特殊判断（传入过来的是ID）
        if(StrUtil.isNotBlank(spage.getDeptQuery())){
            List<String> deptCodes = CollUtil.toList(spage.getDeptQuery().split(","));
            if(null != deptCodes && deptCodes.size() > 0){
                queryWrapper.in("dept_code",deptCodes);
            }
        }

        //根据当前登录人部门做数据权限（传入过来的是code）[查询条件]
//        if(StrUtil.isNotBlank(spage.getDeptCode())){
//            queryWrapper.likeRight("dept_code", spage.getDeptCode());
//        }
        //当前登陆部门
//        if(StrUtil.isNotBlank(spage.getDeptQuery())){
//            spage.setDeptCode(spage.getDeptQuery()+"*");
//        }

        queryWrapper.orderByAsc("sort");
        IPage<SopHomePage> pageList = page(page, queryWrapper);
        List<SopHomePage> records = pageList.getRecords();



        List<String> dicList = new ArrayList<>();
        dicList.add("MES3_SOP_FREQUENCY");
        List<MdmDict> listCheckInterval = dicService.getDictAllOrByGroupCode(dicList);

        List<String> sopIDS = CollStreamUtil.toList(records,SopHomePage::getId);
        if(CollUtil.isEmpty(sopIDS)){
            //随机生成一个long，避免后续使用in报错
            Random random = new Random();
            sopIDS.add(random.nextLong()+"");
        }
        //查询所有的SOP回顾计划，将最新的关键SOP、回顾频次、最新版版本号、实施时间
        List<SopReviewPlan> listReviewPlan = iSopReviewPlanService.lambdaQuery()
                .in(SopReviewPlan::getSopId,sopIDS)
                .isNotNull(SopReviewPlan::getLatestVersionNumber)
                .orderByDesc(SopReviewPlan::getCreateTime)
                .list();
        for(SopReviewPlan sopReviewPlan : listReviewPlan){
            Optional<MdmDict> dictModel = listCheckInterval.stream().filter(checkInterval -> checkInterval.getItemVal().equals(sopReviewPlan.getFrequency())).findFirst();
            dictModel.ifPresent(mdmDict -> sopReviewPlan.setFrequencyName(mdmDict.getItemKey()));
        }

        //查询所有的SOP回顾计划中上传的附件
        List<SopReviewPlanFile> listReviewPlanFile = sopReviewPlanFileService.lambdaQuery().list();

        //查询所有的SOP诊断计划
        List<SopStatisticData> statisticDataList = sopStatisticDataService.lambdaQuery()
                .in(SopStatisticData::getSopId,sopIDS).list();



        records.forEach(sopHomePage ->{
            List<SopReviewPlan> filterList = listReviewPlan.stream().filter(reviewPlan ->
                    reviewPlan.getSopId().equals(sopHomePage.getId())
            ).sorted(Comparator.comparing(SopReviewPlan::getCreateTime).reversed()).toList();

            //查询出回顾SOP中上传的附件
            List<SopReviewPlanFile> listFiles =  new ArrayList<>();

            if (CollUtil.isNotEmpty(filterList)){
                //将回顾SOP对象注入
                sopHomePage.setSopReviewPlan(filterList.get(0));

                listFiles = listReviewPlanFile.stream().filter(reviewPlanFile ->
                        null != reviewPlanFile.getReviewPlanId() &&
                                reviewPlanFile.getReviewPlanId().equals(filterList.get(0).getId())
                ).collect(Collectors.toList());
            }

            //从附件中找到最新一条记录，根据week来查找，week格式：1.1-1.10
            SopReviewPlanFile newPlanFile = new SopReviewPlanFile();

            if(listFiles.size() > 1){
                listFiles.sort((o1, o2) -> {
                    String[] dates1 = o1.getWeek().split("-");
                    String[] dates2 = o2.getWeek().split("-");
                    int startDate1 = parseDate(dates1[1]);
                    int startDate2 = parseDate(dates2[1]);
                    return startDate2 - startDate1;
                });
            }
            if(listFiles.size() > 0){
                newPlanFile = listFiles.get(0);
            }

            // 处理 writeTwoRr 字段
            String writeTwoRr = sopHomePage.getWriteTwoRr();
            if (StrUtil.isNotEmpty(writeTwoRr) && filterList.size() > 0) {
                List<WriteTwoRrDto> writeTwoRrList = JSONArray.parseArray(writeTwoRr, WriteTwoRrDto.class);
                //sop文件名称
                List<String> sopTitlesInWriteTwoRrList = writeTwoRrList.stream()
                        .map(WriteTwoRrDto::getSopTitle)
                        .collect(Collectors.toList());
                //有上传附件
                if(StringUtils.isNotEmpty(newPlanFile.getId())){
                    if(!sopTitlesInWriteTwoRrList.contains(newPlanFile.getFileName())){
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateStr = sf.format(newPlanFile.getCreateTime());
                        writeTwoRrList.add(new WriteTwoRrDto(newPlanFile.getFileName(), null, null, null, null, null,dateStr));
                    }
                }
                // 获取唯一的 R1 值，并设置到 putIntoNames 字段
                String putIntoNames = writeTwoRrList.stream()
                        .map(WriteTwoRrDto::getR1)
                        .filter(StrUtil::isNotEmpty)
                        .distinct()
                        .collect(Collectors.joining(","));

                sopHomePage.setPutIntoNames(putIntoNames);
                sopHomePage.setWriteTwoRrList(writeTwoRrList.stream().filter(w->StrUtil.isNotEmpty(w.getTime())).sorted(Comparator.comparing(WriteTwoRrDto::getTime).reversed()).limit(1).collect(Collectors.toList()));
            } else {
                // 如果 writeTwoRr 为空，为每个 sopDescDto 创建新的 WriteTwoRrDto
                if(StringUtils.isNotEmpty(newPlanFile.getId())){
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateStr = sf.format(newPlanFile.getCreateTime());
                    List<WriteTwoRrDto> newWriteTwoRrList = new ArrayList<>();
                    newWriteTwoRrList.add(new WriteTwoRrDto(newPlanFile.getFileName(), null, null, null, null, null,dateStr));
                    sopHomePage.setWriteTwoRrList(newWriteTwoRrList);
                }
            }

            //owd频次，来源诊断计划中的诊断频率,取最新一条
            List<SopStatisticData> listStatistic = statisticDataList.stream().filter(statisticData ->
                    statisticData.getSopId().equals(sopHomePage.getId().toString())
            ).sorted(Comparator.comparing(SopStatisticData::getCreateTime).reversed()).collect(Collectors.toList());
            if(listStatistic.size() > 0 ){
                SopStatisticData sopStatisticData = listStatistic.get(0);
                //对诊断频率进行翻译
                String checkInterval = sopStatisticData.getCheckInterval();
                if(StrUtil.isNotEmpty(checkInterval)){
                    Optional<MdmDict> dictModelFrequencyOptional = listCheckInterval.stream().filter(dictModel -> dictModel.getItemVal().equals(checkInterval)).findFirst();
                    if(dictModelFrequencyOptional.isPresent()){
                        sopStatisticData.setCheckIntervalName(dictModelFrequencyOptional.get().getItemVal());
                    }
                }
                sopHomePage.setStatisticData(sopStatisticData);
            }
        });

        return pageList;
    }

    /**
     * 解析日期字符串并返回整数值。
     *
     * @param dateStr 日期字符串，格式为"MM.DD"，其中MM为月份，DD为日期
     * @return 解析后的整数值，格式为MM*100 + DD
     */
    private int parseDate(String dateStr) {
        String[] parts = dateStr.split("\\.");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        return month * 100 + day;
    }

    /**
     * 存储
     * @param sopHomePageList
     */
    @Override
    public void batchAddOrUpdate(List<SopHomePage> sopHomePageList) {
        //sop名称保存
        Map<String, String> repeatSop = new HashMap<>();
        sopHomePageList.forEach(homePage -> {
            String sopDesc = homePage.getSopDesc();
            //如果id为空或者temp开头，则说明是新增数据，需要清空id
            if (StrUtil.isBlank(homePage.getId()) || homePage.getId().startsWith("temp")){
                homePage.setId(null);
                //获取key查看是否已经有重复sop名称
                String key = homePage.getDeptCode() +'-'+ homePage.getLineCode() +'-'+ homePage.getSopDesc();
                if(repeatSop.containsKey(key)){
                    throw new JeecgBootException("SOP名称:"+sopDesc+" 重复");
                }
                repeatSop.put(key, key);
            }
            long count = count(new LambdaQueryWrapper<SopHomePage>()
                    .ne(StrUtil.isNotBlank(homePage.getId()), SopHomePage::getId, homePage.getId())
                    .eq(SopHomePage::getSopDesc, sopDesc)
                    .eq(SopHomePage::getLineCode, homePage.getLineCode())
                    .eq(SopHomePage::getDeptCode, homePage.getDeptCode()));
            if (count > 0){
                throw new JeecgBootException("SOP名称:"+sopDesc+" 重复");
            }

            if (StrUtil.isNotBlank(homePage.getId())){
                //更新诊断计划
                List<SopStatisticData> list = sopStatisticDataService.list(new LambdaQueryWrapper<SopStatisticData>().eq(SopStatisticData::getSopId, homePage.getId()));
                if (CollUtil.isNotEmpty(list)){
                    list.forEach(ss->{
                        ss.setLineCode(homePage.getLineCode());
                        ss.setDeptCode(homePage.getDeptCode());
                    });
                    sopStatisticDataService.updateBatchById(list);
                }
            }
        });
        saveOrUpdateBatch(sopHomePageList);
    }

    /**
     * 导出
     * @param request
     * @param response
     * @param sopHomePageClass
     * @return
     */
    @Override
    @Transactional
    public Result<?> importExcelGenCode(HttpServletRequest request, HttpServletResponse response, Class<SopHomePage> sopHomePageClass) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        //获取部门
        List<SysDepart> departList = departService.list();
        Map<String, String> departListMap = CollStreamUtil.toMap(departList, SysDepart::getOrgCode, SysDepart::getDepartName);

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
                List<SopHomePage> list = ExcelImportUtil.importExcel(file.getInputStream(), sopHomePageClass, params);
                List<SopHomePage> result = new ArrayList<>();
                int lineNumber = 4;
                for (int i = 0; i < list.size(); i++) {
                    SopHomePage sopHomePage = list.get(i);

                    if (!departListMap.containsKey(sopHomePage.getFactoryCode())) {
                        errorLines++;
                        lineNumber = i + lineNumber;
                        errorMessage.add("第" + lineNumber + "行 工厂不存在，忽略导入。");
                    }

                    String dept = sopHomePage.getDeptCode();
                    if (!departListMap.containsKey(dept)) {
                        errorLines++;
                        lineNumber = i + lineNumber;
                        errorMessage.add("第" + lineNumber + "行 部门不存在，忽略导入。");
                    }
                    String line = sopHomePage.getLineCode();
                    if(line == null){
                        sopHomePage.setLineCode("");
                    }else if(!line.isEmpty() && !departListMap.containsKey(line)){
                        errorLines++;
                        lineNumber = i + lineNumber;
                        errorMessage.add("第" + lineNumber + "行 产线不存在，忽略导入。");
                    }
                    result.add(sopHomePage);
                    successLines++;
                }
                //update-begin-author:taoyan date:20190528 for:批量插入数据
                long start = System.currentTimeMillis();
                if(errorLines==0){
                    saveOrUpdateBatch(result);
                }
                log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
                //update-end-author:taoyan date:20190528 for:批量插入数据
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


    /**
     * 诊断统计
     * @param year
     * @param depts
     * @param dept
     * @return
     */
    @Override
    public List<SopDepartScheduleTreeVo> getSopDept(String year, String depts, String dept) {
        //获取部门
        List<String> deptCodeList = new ArrayList<>();
        if(StrUtil.isNotBlank(depts)){
            deptCodeList = CollUtil.toList(depts.split(","));
        }
        //获取部门树
        List<SysDepartTreeModel> departList = queryTreeList(deptCodeList, dept);

        List<SopDepartScheduleTreeVo> deptList = new ArrayList<>();
        if(CollUtil.isNotEmpty(departList)){
            //获取sop统计数
            List<SopManageVo> sopTotalList = iSopReviewPlanService.getSoptotal(depts);
            //按产线转换map
            Map<String, SopManageVo> groupSopMap = sopTotalList.stream()
                    .collect(Collectors.toMap(
                            SopManageVo::getDept,
                            item -> item,
                            //如果key冲突，保留第一个
                            (existing, replacement) -> existing
                    ));
            //获取诊断计划
            List<SopStatisticData> diagnosisList = iSopReviewPlanService.getDiagnosisByYear(year, depts);
            //按产线分组
            Map<String, List<SopStatisticData>> groupDiagnosisByLineCodeMap = diagnosisList.stream()
                    .collect(Collectors.groupingBy(SopStatisticData::getLineCode));
            //按人员分组
            Map<String, List<SopStatisticData>> groupDiagnosisByUserMap = diagnosisList.stream()
                    .collect(Collectors.groupingBy(SopStatisticData::getByCheckUserId));

            //获取所有人
            List<SysUser> userList = iSysUserService.list();
            //按部门分组
            Map<String, List<SysUser>> groupUserByIdMap = userList.stream()
                    .filter(item -> item.getOrgCode() != null)
                    .collect(Collectors.groupingBy(
                            item -> item.getOrgCode().length() >= 12 ? item.getOrgCode().substring(0, 12) : item.getOrgCode()
                    ));
            for(SysDepartTreeModel sysDepartTreeModel : departList){
                //判断是否有人员属于该部门
                if(groupUserByIdMap.containsKey(sysDepartTreeModel.getOrgCode())){
                    //创建一个新的部门子对象
                    List<SysDepartTreeModel> sysChildDeptList = sysDepartTreeModel.getChildren();
                    if(CollUtil.isEmpty(sysChildDeptList)){
                        sysChildDeptList = new ArrayList<>();
                    }
                    //获取部门下的人员
                    List<SysUser> childUserList = groupUserByIdMap.get(sysDepartTreeModel.getOrgCode());
                    for(SysUser sysUser : childUserList){
                        SysDepartTreeModel sysDepartTreeChild = new SysDepartTreeModel();
                        sysDepartTreeChild.setKey(sysUser.getId());
                        sysDepartTreeChild.setTitle(sysUser.getRealname());
                        sysDepartTreeChild.setOrgCode(sysUser.getOrgCode()+"--"+sysUser.getId());
                        sysChildDeptList.add(sysDepartTreeChild);
                    }
                    sysDepartTreeModel.setChildren(sysChildDeptList);
                }
                //转换部门为统计对象
                SopDepartScheduleTreeVo departTreeModelVo = new SopDepartScheduleTreeVo();
                BeanUtil.copyProperties(sysDepartTreeModel, departTreeModelVo);
                //SOP数
                int sopNumber = 0;
                //诊断数
                int planNumber = 0;
                //已完成
                int planSuccess = 0;
                //计划
                int plan = 0;
                //延迟
                int planDelay = 0;
                //完成率
                String planRate = "";
                //处理统计数据
                if(CollUtil.isNotEmpty(sysDepartTreeModel.getChildren())){
                    //新的子部门对象
                    List<SopDepartScheduleTreeVo> childDeptList = new ArrayList<>();
                    for(SysDepartTreeModel childTree : sysDepartTreeModel.getChildren()){
                        //新的统计对象
                        SopDepartScheduleTreeVo departTreeModelChildVo = new SopDepartScheduleTreeVo();
                        BeanUtil.copyProperties(childTree, departTreeModelChildVo);
                        departTreeModelChildVo.setChildren(null);
                        String[] arr = departTreeModelChildVo.getOrgCode().split("--");
                        //判断是否有sop数据
                        if(groupSopMap.containsKey(departTreeModelChildVo.getOrgCode())){
                            int total = groupSopMap.get(departTreeModelChildVo.getOrgCode()).getTotal();
                            departTreeModelChildVo.setSopNumber(total);
                            //累加总sop数
                            sopNumber += total;
                        } else {
                            departTreeModelChildVo.setSopNumber(0);
                        }
                        //诊断数
                        int planNumberTemp = 0;
                        //已完成
                        int planSuccessTemp = 0;
                        //计划
                        int planTemp = 0;
                        //延迟
                        int planDelayTemp = 0;
                        //根据部门或个人取不同数据
                        boolean isDept = false;
                        List<SopStatisticData> tempStatisticDataList = new ArrayList<>();

                        if(groupDiagnosisByLineCodeMap.containsKey(departTreeModelChildVo.getOrgCode()) && arr.length<2){
                            tempStatisticDataList = groupDiagnosisByLineCodeMap.get(departTreeModelChildVo.getOrgCode());
                            isDept = true;
                        } else if(groupDiagnosisByUserMap.containsKey(departTreeModelChildVo.getKey())){
                            tempStatisticDataList = groupDiagnosisByUserMap.get(departTreeModelChildVo.getKey());
                        }
                        //判断是否有诊断数据
                        if(CollUtil.isNotEmpty(tempStatisticDataList)){
                            planNumberTemp = tempStatisticDataList.size();
                            //部门才累加统计数
                            if(isDept){
                                planNumber += tempStatisticDataList.size();
                            }
                            for(SopStatisticData statisticData: tempStatisticDataList){
                                if(StrUtil.isNotBlank(statisticData.getResultData())){
                                    JSONObject jsonObject = JSONObject.parseObject(statisticData.getResultData());
                                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                                        int value = 0;
                                        if (entry.getValue() instanceof Integer) {
                                            value = (Integer) entry.getValue();
                                        }
                                        if(value == 1){
                                            //计划
                                            planTemp++;
                                        } else if(value == 3) {
                                            //已完成
                                            planSuccessTemp++;
                                        } else if(value == 4) {
                                            //延迟
                                            planDelayTemp++;
                                        }
                                    }
                                }
                            }
                            //部门才累加统计数
                            if(isDept){
                                plan += planTemp;
                                planSuccess += planSuccessTemp;
                                planDelay += planDelayTemp;
                            }
                        }
                        //设置各项统计数
                        departTreeModelChildVo.setPlanSuccess(planSuccessTemp);
                        departTreeModelChildVo.setPlan(planTemp);
                        departTreeModelChildVo.setPlanDelay(planDelayTemp);
                        departTreeModelChildVo.setPlanNumber(planNumberTemp);
                        childDeptList.add(departTreeModelChildVo);
                    }
                    departTreeModelVo.setChildren(childDeptList);
                }
                //判断是否有sop数据
                if(groupSopMap.containsKey(departTreeModelVo.getOrgCode())){
                    int total = groupSopMap.get(departTreeModelVo.getOrgCode()).getTotal();
                    //累加总sop数
                    sopNumber += total;
                }
                departTreeModelVo.setSopNumber(sopNumber);
                departTreeModelVo.setPlanSuccess(planSuccess);
                departTreeModelVo.setPlan(plan);
                departTreeModelVo.setPlanDelay(planDelay);
                departTreeModelVo.setPlanNumber(planNumber);
                deptList.add(departTreeModelVo);
            }
//            //人员匹配
//            List<UserStatisticsDto> userStatisticsList = new ArrayList<>();
//            for (SysUser sysUser : userList) {
//                UserStatisticsDto userStatisticsDto = BeanUtil.copyProperties(sysUser, UserStatisticsDto.class);
//                //诊断数
//                int planNumber = 0;
//                //已完成
//                int planSuccess = 0;
//                //计划
//                int plan = 0;
//                //延迟
//                int planDelay = 0;
//                //sopId集合
//                for (SopStatisticData sopStatisticData : diagnosisList) {
//                    if(sysUser.getId().equals(sopStatisticData.getByCheckUserId())){
//                        planNumber++;
//                        //获取最新判断最新状态
//                        String resultData = sopStatisticData.getResultData();
//                        JSONObject jsonObject = JSONObject.parseObject(resultData);
//
//                        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
//                        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
//                            Integer value = null;
//                            if (entry.getValue() instanceof Integer) {
//                                value = (Integer) entry.getValue();
//                            }
//                            if (value!= null) {
//                                entries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), value));
//                            }
//                        }
//
//                        entries.sort((e1, e2) -> {
//                            String key1 = e1.getKey();
//                            String key2 = e2.getKey();
//                            return key2.compareTo(key1);
//                        });
//
//                        String firstNonZeroKey = null;
//                        int latestNonZeroValue=0;
//                        for (Map.Entry<String, Integer> entry : entries) {
//                            if (entry.getValue()!= 0) {
//                                firstNonZeroKey = entry.getKey();
//                                latestNonZeroValue = entry.getValue();
//                                break;
//                            }
//                        }
//                        if(firstNonZeroKey!= null){
//                            switch (latestNonZeroValue + "") {
//                                case "1" -> {
//                                    //计划
//                                    plan++;
//                                    sopStatisticData.setNewState("1");
//                                }
//                                case "3" -> {
//                                    //成功
//                                    planSuccess++;
//                                    sopStatisticData.setNewState("3");
//                                }
//                                case "4" -> {
//                                    //延迟
//                                    planDelay++;
//                                    sopStatisticData.setNewState("4");
//                                }
//                            }
//                        }else{
//                            //都为0 定义为计划
//                            plan++;
//                            sopStatisticData.setNewState("1");
//                        }
//                        //sopStatisticData.get
//                    }
//                }
//                userStatisticsDto.setSopNumber(planNumber);
//                userStatisticsDto.setSopSuccess(planSuccess);
//                userStatisticsDto.setSopPlan(plan);
//                userStatisticsDto.setSopDelay(planDelay);
//
//                if(userStatisticsDto.getSopNumber()!=0 && userStatisticsDto.getSopSuccess()!=0){
//                    userStatisticsDto.setSopRate(NumberUtil.round(userStatisticsDto.getSopSuccess()*100.0/userStatisticsDto.getSopNumber(),2)+"%");
//                }else{
//                    userStatisticsDto.setSopRate("0%");
//                }
//
//                //诊断计划完成进度
//                userStatisticsDto.setPlanNumber(planNumber);
//                userStatisticsDto.setPlanSuccess(planSuccess);
//                userStatisticsDto.setPlan(plan);
//                userStatisticsDto.setPlanDelay(planDelay);
//                if(userStatisticsDto.getPlanSuccess()!=0 && userStatisticsDto.getPlanNumber()!=0){
//                    userStatisticsDto.setPlanRate(NumberUtil.round(userStatisticsDto.getPlanSuccess()*100.0/userStatisticsDto.getPlanNumber(),2)+"%");
//                }else{
//                    userStatisticsDto.setPlanRate("0%");
//                }
//                userStatisticsList.add(userStatisticsDto);
//            }
//
//            //获取sop所有数据
//            List<SopHomePage> sopHomePagesList = iSopReviewPlanService.getSopAll();
//
//            //计算[SOP诊断完成进度] 完成计划延迟
//            for (SopHomePage sopHomePage : sopHomePagesList) {
//                SopManageVo sopManageVo;
//                if(personMap.containsKey(sopHomePage.getDeptCode())){
//                    sopManageVo = personMap.get(sopHomePage.getDeptCode());
//                } else {
//                    sopManageVo = new SopManageVo();
//                    //初始化参数
//                    sopManageVo.setSopSuccess(0);
//                    sopManageVo.setSopPlan(0);
//                    sopManageVo.setSopDelay(0);
//                }
//
//                boolean isDelay = false;
//                boolean issuccess = false;
//                for (SopStatisticData sopStatisticData : diagnosisList) {
//                    //判断key 是否与 sopStatisticData.getDept 相同
//                    if((sopHomePage.getId()+"").equals(sopStatisticData.getSopId())){
//                        //1计划 3成功 4延迟
//                        if("1".equals(sopStatisticData.getNewState())){
//                            //计划
//                            issuccess = true;
//                            break;
//                        }else if("4".equals(sopStatisticData.getNewState())){
//                            //延期
//                            isDelay = true;
//                            break;
//                        }
//                    }
//                }
//                if(isDelay){
//                    //延期
//                    sopManageVo.setSopDelay(sopManageVo.getSopDelay()+1);
//                }else if(issuccess){
//                    sopManageVo.setSopSuccess(sopManageVo.getSopSuccess()+1);
//                }else{
//                    sopManageVo.setSopPlan(sopManageVo.getSopPlan()+1);
//                }
//            }
//            deptList = convertTree(departList,userStatisticsList,null,personMap);
        }
        return deptList;
    }

    /**
     * 初始化统计表
     * @param treeNodes
     * @param userList
     * @param belowUserList
     * @param personMap
     * @return
     */
    public List<SopDepartScheduleTreeVo> convertTree(List<SysDepartTreeModel> treeNodes, List<UserStatisticsDto> userList, List<UserStatisticsDto> belowUserList,
                                                               Map<String, SopManageVo> personMap) {
        List<SopDepartScheduleTreeVo> newTreeNodes = new ArrayList<>();
        for (SysDepartTreeModel node : treeNodes) {
            SopDepartScheduleTreeVo newNode = new SopDepartScheduleTreeVo();
            newNode.setKey(node.getKey());
            newNode.setTitle(node.getTitle());
            newNode.setOrgCode(node.getOrgCode());
            //部门
            newNode = initData(newNode,personMap);
            //匹配人员
            List<UserStatisticsDto> belowUser = new ArrayList<>();
            for (UserStatisticsDto sysUser : userList) {
                if(node.getOrgCode().equals(sysUser.getOrgCode())){
                    belowUser.add(sysUser);
                }
            }
            if (node.getChildren()!= null &&!node.getChildren().isEmpty()) {
                newNode.setChildren(convertTree(node.getChildren(),userList,belowUser,personMap));
                //向上求和
                //用户层不需要求和
                if(CollUtil.isEmpty(belowUserList)){
                    newNode.setSopNumber(newNode.getSopNumber()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getSopNumber).sum());//sop数向上求和
                    newNode.setSopSuccess(newNode.getSopSuccess()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getSopSuccess).sum());//sop数向上求和
                    newNode.setSopPlan(newNode.getSopPlan()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getSopPlan).sum());//sop数向上求和
                    newNode.setSopDelay(newNode.getSopDelay()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getSopDelay).sum());//sop数向上求和
                }

                if(newNode.getSopNumber()!=0 && newNode.getSopSuccess()!=0){
                    newNode.setSopRate(NumberUtil.round(newNode.getSopSuccess()*100.0/newNode.getSopNumber(),2)+"%");
                }else{
                    newNode.setSopRate("0%");
                }
                //诊断计划完成进度
                newNode.setPlanNumber(newNode.getPlanNumber()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getPlanNumber).sum());//诊断数向上求和
                newNode.setPlanSuccess(newNode.getPlanSuccess()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getPlanSuccess).sum());//已完成数向上求和
                newNode.setPlan(newNode.getPlan()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getPlan).sum());//计划数数向上求和
                newNode.setPlanDelay(newNode.getPlanDelay()+newNode.getChildren().stream().mapToInt(SopDepartScheduleTreeVo::getPlanDelay).sum());//计划数数向上求和
                if(newNode.getPlanSuccess()!=0 && newNode.getPlanNumber()!=0){
                    newNode.setPlanRate(NumberUtil.round(newNode.getPlanSuccess()*100.0/newNode.getPlanNumber(),2)+"%");
                }else{
                    newNode.setPlanRate("0%");
                }
            }else{
                newNode.setChildren(null);
            }
            newTreeNodes.add(newNode);
        }
        //匹配下级
        if(null != belowUserList){
            //添加人员
            for (UserStatisticsDto sysUser : belowUserList) {
                SopDepartScheduleTreeVo newNodeUser = new SopDepartScheduleTreeVo();
                newNodeUser.setKey(sysUser.getId());
                newNodeUser.setTitle(sysUser.getRealname());
                newNodeUser.setSopNumber(sysUser.getSopNumber());//sop数
                newNodeUser.setSopSuccess(sysUser.getSopSuccess());
                newNodeUser.setSopPlan(sysUser.getSopPlan());
                newNodeUser.setSopDelay(sysUser.getSopDelay());
                newNodeUser.setSopRate(sysUser.getSopRate());//完成率
                newNodeUser.setPlanNumber(sysUser.getPlanNumber());
                newNodeUser.setPlanSuccess(sysUser.getPlanSuccess());
                newNodeUser.setPlan(sysUser.getPlan());
                newNodeUser.setPlanDelay(sysUser.getPlanDelay());
                newNodeUser.setPlanRate(sysUser.getPlanRate()); //完成率
                newNodeUser.setChildren(null);
                newTreeNodes.add(newNodeUser);
            }
        }
        return newTreeNodes;
    }

    /**
     * 初始化统计数据
     * @param newNode
     * @param personMap
     * @return
     */
    public SopDepartScheduleTreeVo  initData(SopDepartScheduleTreeVo newNode,Map<String, SopManageVo> personMap){
        SopManageVo sopManageVo = personMap.get(newNode.getOrgCode());
        if(null != sopManageVo){
            newNode.setSopNumber(sopManageVo.getTotal()!=null?sopManageVo.getTotal():0);//sop数
            newNode.setSopSuccess(sopManageVo.getSopSuccess()!=null?sopManageVo.getSopSuccess():0);//已完成数
            newNode.setSopPlan(sopManageVo.getSopPlan()!=null?sopManageVo.getSopPlan():0);//计划数
            newNode.setSopDelay(sopManageVo.getSopDelay()!=null?sopManageVo.getSopDelay():0);//延迟数
        }else{
            newNode.setSopNumber(0);//sop数
            newNode.setSopSuccess(0);//已完成数
            newNode.setSopPlan(0);//计划数
            newNode.setSopDelay(0);//延迟数
        }
        newNode.setSopRate("0%");
        newNode.setPlanNumber(0);
        newNode.setPlanSuccess(0);
        newNode.setPlan(0);
        newNode.setPlanDelay(0);
        newNode.setPlanRate("0%");
        return newNode;
    }

    public List<SysDepartTreeModel> queryTreeListByOrgCode(List<String> deptCodeList,String dept) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        // 遍历 deptCodeList 列表，对同一个字段进行多个 like 查询
        if(null!= deptCodeList && deptCodeList.size() > 0){
            query.and(wrapper -> {
                for (String orgCode : deptCodeList) {
                    wrapper.or().likeRight(SysDepart::getOrgCode, orgCode);
                }
                wrapper.or().eq(SysDepart::getParentId, null).or().eq(SysDepart::getParentId, "");
            });
        }
        //查询权限
        query.and(wrapper -> {
            wrapper.or().likeRight(SysDepart::getOrgCode, dept);
            //wrapper.or().eq(SysDepart::getParentId, null).or().eq(SysDepart::getParentId, "");
        });
        query.orderByAsc(SysDepart::getDepartOrder);
        List<SysDepart> list = sysDepartService.list(query);
        // 调用wrapTreeDataToTreeList方法生成树状数据
        List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
        return listResult;
    }

    /**
     * 获取部门产线
     * @param deptCodeList 所有部门数据
     * @param dept 要筛选的部门
     * @return 树形部门数据
     */
    public List<SysDepartTreeModel> queryTreeList(List<String> deptCodeList, String dept) {
        List<SysDepartTreeModel> list = new ArrayList<SysDepartTreeModel>();
        //获取部门
        List<SysDepart> departList = queryDept(dept,true);

        //获取产线
        LambdaQueryWrapper<SysDepart> queryLine = new LambdaQueryWrapper<SysDepart>();
        queryLine.eq(SysDepart::getOrgType,'6');
        List<SysDepart> lineList = sysDepartService.list(queryLine);
        for (SysDepart depart : departList) {
            //条件查询
            if(CollUtil.isNotEmpty(deptCodeList) && !deptCodeList.contains(depart.getOrgCode())){
                continue;
            }
            SysDepartTreeModel sysDepartTreeModel = BeanUtil.copyProperties(depart, SysDepartTreeModel.class);
            sysDepartTreeModel.setTitle(depart.getDepartName());
            List<SysDepartTreeModel> lineDatalist = new ArrayList<>();
            for (SysDepart lineDepart : lineList) {
                String orgCode = lineDepart.getOrgCode();
                orgCode = orgCode.substring(0,12);
                if(depart.getOrgCode().equals(orgCode)){
                    SysDepartTreeModel temp = BeanUtil.copyProperties(lineDepart, SysDepartTreeModel.class);
                    temp.setTitle(lineDepart.getDepartName());
                    lineDatalist.add(temp);
                }
            }
            if(CollUtil.isNotEmpty(lineDatalist)){
                sysDepartTreeModel.setChildren(lineDatalist);
            }else{
                sysDepartTreeModel.setChildren(null);
            }

            list.add(sysDepartTreeModel);
        }
        return list;
    }

    /**
     * 获取部门
     * @param dept
     * @return
     */
    public List<SysDepart> queryDept(String dept,Boolean isF) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        if(isF){
            query.in(SysDepart::getOrgType,"3","4");
        }else{
            query.eq(SysDepart::getOrgType,"4");
        }

        query.and(wrapper -> {
            wrapper.or().likeRight(SysDepart::getOrgCode, dept);
        });
        query.orderByAsc(SysDepart::getOrgCode);
        return sysDepartService.list(query);
    }


    /**
     * SOP管理回顾统计
     * @param year
     * @param month
     * @param depts
     * @param dept
     * @return
     */
    @Override
    public List<SopManageVo> getSopManage(String year, String month, String depts, String dept) {
        //获取部门
        List<String> deptCodeList = new ArrayList<>();
        if(StrUtil.isNotBlank(depts)){
            deptCodeList = CollUtil.toList(depts.split(","));
        }
//        List<SysDepartTreeModel> departList = sysDepartService.queryTreeList();
        List<SysDepart> departList = queryDept(dept,false);

        List<SopManageVo> deptList = new ArrayList<>();
        if(null != departList && departList.size()>0){
            //List<SysDepartTreeModel> deptListOne = departList.get(0).getChildren();
            //获取总sop数
            List<SopManageVo> sopTotalList = iSopReviewPlanService.getSoptotalByDept();
            Map<String, SopManageVo> personMap = sopTotalList.stream()
                    .collect(Collectors.toMap(SopManageVo::getDept, sopManageVo -> sopManageVo));

            //回顾统计
            List<SopReviewPlan> planList = iSopReviewPlanService.getSopReviewByDept(year);

            //查询指定月份的周列表，构建key白名单，避免跨月周被错误匹配
            Set<String> monthWeekKeys = new HashSet<>();
            if(StrUtil.isNotBlank(month)){
                MdmWeek mdmWeekQuery = new MdmWeek();
                mdmWeekQuery.setYear(year);
                mdmWeekQuery.setMonth(month);
                List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeekQuery);
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.DATE_MONTH_DAY);
                for (MdmWeek w : weekList) {
                    String weekKey = sdf.format(w.getStartDate()) + "-" + sdf.format(w.getEndDate());
                    monthWeekKeys.add(weekKey);
                }
            }

            for (SopReviewPlan mdmSopReviewPlan : planList) {
                //获取周记录
                String json = mdmSopReviewPlan.getResultData();
                //json字符转json对象
                JSONObject jsonObj = JSON.parseObject(json);
                int planReview = 0;
                int realityReview = 0; //实际回顾数
                String reviewRate = "0%"; //回顾完成率
                int ninePlanReview = 0; //月计划回顾数
                int nineRealityReview = 0; //月实际回顾数
                String nineReviewRate = "0%"; //月回顾完成率
                if(jsonObj != null){
                    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue()+"";
                        //计算年计划回顾-实际回顾数
                        if(value.equals("3")){
                            realityReview++;
                        }
                        //计算年计划回顾数
                        if(value.equals("1")||value.equals("3")){
                            planReview++;
                        }
                        //计算月计划 - 使用周白名单精确匹配，避免跨月周被错误计入
                        if(monthWeekKeys.contains(key)){
//                        ninePlanReview++;
                            //计算9月计划回顾-实际回顾数
                            if(value.equals("3")){
                                nineRealityReview++;
                            }
                            //计算9月计划回顾数
                            if(value.equals("1")||value.equals("3")){
                                ninePlanReview++;
                            }
                        }
                    }
                }

                //判断personMap key 是否存在
                if(personMap.containsKey(mdmSopReviewPlan.getDeptCode())){
                    SopManageVo sopManageVo = personMap.get(mdmSopReviewPlan.getDeptCode());
                    //计划回顾数
                    int planReviewNum = sopManageVo.getPlanReview()!=null?sopManageVo.getPlanReview():0;
                    sopManageVo.setPlanReview(planReviewNum + planReview);
                    //实际回顾数
                    int realityReviewNum = sopManageVo.getRealityReview()!=null?sopManageVo.getRealityReview():0;
                    sopManageVo.setRealityReview(realityReviewNum + realityReview);
                    //回顾完成率
                    if(null != sopManageVo.getRealityReview() && sopManageVo.getRealityReview() > 0 &&
                            null != sopManageVo.getPlanReview() && sopManageVo.getPlanReview()>0){
                        reviewRate = NumberUtil.round(sopManageVo.getRealityReview()*100.0/sopManageVo.getPlanReview(),2)+"%";
                        sopManageVo.setReviewRate(reviewRate);
                    }
                    //9月计划回顾数
                    int ninePlanReviewNum = sopManageVo.getNinePlanReview()!=null?sopManageVo.getNinePlanReview():0;
                    sopManageVo.setNinePlanReview(ninePlanReviewNum + ninePlanReview);
                    //9月实际回顾数
                    int nineRealityReviewNum = sopManageVo.getNineRealityReview()!=null?sopManageVo.getNineRealityReview():0;
                    sopManageVo.setNineRealityReview(nineRealityReviewNum + nineRealityReview);
                    //9月回顾完成率
                    if(null != sopManageVo.getNineRealityReview() && sopManageVo.getNineRealityReview() > 0 &&
                            null != sopManageVo.getNinePlanReview() && sopManageVo.getNinePlanReview()>0){
                        //保留两位小数
                        nineReviewRate = NumberUtil.round(sopManageVo.getNineRealityReview()*100.0/sopManageVo.getNinePlanReview(),2)+"%";
//                        nineReviewRate = (sopManageVo.getNineRealityReview()*100/sopManageVo.getNinePlanReview())+"%";
                        sopManageVo.setNineReviewRate(nineReviewRate);
                    }
                }else{
                    SopManageVo sopManageVo = new SopManageVo();
                    sopManageVo.setTotal(0);
                    //计划回顾数
                    int planReviewNum = sopManageVo.getPlanReview()!=null?sopManageVo.getPlanReview():0;
                    sopManageVo.setPlanReview(planReviewNum + planReview);
                    //实际回顾数
                    int realityReviewNum = sopManageVo.getRealityReview()!=null?sopManageVo.getRealityReview():0;
                    sopManageVo.setRealityReview(realityReviewNum + realityReview);
                    //回顾完成率
                    if(null != sopManageVo.getRealityReview() && sopManageVo.getRealityReview() > 0 &&
                            null != sopManageVo.getPlanReview() && sopManageVo.getPlanReview()>0){
                        reviewRate = NumberUtil.round(sopManageVo.getRealityReview()*100.0/sopManageVo.getPlanReview(),2)+"%";
                        sopManageVo.setReviewRate(reviewRate);
                    }
                    //9月计划回顾数
                    int ninePlanReviewNum = sopManageVo.getNinePlanReview()!=null?sopManageVo.getNinePlanReview():0;
                    sopManageVo.setNinePlanReview(ninePlanReviewNum + ninePlanReview);
                    //9月实际回顾数
                    int nineRealityReviewNum = sopManageVo.getNineRealityReview()!=null?sopManageVo.getNineRealityReview():0;
                    sopManageVo.setNineRealityReview(nineRealityReviewNum + nineRealityReview);
                    //9月回顾完成率
                    if(null != sopManageVo.getNineRealityReview() && sopManageVo.getNineRealityReview() > 0 &&
                            null != sopManageVo.getNinePlanReview() && sopManageVo.getNinePlanReview()>0){
                        //保留两位小数
                        nineReviewRate = NumberUtil.round(sopManageVo.getNineRealityReview()*100.0/sopManageVo.getNinePlanReview(),2)+"%";
//                        nineReviewRate = (sopManageVo.getNineRealityReview()*100/sopManageVo.getNinePlanReview())+"%";
                        sopManageVo.setNineReviewRate(nineReviewRate);
                    }
                    personMap.put(mdmSopReviewPlan.getDeptCode(),sopManageVo);
                }
            }
            //组装统计结果
            for (SysDepart sysDepart : departList) {
                //权限
                if(dept.length()>9 && !sysDepart.getOrgCode().equals(dept)){
                    continue;
                }
                //条件查询
                if(CollUtil.isNotEmpty(deptCodeList) && !deptCodeList.contains(sysDepart.getOrgCode())){
                    continue;
                }

                SopManageVo sopManageVo = new SopManageVo();
                SopManageVo sopManageVoMap = personMap.get(sysDepart.getOrgCode());
                if(null != sopManageVoMap){
                    Integer total = sopManageVoMap.getTotal();
                    sopManageVo.setTotal(null != total?total:0);
                    sopManageVo.setPlanReview(sopManageVoMap.getPlanReview() != null?sopManageVoMap.getPlanReview():0);
                    sopManageVo.setRealityReview(sopManageVoMap.getRealityReview() != null?sopManageVoMap.getRealityReview():0);
                    sopManageVo.setReviewRate(StrUtil.isNotBlank(sopManageVoMap.getReviewRate())?sopManageVoMap.getReviewRate():"0%");
                    sopManageVo.setNinePlanReview(sopManageVoMap.getNinePlanReview() != null?sopManageVoMap.getNinePlanReview():0);
                    sopManageVo.setNineRealityReview(sopManageVoMap.getNineRealityReview() != null?sopManageVoMap.getNineRealityReview():0);
                    sopManageVo.setNineReviewRate(StrUtil.isNotBlank(sopManageVoMap.getNineReviewRate())?sopManageVoMap.getNineReviewRate():"0%");
                }else{
                    sopManageVo.setTotal(0);
                    sopManageVo.setPlanReview(0);
                    sopManageVo.setRealityReview(0);
                    sopManageVo.setNinePlanReview(0);
                    sopManageVo.setNineRealityReview(0);
                    sopManageVo.setNineReviewRate("0%");
                    sopManageVo.setReviewRate("0%");
                }
                sopManageVo.setKey(sysDepart.getId());
                sopManageVo.setTitle(sysDepart.getDepartName());
                deptList.add(sopManageVo);
            }
        }
        return deptList;
    }

    /**
     * 编写职责-编辑
     * @param input
     */
    @Override
    public void updateByWriteTwoRr(JSONObject input) {
        String id = input.getString("id");
        JSONArray writeTwoRrList = input.getJSONArray("writeTwoRrList");
        SopHomePage sopHomePage = getById(id);
        sopHomePage.setWriteTwoRr(JSON.toJSONString(writeTwoRrList));
        updateById(sopHomePage);
    }

    /**
     * 批量 SOP清单删除
     * 1.删除 SOP清单
     * 2.删除 SOP诊断计划
     * 3.删除 SOP管理回顾
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteData(String ids) {
        //排查空
        if(StrUtil.isBlank(ids)){return false;}
        List<String> list = Arrays.asList(ids.split(","));
        //删除 SOP清单
        removeByIds(Arrays.asList(ids.split(",")));
        //获取当前用户
        LoginUser sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
        String username = "";
        if(sysUser != null){
            username = sysUser.getUsername();
        }

        //删除 诊断计划
        LambdaUpdateWrapper<SopStatisticData> updateStatisticWrapper = new LambdaUpdateWrapper<>();
        updateStatisticWrapper.in(SopStatisticData::getSopId, list)
                .set(SopStatisticData::getDelFlag, "1")
                .set(SopStatisticData::getUpdateTime, new Date())
                .set(SopStatisticData::getUpdateBy, username);
        sopStatisticDataService.update(updateStatisticWrapper);

        //删除 SOP管理回顾
        LambdaUpdateWrapper<SopReviewPlan> updateReviewWrapper = new LambdaUpdateWrapper<>();
        updateReviewWrapper.in(SopReviewPlan::getSopId, list)
                .set(SopReviewPlan::getDelFlag, "1")
                .set(SopReviewPlan::getUpdateTime, new Date())
                .set(SopReviewPlan::getUpdateBy, username);
        iSopReviewPlanService.update(updateReviewWrapper);
        return true;
    }
}
