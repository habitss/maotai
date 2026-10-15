package com.keytech.modules.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.alarm.entity.AlarmTypes;

import java.util.List;
import java.util.Map;

/**
 * 报警点位类型
 * @author liu
 * @since 2026.01.26
 * */
public interface AlarmTypesMapper extends BaseMapper<AlarmTypes> {

	/**
	 * 编辑节点状态
	 * @param id
	 * @param status
	 */
	void updateTreeNodeStatus(@Param("id") Integer id, @Param("status") String status);

	/**
	 * 根据父级ID查询树节点数据
	 * @param pid
	 * @param query
	 * @return
	 */
	List<SelectTreeModel> queryListByPid(@Param("pid") Integer pid, @Param("query") Map<String, String> query);

}
