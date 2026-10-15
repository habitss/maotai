package com.keytech.modules.alarm.service;

import com.keytech.base.service.BaseService;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.alarm.entity.AlarmTypes;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

/**
 * 报警点位类型
 * @author liu
 * @since 2026.01.26
 * */
public interface IAlarmTypesService extends BaseService<AlarmTypes> {

	/**
	 * 新增节点
	 * @param alarmTypes
	 */
	void addAlarmTypes(AlarmTypes alarmTypes);
	
	/**
	 * 修改节点
	 * @param alarmTypes
	 * @throws JeecgBootException
	*/
	void updateAlarmTypes(AlarmTypes alarmTypes) throws JeecgBootException;
	
	/**
	 * 删除节点
	 * @param id
     * @throws JeecgBootException
	*/
	void deleteAlarmTypes(Integer id) throws JeecgBootException;

	/**
	 * 查询所有数据，无分页
	 * @param queryWrapper
	 * @return List<AlarmTypes>
	*/
    List<AlarmTypes> queryTreeListNoPage(QueryWrapper<AlarmTypes> queryWrapper);

	/**
	 * 根据父级编码加载分类字典的数据
	 * @param parentCode
	 * @return
	 */
	List<SelectTreeModel> queryListByCode(Integer parentCode);

	/**
	 * 根据pid查询子节点集合
	 * @param pid
	 * @return
	*/
	List<SelectTreeModel> queryListByPid(Integer pid);

}
