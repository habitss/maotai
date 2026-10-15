package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.dto.DepartDto;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.base.service.BaseService;
import org.apache.ibatis.annotations.Select;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.system.entity.SysDepart;

import java.util.List;
import java.util.Map;

/**
 * @Description: KPI分类
 * @Author: HQ
 * @Date:   2025-02-06
 * @Version: V1.0
 */
public interface IKpiTypeService extends BaseService<KpiType> {

    /**
     * KPI分类查询
     * @param id
     * @return
     */
    List<KpiType> typeDataList(String id,String year);

    List<SelectTreeModel> queryListByPid(String parentId);

    /**
     * 获取部门
     * @return
     */
    List<DepartDto> deptList(String isRoot);

    Map<Integer, KpiType> listTypeMap(String factory);

    List<KpiType> listAll(String factory, String dept);

    /**
     * 获取第一层KPI类型
     * @return
     */
    List<KpiType> getRootKpiType();
}
