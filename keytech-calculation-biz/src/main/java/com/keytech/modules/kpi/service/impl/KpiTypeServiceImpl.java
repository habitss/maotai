package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import com.keytech.modules.kpi.dto.DepartDto;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.mapper.KpiTypeMapper;
import com.keytech.modules.kpi.service.IKpiTypeService;
import com.keytech.modules.kpi.util.TreeListUtil;
import jakarta.annotation.Resource;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: KPI分类
 * @Author: HQ
 * @Date:   2025-02-06
 * @Version: V1.0
 */
@Service
public class KpiTypeServiceImpl extends BaseServiceImpl<KpiTypeMapper, KpiType> implements IKpiTypeService {

    @Resource
    private KpiTypeMapper kpiTypeMapper;

    @Override
    public List<KpiType> typeDataList(String id,String year) {
        QueryWrapper<KpiType> query = Wrappers.query();
        if (Objects.nonNull(id)) {
            query.and(wq -> wq.eq("id", id).or().eq("parent_id", id));
        }
        query.eq(StrUtil.isNotBlank(year),"year",year);
        query.orderByAsc("sort");
        List<KpiType> kpiTypes = this.kpiTypeMapper.selectList(query);
        //查询最下级的数据
        return TreeListUtil.getTreeList(kpiTypes, KpiType::getId, KpiType::getParentId, KpiType::getChildren).stream()
                .filter(M -> Objects.isNull(M.getParentId()))
                .collect(Collectors.toList());
    }


    @Override
    public List<SelectTreeModel> queryListByPid(String parentId) {
        if (oConvertUtils.isEmpty(parentId)) {
            parentId = "";
        }
        return kpiTypeMapper.queryListByPid(parentId, null);
    }

    @Override
    public List<DepartDto> deptList(String isRoot) {
        return kpiTypeMapper.deptList(isRoot);
    }

    @Override
    public Map<Integer, KpiType> listTypeMap(String factory) {
        List<KpiType> kpiTypes = listAll(factory, null, null);
        return CollStreamUtil.toMap(kpiTypes, (data) -> NumberUtil
                .parseInt(StrUtil.join("", data.getId())), bean -> bean);

    }

    public List<KpiType> listAll(String factory, String dept, Integer isomerism) {
        LambdaQueryWrapper<KpiType> queryWrapper = Wrappers.lambdaQuery(KpiType.class);
        queryWrapper.select(KpiType::getId, KpiType::getKpiType, KpiType::getParentId,KpiType::getSort);
        queryWrapper.likeRight(StrUtil.isNotBlank(factory), KpiType::getFactoryCode, factory);
        return list(queryWrapper);
    }

    /**
     * 获取KPI分类
     * @param factory
     * @param dept
     * @return
     */
    @Override
    public List<KpiType> listAll(String factory, String dept) {
        LambdaQueryWrapper<KpiType> queryWrapper = Wrappers.lambdaQuery(KpiType.class);
        queryWrapper.select(KpiType::getId, KpiType::getKpiType, KpiType::getParentId,KpiType::getSort);
        queryWrapper.likeRight(StrUtil.isNotBlank(factory), KpiType::getFactoryCode, factory);
//        queryWrapper.likeRight(StrUtil.isNotBlank(dept), KpiType::getDept, dept);
        return list(queryWrapper);
    }

    @Override
    public List<KpiType> getRootKpiType() {
        LambdaQueryWrapper<KpiType> queryWrapper = Wrappers.lambdaQuery(KpiType.class);
        queryWrapper.isNull(KpiType::getParentId);
        queryWrapper.orderByAsc(KpiType::getSort);
        return list(queryWrapper);
    }
}
