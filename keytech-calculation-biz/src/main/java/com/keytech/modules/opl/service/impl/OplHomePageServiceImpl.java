package com.keytech.modules.opl.service.impl;

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
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.opl.dto.OplWriteTwoRrDto;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.entity.OplReviewPlanFile;
import com.keytech.modules.opl.mapper.OplHomePageMapper;
import com.keytech.modules.opl.service.IOplHomePageService;
import com.keytech.modules.opl.service.IOplReviewPlanFileService;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import com.keytech.modules.opl.vo.OplDepartScheduleTreeVo;
import com.keytech.modules.opl.vo.OplManageVo;
import com.keytech.modules.sop.entity.SopReviewPlan;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
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
 * @Description: Opl 清单
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Service
@Slf4j
public class OplHomePageServiceImpl extends BaseServiceImpl<OplHomePageMapper, OplHomePage> implements IOplHomePageService {
    @Autowired
    private ISysDepartService departService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private IOplReviewPlanService iOplReviewPlanService;
    @Autowired
    private IMdmDictService dicService;
    @Autowired
    private IOplReviewPlanFileService oplReviewPlanFileService;
    /**
     * 查询
     * @param page 分页参数
     * @param oplPage opl对象
     * @param req 请求参数
     * @return 结果
     */
    @Override
    public IPage<OplHomePage> getQueryPageData(Page<OplHomePage> page, OplHomePage oplPage, HttpServletRequest req) {
        //根据Opl名称添加模糊查询
        if(StrUtil.isNotBlank(oplPage.getOplDesc())){
            oplPage.setOplDesc("*"+oplPage.getOplDesc()+"*");
        }
        if(StrUtil.isNotBlank(oplPage.getDeptCode())){
            oplPage.setDeptCode(oplPage.getDeptCode()+"*");
        }
        QueryWrapper<OplHomePage> queryWrapper = QueryGenerator.initQueryWrapper(oplPage, req.getParameterMap());

        //根据查询条件的部门做特殊判断（传入过来的是ID）
        if(StrUtil.isNotBlank(oplPage.getDeptQuery())){
            List<String> deptCodeList = CollUtil.toList(oplPage.getDeptQuery().split(","));
            if(CollUtil.isNotEmpty(deptCodeList)){
                queryWrapper.in("dept_code", deptCodeList);
            }
        }
        //查询opl数据
        queryWrapper.orderByAsc("sort");
        IPage<OplHomePage> iPage = page(page, queryWrapper);
        //无数据直接返回null
        if(iPage.getTotal() <= 0){
            return null;
        }
        //opl集合数据
        List<OplHomePage> opList = iPage.getRecords();
        //获取opl所有id
        List<String> oplIds = CollStreamUtil.toList(opList,OplHomePage::getId);

        //获取频次字典
        List<String> dictList = new ArrayList<>();
        dictList.add("MES3_OPL_FREQUENCY");
        List<MdmDict> oplFrequencyList = dicService.getDictAllOrByGroupCode(dictList);
        //字典转换为map
        Map<String,String> oplFrequencyMap = CollStreamUtil.toMap(oplFrequencyList,MdmDict::getItemVal,MdmDict::getItemKey);

        //根据opl查询所有的Opl回顾计划
        List<OplReviewPlan> reviewPlanList = iOplReviewPlanService.lambdaQuery()
                .in(OplReviewPlan::getOplId,oplIds)
                .isNotNull(OplReviewPlan::getLatestVersionNumber)
                .orderByDesc(OplReviewPlan::getYear)
                .list();
        for(OplReviewPlan oplReviewPlan : reviewPlanList){
            //获取频次中文
            if(oplFrequencyMap.containsKey(oplReviewPlan.getFrequency())){
                oplReviewPlan.setFrequencyName(oplFrequencyMap.get(oplReviewPlan.getFrequency()));
            }
        }

        //查询所有的Opl回顾计划中上传的附件
        List<OplReviewPlanFile> reviewPlanFileList = oplReviewPlanFileService.lambdaQuery().list();
        //处理清单数据
        opList.forEach(oplHomePage ->{

            List<OplReviewPlan> filterList = reviewPlanList.stream()
                    .filter(reviewPlan ->reviewPlan.getOplId().equals(oplHomePage.getId()))
                    .sorted(Comparator.comparing(OplReviewPlan::getCreateTime).reversed())
                    .toList();

            //查询出回顾Opl中上传的附件
            List<OplReviewPlanFile> findFileList =  new ArrayList<>();

            if (CollUtil.isNotEmpty(filterList)){
                //将回顾Opl对象注入
                oplHomePage.setOplReviewPlan(filterList.get(0));

                findFileList = reviewPlanFileList.stream().filter(reviewPlanFile ->
                        null != reviewPlanFile.getReviewPlanId() &&
                                reviewPlanFile.getReviewPlanId().equals(filterList.get(0).getId())
                ).collect(Collectors.toList());
            }

            //从附件中找到最新一条记录，根据week来查找，week格式：1.1-1.10
            OplReviewPlanFile newPlanFile = new OplReviewPlanFile();

            if(CollUtil.isNotEmpty(findFileList)){
                if(findFileList.size() > 1){
                    findFileList.sort((o1, o2) -> {
                        String[] dates1 = o1.getWeek().split("-");
                        String[] dates2 = o2.getWeek().split("-");
                        int startDate1 = parseDate(dates1[0]);
                        int startDate2 = parseDate(dates2[0]);
                        return startDate2 - startDate1;
                    });
                }
                newPlanFile = findFileList.get(0);
            }

            // 处理 writeTwoRr 字段
            String writeTwoRr = oplHomePage.getWriteTwoRr();
            if (StrUtil.isNotEmpty(writeTwoRr) && CollUtil.isNotEmpty(filterList)) {
                List<OplWriteTwoRrDto> writeTwoRrList = JSONArray.parseArray(writeTwoRr, OplWriteTwoRrDto.class);
                //opl文件名称
                List<String> oplTitlesInWriteTwoRrList = writeTwoRrList.stream()
                        .map(OplWriteTwoRrDto::getOplTitle)
                        .toList();
                //有上传附件
                if(StringUtils.isNotEmpty(newPlanFile.getId())){
                    if(!oplTitlesInWriteTwoRrList.contains(newPlanFile.getFileName())){
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateStr = sf.format(newPlanFile.getCreateTime());
                        writeTwoRrList.add(new OplWriteTwoRrDto(newPlanFile.getFileName(), null, null, null, null, null,dateStr));
                    }
                }
                // 获取唯一的 R1 值，并设置到 putIntoNames 字段
                String putIntoNames = writeTwoRrList.stream()
                        .map(OplWriteTwoRrDto::getR1)
                        .filter(StrUtil::isNotEmpty)
                        .distinct()
                        .collect(Collectors.joining(","));

                oplHomePage.setPutIntoNames(putIntoNames);
                oplHomePage.setWriteTwoRrList(writeTwoRrList.stream().filter(w->StrUtil.isNotEmpty(w.getTime())).sorted(Comparator.comparing(OplWriteTwoRrDto::getTime).reversed()).limit(1).collect(Collectors.toList()));
            } else {
                // 如果 writeTwoRr 为空，为每个 oplDescDto 创建新的 OplWriteTwoRrDto
                if(StringUtils.isNotEmpty(newPlanFile.getId())){
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateStr = sf.format(newPlanFile.getCreateTime());
                    List<OplWriteTwoRrDto> newWriteTwoRrList = new ArrayList<>();
                    newWriteTwoRrList.add(new OplWriteTwoRrDto(newPlanFile.getFileName(), null, null, null, null, null,dateStr));
                    oplHomePage.setWriteTwoRrList(newWriteTwoRrList);
                }
            }
        });

        return iPage;
    }

