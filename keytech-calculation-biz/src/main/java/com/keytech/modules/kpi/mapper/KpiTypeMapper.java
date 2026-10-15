package com.keytech.modules.kpi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.kpi.dto.DepartDto;
import com.keytech.modules.kpi.entity.KpiType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.system.vo.SelectTreeModel;

import java.util.List;
import java.util.Map;

/**
 * @Description: KPI分类
 * @Author: HQ
 * @Date:   2025-02-06
 * @Version: V1.0
 */
@Mapper
public interface KpiTypeMapper extends BaseMapper<KpiType> {

    List<SelectTreeModel> queryListByPid(@Param("parentId") String parentId, @Param("query") Map<String, String> query);


    List<DepartDto> deptList(String isRoot);
}
