package com.keytech.modules.action.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;

import java.util.List;
import java.util.Map;

/**
 * @Description: 行动计划-问题分类
 * @Author: jeecg-boot
 * @Date:   2025-01-07
 * @Version: V1.0
 */
@Mapper
public interface ActionPlanProblemTypeMapper extends BaseMapper<ActionPlanProblemType> {

	/**
	 * 编辑节点状态
	 * @param id
	 * @param status
	 */
	void updateTreeNodeStatus(@Param("id") String id,@Param("status") String status);

	/**
	 * 【vue3专用】根据父级ID查询树节点数据
	 *
	 * @param pid
	 * @param query
	 * @return
	 */
	List<SelectTreeModel> queryListByPid(@Param("pid") String pid, @Param("query") Map<String, String> query);

}
