package com.keytech.modules.cpk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.system.vo.SelectTreeModel;
import com.keytech.modules.cpk.entity.CpkConfig;

import java.util.List;
import java.util.Map;

/**
 * @Description: CPK配置
 * @Author: jeecg-boot
 * @Date:   2025-12-11
 * @Version: V1.0
 */
public interface CpkConfigMapper extends BaseMapper<CpkConfig> {

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

	/**
	 * 获取最大节点编码
	 * @param parentCode 父级编码
	 * @return 编码
	 */
	CpkConfig getMaxCode(@Param("parentCode") String parentCode);

}
