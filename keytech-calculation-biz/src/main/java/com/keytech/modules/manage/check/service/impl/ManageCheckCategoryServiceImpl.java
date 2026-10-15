package com.keytech.modules.manage.check.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.manage.check.entity.ManageCheckCategory;
import com.keytech.modules.manage.check.mapper.ManageCheckCategoryMapper;
import com.keytech.modules.manage.check.service.IManageCheckCategoryService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @Description: 管理者检查清单-分层检查分类
 * @Author: jeecg-boot
 * @Date:   2025-03-30
 * @Version: V1.0
 */
@Service
public class ManageCheckCategoryServiceImpl extends BaseServiceImpl<ManageCheckCategoryMapper, ManageCheckCategory> implements IManageCheckCategoryService {

	@Override
	public void addManageCheckCategory(ManageCheckCategory manageCheckCategory) {
	   //新增时设置hasChild为0
	    manageCheckCategory.setHasChild(IManageCheckCategoryService.NOCHILD);
		if(oConvertUtils.isEmpty(manageCheckCategory.getPid())){
			manageCheckCategory.setPid(IManageCheckCategoryService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			ManageCheckCategory parent = baseMapper.selectById(manageCheckCategory.getPid());
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
		}
        manageCheckCategory.setCode(getCode(manageCheckCategory));
		baseMapper.insert(manageCheckCategory);
	}
	
	@Override
	public void updateManageCheckCategory(ManageCheckCategory manageCheckCategory) {
		ManageCheckCategory entity = this.getById(manageCheckCategory.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getPid();
		String new_pid = manageCheckCategory.getPid();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				manageCheckCategory.setPid(IManageCheckCategoryService.ROOT_PID_VALUE);
			}
			if(!IManageCheckCategoryService.ROOT_PID_VALUE.equals(manageCheckCategory.getPid())) {
				baseMapper.updateTreeNodeStatus(manageCheckCategory.getPid(), IManageCheckCategoryService.HASCHILD);
			}
            manageCheckCategory.setCode(getCode(manageCheckCategory));
		}
		baseMapper.updateById(manageCheckCategory);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteManageCheckCategory(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    ManageCheckCategory manageCheckCategory = this.getById(idVal);
                    String pidVal = manageCheckCategory.getPid();
                    //查询此节点上一级是否还有其他子节点
                    List<ManageCheckCategory> dataList = baseMapper.selectList(new QueryWrapper<ManageCheckCategory>().eq("pid", pidVal).notIn("id",Arrays.asList(idArr)));
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
            ManageCheckCategory manageCheckCategory = this.getById(id);
            if(manageCheckCategory==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(manageCheckCategory.getPid());
            baseMapper.deleteById(id);
        }
	}
	
	@Override
    public List<ManageCheckCategory> queryTreeListNoPage(QueryWrapper<ManageCheckCategory> queryWrapper) {
        List<ManageCheckCategory> dataList = baseMapper.selectList(queryWrapper);
        List<ManageCheckCategory> mapList = new ArrayList<>();
        for(ManageCheckCategory data : dataList){
            String pidVal = data.getPid();
            //递归查询子节点的根节点
            if(pidVal != null && !IManageCheckCategoryService.NOCHILD.equals(pidVal)){
                ManageCheckCategory rootVal = this.getTreeRoot(pidVal);
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
            LambdaQueryWrapper<ManageCheckCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ManageCheckCategory::getPid, parentCode);
            List<ManageCheckCategory> list = baseMapper.selectList(queryWrapper);
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
		if(!IManageCheckCategoryService.ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<ManageCheckCategory>().eq("pid", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, IManageCheckCategoryService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private ManageCheckCategory getTreeRoot(String pidVal){
        ManageCheckCategory data =  baseMapper.selectById(pidVal);
        if(data != null && !IManageCheckCategoryService.ROOT_PID_VALUE.equals(data.getPid())){
            return this.getTreeRoot(data.getPid());
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
        List<ManageCheckCategory> dataList = baseMapper.selectList(new QueryWrapper<ManageCheckCategory>().eq("pid", pidVal));
        if(dataList != null && dataList.size()>0){
            for(ManageCheckCategory tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

    private String getCode(ManageCheckCategory manageCheckCategory){
        String code = "100";
        //如果当前节点编码为空 或者 当前节点的父级编码不包含在当前节点编码中，则需要重新生成编码
        if(StrUtil.isBlank(manageCheckCategory.getCode())
        || !manageCheckCategory.getCode().startsWith(manageCheckCategory.getParentCode())){
            //查询父节点数据，获取最大值
            LambdaQueryWrapper<ManageCheckCategory> queryWrapper = new LambdaQueryWrapper<ManageCheckCategory>()
                    .eq(ManageCheckCategory::getPid, manageCheckCategory.getPid())
                    .orderByDesc(ManageCheckCategory::getCode)
                    .last("limit 1");
            ManageCheckCategory category = this.getOne(queryWrapper);
            //如果查询到数据code+1
            if(null != category){
                code = String.valueOf(Integer.valueOf(category.getCode())+1);
            } else {
                //如果没有查到数据，则取父节点编码拼接上100
                if(StrUtil.isNotBlank(manageCheckCategory.getParentCode())){
                    code = manageCheckCategory.getParentCode() + code;
                }
            }
        } else {
            code = manageCheckCategory.getCode();
        }
        return code;
    }
}
