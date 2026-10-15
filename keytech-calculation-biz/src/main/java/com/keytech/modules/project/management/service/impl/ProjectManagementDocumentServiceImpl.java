package com.keytech.modules.project.management.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.NumberCalcUtil;
import com.keytech.modules.common.ReflectionUtil;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.project.management.dto.ProjectManagementDocumentDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.entity.ProjectManagementDocument;
import com.keytech.modules.project.management.entity.ProjectManagementNode;
import com.keytech.modules.project.management.enums.ProjectScoreItemEnum;
import com.keytech.modules.project.management.mapper.ProjectManagementDocumentMapper;
import com.keytech.modules.project.management.mapper.ProjectManagementMapper;
import com.keytech.modules.project.management.service.IProjectManagementDocumentService;
import com.keytech.modules.project.management.service.IProjectManagementNodeService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.def.MapExcelConstants;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.params.ExcelExportEntity;
import org.jeecgframework.poi.excel.view.JeecgMapExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 项目清单
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Service
public class ProjectManagementDocumentServiceImpl extends BaseServiceImpl<ProjectManagementDocumentMapper, ProjectManagementDocument> implements IProjectManagementDocumentService {
    //计划完成节点
    final String PLAN_NODE_IDS = "planNodeIds";
    //实际完成节点
    final String REAL_NODE_IDS = "realNodeIds";
    //延期说明及改进
    final String IMPROVE_REMARK = "improveRemark";
    //效果
    final String EFFECT = "effect";
    //id
    final String ID = "id";
    //documentId
    final String DOCUMENT_ID = "documentId";
    //计划分
    final String PLAN_SCORE = "planScore";
    //加分
    final String ADD_SCORE = "addScore";

