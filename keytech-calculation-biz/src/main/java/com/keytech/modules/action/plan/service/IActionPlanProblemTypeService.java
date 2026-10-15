package com.keytech.modules.action.plan.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

/**
 * @Description: 行动计划-问题分类
 * @Author: jeecg-boot
 * @Date:   2025-01-07
 * @Version: V1.0
 */
public interface IActionPlanProblemTypeService extends BaseService<ActionPlanProblemType> {

	/**根节点父ID的值*/
	public static final String ROOT_PID_VALUE = "0";
	
	/**树节点有子节点状态值*/
	public static final String HASCHILD = "1";
	
	/**树节点无子节点状态值*/
	public static final String NOCHILD = "0";

	/**
	 * 新增节点
	 *
	 * @param actionPlanProblemType
	 */
	void addActionPlanProblemType(ActionPlanProblemType actionPlanProblemType);
	
	/**
   * 修改节点
   *
   * @param actionPlanProblemType
   * @throws JeecgBootException
   */
	void updateActionPlanProblemType(ActionPlanProblemType actionPlanProblemType) throws JeecgBootException;
	
	/**
	 * 删除节点
	 *
	 * @param id
   * @throws JeecgBootException
	 */
	void deleteActionPlanProblemType(String id) throws JeecgBootException;

	  /**
	   * 查询所有数据，无分页
	   *
	   * @param queryWrapper
	   * @return List<ActionPlanProblemType>
	   */
    List<ActionPlanProblemType> queryTreeListNoPage(QueryWrapper<ActionPlanProblemType> queryWrapper);

	/**
	 * 【vue3专用】根据父级编码加载分类字典的数据
	 *
	 * @param parentCode
	 * @return
	 */
	List<SelectTreeModel> queryListByCode(String parentCode);

	/**
	 * 【vue3专用】根据pid查询子节点集合
	 *
	 * @param pid
	 * @return
	 */
	List<SelectTreeModel> queryListByPid(String pid);

}
