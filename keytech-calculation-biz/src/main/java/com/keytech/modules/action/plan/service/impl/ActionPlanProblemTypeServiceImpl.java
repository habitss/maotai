package com.keytech.modules.action.plan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.mapper.ActionPlanProblemTypeMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 行动计划-问题分类
 * @Author: jeecg-boot
 * @Date:   2025-01-07
 * @Version: V1.0
 */
@Service
public class ActionPlanProblemTypeServiceImpl extends BaseServiceImpl<ActionPlanProblemTypeMapper, ActionPlanProblemType> implements IActionPlanProblemTypeService {

	@Override
	public void addActionPlanProblemType(ActionPlanProblemType actionPlanProblemType) {
	   //新增时设置hasChild为0
	    actionPlanProblemType.setHasChild(IActionPlanProblemTypeService.NOCHILD);
		if(oConvertUtils.isEmpty(actionPlanProblemType.getParentId())){
			actionPlanProblemType.setParentId(IActionPlanProblemTypeService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			ActionPlanProblemType parent = baseMapper.selectById(actionPlanProblemType.getParentId());
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
		}
		baseMapper.insert(actionPlanProblemType);
	}
	
	@Override
	public void updateActionPlanProblemType(ActionPlanProblemType actionPlanProblemType) {
		ActionPlanProblemType entity = this.getById(actionPlanProblemType.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getParentId();
		String new_pid = actionPlanProblemType.getParentId();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				actionPlanProblemType.setParentId(IActionPlanProblemTypeService.ROOT_PID_VALUE);
			}
			if(!IActionPlanProblemTypeService.ROOT_PID_VALUE.equals(actionPlanProblemType.getParentId())) {
				baseMapper.updateTreeNodeStatus(actionPlanProblemType.getParentId(), IActionPlanProblemTypeService.HASCHILD);
			}
		}
		baseMapper.updateById(actionPlanProblemType);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteActionPlanProblemType(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    ActionPlanProblemType actionPlanProblemType = this.getById(idVal);
                    String pidVal = actionPlanProblemType.getParentId();
                    //查询此节点上一级是否还有其他子节点
                    List<ActionPlanProblemType> dataList = baseMapper.selectList(new QueryWrapper<ActionPlanProblemType>().eq("parent_id", pidVal).notIn("id",Arrays.asList(idArr)));
                    boolean flag = (dataList == null || dataList.size() == 0) && !Arrays.asList(idArr).contains(pidVal) && !sb.toString().contains(pidVal);
                    if(flag){
                        //如果当前节点原本有子节点 现在木有了，更新状态
                        sb.append(pidVal).append(",");
                    }
                }
            }
            //批量删除节点
            baseMapper.deleteBatchIds(Arrays.asList(idArr));
            //修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            for(String pid : pidArr){
                this.updateOldParentNode(pid);
            }
        }else{
            ActionPlanProblemType actionPlanProblemType = this.getById(id);
            if(actionPlanProblemType==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(actionPlanProblemType.getParentId());
            baseMapper.deleteById(id);
        }
	}
	
	@Override
    public List<ActionPlanProblemType> queryTreeListNoPage(QueryWrapper<ActionPlanProblemType> queryWrapper) {
        List<ActionPlanProblemType> dataList = baseMapper.selectList(queryWrapper);
        List<ActionPlanProblemType> mapList = new ArrayList<>();
        for(ActionPlanProblemType data : dataList){
            String pidVal = data.getParentId();
            //递归查询子节点的根节点
            if(pidVal != null && !IActionPlanProblemTypeService.NOCHILD.equals(pidVal)){
                ActionPlanProblemType rootVal = this.getTreeRoot(pidVal);
                if(rootVal != null && !mapList.contains(rootVal)){
                    mapList.add(rootVal);
                }
            }else{
                if(!mapList.contains(data)){
                    mapList.add(data);
                }
            }
        }
        return mapList;
    }

    @Override
    public List<SelectTreeModel> queryListByCode(String parentCode) {
        String pid = ROOT_PID_VALUE;
        if (oConvertUtils.isNotEmpty(parentCode)) {
            LambdaQueryWrapper<ActionPlanProblemType> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ActionPlanProblemType::getParentId, parentCode);
            List<ActionPlanProblemType> list = baseMapper.selectList(queryWrapper);
            if (list == null || list.size() == 0) {
                throw new JeecgBootException("该编码【" + parentCode + "】不存在，请核实!");
            }
            if (list.size() > 1) {
                throw new JeecgBootException("该编码【" + parentCode + "】存在多个，请核实!");
            }
            pid = list.get(0).getId();
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public List<SelectTreeModel> queryListByPid(String pid) {
        if (oConvertUtils.isEmpty(pid)) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, null);
    }

	/**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(String pid) {
		if(!IActionPlanProblemTypeService.ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<ActionPlanProblemType>().eq("parent_id", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, IActionPlanProblemTypeService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private ActionPlanProblemType getTreeRoot(String pidVal){
        ActionPlanProblemType data =  baseMapper.selectById(pidVal);
        if(data != null && !IActionPlanProblemTypeService.ROOT_PID_VALUE.equals(data.getParentId())){
            return this.getTreeRoot(data.getParentId());
        }else{
            return data;
        }
    }

    /**
     * 根据id查询所有子节点id
     * @param ids
     * @return
     */
    private String queryTreeChildIds(String ids) {
        //获取id数组
        String[] idArr = ids.split(",");
        StringBuffer sb = new StringBuffer();
        for (String pidVal : idArr) {
            if(pidVal != null){
                if(!sb.toString().contains(pidVal)){
                    if(sb.toString().length() > 0){
                        sb.append(",");
                    }
                    sb.append(pidVal);
                    this.getTreeChildIds(pidVal,sb);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 递归查询所有子节点
     * @param pidVal
     * @param sb
     * @return
     */
    private StringBuffer getTreeChildIds(String pidVal,StringBuffer sb){
        List<ActionPlanProblemType> dataList = baseMapper.selectList(new QueryWrapper<ActionPlanProblemType>().eq("parent_id", pidVal));
        if(dataList != null && dataList.size()>0){
            for(ActionPlanProblemType tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

}