    /**
     * 解析日期字符串并返回整数值。
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
     * @param oplHomePageList opl数据
     */
    @Override
    @Transactional
    public void batchAddOrUpdate(List<OplHomePage> oplHomePageList) {
        //opl名称保存
        Map<String, String> repeatOpl = new HashMap<>();
        oplHomePageList.forEach(homePage -> {
            String oplDesc = homePage.getOplDesc();
            //如果id为空或者temp开头，则说明是新增数据，需要清空id
            if (StrUtil.isBlank(homePage.getId()) || homePage.getId().startsWith("temp")){
                homePage.setId(null);
                //获取key查看是否已经有重复opl名称
                String key = homePage.getDeptCode() +'-'+ homePage.getLineCode() +'-'+ homePage.getOplDesc();
                if(repeatOpl.containsKey(key)){
                    throw new JeecgBootException("Opl名称:"+oplDesc+" 重复");
                }
                repeatOpl.put(key, key);
            }
            long count = count(new LambdaQueryWrapper<OplHomePage>()
                    .ne(StrUtil.isNotBlank(homePage.getId()), OplHomePage::getId, homePage.getId())
                    .eq(OplHomePage::getOplDesc, oplDesc)
                    .eq(OplHomePage::getLineCode, homePage.getLineCode())
                    .eq(OplHomePage::getDeptCode, homePage.getDeptCode()));
            if (count > 0){
                throw new JeecgBootException("Opl名称:"+oplDesc+" 重复");
            }
        });
        this.saveOrUpdateBatch(oplHomePageList);
    }

