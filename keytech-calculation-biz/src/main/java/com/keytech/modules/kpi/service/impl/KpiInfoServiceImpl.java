package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.kpi.dto.DepartDto;
import com.keytech.modules.kpi.dto.KPiConfigDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiPlanData;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.mapper.KpiInfoMapper;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiPlanDataService;
import com.keytech.modules.kpi.service.IKpiTypeService;
import com.keytech.modules.kpi.util.BizConstant;
import com.keytech.modules.kpi.util.DataPageUtils;
import com.keytech.modules.kpi.util.TreeListUtil;
import com.keytech.modules.kpi.vo.ControlConfigurationVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.PmsUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.dict.service.AutoPoiDictServiceI;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @Description: KPI清单
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Service
@Slf4j
public class KpiInfoServiceImpl extends BaseServiceImpl<KpiInfoMapper, KpiInfo> implements IKpiInfoService {

    @Autowired
    private KpiInfoMapper kpiInfoMapper;
    @Autowired
    private IKpiTypeService kpiTypeService;
    @Autowired
    private AutoPoiDictServiceI autoPoiDictServiceI;
    @Autowired
    private ISysDepartService departService;
    @Autowired
    private IKpiPlanDataService kpiPlanDataService;
    @Autowired
    private ISysUserService sysUserService;


    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    /**
     * 层级
     * kpi 树 分类(项目)和kpi
     *
     * @param dto
     * @return
     */
    @Override
    public Object listTree(KpiTypeDto dto) {
        //kpi类型
        String typeId = dto.getPerentId();
        //kpi名称
        String kpiName = dto.getKpiName();

        //当前部门
        String dept = dto.getDept();
        //是否控制当前部门的权限
//        if(!dto.getIsShowAll()){
//            //当前部门
//            dept = dto.getDept();
//        }
        LambdaQueryWrapper<KpiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(dto.getYear()), KpiInfo::getYear,dto.getYear());
        if(Objects.nonNull(typeId)){
            //查询当前类型以及对应的子类,考虑2层层级
            //先查询出所有的kpi类型
            List<KpiType> listType = kpiTypeService.lambdaQuery().and(q-> q.eq(KpiType::getId,typeId).or().eq(KpiType::getParentId,typeId)).list();
            List<String> typeIds = CollStreamUtil.toList(listType,KpiType::getId);
            queryWrapper.in(KpiInfo::getPerentId,typeIds);
        }
        if(StrUtil.isNotBlank(kpiName)){
            queryWrapper.like(KpiInfo::getKpiName,kpiName);
        }

        if(StrUtil.isNotBlank(dept)){
            queryWrapper.likeRight(KpiInfo::getDeptCode,dept);
        }
        //填写部门(查询条件)
        queryWrapper.eq(StrUtil.isNotBlank(dto.getInputDeptQuery()), KpiInfo::getInputPermission, dto.getInputDeptQuery());
        //层级部门(查询条件)
        queryWrapper.eq(StrUtil.isNotBlank(dto.getDeptCode()), KpiInfo::getDeptCode, dto.getDeptCode());

        queryWrapper.orderByAsc(KpiInfo::getSort).orderByDesc(KpiInfo::getCreateTime);
        //符合条件的kpi清单
        List<KpiInfo> listKpiInfo = list(queryWrapper);

        //查询所有的部门
        List<DepartDto> listDepart = kpiTypeService.deptList("");
        //查询所有的KPI类型
        List<KpiType> listType = kpiTypeService.list();

