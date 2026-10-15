package com.keytech.modules.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.alarm.entity.AlarmTypes;
import com.keytech.modules.alarm.mapper.AlarmTypesMapper;
import com.keytech.modules.alarm.service.IAlarmTypesService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报警点位类型
 * @author liu
 * @since 2026.01.26
 * */
@Service
public class AlarmTypesServiceImpl extends BaseServiceImpl<AlarmTypesMapper, AlarmTypes> implements IAlarmTypesService {

    /**根节点父ID的值*/
    public static final Integer ROOT_PID_VALUE = 0;
    /**树节点有子节点状态值*/
    public static final String HASCHILD = "1";
    /**树节点无子节点状态值*/
    public static final String NOCHILD = "0";

    /**
     * 新增节点
     * @param alarmTypes
     */
	@Override
	public void addAlarmTypes(AlarmTypes alarmTypes) {
	    //新增时设置hasChild为0
	    alarmTypes.setHasChild(NOCHILD);
		if(alarmTypes.getParentId() == null){
			alarmTypes.setParentId(ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			AlarmTypes parent = baseMapper.selectById(alarmTypes.getParentId());
			if(parent != null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
		}
		baseMapper.insert(alarmTypes);
	}

    /**
     * 修改节点
     * @param alarmTypes
     */
	@Override
	public void updateAlarmTypes(AlarmTypes alarmTypes) {
		AlarmTypes entity = this.getById(alarmTypes.getId());
		if(entity == null) {
			throw new JeecgBootException("未找到对应实体");
		}
		Integer old_pid = entity.getParentId();
        Integer new_pid = alarmTypes.getParentId();
		if(!old_pid.equals(new_pid)) {
			this.updateOldParentNode(old_pid);
			if(new_pid == null){
				alarmTypes.setParentId(ROOT_PID_VALUE);
			}
			if(!ROOT_PID_VALUE.equals(alarmTypes.getParentId())) {
				baseMapper.updateTreeNodeStatus(alarmTypes.getParentId(), HASCHILD);
			}
		}
		baseMapper.updateById(alarmTypes);
	}

    /**
     * 删除节点
     * @param id
     * @throws JeecgBootException
     */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteAlarmTypes(Integer id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        String ids = this.queryTreeChildIds(id.toString());
        if(ids.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = ids.split(",");
            List<Integer> idList = Arrays.stream(idArr).filter(StringUtils :: isNotBlank).map(Integer :: valueOf).collect(Collectors.toList());
            for (String idVal : idArr) {
                if(idVal != null){
                    AlarmTypes alarmTypes = this.getById(Integer.parseInt(idVal));
                    Integer pidVal = alarmTypes.getParentId();
                    //查询此节点上一级是否还有其他子节点
                    List<AlarmTypes> dataList = baseMapper.selectList(new QueryWrapper<AlarmTypes>().eq("parent_id", pidVal).notIn("id", idList));
                    boolean flag = (dataList == null || dataList.isEmpty()) && !idList.contains(pidVal) && !sb.toString().contains(pidVal.toString());
                    if(flag){
                        //如果当前节点原本有子节点 现在没有了，更新状态
                        sb.append(pidVal).append(",");
                    }
                }
            }
            //批量删除节点
            baseMapper.deleteBatchIds(idList);
            //修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            List<Integer> pidList = Arrays.stream(pidArr).filter(StringUtils :: isNotBlank).map(Integer :: valueOf).collect(Collectors.toList());
            for(Integer pid : pidList){
                this.updateOldParentNode(pid);
            }
        }else{
            AlarmTypes alarmTypes = this.getById(id);
            if(alarmTypes == null) {
                throw new JeecgBootException("未找到对应实体");
            }
            this.updateOldParentNode(alarmTypes.getParentId());
            baseMapper.deleteById(id);
        }
	}

    /**
     * 查询所有数据，无分页
     * @param queryWrapper
     * @return List<AlarmTypes>
     */
	@Override
    public List<AlarmTypes> queryTreeListNoPage(QueryWrapper<AlarmTypes> queryWrapper) {
        List<AlarmTypes> dataList = baseMapper.selectList(queryWrapper);
        List<AlarmTypes> mapList = new ArrayList<>();
        for(AlarmTypes data : dataList){
            Integer pidVal = data.getParentId();
            //递归查询子节点的根节点
            if(pidVal != null && !ROOT_PID_VALUE.equals(pidVal)){
                AlarmTypes rootVal = this.getTreeRoot(pidVal);
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

    /**
     * 根据父级编码加载分类字典的数据
     * @param parentCode
     * @return
     */
    @Override
    public List<SelectTreeModel> queryListByCode(Integer parentCode) {
        Integer pid = ROOT_PID_VALUE;
        if (parentCode != null) {
            LambdaQueryWrapper<AlarmTypes> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AlarmTypes::getParentId, parentCode);
            List<AlarmTypes> list = baseMapper.selectList(queryWrapper);
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

    /**
     * 根据pid查询子节点集合
     * @param pid
     */
    @Override
    public List<SelectTreeModel> queryListByPid(Integer pid) {
        if (pid == null) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, null);
    }

	/**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(Integer pid) {
		if(!ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<AlarmTypes>().eq("parent_id", pid));
			if(count == null || count <= 1) {
				baseMapper.updateTreeNodeStatus(pid, NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private AlarmTypes getTreeRoot(Integer pidVal){
        AlarmTypes data =  baseMapper.selectById(pidVal);
        if(data != null && !ROOT_PID_VALUE.equals(data.getParentId())){
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
                    this.getTreeChildIds(pidVal, sb);
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
    private StringBuffer getTreeChildIds(String pidVal, StringBuffer sb){
        List<AlarmTypes> dataList = baseMapper.selectList(new QueryWrapper<AlarmTypes>().eq("parent_id", Integer.valueOf(pidVal)));
        if(dataList != null && !dataList.isEmpty()){
            for(AlarmTypes tree : dataList) {
                if(!sb.toString().contains(tree.getId().toString())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId().toString(), sb);
            }
        }
        return sb;
    }

}