    /**
     * 导出
     * @param request 请求
     * @param response 响应
     * @param oplHomePageClass 类
     * @return 结果
     */
    @Transactional
    @Override
    public Result<?> importExcelGenCode(HttpServletRequest request, HttpServletResponse response, Class<OplHomePage> oplHomePageClass) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        //错误信息
        List<String> errorMessage = new ArrayList<>();
        //获取部门
        List<SysDepart> departList = departService.list();
        Map<String, String> departListMap = CollStreamUtil.toMap(departList, SysDepart::getOrgCode, SysDepart::getDepartName);
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<OplHomePage> list = ExcelImportUtil.importExcel(file.getInputStream(), oplHomePageClass, params);
                List<OplHomePage> result = new ArrayList<>();
                int lineNumber = 4;
                for (int i = 0; i < list.size(); i++) {
                    OplHomePage oplHomePage = list.get(i);

                    if (!departListMap.containsKey(oplHomePage.getFactoryCode())) {
                        errorLines++;
                        lineNumber = i + lineNumber;
                        errorMessage.add("第" + lineNumber + "行 工厂不存在，忽略导入。");
                    }

                    String dept = oplHomePage.getDeptCode();
                    if (!departListMap.containsKey(dept)) {
                        errorLines++;
                        lineNumber = i + lineNumber;
                        errorMessage.add("第" + lineNumber + "行 部门不存在，忽略导入。");
                    }
                    String line = oplHomePage.getLineCode();
                    if(null == line){
                        oplHomePage.setLineCode("");
                    }else if(!line.isEmpty() && !departListMap.containsKey(line)){
                        errorLines++;
                        lineNumber = i + lineNumber;
                        errorMessage.add("第" + lineNumber + "行 产线不存在，忽略导入。");
                    }
                    result.add(oplHomePage);
                    successLines++;
                }
                long start = System.currentTimeMillis();
                if(errorLines==0){
                    saveOrUpdateBatch(result);
                }
                log.info("消耗时间{}毫秒", System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }


