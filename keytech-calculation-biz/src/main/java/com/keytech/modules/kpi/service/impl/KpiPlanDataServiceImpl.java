package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.entity.KpiPlanData;
import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.keytech.modules.kpi.entity.KpiPlatoThree;
import com.keytech.modules.kpi.mapper.KpiPlanDataMapper;
import com.keytech.modules.kpi.service.IKpiPlanDataService;
import com.keytech.modules.kpi.service.IKpiPlatoOneService;
import com.keytech.modules.kpi.service.IKpiPlatoThreeService;
import com.keytech.modules.kpi.util.TreeListUtil;
import com.keytech.modules.kpi.vo.ControlConfigurationVo;
import com.keytech.modules.kpi.vo.KpiTreeVo;
import com.keytech.modules.kpi.vo.YearDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: KPI树-基础数据
 * @Author: jeecg-boot
 * @Date:   2025-03-07
 * @Version: V1.0
 */
@Service
public class KpiPlanDataServiceImpl extends BaseServiceImpl<KpiPlanDataMapper, KpiPlanData> implements IKpiPlanDataService {

    @Autowired
    private KpiPlanDataMapper kpiPlanDataMapper;
    @Autowired
    private IKpiPlatoOneService oneService;
    @Autowired
    private IKpiPlatoThreeService platoThreeService;

    /**
     * 根据年份和三级ID查询年度数据
     * @param year
     * @param threeId
     * @return
     */
    @Override
    public List<YearDataVo> yearData(String year, String threeId) {
        return kpiPlanDataMapper.yearData(year, threeId);
    }

    @Transactional
    public List<KpiTreeVo> treeData(String month, String kpiCode) {
        //年
        String year = month.substring(0, 4);
        // 根据kpi查询柏拉图配置
        Map<String, String> map = oneService.listMapByKpiCode(kpiCode,year);

        // 查询柏拉图数据
        List<String> patterns = oneService.list(new LambdaQueryWrapper<KpiPlatoOne>().eq(KpiPlatoOne::getParentCode, kpiCode)
                .eq(KpiPlatoOne::getYear, year)).stream().map(platoOne -> platoOne.getCode() + "%").collect(Collectors.toList());

        // 确保map和patterns都不为空
        if (map.isEmpty() || patterns.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建 SQL 参数
        String parameterTree = patterns.stream().map(pattern -> "three_id LIKE '" + pattern + "'").collect(Collectors.joining(" OR "));

        // 查询数据
        List<KpiTreeVo> r = kpiPlanDataMapper.treeData(month, parameterTree);

        // 设置节点名称
        r.forEach(t -> t.setName(map.getOrDefault(t.getId(), "")));

        return TreeListUtil.getTreeList(r, KpiTreeVo::getId, KpiTreeVo::getParentId, KpiTreeVo::getChildren).stream().filter(w -> "0".equals(w.getParentId())).collect(Collectors.toList());
    }

    /**
     * KPI树基础数据-存储
     * @param controlConfigurationVos
     * @return
     */
    @Override
    public List<KpiPlanData> batchAddOrEdit(List<ControlConfigurationVo> controlConfigurationVos) {
        List<KpiPlatoThree> threes = new ArrayList<>();
        controlConfigurationVos.forEach(vo -> {
            KpiPlatoThree three = new KpiPlatoThree();
            three.setId(vo.getPlatoThreeKey());
            three.setControlActivity(vo.getControlActivity());
            three.setStandard(vo.getStandard());
            three.setUnit(vo.getUnit());
            threes.add(three);
        });
        platoThreeService.updateBatchById(threes);
        List<KpiPlanData> list = new ArrayList<>();
        controlConfigurationVos.stream().map(ControlConfigurationVo::getStringListMap).forEach(bean -> {
            list.addAll(bean);
            Map<String, KpiPlanData> stringMdmPlanDataMap = CollStreamUtil.toMap(bean, KpiPlanData::getPlanName, data -> data);
            //计划 实际
            KpiPlanData amountOfPlan = stringMdmPlanDataMap.get("amountOfPlan");
            KpiPlanData actualQuantity = stringMdmPlanDataMap.get("actualQuantity");
            if (amountOfPlan != null && actualQuantity != null) {
                KpiPlanData mdmPlanData = BeanUtil.copyProperties(amountOfPlan, KpiPlanData.class);
                //差距量
                mdmPlanData.setPlanName("diffQuantity");
                String amountOfPlanValue = amountOfPlan.getPlanValues();
                String actualQuantityValue = actualQuantity.getPlanValues();
                BigDecimal sub = NumberUtil.sub(actualQuantityValue, amountOfPlanValue);
                mdmPlanData.setPlanValues(NumberUtil.toStr(sub));
                list.add(mdmPlanData);
            }
        });
        //查看数据范围
        List<String> dayList = list.stream().map(KpiPlanData::getTimeDay).distinct().collect(Collectors.toList());
        List<KpiPlanData> dataDb = lambdaQuery().in(KpiPlanData::getTimeDay, dayList).list();
        Map<String, KpiPlanData> dataMap = CollStreamUtil.toMap(dataDb, this::groupByKey, bean -> bean);
        //修改更新
        for (KpiPlanData mdmPlanData : list) {
            KpiPlanData one = dataMap.get(groupByKey(mdmPlanData));
            if (one != null) {
                mdmPlanData.setId(one.getId());
            }
        }
        saveOrUpdateBatch(list);
        return list;
    }

    /**
     * 根据给定的 MdmPlanData 对象，将其类标题、计划名称、时间日期和三级ID通过"#"连接成一个字符串
     *
     * @param bean 要进行分组键生成的 KpiPlanData 对象
     * @return 通过"#"连接的字符串
     */
    private String groupByKey(KpiPlanData bean) {
        return StrUtil.join("#", bean.getClassTitle(), bean.getPlanName(), bean.getTimeDay(), bean.getThreeId());
    }
}