    @Autowired
    private ProjectManagementMapper projectManagementMapper;
    @Autowired
    private IProjectManagementNodeService projectManagementNodeService;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public List<JSONObject> queryAllList(ProjectManagement projectManagement, HttpServletRequest req) throws NoSuchFieldException, IllegalAccessException {
        //月份数据
        if(StrUtil.isNotBlank(projectManagement.getMonths())){
            List<String> months = Arrays.stream(projectManagement.getMonths().split(","))
                    .collect(Collectors.toList());
            projectManagement.setMonthList(months);
        }

        //项目数据
        if(StrUtil.isNotBlank(projectManagement.getProjectLevel())){
            List<String> projectLevelList = Arrays.stream(projectManagement.getProjectLevel().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectLevelList(projectLevelList);
        }

        //项目类型
        if(StrUtil.isNotBlank(projectManagement.getProjectType())){
            List<String> projectTypeList = Arrays.stream(projectManagement.getProjectType().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectTypelList(projectTypeList);
        }

        //项目来源
        if(StrUtil.isNotBlank(projectManagement.getProjectSource())){
            List<String> projectSourceList = Arrays.stream(projectManagement.getProjectSource().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectSourceList(projectSourceList);
        }

        //获取项目数据
        List<ProjectManagement> projectList = projectManagementMapper.queryAll(projectManagement);
        if (projectList.isEmpty()){
            return new ArrayList<>();
        }
        // 项目 ids
        List<String> projectIds = projectList.stream()
                .map(ProjectManagement::getId)
                .collect(Collectors.toList());
        // 根据项目id 集合 获取 项目清单数据
        List<ProjectManagementDocument> documentList = lambdaQuery()
                .eq(ProjectManagementDocument::getDocumentYear, projectManagement.getYear())
                .in(CollUtil.isNotEmpty(projectManagement.getMonthList()),ProjectManagementDocument::getDocumentMonth, projectManagement.getMonthList())
                .in(ProjectManagementDocument::getProjectId, projectIds)
                .list();

        return getItems(projectList, documentList, projectManagement.getMonthList(), projectManagement.getYear());
    }

    @Override
    public void saveOrUpdateData(ProjectManagementDocumentDto documentDto) {
        // 按项目id分组
        Map<String, List<JSONObject>> groupedById = documentDto.getDataList().stream()
                .collect(Collectors.groupingBy(jsonObj -> jsonObj.getStr("id")));
        //获取需要保存或新增的数据
        List<ProjectManagementDocument> list = new ArrayList<>();
        for (Map.Entry<String, List<JSONObject>> entry : groupedById.entrySet()) {
            list.addAll(processData(entry.getValue(), documentDto.getYear(), documentDto.getMonths()));
        }
        this.saveOrUpdateBatch(list);
    }

    @Override
    public ModelAndView exportXlsBy(HttpServletRequest request, ProjectManagement projectManagement) throws NoSuchFieldException, IllegalAccessException {
        //获取导出数据
        List<JSONObject> list = this.queryAllList(projectManagement, request);
        List<String> idList = new ArrayList<>();
        for(JSONObject obj : list){
            idList.add(obj.getStr("id"));
            obj.set("scoreRate", NumberCalcUtil.calculatePercentageWithTwoDecimal(obj.getStr("actualAddScore"), obj.getStr("planAddScore"), true));
        }
        //获取节点数据
        List<ProjectManagementNode> nodeList = projectManagementNodeService
                .lambdaQuery()
                .in(ProjectManagementNode::getProjectId, idList)
                .isNotNull(ProjectManagementNode::getNodeName)
                .list();
        Map<String, String> nodeMap = nodeList.stream()
                .collect(Collectors.toMap(
                        ProjectManagementNode::getId,
                        ProjectManagementNode::getNodeName
                ));


        //获取当前用户
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //组装表头
        List<ExcelExportEntity> beanList = new ArrayList<ExcelExportEntity>();
        beanList.add(new ExcelExportEntity("项目名称", "projectName", 20));
        beanList.add(new ExcelExportEntity("立项编号", "projectCode", 15));
        beanList.add(new ExcelExportEntity("项目等级", "projectLevel_dictText", 15));
        beanList.add(new ExcelExportEntity("项目类型", "projectType_dictText", 15));
        beanList.add(new ExcelExportEntity("项目来源", "projectSource_dictText", 15));
        beanList.add(new ExcelExportEntity("主控部门", "sysOrgId_dictText", 15));
        beanList.add(new ExcelExportEntity("项目总监", "projectBossId_dictText", 15));
        beanList.add(new ExcelExportEntity("项目经理", "projectManageId_dictText", 15));
        beanList.add(new ExcelExportEntity("目标类型", "targetType", true));
        beanList.add(new ExcelExportEntity("计划加分", "planAddScore", true));
        beanList.add(new ExcelExportEntity("累计加分", "actualAddScore", true));
        beanList.add(new ExcelExportEntity("得分率", "scoreRate", true));
        //设置合并表头
        ExcelExportEntity scoreEx = new ExcelExportEntity("项目加分","score",true);
        List<String> sub = new ArrayList<>();
        sub.add("targetType");
        sub.add("planAddScore");
        sub.add("actualAddScore");
        sub.add("scoreRate");
        scoreEx.setSubColumnList(sub);
        beanList.add(scoreEx);

        //根据月份数据设置月份表头
        if(StrUtil.isNotBlank(projectManagement.getMonths())){
            List<Integer> monthList = Arrays.stream(projectManagement.getMonths().split(","))
                    .map(Integer::parseInt)
                    .sorted()
                    .collect(Collectors.toList());
            for(Integer month : monthList){
                String prefix = projectManagement.getYear()+"-"+month+"-";

                beanList.add(new ExcelExportEntity("本月计划完成节点", prefix+PLAN_NODE_IDS+"_dictText", true));
                beanList.add(new ExcelExportEntity("本月实际完成节点", prefix+REAL_NODE_IDS+"_dictText", true));
                beanList.add(new ExcelExportEntity("效果", prefix+EFFECT, true));
                beanList.add(new ExcelExportEntity("延期说明及改进", prefix+IMPROVE_REMARK, true));
                beanList.add(new ExcelExportEntity("计划分", prefix+PLAN_SCORE, true));
                beanList.add(new ExcelExportEntity("加分", prefix+ADD_SCORE, true));
                //设置合并表头
                ExcelExportEntity monthEx = new ExcelExportEntity(month+"月", month,true);
                List<String> monthSub = new ArrayList<>();
                monthSub.add(prefix+PLAN_NODE_IDS+"_dictText");
                monthSub.add(prefix+REAL_NODE_IDS+"_dictText");
                monthSub.add(prefix+EFFECT);
                monthSub.add(prefix+IMPROVE_REMARK);
                monthSub.add(prefix+PLAN_SCORE);
                monthSub.add(prefix+ADD_SCORE);
                monthEx.setSubColumnList(monthSub);
                beanList.add(monthEx);

                for(JSONObject jsonObject : list){
                    jsonObject.set(prefix+PLAN_NODE_IDS+"_dictText", getNodeName(jsonObject.getStr(prefix+PLAN_NODE_IDS), nodeMap));
                    jsonObject.set(prefix+REAL_NODE_IDS+"_dictText", getNodeName(jsonObject.getStr(prefix+REAL_NODE_IDS), nodeMap));
                }
            }
        }

        String title = "项目跟踪与激励";
        ModelAndView mv = new ModelAndView(new JeecgMapExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(MapExcelConstants.ENTITY_LIST, beanList);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams(title, "导出人:" + sysUser.getRealname(), title));
        mv.addObject(NormalExcelConstants.MAP_LIST, list);
        return mv;
    }

    /**
     * 查询数据时用于获取动态项数据
     * @param projectList
     * @param documentList
     * @param monthList
     * @param year
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private List<JSONObject> getItems(List<ProjectManagement> projectList, List<ProjectManagementDocument> documentList, List<String> monthList, String year) throws NoSuchFieldException, IllegalAccessException {
        //获取项目等级字典、项目类型字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of(
                        "MES3_PROJECT_LEVEL"
                        , "MES3_PROJECT_TYPE"
                        , "MES3_PROJECT_SOURCE"
                )
        );
        //字典数据map集合，key为groupCode-itemVal,value为对象信息
        Map<String, String> dictMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getGroupCode() + "-" + item.getItemVal(),
                        item -> item.getItemKey()
                ));
        //所有用户信息
        List<SysUser> allUserList = sysUserService.list();
        Map<String, String> allUserMap = CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean.getRealname());
        //所有单位信息
        List<SysDepart> allDeptList = sysDepartService.list();
        Map<String, String> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getId, bean -> bean.getDepartName());

        List<JSONObject> resultList = new ArrayList<>(0);
        for(ProjectManagement project : projectList){
            resultList.addAll(getScore(project, documentList, monthList, project.getId(), year, dictMap, allUserMap, allDeptMap));
        }
        return resultList;
    }

    /**
     * 获取各类分数
     * @param project
     * @param documentList
     * @param monthList
     * @param projectId
     * @param year
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public List<JSONObject> getScore(ProjectManagement project, List<ProjectManagementDocument> documentList,
                                            List<String> monthList, String projectId, String year,
                                     Map<String, String> dictMap, Map<String, String> allUserMap, Map<String, String> allDeptMap
    ) throws NoSuchFieldException, IllegalAccessException {

        List<JSONObject> resultList = new ArrayList<>(0);
        //转换清单数据为map
        Map<String, ProjectManagementDocument> documentMap = documentList.stream()
                .collect(Collectors.toMap(
                        data -> data.getProjectId()+"-"+data.getDocumentYear()+"-"+data.getDocumentMonth(),
                        data -> data
                ));
        //获取枚举值
        for (ProjectScoreItemEnum item : ProjectScoreItemEnum.values()) {
            //累计-计划分
            double planAddScore = 0.0;
            //累计-加分
            double actualAddScore = 0.0;

            //拆分枚举值
            String[] itemArr = item.getValues();
            //复制对象
            //定义需要返回的JSON数据
            JSONObject jsonObject = new JSONObject();
            BeanUtil.copyProperties(project, jsonObject);
            for(String month: monthList){
                //计划分
                double planScore = 0.0;
                //加分
                double addScore = 0.0;
                //查询的key
                String key = projectId+"-"+year+"-"+month;
                if(documentMap.containsKey(key)){
                    //封装数据
                    ProjectManagementDocument document = documentMap.get(key);
                    planScore = ReflectionUtil.getDoubleField(document, itemArr[1]);
                    addScore = ReflectionUtil.getDoubleField(document, itemArr[2]);
                    jsonObject.set(year+"-"+month+"-"+PLAN_NODE_IDS, document.getPlanNodeIds());
                    jsonObject.set(year+"-"+month+"-"+REAL_NODE_IDS, document.getRealNodeIds());
                    jsonObject.set(year+"-"+month+"-"+IMPROVE_REMARK, document.getImproveRemark());
                    jsonObject.set(year+"-"+month+"-"+DOCUMENT_ID, document.getId());
                    jsonObject.set(year+"-"+month+"-"+EFFECT, document.getEffect());
                }
                jsonObject.set(year+"-"+month+"-"+PLAN_SCORE, planScore);
                jsonObject.set(year+"-"+month+"-"+ADD_SCORE, addScore);
                planAddScore += planScore;
                actualAddScore += addScore;
            }
            jsonObject.set("targetType", itemArr[0]);
            jsonObject.set("planAddScore", planAddScore);
            jsonObject.set("actualAddScore", actualAddScore);
            jsonObject.set("projectLevel_dictText", getDictItem("MES3_PROJECT_LEVEL-"+jsonObject.getStr("projectLevel"), dictMap));
            jsonObject.set("projectType_dictText", getDictItem("MES3_PROJECT_TYPE-"+jsonObject.getStr("projectType"), dictMap));
            jsonObject.set("projectSource_dictText", getDictItem("MES3_PROJECT_SOURCE-"+jsonObject.getStr("projectSource"), dictMap));
            jsonObject.set("projectBossId_dictText", getDictItem(jsonObject.getStr("projectBossId"), allUserMap));
            jsonObject.set("projectManageId_dictText", getDictItem(jsonObject.getStr("projectManageId"), allUserMap));
            jsonObject.set("sysOrgId_dictText", getDictItem(jsonObject.getStr("sysOrgId"), allDeptMap));
            resultList.add(jsonObject);
        }

        return resultList;
    }

    /**
     * 处理数据
     * @param dataList 待处理的数据
     * @param year 年份
     * @param months 月份数组
     * @return
     */
    private List<ProjectManagementDocument> processData(List<JSONObject> dataList, String year, List<String> months) {
        //返回的数据
        List<ProjectManagementDocument> resultList = new ArrayList<>();
        //获取数据
        List<ProjectManagementDocument> oldList = this.list();
        Map<String, ProjectManagementDocument> oldMap = oldList.stream()
                .collect(Collectors.toMap(
                        data -> data.getProjectId()+"-"+data.getDocumentYear()+"-"+data.getDocumentMonth(),
                        data -> data
                ));
        for(String month : months){
            //获取查询的key
            String key = year+"-"+month;
            ProjectManagementDocument newDocument = new ProjectManagementDocument();
            for (JSONObject jsonObject : dataList) {
                //判断添加不同的分值
                switch (jsonObject.getStr("targetType")) {
                    case "立项":
                        newDocument.setPlanApprovalScore(jsonObject.getDouble(key + "-" + PLAN_SCORE));
                        newDocument.setAddApprovalScore(jsonObject.getDouble(key + "-" + ADD_SCORE));
                        break;
                    case "主目标":
                        newDocument.setPlanTargetScore(jsonObject.getDouble(key + "-" + PLAN_SCORE));
                        newDocument.setAddTargetScore(jsonObject.getDouble(key + "-" + ADD_SCORE));
                        break;
                    case "举措目标":
                        newDocument.setPlanActScore(jsonObject.getDouble(key + "-" + PLAN_SCORE));
                        newDocument.setAddActScore(jsonObject.getDouble(key + "-" + ADD_SCORE));
                        break;
                    case "结项":
                        newDocument.setPlanEndScore(jsonObject.getDouble(key + "-" + PLAN_SCORE));
                        newDocument.setAddEndScore(jsonObject.getDouble(key + "-" + ADD_SCORE));
                        break;
                    case "合计":
                        newDocument.setPlanTotalScore(jsonObject.getDouble(key + "-" + PLAN_SCORE));
                        newDocument.setAddTotalScore(jsonObject.getDouble(key + "-" + ADD_SCORE));
                        break;
                }
            }
            //封装必要属性
            JSONObject jsonObject = dataList.get(0);
            newDocument.setDocumentYear(year);
            newDocument.setDocumentMonth(month);
            newDocument.setPlanNodeIds(jsonObject.getStr(key+"-"+PLAN_NODE_IDS));
            newDocument.setRealNodeIds(jsonObject.getStr(key+"-"+REAL_NODE_IDS));
            newDocument.setImproveRemark(jsonObject.getStr(key+"-"+IMPROVE_REMARK));
            newDocument.setEffect(jsonObject.getStr(key+"-"+EFFECT));
            newDocument.setProjectId(jsonObject.getStr(ID));
            //判断对应项目对应年月是否存在数据，避免同时操作新增两条数据
            if(null == jsonObject.getStr(key+"-"+DOCUMENT_ID)){
                if(oldMap.containsKey(jsonObject.getStr(ID)+"-"+key)){
                    newDocument.setId(oldMap.get(jsonObject.getStr(ID)+"-"+key).getId());
                }
            } else {
                newDocument.setId(jsonObject.getStr(key+"-"+DOCUMENT_ID));
            }
            resultList.add(newDocument);
        }
        return resultList;
    }

    private String getDictItem(String key, Map<?, ?> dictMap){
        if(StrUtil.isBlank(key) || !dictMap.containsKey(key)){
            return "";
        }
        return MapUtil.getStr(dictMap, key);
    }

    private String getNodeName(String nodeIds, Map<String, String> nodeMap){
        String nodeName = "";
        if(StrUtil.isNotBlank(nodeIds)){
            String[] nodeIdArr = nodeIds.split(",");
            for(int i = 0; i < nodeIdArr.length; i++){
                nodeName += MapUtil.getStr(nodeMap, nodeIdArr[i])+"，";
            }
        }
        return nodeName;
    }
}
