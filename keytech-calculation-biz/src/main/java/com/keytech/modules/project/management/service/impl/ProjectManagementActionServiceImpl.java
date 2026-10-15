package com.keytech.modules.project.management.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.project.management.entity.ProjectManagementAction;
import com.keytech.modules.project.management.entity.ProjectManagementNode;
import com.keytech.modules.project.management.mapper.ProjectManagementActionMapper;
import com.keytech.modules.project.management.service.IProjectManagementActionService;
import com.keytech.modules.project.management.service.IProjectManagementNodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 项目行动计划
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Service
public class ProjectManagementActionServiceImpl extends BaseServiceImpl<ProjectManagementActionMapper, ProjectManagementAction> implements IProjectManagementActionService {
    @Autowired
    private IProjectManagementNodeService projectManagementNodeService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public List<ProjectManagementAction> queryList(ProjectManagementAction projectManagementAction) {
        return baseMapper.queryList(projectManagementAction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveAction(List<ProjectManagementAction> list) {
        //项目节点数据
        List<ProjectManagementNode> nodeList = new ArrayList<>(0);
        //项目行动计划数据
        List<ProjectManagementAction> addActionList = new ArrayList<>(0);
        List<ProjectManagementAction> editActionList = new ArrayList<>(0);
        //根据nodeid将数据分组
        Map<String, List<ProjectManagementAction>> groupMap = list.stream()
                .collect(Collectors.groupingBy(ProjectManagementAction::getNodeId));
        //处理数据
        for (Map.Entry<String, List<ProjectManagementAction>> entry : groupMap.entrySet()) {
            String mapKey = entry.getKey();
            List<ProjectManagementAction> mapValue = entry.getValue();
            //如果为临时id转为雪花id
            if(mapKey.startsWith("temp")){
                mapKey = IdUtil.getSnowflakeNextIdStr();
            }
            //获取节点数据
            ProjectManagementNode node = new ProjectManagementNode();
            BeanUtil.copyProperties(mapValue.get(0), node);
            node.setId(mapKey);
            node.setSort(mapValue.get(0).getNodeSort());
            nodeList.add(node);

            //循环处理数据
            for(ProjectManagementAction action : mapValue){
                action.setNodeId(mapKey);
                if(action.getId().startsWith("temp")){
                    action.setId(IdUtil.getSnowflakeNextIdStr());
                    addActionList.add(action);
                }else{
                    editActionList.add(action);
                }
            }
        }
        projectManagementNodeService.saveOrUpdateBatch(nodeList);
        this.updateBatchById(editActionList);
        this.saveBatch(addActionList);
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAction(String id, String nodeId) {
        List<ProjectManagementAction> list = this.list(
                new LambdaQueryWrapper<ProjectManagementAction>().eq(ProjectManagementAction::getNodeId, nodeId)
        );
        //如果只剩最后一条行动，则删除节点表数据
        if(list.size() == 1){
            projectManagementNodeService.removeById(nodeId);
        }
        this.removeById(id);
    }

    @Override
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String projectId = request.getParameter("projectId");
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
                //获取项目节点
                List<ProjectManagementNode> oldNodeList = projectManagementNodeService.lambdaQuery()
                        .eq(ProjectManagementNode::getProjectId, projectId).list();
                Map<String, String> nodeMap = CollStreamUtil.toMap(oldNodeList, ProjectManagementNode::getNodeName, bean -> bean.getId());
                //所有用户信息
                List<SysUser> allUserList = sysUserService.list();
                Map<String, SysUser> allUserMap = CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean);
                //所有单位信息
                List<SysDepart> allDeptList = sysDepartService.list();
                Map<String, SysDepart> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getId, bean -> bean);

                //获取上传文件中的数据
                List<ProjectManagementAction> list = ExcelImportUtil.importExcel(file.getInputStream(), ProjectManagementAction.class, params);

                //操作数据
                int lineNumber = 3;
                for(ProjectManagementAction projectManagementAction : list){
                    lineNumber++;
                    if(StrUtil.isBlank(projectManagementAction.getNodeName())){
                        errorMessage.add("第" + lineNumber + "行，项目关键节点为空");
                        errorLines++;
                        continue;
                    }
                    if(!allUserMap.containsKey(projectManagementAction.getLeaderUserId())){
                        errorMessage.add("第" + lineNumber + "行，负责人不存在");
                        errorLines++;
                        continue;
                    }
                    if(!allDeptMap.containsKey(projectManagementAction.getLeaderDeptId())){
                        errorMessage.add("第" + lineNumber + "行，负责部门不存在");
                        errorLines++;
                        continue;
                    }
                    if(null == projectManagementAction.getPlanCompletionTime() && null != projectManagementAction.getRealCompletionTime()){
                        errorMessage.add("第" + lineNumber + "行，有实际完成时间时，计划完成时间不能为空");
                        errorLines++;
                        continue;
                    }
                    //如果节点名称重复则合并节点
                    if(nodeMap.containsKey(projectManagementAction.getNodeName())){
                        projectManagementAction.setNodeId(nodeMap.get(projectManagementAction.getNodeName()));
                    }

                    //计划完成日期不为空
                    if(null != projectManagementAction.getPlanCompletionTime()){
                        if(null != projectManagementAction.getRealCompletionTime()){
                            //实际完成日期小于等于计划完成日期，完成情况为已完成，否则为延迟完成-
                            if(projectManagementAction.getRealCompletionTime().compareTo(projectManagementAction.getPlanCompletionTime()) <= 0){
                                projectManagementAction.setActionState(1);
                            }else{
                                projectManagementAction.setActionState(4);
                            }
                        } else {
                            //实际完成日期为空，当前日期小于等于计划完成日期，完成情况为进行中，否则为延迟
                            if(DateUtil.endOfDay(projectManagementAction.getPlanCompletionTime()).compareTo(new Date()) < 0){
                                projectManagementAction.setActionState(2);
                            }else{
                                projectManagementAction.setActionState(0);
                            }
                        }
                    }
                }
                if(errorLines == 0){
                    //项目节点数据
                    List<ProjectManagementNode> nodeList = new ArrayList<>(0);
                    //项目行动计划数据
                    List<ProjectManagementAction> addActionList = new ArrayList<>(0);
                    List<ProjectManagementAction> editActionList = new ArrayList<>(0);
                    //根据nodeName将数据分组
                    Map<String, List<ProjectManagementAction>> groupMap = list.stream()
                            .collect(Collectors.groupingBy(ProjectManagementAction::getNodeName));
                    //处理数据
                    for (Map.Entry<String, List<ProjectManagementAction>> entry : groupMap.entrySet()) {
                        String mapKey = entry.getKey();
                        List<ProjectManagementAction> mapValue = entry.getValue();

                        //如果没有nodeid则新增一个
                        String nodeId = IdUtil.getSnowflakeNextIdStr();
                        if(StringUtils.isNotBlank(mapValue.get(0).getNodeId())){
                            nodeId = mapValue.get(0).getNodeId();
                        }
                        //获取节点数据
                        ProjectManagementNode node = new ProjectManagementNode();
                        BeanUtil.copyProperties(mapValue.get(0), node);
                        node.setId(nodeId);
                        node.setSort(mapValue.get(0).getNodeSort());
                        node.setProjectId(projectId);
                        nodeList.add(node);

                        //循环处理数据
                        for(ProjectManagementAction action : mapValue){
                            action.setNodeId(nodeId);
                            action.setProjectId(projectId);
                            if(StrUtil.isBlank(action.getId())){
                                action.setId(IdUtil.getSnowflakeNextIdStr());
                                addActionList.add(action);
                            }else{
                                editActionList.add(action);
                            }
                            successLines++;
                        }
                    }
                    projectManagementNodeService.saveOrUpdateBatch(nodeList);
                    this.updateBatchById(editActionList);
                    this.saveBatch(addActionList);
                }
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

    @Override
    public void updateDataByJob() {
        this.lambdaUpdate()
                .set(ProjectManagementAction::getActionState, 2)
                .lt(ProjectManagementAction::getPlanCompletionTime, DateUtil.format(new Date(), "yyyy-MM-dd"))
                .isNull(ProjectManagementAction::getRealCompletionTime)
                .update();
    }
}
