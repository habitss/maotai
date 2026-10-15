package com.keytech.modules.manage.check.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.BaseService;
import com.keytech.modules.manage.check.entity.ManageCheckCategory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SelectTreeModel;

import java.util.List;

/**
 * @Description: 管理者检查清单-分层检查分类
 * @Author: jeecg-boot
 * @Date:   2025-03-30
 * @Version: V1.0
 */
public interface IManageCheckCategoryService extends BaseService<ManageCheckCategory> {

	/**根节点父ID的值*/
	public static final String ROOT_PID_VALUE = "0";
	
	/**树节点有子节点状态值*/
	public static final String HASCHILD = "1";
	
	/**树节点无子节点状态值*/
	public static final String NOCHILD = "0";

	/**
	 * 新增节点
	 *
	 * @param manageCheckCategory
	 */
	void addManageCheckCategory(ManageCheckCategory manageCheckCategory);
	
	/**
   * 修改节点
   *
   * @param manageCheckCategory
   * @throws JeecgBootException
   */
	void updateManageCheckCategory(ManageCheckCategory manageCheckCategory) throws JeecgBootException;
	
	/**
	 * 删除节点
	 *
	 * @param id
   * @throws JeecgBootException
	 */
	void deleteManageCheckCategory(String id) throws JeecgBootException;

	  /**
	   * 查询所有数据，无分页
	   *
	   * @param queryWrapper
	   * @return List<ManageCheckCategory>
	   */
    List<ManageCheckCategory> queryTreeListNoPage(QueryWrapper<ManageCheckCategory> queryWrapper);

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
