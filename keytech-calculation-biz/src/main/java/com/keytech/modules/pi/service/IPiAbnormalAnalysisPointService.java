package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SelectTreeModel;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 异常点位配置
 * @Author: jeecg-boot
 * @Date:   2025-03-04
 * @Version: V1.0
 */
public interface IPiAbnormalAnalysisPointService extends BaseService<PiAbnormalAnalysisPoint> {

	/**根节点父ID的值*/
	public static final String ROOT_PID_VALUE = "0";
	
	/**树节点有子节点状态值*/
	public static final String HASCHILD = "1";
	
	/**树节点无子节点状态值*/
	public static final String NOCHILD = "0";

	/**
	 * 新增节点
	 *
	 * @param piAbnormalAnalysisPoint
	 */
	void addPiAbnormalAnalysisPoint(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint);
	
	/**
   * 修改节点
   *
   * @param piAbnormalAnalysisPoint
   * @throws JeecgBootException
   */
	void updatePiAbnormalAnalysisPoint(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint) throws JeecgBootException;
	
	/**
	 * 删除节点
	 *
	 * @param id
   * @throws JeecgBootException
	 */
	void deletePiAbnormalAnalysisPoint(String id) throws JeecgBootException;

	  /**
	   * 查询所有数据，无分页
	   *
	   * @param queryWrapper
	   * @return List<PiAbnormalAnalysisPoint>
	   */
    List<PiAbnormalAnalysisPoint> queryTreeListNoPage(QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper);

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
	 * 自定义导入功能
	 * @param request
	 * @param response
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PiAbnormalAnalysisPoint> clazz) throws IOException;
}
