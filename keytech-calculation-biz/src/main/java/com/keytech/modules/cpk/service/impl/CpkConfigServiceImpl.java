package com.keytech.modules.cpk.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cpk.service.ICpkConfigDetailService;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.cpk.entity.CpkConfig;
import com.keytech.modules.cpk.mapper.CpkConfigMapper;
import com.keytech.modules.cpk.service.ICpkConfigService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: CPK配置
 * @Author: jeecg-boot
 * @Date:   2025-12-11
 * @Version: V1.0
 */
@Service
public class CpkConfigServiceImpl extends ServiceImpl<CpkConfigMapper, CpkConfig> implements ICpkConfigService {
    @Resource
    private ICpkConfigDetailService cpkConfigDetailService;

	@Override
	public void addCpkConfig(CpkConfig cpkConfig) {
        cpkConfig.setNodeCode(generateChildCode(cpkConfig.getParentCode()));
	    //新增时设置hasChild为0
	    cpkConfig.setHasChild(ICpkConfigService.NOCHILD);
		if(oConvertUtils.isEmpty(cpkConfig.getParentId())){
			cpkConfig.setParentId(ICpkConfigService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			CpkConfig parent = baseMapper.selectById(cpkConfig.getParentId());
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
		}
		baseMapper.insert(cpkConfig);
	}
	
	@Override
	public void updateCpkConfig(CpkConfig cpkConfig) {
		CpkConfig entity = this.getById(cpkConfig.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getParentId();
		String new_pid = cpkConfig.getParentId();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				cpkConfig.setParentId(ICpkConfigService.ROOT_PID_VALUE);
			}
			if(!ICpkConfigService.ROOT_PID_VALUE.equals(cpkConfig.getParentId())) {
				baseMapper.updateTreeNodeStatus(cpkConfig.getParentId(), ICpkConfigService.HASCHILD);
			}
		}
		baseMapper.updateById(cpkConfig);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteCpkConfig(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    CpkConfig cpkConfig = this.getById(idVal);
                    String pidVal = cpkConfig.getParentId();
                    //查询此节点上一级是否还有其他子节点
                    List<CpkConfig> dataList = baseMapper.selectList(new QueryWrapper<CpkConfig>().eq("parent_id", pidVal).notIn("id",Arrays.asList(idArr)));
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
            CpkConfig cpkConfig = this.getById(id);
            if(cpkConfig==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(cpkConfig.getParentId());
            baseMapper.deleteById(id);
        }
	}
	
	@Override
    public List<CpkConfig> queryTreeListNoPage(QueryWrapper<CpkConfig> queryWrapper) {
        List<CpkConfig> dataList = baseMapper.selectList(queryWrapper);
        List<CpkConfig> mapList = new ArrayList<>();
        for(CpkConfig data : dataList){
            String pidVal = data.getParentId();
            //递归查询子节点的根节点
            if(pidVal != null && !ICpkConfigService.NOCHILD.equals(pidVal)){
                CpkConfig rootVal = this.getTreeRoot(pidVal);
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
            LambdaQueryWrapper<CpkConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CpkConfig::getParentId, parentCode);
            List<CpkConfig> list = baseMapper.selectList(queryWrapper);
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

    @Override
    @Transactional
    public void saveDetail(CpkConfig cpkConfig) {
        this.updateById(cpkConfig);
        cpkConfig.getDetailList().removeIf(detail -> {
            if(StrUtil.isBlank(detail.getConditionField())){
                return true;
            }

            if(detail.getId().startsWith("temp")){
                detail.setId(null);
            }
            return false;
        });
        cpkConfigDetailService.saveOrUpdateBatch(cpkConfig.getDetailList());
    }

    /**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(String pid) {
		if(!ICpkConfigService.ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<CpkConfig>().eq("parent_id", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, ICpkConfigService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private CpkConfig getTreeRoot(String pidVal){
        CpkConfig data =  baseMapper.selectById(pidVal);
        if(data != null && !ICpkConfigService.ROOT_PID_VALUE.equals(data.getParentId())){
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
        List<CpkConfig> dataList = baseMapper.selectList(new QueryWrapper<CpkConfig>().eq("parent_id", pidVal));
        if(dataList != null && dataList.size()>0){
            for(CpkConfig tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

    public String generateChildCode(String parentCode) {
        CpkConfig cpkConfig = baseMapper.getMaxCode(parentCode);
        String maxCode = null == cpkConfig ? "0" : cpkConfig.getNodeCode();
        // 如果是根节点
        if (StrUtil.isBlank(parentCode)) {
            int next = Integer.parseInt(maxCode) + 1;
            return String.format("%04d", next);
        } else {
            int nextSuffix = 1;
            if (!"0".equals(maxCode)) {
                // 取后3位
                String suffix = maxCode.substring(maxCode.length() - 3);
                nextSuffix = Integer.parseInt(suffix) + 1;
            }
            return parentCode + String.format("%04d", nextSuffix);
        }
    }
}
