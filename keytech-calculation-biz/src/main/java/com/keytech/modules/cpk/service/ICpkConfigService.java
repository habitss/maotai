package com.keytech.modules.cpk.service;

import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.cpk.entity.CpkConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

/**
 * @Description: CPK配置
 * @Author: jeecg-boot
 * @Date:   2025-12-11
 * @Version: V1.0
 */
public interface ICpkConfigService extends IService<CpkConfig> {

	/**根节点父ID的值*/
	public static final String ROOT_PID_VALUE = "0";
	
	/**树节点有子节点状态值*/
	public static final String HASCHILD = "1";
	
	/**树节点无子节点状态值*/
	public static final String NOCHILD = "0";

	/**
	 * 新增节点
	 *
	 * @param cpkConfig
	 */
	void addCpkConfig(CpkConfig cpkConfig);
	
	/**
   * 修改节点
   *
   * @param cpkConfig
   * @throws JeecgBootException
   */
	void updateCpkConfig(CpkConfig cpkConfig) throws JeecgBootException;
	
	/**
	 * 删除节点
	 *
	 * @param id
   * @throws JeecgBootException
	 */
	void deleteCpkConfig(String id) throws JeecgBootException;

	  /**
	   * 查询所有数据，无分页
	   *
	   * @param queryWrapper
	   * @return List<CpkConfig>
	   */
    List<CpkConfig> queryTreeListNoPage(QueryWrapper<CpkConfig> queryWrapper);

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

	/**
	 * 新增详细配置
	 *
	 * @param cpkConfig
	 */
	void saveDetail(CpkConfig cpkConfig);

}