        //对层级部门、填写部门、kpi类型转中文说明（列表树中二层级无法用注解转成汉字，此处单独处理）
        for(KpiInfo kpiInfo : listKpiInfo){
            //层级部门转汉字
            Optional<DepartDto> departOptional = listDepart.stream().filter(depart->depart.getOrgCode().equals(kpiInfo.getDeptCode())).findFirst();
            if(departOptional.isPresent()){
                kpiInfo.setDeptName(departOptional.get().getDepartName());
            }
            //填写部门转汉字
            Optional<DepartDto> inputDepartOptional = listDepart.stream().filter(depart->depart.getOrgCode().equals(kpiInfo.getInputPermission())).findFirst();
            if(inputDepartOptional.isPresent()){
                kpiInfo.setInputDeptName(inputDepartOptional.get().getDepartName());
            }
            //KPI类型转汉字
            Optional<KpiType> kpiTypeOptional = listType.stream().filter(kpiType->
                    Objects.nonNull(kpiInfo.getPerentId()) &&
                            kpiType.getId().toString().equals(kpiInfo.getPerentId().toString())
            ).findFirst();
            if(kpiTypeOptional.isPresent()){
                kpiInfo.setKpiTypeName(kpiTypeOptional.get().getKpiType());
            }
        }


        //组装成tree类型数据
        List<KpiInfo> treeList = getTreeList(listKpiInfo);
        //最下级的查询
        Page<KpiInfo> kpiInfoPage = DataPageUtils.dataStreamPage(treeList, dto.getPageNo(), dto.getPageSize(), kpiInfo -> true);
        TreeListUtil.getTreeList(kpiInfoPage.getRecords(), KpiInfo::getId, KpiInfo::getParentId, KpiInfo::getChildren);
//                .stream()
//                .filter(M -> Objects.isNull(M.getParentId()))
//                .collect(Collectors.toList());
        return kpiInfoPage;
    }


    /**
     * 将传入的kpi集合组装成树，去重
     * @param kpiInfoList
     * @return
     */
    public List<KpiInfo> getTreeList(List<KpiInfo> kpiInfoList){
        //所有注入到child中的元素都需要删除
        List<KpiInfo> removeAllList = new ArrayList<>();
        //循环2次，进行组装
        for(KpiInfo kpiInfoF : kpiInfoList){
            //当前循环中注入到child中的元素
            List<KpiInfo> removeList = new ArrayList<>();
            for(KpiInfo kpiInfoS : kpiInfoList){
                //第一层KPICODE不为空
                if(StrUtil.isNotBlank(kpiInfoF.getKpiCode())){
                    //第二层KPI父节点id不为空
                    if(Objects.nonNull(kpiInfoS.getParentId()) && !kpiInfoS.getId().equals(kpiInfoF.getId())){
                        if(String.valueOf(kpiInfoS.getParentId()).equals(kpiInfoF.getKpiCode())){
                            removeList.add(kpiInfoS);
                        }
                    }
                }
            }
            //注入到子节点中
            kpiInfoF.setChildren(removeList);
            //存储到待删除的集合中
            removeAllList.addAll(removeList);
        }
        //将所有removeAllList，删除掉
        kpiInfoList.removeAll(removeAllList);
        return kpiInfoList;
    }


    /**
     * 添加KPI,生成KPI CODE
     * @param kpiInfo
     */
    @Override
    public void kpiSave(KpiInfo kpiInfo) {
        //保存
        save(kpiInfo);
        //生成KPI CODE
        String key = String.valueOf(kpiInfo.getId());
//        if(Objects.nonNull(kpiInfo.getParentId())){
//            key = StrUtil.join("", kpiInfo.getParentId(), key);
//        }
        kpiInfo.setKpiCode(key);
        updateById(kpiInfo);
    }


    /**
     * 单位部门树
     * @param isRoot
     * @param factory
     * @return
     */
    @Override
    public Object queryDeptTreeList(String isRoot, String factory) {
        //获取部门
        List<DepartDto> listDepart = kpiTypeService.deptList(isRoot);
        if(null != listDepart){
            DepartDto departDto =  listDepart.get(0);
            departDto.setParentId(null);
        }
        //生成树
        return TreeListUtil.getTreeList(listDepart, DepartDto::getId, DepartDto::getParentId, DepartDto::getChildren).stream()
                .filter(M -> Objects.isNull(M.getParentId()))
                .collect(Collectors.toList());
    }

    /**
     * 导出
     * @param request
     * @param kpiInfo
     * @return
     */
    @Override
    public ModelAndView kpiInfoExportXls(HttpServletRequest request, KpiInfo kpiInfo) {
        String[] replace = autoPoiDictServiceI.queryDict("mes3_kpi_type", "id", "kpi_type");
        request.setAttribute(BizConstant.KPI_TYPE_DATA_DICT, replace);
        // Step.1 获取导出数据
        List<KpiInfo> pageList = doGetDataList(request, kpiInfo);
        // Step.2 获取导出人
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<KpiInfo> exportList = null;
        // 过滤选中数据
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            exportList = pageList.stream().filter(item -> selectionList.contains(getId(item))).collect(Collectors.toList());
        } else {
            exportList = pageList;
        }
        // Step.3 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        String title = "KPI清单";
        mv.addObject(NormalExcelConstants.FILE_NAME, title); //此处设置的filename无效 ,前端会重更新设置一下
        mv.addObject(NormalExcelConstants.CLASS, KpiInfo.class);
        //update-begin--Author:liusq  Date:20210126 for：图片导出报错，ImageBasePath未设置--------------------
        ExportParams exportParams = new ExportParams(title + "报表", "导出人:" + sysUser.getRealname(), title);
        exportParams.setImageBasePath(upLoadPath);
        //update-end--Author:liusq  Date:20210126 for：图片导出报错，ImageBasePath未设置----------------------
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        return mv;
    }

    protected List<KpiInfo> doGetDataList(HttpServletRequest request, KpiInfo kpiInfo) {
        //年like查询
        if(StrUtil.isNotBlank(kpiInfo.getYear())){kpiInfo.setYear("*"+kpiInfo.getYear()+"*");}
        QueryWrapper<KpiInfo> queryWrapper = QueryGenerator.initQueryWrapper(kpiInfo, request.getParameterMap());
        return list(queryWrapper);
    }

    private String getId(KpiInfo item) {
        try {
            return PropertyUtils.getProperty(item, "id").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //KPI清单导入
    @Override
    public Result<?> kpiImportExcel(HttpServletRequest request, HttpServletResponse response,Class<KpiInfo> kpiInfoClass) throws IOException{
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        //获取用户(匹配R A C)
        List<SysUser> userList = sysUserService.list();
        Map<String, SysUser> userMap = userList.stream().collect(Collectors.toMap(bean -> bean.getRealname(), bean -> bean, (k1, k2) -> k2));
        AtomicLong counter = new AtomicLong(1);
        Long maxId = kpiInfoMapper.kpiInfomaxId();
        if (Objects.nonNull(maxId)) {
            counter.set(maxId + 1);
        }
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        //需要新增的KPI
        List<KpiInfo> resultSave = new ArrayList<>();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            //去重
            List<KpiInfo> infoList = list();
            Map<String, KpiInfo> kpiInfoMap = infoList.stream().collect(Collectors.toMap(bean -> bean.getDeptCode() + bean.getKpiName(), bean -> bean, (k1, k2) -> k2));
            try {
                //kpi类型
                //List<KpiType> kpiTypes = kpiTypeService.list();
                //部门集合
                List<DepartDto> listDepart = kpiTypeService.deptList("");
                Map<String,String> departMap = CollStreamUtil.toMap(listDepart, bean -> bean.getOrgCode(), DepartDto::getDepartName);
                List<KpiInfo> list = ExcelImportUtil.importExcel(file.getInputStream(), kpiInfoClass, params);

                List<KpiInfo> resultUpdate = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    KpiInfo kpiInfoEntry = list.get(i);
                    String dept = kpiInfoEntry.getDeptCode();
                    //判断新增的KPI是否在系统中已经存在
                    if (kpiInfoMap.containsKey(dept + kpiInfoEntry.getKpiName()) && null == kpiInfoEntry.getId()) {
                        errorLines++;
                        int lineNumber = i + 1;
                        errorMessage.add("第 " + lineNumber + " " + kpiInfoEntry.getKpiName() + "已存在，忽略导入。");
                        continue;
                    }
                    if (!departMap.containsKey(dept)) {
                        errorLines++;
                        int lineNumber = i + 1;
                        errorMessage.add("第 " + lineNumber + " " + "部门层级不存在，忽略导入。");
                        continue;
                    }
                    //层级id
//                    String kpiTypeName = kpiInfoEntry.getKpiTypeName();
//                    String typeId = "";
//                    Optional<KpiType> kpiTypesOptional =  kpiTypes.stream().filter(kpiType -> kpiType.getKpiType().equals(kpiInfoEntry.getKpiTypeName())).findFirst();
//                    if(!kpiTypesOptional.isPresent()){
                    if(kpiInfoEntry.getPerentId() == null){
                        errorLines++;
                        int lineNumber = i + 1;
//                        errorMessage.add("第 " + lineNumber + " " + kpiTypeName + "不存在，忽略导入。");
                        errorMessage.add("第 " + lineNumber + "KPI分类不存在，忽略导入。");
                        continue;
                    }
                    if(StrUtil.isNotBlank(dept)){
                        kpiInfoEntry.setFactoryCode(dept.substring(0,9));
                    }
//                    else{
//                        typeId = kpiTypesOptional.get().getId().toString();
//                    }
                    KpiInfo kpiInfo = new KpiInfo();
                    BeanUtil.copyProperties(kpiInfoEntry, kpiInfo);
                    //匹配 R A C
                    if(StrUtil.isNotBlank(kpiInfo.getProvideBy())){
                        //R
                        SysUser user = userMap.get(kpiInfo.getProvideBy());
                        if(user != null){
                            kpiInfo.setProvideBy(user.getId());
                        }
                    }
                    if(StrUtil.isNotBlank(kpiInfo.getPossessBy())){
                        //A
                        SysUser user = userMap.get(kpiInfo.getPossessBy());
                        if(user != null){
                            kpiInfo.setPossessBy(user.getId());
                        }
                    }
                    if(StrUtil.isNotBlank(kpiInfo.getConsultedBy())){
                        //C
                        SysUser user = userMap.get(kpiInfo.getConsultedBy());
                        if(user != null){
                            kpiInfo.setConsultedBy(user.getId());
                        }
                    }

                    //kpiInfo.setPerentId(typeId);
                    if(null != kpiInfo.getId()){
                        resultUpdate.add(kpiInfo);
                    }else{
                        resultSave.add(kpiInfo);
                    }

                    successLines++;
                }

                long start = System.currentTimeMillis();
                //修改
                if(resultUpdate.size() > 0){
                    saveOrUpdateBatch(resultUpdate);
                }
                //新增
                if(resultSave.size() > 0){
                    saveBatch(resultSave);
                }

                //保存了result，生成了id，反向生成kpicode
//                for(KpiInfo kpiInfo : resultSave){
//                    String key = String.valueOf(kpiInfo.getId());
////                    if(Objects.nonNull(kpiInfo.getParentId())){
////                        key = StrUtil.join("", kpiInfo.getParentId(), key);
////                    }
//                    kpiInfo.setKpiCode(key);
//                }
//                //重新执行，将kpicode更新
//                saveOrUpdateBatch(resultSave);
                log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
                //update-end-author:taoyan date:20190528 for:批量插入数据
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    //保存了result，生成了id，反向生成kpicode
                    for(KpiInfo kpiInfo : resultSave){
                        String key = String.valueOf(kpiInfo.getId());
                        kpiInfo.setKpiCode(key);
                    }
                    //重新执行，将kpicode更新
                    saveOrUpdateBatch(resultSave);

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



    /**
     * 获取KPI集合
     * @param dto
     * @return
     */
    @Override
    public List<KPiConfigDto> listKpi(KpiReportValueDto dto) {
        QueryWrapper<KpiInfo> queryWrapper = Wrappers.query();
        //这个参数可能是分类可能是kpiCode, 改成只能是分类
        String parentId = dto.getTypeId();
        if (StrUtil.isNotBlank(parentId)) {
            //queryWrapper.and(q -> q.eq("y.id", parentId).or().eq("y.parent_id", parentId).or().eq("c.perent_id", parentId).or().eq("c.kpi_code", parentId));
            queryWrapper.and(q -> q.eq("y.id", parentId).or().eq("y.parent_id", parentId).or().eq("c.perent_id", parentId));
//            queryWrapper.and(q -> q.eq("c.perent_id", parentId));
        }
        queryWrapper.likeRight(StrUtil.isNotBlank(dto.getFactory()), "c.factory_code", dto.getFactory());

        //是否可用
        queryWrapper.eq("c.active_look", 1);
        //匹配年
        queryWrapper.like("c.year", dto.getYear());
        queryWrapper.eq("c.del_flag", 0);

        //排序
        if("report".equals(dto.getTypeSort())){
            //KPI日报
            queryWrapper.orderByAsc("c.report_sort");
            //是否KPI日报中展示
            queryWrapper.eq("c.visible", 1);
        }else{
            //录入
            queryWrapper.orderByAsc("c.input_sort");
            //是否在录入中展示
            queryWrapper.eq("c.visible_write", 1);
        }
        //录入权限(填写部门)[当前人部门]
        if("1".equals(dto.getVisibleWrite()+"")){
            //录入部门 like 工厂查询到所有
            queryWrapper.likeRight(StrUtil.isNotBlank(dto.getDept()), "c.input_permission", dto.getDept());
        }else{
            //查看
            queryWrapper.likeRight(StrUtil.isNotBlank(dto.getDept()), "c.dept_code", dto.getDept());
        }
        //KPI code
        if(StrUtil.isNotBlank(dto.getKpiCode())){
            queryWrapper.eq("c.kpi_code", dto.getKpiCode());
        }

        //层级部门
        if(StrUtil.isNotBlank(dto.getDeptQuery())){
            String[] deptArr = dto.getDeptQuery().split(",");
            if("1".equals(dto.getDeptQueryType())){
                String dept = deptArr.length > 0? deptArr[0] : "";
                queryWrapper.likeRight("c.dept_code", dept);
            }else{
                queryWrapper.in("c.dept_code", deptArr);
            }
        }
        //KPI名称
        queryWrapper.like(StrUtil.isNotBlank(dto.getKpiName()),"c.kpi_name", dto.getKpiName());
        //填报部门(查询条件)
        if(StrUtil.isNotBlank(dto.getInputDeptQuery())){
            String[] inputDeptQueryArr = dto.getInputDeptQuery().split(",");
            queryWrapper.in("c.input_permission", inputDeptQueryArr);
        }
        //标签
        if(StrUtil.isNotBlank(dto.getLabel())){
//            queryWrapper.in("c.label", dto.getLabel());
            queryWrapper.like("c.label", dto.getLabel());
        }

        List<KPiConfigDto> kpiList = new ArrayList();
        List<KPiConfigDto> kpiAllList = kpiInfoMapper.listKpi(queryWrapper);
        //是否显示二级kpi
        if (!dto.getShowChildrenKpi()) {
            //显示上级kpi(过滤没有父节点的kpi[kpi层级])
            //提取所有kpi的id
            List<String> allIds = kpiAllList.stream()
                    .map(object -> String.valueOf(object.getKpiCode()))
                    .collect(Collectors.toList());
            //KpiParentId为空  或者 KpiParentId不为空,且没有上级
            kpiList= kpiAllList.stream()
                    .filter(node -> (node.getKpiParentId() == null) || (node.getKpiParentId()!= null && !allIds.contains(node.getKpiParentId())))
                    .collect(Collectors.toList());
        }else{
            //显示二级以下kpi
            kpiList = kpiAllList;
        }
        return kpiList;
    }


    public <T> List<T> kpiSortList(KpiTypeDto kpiTypeDto, Map<String, T> kpiCodeMap) {
        List<T> kpiInfoList = new ArrayList<>();
        List<Tree<String>> treeList = kpiTree(kpiTypeDto);
        if (CollUtil.isNotEmpty(treeList) && CollUtil.isNotEmpty(kpiCodeMap)) {
            getKpiTargetList(treeList, kpiInfoList, kpiCodeMap);
        }
        return kpiInfoList;
    }


    /**
     * KPI树，待分类
     * @param dto
     * @return
     */
    public List<Tree<String>> kpiTree(KpiTypeDto dto) {
//        String dept = dto.getDept();
        String factory = dto.getFactory();
        String kpiName = dto.getKpiName();
        String sortType = dto.getSortType();
//        QueryWrapper<KpiType> query = Wrappers.query();
//        query.eq(StrUtil.isNotBlank(dto.getOrgCode()), "o.hierarchy", dto.getOrgCode());
//        query.likeRight(StrUtil.isNotBlank(factory), "o.factory", factory);
//        List<KpiType> kpiTypes = typeService.listByCode(query);
        //查询所有kpi分类
        List<KpiType> kpiTypes = kpiTypeService.lambdaQuery().orderByAsc(KpiType::getSort).list();

        List<TreeNode<String>> nodeList = CollUtil.newArrayList();
        Boolean enableSort = dto.getEnableSort();
        for (KpiType kpiType : kpiTypes) {
            String parentId = kpiType.getParentId();
            String parentIdStr = "0";
            int weight = 0;
            if (Objects.nonNull(parentId)) {
                parentIdStr = String.valueOf(parentId);
                weight = 1;
            }
//            if (enableSort) {
//                weight = kpiType.getSort() == null ? 0 : kpiType.getSort();
//            }
            TreeNode<String> treeNode = new TreeNode<>(String.valueOf(kpiType.getId()), parentIdStr, kpiType.getKpiType(), weight);
            nodeList.add(treeNode);
        }
        //kpi
        CompletableFuture<List<KpiInfo>> listCompletableFuture = CompletableFuture
                .supplyAsync(() -> listAll(factory, null, kpiName,sortType)).exceptionally(ex -> new ArrayList<>());
        List<KpiInfo> kpiInfoList;
        try {
            kpiInfoList = listCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new JeecgBootException(e);
        }
        for (KpiInfo info : kpiInfoList) {
            String parentIdStr;
            if (Objects.nonNull(info.getParentId())) {
                parentIdStr = info.getParentId();
            } else {
                String typeIdStr = info.getPerentId();
                parentIdStr = typeIdStr;
            }
            int weight = 2;
            if (enableSort) {
                if("report".equals(sortType)){
                    weight = info.getReportSort() == null ? 0 : (int) Math.round(info.getReportSort());
                }else{
                    weight = info.getInputSort() == null ? 0 : (int) Math.round(info.getInputSort());
                }
            }
            TreeNode<String> treeNode = new TreeNode<>(info.getKpiCode(), parentIdStr, info.getKpiName(), weight);
            nodeList.add(treeNode);
        }
        return TreeUtil.build(nodeList, "0");

    }

    /**
     * 获取KPI
     * @param factory
     * @param dept
     * @param kpiName
     * @param typeSort
     * @return
     */
    private List<KpiInfo> listAll(String factory, String dept, String kpiName, String typeSort) {
        LambdaQueryWrapper<KpiInfo> queryWrapper = LambdaQueryOrderInputWrapper(factory, dept, kpiName, typeSort);
        return list(queryWrapper);
    }
    private LambdaQueryWrapper<KpiInfo> LambdaQueryOrderInputWrapper(String factory, String dept, String kpiName, String typeSort) {
        LambdaQueryWrapper<KpiInfo> queryWrapper = Wrappers.lambdaQuery(KpiInfo.class);
        queryWrapper.select(KpiInfo::getId, KpiInfo::getKpiCode, KpiInfo::getPerentId, KpiInfo::getKpiName, KpiInfo::getUnit, KpiInfo::getText, KpiInfo::getParentId, KpiInfo::getSort, KpiInfo::getInputSort, KpiInfo::getReportSort);
        queryWrapper.likeRight(StrUtil.isNotBlank(factory), KpiInfo::getFactoryCode, factory);
        queryWrapper.like(StrUtil.isNotEmpty(kpiName), KpiInfo::getKpiName, kpiName);
        if("report".equals(typeSort)){
            queryWrapper.orderByAsc(KpiInfo::getReportSort);
        }else{
            queryWrapper.orderByAsc(KpiInfo::getInputSort);
        }
        return queryWrapper;
    }

    /**
     * 生成树
     * @param trees
     * @param list
     * @param kpiCodeMap
     * @param <T>
     */
    private <T> void getKpiTargetList(List<Tree<String>> trees, List<T> list, Map<String, T> kpiCodeMap) {
        System.out.println(JSONUtil.toJsonStr(trees));
        for (Tree<String> tree : trees) {
            String id = tree.getId();
            //多级KPI也显示
            T t = kpiCodeMap.get(id);
            if (Objects.nonNull(t)) {
                list.add(t);
            }
            List<Tree<String>> children = tree.getChildren();
            if (CollUtil.isNotEmpty(children)) {
                getKpiTargetList(children, list, kpiCodeMap);
            }
        }
    }

    /**
     * 获取KPI树基础数据
     * @param kpiInfo
     * @return
     */
    @Override
    public Result<?> kpiControlConfiguration(KpiInfo kpiInfo) {
        LambdaQueryWrapper<KpiInfo> query = new LambdaQueryWrapper<>();
        query.likeRight(StringUtils.hasLength(kpiInfo.getFactoryCode()), KpiInfo::getFactoryCode, kpiInfo.getFactoryCode());
        query.likeRight(KpiInfo::getKpiCode, kpiInfo.getKpiCode());
        query.eq(KpiInfo::getYear, kpiInfo.getYear());
        //是否进入柏拉图
        query.likeRight(KpiInfo::getPlato, "1");
        List<KpiInfo> kpiInfos = list(query);
        if(null != kpiInfos && kpiInfos.size()>0){
            List<ControlConfigurationVo> ts = kpiInfoMapper.kpiControlConfiguration(kpiInfo.getFactoryCode(), kpiInfo.getKpiCode());
            //部门集合
            List<SysDepart> departList = departService.list();
            //基础数据集合 ***
            List<KpiPlanData> planDataList = kpiPlanDataService.list();
            ts.forEach(C -> {
                String deptName = "";
                if(StrUtil.isNotBlank(C.getDeptCode())){
                    Optional<SysDepart> departOptional = departList.stream().filter(depart -> depart.getOrgCode().equals(C.getDeptCode())).findFirst();
                    if(departOptional.isPresent()){
                        deptName = departOptional.get().getDepartName();
                    }
                }
                C.setParentName(deptName);
                if (kpiInfo.getTypeBool()) {
                    C.setYearValue("0");
                    C.setYearDataVos(kpiPlanDataService.yearData(kpiInfo.getYear(), C.getPlatoThreeId()));
                } else {
                    String platoThreeId = C.getPlatoThreeId();
                    if (StrUtil.isNotBlank(platoThreeId)) {
                        List<KpiPlanData> list = planDataList.stream().filter(panData -> panData.getThreeId().equals(platoThreeId)).collect(Collectors.toList());
                        if(null != list && list.size() > 0){
                            List<String> tian = kpiInfo.getTian();
                            if(null != tian && tian.size() > 0){
                                list = list.stream().filter(panData -> tian.contains(panData.getTimeDay())).collect(Collectors.toList());
                            }
                        }
                        C.setStringListMap(list);
                    }
                }
            });
            return Result.OK(ts);
        }else{
            return Result.error(0, "请在[KPI配置]内设置该KPI进入柏拉图再试");
        }
    }

    @Override
    public List<KpiInfo> listKpiInfo(KpiInfo kpiInfo) {
        //当前登录人部门
        String dept = kpiInfo.getDeptCode();
        String type_id = kpiInfo.getPerentId();
        //避免PerentId注入
        kpiInfo.setPerentId(null);
        QueryWrapper<KpiInfo> queryWrapper = QueryGenerator.initQueryWrapper(kpiInfo, null);
        queryWrapper.eq(StrUtil.isNotBlank(dept), "c.dept_code", dept);
        queryWrapper.orderByAsc("c.sort");
        if (Objects.nonNull(type_id)) {
            //当长度小于7的时候，说明选择的是kpi类型，否则选择的是kpi
//            if(String.valueOf(type_id).length() < 7){
//                //查询当前类型以及对应的子类,考虑2层层级
//                //先查询出所有的kpi类型
//                List<KpiType> listType = typeService.lambdaQuery().and(q-> q.eq(KpiType::getId,type_id).or().eq(KpiType::getParentId,type_id)).list();
//                List<Integer> typeIds = CollStreamUtil.toList(listType,KpiType::getId);
//                queryWrapper.in("c.perent_id",typeIds);
//            }else{
//                queryWrapper.eq("c.kpi_code",type_id);
//            }
            queryWrapper.and(q -> q.eq("c.perent_id", type_id));

        }
        queryWrapper.orderByDesc("c.create_time");
        return kpiInfoMapper.listKPiInfoJoin(queryWrapper);
    }

    /**
     * 根据工厂、部门和年份获取KPI名称映射信息。
     *
     * @param factory 工厂名称
     * @param dept    部门名称
     * @param year    年份
     * @return KPI名称映射信息，键为KPI代码，值为KPI信息对象
     */
    @Override
    public Map<String, KpiInfo> kpiNameMapping(String factory, String dept, String year) {
        LambdaQueryWrapper<KpiInfo> query = new LambdaQueryWrapper<>();
        query.likeRight(StringUtils.hasLength(factory), KpiInfo::getFactoryCode, factory);
        query.likeRight(StringUtils.hasLength(dept), KpiInfo::getDeptCode, dept);
        query.like(KpiInfo::getYear, year);
        List<KpiInfo> kpiInfos = this.kpiInfoMapper.selectList(query);
        //获取映射key kpiCode--> KpiInfo
        return kpiInfos.stream().collect(Collectors.toMap(KpiInfo::getKpiCode, bean -> bean));
    }

    /**
     * 复制
     * @param kpiInfo
     * @return
     */
    @Override
    public Map<String, String> copyData(KpiInfo kpiInfo) {
        Map<String,String> map = new HashMap<>();
        //年份
        String year = kpiInfo.getYear();
        //目标年份
        String targetYear = kpiInfo.getTargetYear();
        //获取源KPI集合
        LambdaQueryWrapper<KpiInfo> query = new LambdaQueryWrapper<>();
        query.eq(KpiInfo::getYear, year);
        List<KpiInfo> kpiInfos = list(query);
        if(null != kpiInfos && kpiInfos.size() > 0){
            //获取目标年KPI
            LambdaQueryWrapper<KpiInfo> targetQuery = new LambdaQueryWrapper<>();
            targetQuery.eq(KpiInfo::getYear, targetYear);
            List<KpiInfo> kpiInfoTargets = list(targetQuery);
            //目标年kpi kpiCode集合
            Set<String> kpiCodes = new HashSet<>();
            for (KpiInfo kpiInfoTarget : kpiInfoTargets) {
                kpiCodes.add(kpiInfoTarget.getKpiCode());
            }
            // 找出目标年中不包含的KPI
            List<KpiInfo> result = new ArrayList<>();
            for (KpiInfo kpiInfoSource : kpiInfos) {
                if ((null != kpiCodes && kpiCodes.size() ==0) || !kpiCodes.contains(kpiInfoSource.getKpiCode())) {
                    kpiInfoSource.setId(null);
                    kpiInfoSource.setYear(targetYear);
                    result.add(kpiInfoSource);
                }
            }
            if(null != result && result.size() > 0){
                saveBatch(result);
                map.put("code","0");
            }else{
                //源KPI和目标KPI相同
                map.put("code","2");
            }
        }else{
            //源KPI为空
            map.put("code","1");
        }
        return map;
    }

    /**
     * KPI信息查询
     * @param kpiCodeList
     * @return
     */
    @Override
    public List<KpiInfo> listReprot(Set<String> kpiCodeList) {
        QueryWrapper<KpiInfo> queryWrapper = QueryGenerator.initQueryWrapper(new KpiInfo(), null);
        queryWrapper.in("c.kpi_code", kpiCodeList);
        queryWrapper.eq("c.visible_write", 1);
        return kpiInfoMapper.listKPiInfoJoin(queryWrapper);
    }

}