    /**
     * 诊断统计
     * @param year 年份
     * @param depts 部门
     * @param dept 部门
     * @return 结果
     */
    @Override
    public List<OplDepartScheduleTreeVo> getOplDept(String year, String depts, String dept) {
        //获取部门
        List<String> deptCodeList = new ArrayList<>();
        if(StrUtil.isNotBlank(depts)){
            deptCodeList = CollUtil.toList(depts.split(","));
        }
        //获取部门树
        List<SysDepartTreeModel> departList = queryTreeList(deptCodeList, dept);

        List<OplDepartScheduleTreeVo> deptList = new ArrayList<>();
        if(CollUtil.isNotEmpty(departList)){
            //获取opl统计数
            List<OplManageVo> oplTotalList = iOplReviewPlanService.getOpltotal(depts);
            //按产线分组
            Map<String, List<OplManageVo>> groupOplMap = oplTotalList.stream()
                    .collect(Collectors.groupingBy(OplManageVo::getDept));
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
                        sysDepartTreeChild.setOrgCode(sysUser.getOrgCode());
                        sysChildDeptList.add(sysDepartTreeChild);
                    }
                    sysDepartTreeModel.setChildren(sysChildDeptList);
                }
                //转换部门为统计对象
                OplDepartScheduleTreeVo departTreeModelVo = new OplDepartScheduleTreeVo();
                BeanUtil.copyProperties(sysDepartTreeModel, departTreeModelVo);
                //Opl数
                int oplNumber = 0;
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
                    List<OplDepartScheduleTreeVo> childDeptList = new ArrayList<>();
                    for(SysDepartTreeModel childTree : sysDepartTreeModel.getChildren()){
                        //新的统计对象
                        OplDepartScheduleTreeVo departTreeModelChildVo = new OplDepartScheduleTreeVo();
                        BeanUtil.copyProperties(childTree, departTreeModelChildVo);
                        departTreeModelChildVo.setChildren(null);
                        //判断是否有opl数据
                        if(groupOplMap.containsKey(departTreeModelChildVo.getOrgCode())){
                            departTreeModelChildVo.setOplNumber(groupOplMap.get(departTreeModelChildVo.getOrgCode()).size());
                            //累加总opl数
                            oplNumber += departTreeModelChildVo.getOplNumber();
                        } else {
                            departTreeModelChildVo.setOplNumber(0);
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
                        //设置各项统计数
                        departTreeModelChildVo.setPlanSuccess(planSuccessTemp);
                        departTreeModelChildVo.setPlan(planTemp);
                        departTreeModelChildVo.setPlanDelay(planDelayTemp);
                        departTreeModelChildVo.setPlanNumber(planNumberTemp);
                        childDeptList.add(departTreeModelChildVo);
                    }
                    departTreeModelVo.setChildren(childDeptList);
                }
                departTreeModelVo.setOplNumber(oplNumber);
                departTreeModelVo.setPlanSuccess(planSuccess);
                departTreeModelVo.setPlan(plan);
                departTreeModelVo.setPlanDelay(planDelay);
                departTreeModelVo.setPlanNumber(planNumber);
                deptList.add(departTreeModelVo);
            }
        }
        return deptList;
    }

    /**
     * 获取部门产线
     * @param deptCodeList 所有部门数据
     * @param dept 要筛选的部门
     * @return 树形部门数据
     */
    public List<SysDepartTreeModel> queryTreeList(List<String> deptCodeList, String dept) {
        List<SysDepartTreeModel> list = new ArrayList<>();
        //获取产线
        LambdaQueryWrapper<SysDepart> queryLine = new LambdaQueryWrapper<SysDepart>();
        queryLine.eq(SysDepart::getOrgType,'6');

        //获取部门
        List<SysDepart> lineList = sysDepartService.list(queryLine);
        List<SysDepart> departList = queryDept(dept,true);
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
     * @param dept 部门编码
     * @return 结果
     */
    public List<SysDepart> queryDept(String dept,Boolean isF) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
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
     * Opl管理回顾统计
     * @param year 年份
     * @param month 月份
     * @param depts 查询部门
     * @param dept 用户登录部门
     * @return 结果
     */
    @Override
    public List<OplManageVo> getOplManage(String year, String month, String depts, String dept) {
        //获取部门
        List<String> deptCodeList = new ArrayList<>();
        if(StrUtil.isNotBlank(depts)){
            deptCodeList = CollUtil.toList(depts.split(","));
        }
        List<SysDepart> departList = queryDept(dept,false);

        List<OplManageVo> deptList = new ArrayList<>();
        if(CollUtil.isNotEmpty(departList)){
            //获取总opl数
            List<OplManageVo> oplTotalList = iOplReviewPlanService.getOpltotalByDept();
            Map<String, OplManageVo> personMap = oplTotalList.stream()
                    .collect(Collectors.toMap(OplManageVo::getDept, oplManageVo -> oplManageVo));

            //回顾统计
            List<OplReviewPlan> planList = iOplReviewPlanService.getOplReviewByDept(year);
            for (OplReviewPlan mdmOplReviewPlan : planList) {
                //计划数
                int planReview = 0;
                //实际回顾数
                int realityReview = 0;
                //回顾完成率
                String reviewRate;
                //月计划回顾数
                int monthPlanReview = 0;
                //月实际回顾数
                int monthRealityReview = 0;
                //月回顾完成率
                String monthReviewRate;
                if(StrUtil.isNotBlank(mdmOplReviewPlan.getResultData())){
                    //json字符转json对象
                    JSONObject resultData = JSON.parseObject(mdmOplReviewPlan.getResultData());
                    for (Map.Entry<String, Object> entry : resultData.entrySet()) {
                        String value = entry.getValue()+"";
                        String key = entry.getKey();
                        //计算年计划回顾数
                        if(value.equals("1")||value.equals("3")){
                            planReview++;
                        }
                        //计算年计划回顾-实际回顾数
                        if(value.equals("3")){
                            realityReview++;
                        }
                        if(key.startsWith(month+".") || key.contains("-"+month+".")){
                            //计算月计划回顾-实际回顾数
                            if(value.equals("3")){
                                monthRealityReview++;
                            }
                            //计算月计划回顾数
                            if(value.equals("1")||value.equals("3")){
                                monthPlanReview++;
                            }
                        }
                    }
                }

                //判断personMap key 是否存在
                if(personMap.containsKey(mdmOplReviewPlan.getDeptCode())){
                    OplManageVo oplManageVo = personMap.get(mdmOplReviewPlan.getDeptCode());
                    //计划回顾数
                    int planReviewNum = oplManageVo.getPlanReview()!=null?oplManageVo.getPlanReview():0;
                    oplManageVo.setPlanReview(planReviewNum + planReview);
                    //实际回顾数
                    int realityReviewNum = oplManageVo.getRealityReview()!=null?oplManageVo.getRealityReview():0;
                    oplManageVo.setRealityReview(realityReviewNum + realityReview);
                    //回顾完成率
                    if(null != oplManageVo.getRealityReview() && oplManageVo.getRealityReview() > 0 &&
                            null != oplManageVo.getPlanReview() && oplManageVo.getPlanReview()>0){
                        reviewRate = NumberUtil.round(oplManageVo.getRealityReview()*100.0/oplManageVo.getPlanReview(),2)+"%";
                        oplManageVo.setReviewRate(reviewRate);
                    }
                    //月计划回顾数
                    int monthPlanReviewNum = oplManageVo.getMonthPlanReview()!=null?oplManageVo.getMonthPlanReview():0;
                    oplManageVo.setMonthPlanReview(monthPlanReviewNum + monthPlanReview);
                    //月实际回顾数
                    int monthRealityReviewNum = oplManageVo.getMonthRealityReview()!=null?oplManageVo.getMonthRealityReview():0;
                    oplManageVo.setMonthRealityReview(monthRealityReviewNum + monthRealityReview);
                    //月回顾完成率
                    if(null != oplManageVo.getMonthRealityReview() && oplManageVo.getMonthRealityReview() > 0 &&
                            null != oplManageVo.getMonthPlanReview() && oplManageVo.getMonthPlanReview()>0){
                        //保留两位小数
                        monthReviewRate = NumberUtil.round(oplManageVo.getMonthRealityReview()*100.0/oplManageVo.getMonthPlanReview(),2)+"%";
                        oplManageVo.setMonthReviewRate(monthReviewRate);
                    }
                }else{
                    OplManageVo oplManageVo = new OplManageVo();
                    oplManageVo.setTotal(0);
                    //计划回顾数
                    int planReviewNum = oplManageVo.getPlanReview()!=null?oplManageVo.getPlanReview():0;
                    oplManageVo.setPlanReview(planReviewNum + planReview);
                    //实际回顾数
                    int realityReviewNum = oplManageVo.getRealityReview()!=null?oplManageVo.getRealityReview():0;
                    oplManageVo.setRealityReview(realityReviewNum + realityReview);
                    //回顾完成率
                    if(null != oplManageVo.getRealityReview() && oplManageVo.getRealityReview() > 0 &&
                            null != oplManageVo.getPlanReview() && oplManageVo.getPlanReview()>0){
                        reviewRate = NumberUtil.round(oplManageVo.getRealityReview()*100.0/oplManageVo.getPlanReview(),2)+"%";
                        oplManageVo.setReviewRate(reviewRate);
                    }
                    //月计划回顾数
                    int monthPlanReviewNum = oplManageVo.getMonthPlanReview()!=null?oplManageVo.getMonthPlanReview():0;
                    oplManageVo.setRealityReview(monthPlanReviewNum + monthPlanReview);
                    //月实际回顾数
                    int monthRealityReviewNum = oplManageVo.getMonthRealityReview()!=null?oplManageVo.getMonthRealityReview():0;
                    oplManageVo.setRealityReview(monthRealityReviewNum + monthRealityReview);
                    //月回顾完成率
                    if(null != oplManageVo.getMonthRealityReview() && oplManageVo.getMonthRealityReview() > 0 &&
                            null != oplManageVo.getMonthPlanReview() && oplManageVo.getMonthPlanReview()>0){
                        //保留两位小数
                        monthReviewRate = NumberUtil.round(oplManageVo.getMonthRealityReview()*100.0/oplManageVo.getMonthPlanReview(),2)+"%";
                        oplManageVo.setMonthReviewRate(monthReviewRate);
                    }
                    personMap.put(mdmOplReviewPlan.getDeptCode(),oplManageVo);
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

                OplManageVo oplManageVo = new OplManageVo();
                OplManageVo oplManageVoMap = personMap.get(sysDepart.getOrgCode());
                if(null != oplManageVoMap){
                    Integer total = oplManageVoMap.getTotal();
                    oplManageVo.setTotal(null != total?total:0);
                    oplManageVo.setPlanReview(oplManageVoMap.getPlanReview() != null?oplManageVoMap.getPlanReview():0);
                    oplManageVo.setRealityReview(oplManageVoMap.getRealityReview() != null?oplManageVoMap.getRealityReview():0);
                    oplManageVo.setReviewRate(StrUtil.isNotBlank(oplManageVoMap.getReviewRate())?oplManageVoMap.getReviewRate():"0%");
                    oplManageVo.setMonthPlanReview(oplManageVoMap.getMonthPlanReview() != null?oplManageVoMap.getMonthPlanReview():0);
                    oplManageVo.setMonthRealityReview(oplManageVoMap.getMonthRealityReview() != null?oplManageVoMap.getMonthRealityReview():0);
                    oplManageVo.setMonthReviewRate(StrUtil.isNotBlank(oplManageVoMap.getMonthReviewRate())?oplManageVoMap.getMonthReviewRate():"0%");
                }else{
                    oplManageVo.setTotal(0);
                    oplManageVo.setPlanReview(0);
                    oplManageVo.setRealityReview(0);
                    oplManageVo.setMonthPlanReview(0);
                    oplManageVo.setMonthRealityReview(0);
                    oplManageVo.setMonthReviewRate("0%");
                    oplManageVo.setReviewRate("0%");
                }
                oplManageVo.setKey(sysDepart.getId());
                oplManageVo.setTitle(sysDepart.getDepartName());
                deptList.add(oplManageVo);
            }
        }
        return deptList;
    }

    /**
     * 编写职责-编辑
     */
    @Override
    public void updateByWriteTwoRr(JSONObject input) {
        String id = input.getString("id");
        JSONArray writeTwoRrList = input.getJSONArray("writeTwoRrList");
        OplHomePage oplHomePage = getById(id);
        oplHomePage.setWriteTwoRr(JSON.toJSONString(writeTwoRrList));
        updateById(oplHomePage);
    }

    /**
     * 批量 Opl清单删除
     * 1.删除 Opl清单
     * 2.删除 Opl诊断计划
     * 3.删除 Opl管理回顾
     * @param ids 主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteData(String ids) {
        //排查空
        if(StrUtil.isBlank(ids)){
            return false;
        }
        List<String> list = Arrays.asList(ids.split(","));
        //删除 Opl清单
        removeByIds(Arrays.asList(ids.split(",")));
        //获取当前用户
        LoginUser sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
        String username = "";
        if(sysUser != null){
            username = sysUser.getUsername();
        }
        //删除 Opl管理回顾
        LambdaUpdateWrapper<OplReviewPlan> updateReviewWrapper = new LambdaUpdateWrapper<>();
        updateReviewWrapper.in(OplReviewPlan::getOplId, list)
                .set(OplReviewPlan::getDelFlag, "1")
                .set(OplReviewPlan::getUpdateTime, new Date())
                .set(OplReviewPlan::getUpdateBy, username);
        iOplReviewPlanService.update(updateReviewWrapper);
        return true;
    }
}
