package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.dto.RecipeExtractDto;
import com.keytech.modules.pi.entity.PiCurveStandard;
import com.keytech.modules.pi.entity.PiCurveStandardConfig;
import com.keytech.modules.pi.entity.PiCurveStandardGroup;
import com.keytech.modules.pi.entity.PiCurveStandardRecipe;
import com.keytech.modules.pi.mapper.PiCurveStandardConfigMapper;
import com.keytech.modules.pi.mapper.PiCurveStandardGroupMapper;
import com.keytech.modules.pi.mapper.PiCurveStandardRecipeMapper;
import com.keytech.modules.pi.service.IPiCurveStandardConfigService;
import com.keytech.modules.pi.service.IPiCurveStandardGroupService;
import com.keytech.modules.pi.service.IPiCurveStandardRecipeService;
import com.keytech.modules.pi.service.IPiCurveStandardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * @Description: 曲线标准配置(多设备)
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
@Service
public class PiCurveStandardConfigServiceImpl extends BaseServiceImpl<PiCurveStandardConfigMapper, PiCurveStandardConfig> implements IPiCurveStandardConfigService {

    private static final Logger log = LoggerFactory.getLogger(PiCurveStandardConfigServiceImpl.class);

    @Autowired
    private IPiCurveStandardService piCurveStandardService;
    @Autowired
    private IPiCurveStandardConfigService iPiCurveStandardConfigService;
    @Autowired
    private IPiCurveStandardRecipeService piCurveStandardRecipeService;
    @Autowired
    private IPiCurveStandardGroupService piCurveStandardGroupService;
    @Autowired
    private PiCurveStandardRecipeMapper piCurveStandardRecipeMapper;
    @Autowired
    private PiCurveStandardGroupMapper piCurveStandardGroupMapper;

    /**
     * 保存标准
     * @param piCurveStandardConfig
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void standardSave(PiCurveStandardConfig piCurveStandardConfig) {

        //保存(多设备)
        save(piCurveStandardConfig);

        //按设备拆分分成单个(,号拼接) 写入 mes3_pi_curve_standard 表中
        String unitNos = piCurveStandardConfig.getUnitNo();
        String unitNames = piCurveStandardConfig.getUnitName();
        if (unitNos != null && !unitNos.isEmpty()) {
            String[] unitNoArr = unitNos.split(",");
            String[] unitNameArr = unitNames != null ? unitNames.split(",") : new String[unitNoArr.length];

            List<PiCurveStandard> standardList = new ArrayList<>();
            for (int i = 0; i < unitNoArr.length; i++) {
                PiCurveStandard standard = new PiCurveStandard();
                //多设备id
                standard.setConfigId(piCurveStandardConfig.getId());
                standard.setTenantId(piCurveStandardConfig.getTenantId());
                standard.setFactoryCode(piCurveStandardConfig.getFactoryCode());
                standard.setDeptCode(piCurveStandardConfig.getDeptCode());
                standard.setLineCode(piCurveStandardConfig.getLineCode());
                standard.setDataSource(piCurveStandardConfig.getDataSource());
                standard.setAreaNo(piCurveStandardConfig.getAreaNo());
                standard.setUnitNo(unitNoArr[i].trim());
                standard.setUnitName(i < unitNameArr.length ? unitNameArr[i].trim() : "");
                standard.setRecipeNo(piCurveStandardConfig.getRecipeNo());
                standard.setRecipeName(piCurveStandardConfig.getRecipeName());
                standard.setEopName(piCurveStandardConfig.getEopName());
                standard.setEopNo(piCurveStandardConfig.getEopNo());
                standard.setRopId(piCurveStandardConfig.getRopId());
                standard.setParam(piCurveStandardConfig.getParam());
                standard.setInitialValue(piCurveStandardConfig.getInitialValue());
                standard.setEndValue(piCurveStandardConfig.getEndValue());
                standard.setDuration(piCurveStandardConfig.getDuration());
                standard.setDurationType(piCurveStandardConfig.getDurationType());
                standard.setUnit(piCurveStandardConfig.getUnit());
                standard.setPointCode(piCurveStandardConfig.getPointCode());
                standard.setGroupName(piCurveStandardConfig.getGroupName());
                standard.setSort(piCurveStandardConfig.getSort());
                standard.setCreateTime(new Date());
                standardList.add(standard);
            }
            if (!standardList.isEmpty()) {
                piCurveStandardService.saveBatch(standardList);
            }
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configEdit(PiCurveStandardConfig piCurveStandardConfig) {
        //更新拆分后数据(通过configId 关联)
        //1.删除旧的拆分数据
        piCurveStandardService.remove(new LambdaQueryWrapper<PiCurveStandard>()
                .eq(PiCurveStandard::getConfigId, piCurveStandardConfig.getId()));

        //2.重新拆分插入
        String unitNos = piCurveStandardConfig.getUnitNo();
        String unitNames = piCurveStandardConfig.getUnitName();
        if (unitNos != null && !unitNos.isEmpty()) {
            String[] unitNoArr = unitNos.split(",");
            String[] unitNameArr = unitNames != null ? unitNames.split(",") : new String[unitNoArr.length];

            List<PiCurveStandard> standardList = new ArrayList<>();
            for (int i = 0; i < unitNoArr.length; i++) {
                PiCurveStandard standard = new PiCurveStandard();
                standard.setConfigId(piCurveStandardConfig.getId());
                standard.setTenantId(piCurveStandardConfig.getTenantId());
                standard.setFactoryCode(piCurveStandardConfig.getFactoryCode());
                standard.setDeptCode(piCurveStandardConfig.getDeptCode());
                standard.setLineCode(piCurveStandardConfig.getLineCode());
                standard.setDataSource(piCurveStandardConfig.getDataSource());
                standard.setAreaNo(piCurveStandardConfig.getAreaNo());
                standard.setUnitNo(unitNoArr[i].trim());
                standard.setUnitName(i < unitNameArr.length ? unitNameArr[i].trim() : "");
                standard.setRecipeNo(piCurveStandardConfig.getRecipeNo());
                standard.setRecipeName(piCurveStandardConfig.getRecipeName());
                standard.setEopName(piCurveStandardConfig.getEopName());
                standard.setEopNo(piCurveStandardConfig.getEopNo());
                standard.setRopId(piCurveStandardConfig.getRopId());
                standard.setParam(piCurveStandardConfig.getParam());
                standard.setInitialValue(piCurveStandardConfig.getInitialValue());
                standard.setEndValue(piCurveStandardConfig.getEndValue());
                standard.setDuration(piCurveStandardConfig.getDuration());
                standard.setDurationType(piCurveStandardConfig.getDurationType());
                standard.setUnit(piCurveStandardConfig.getUnit());
                standard.setPointCode(piCurveStandardConfig.getPointCode());
                standard.setGroupName(piCurveStandardConfig.getGroupName());
                standard.setSort(piCurveStandardConfig.getSort());
                standard.setCreateTime(new Date());
                standardList.add(standard);
            }
            if (!standardList.isEmpty()) {
                piCurveStandardService.saveBatch(standardList);
            }
        }

        //更新(多设备配置)
        updateById(piCurveStandardConfig);
    }

    /**
     * 删除标准配置
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configDelete(String id) {
        //删除拆分后数据(通过configId 关联)
        piCurveStandardService.remove(new LambdaQueryWrapper<PiCurveStandard>()
                .eq(PiCurveStandard::getConfigId, id));

        //删除(多设备配置)
        removeById(id);
    }

    /**
     * 批量复制标准配置
     * @param ids 配置id列表（按勾选顺序）
     * @param copyNum 复制份数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configBatchCopy(List<String> ids, Integer copyNum) {
        if (ids == null || ids.isEmpty() || copyNum == null || copyNum <= 0) {
            return;
        }
        // 查询要复制的配置数据
        List<PiCurveStandardConfig> sourceList = listByIds(ids);
        if (sourceList.isEmpty()) {
            return;
        }

        // 按原数据sort升序排列，保证复制后顺序与原始一致
        sourceList.sort(Comparator.comparing(PiCurveStandardConfig::getSort, Comparator.nullsLast(Comparator.naturalOrder())));

        // 获取当前分组下最大排序值
        String groupId = sourceList.get(0).getGroupId();
        PiCurveStandardConfig maxSortConfig = getOne(new LambdaQueryWrapper<PiCurveStandardConfig>()
                .eq(PiCurveStandardConfig::getGroupId, groupId)
                .orderByDesc(PiCurveStandardConfig::getSort)
                .last("limit 1"));
        double maxSort = (maxSortConfig != null && maxSortConfig.getSort() != null) ? maxSortConfig.getSort() : 0;
        // 使用BigDecimal避免浮点精度问题
        BigDecimal maxSortDecimal = BigDecimal.valueOf(maxSort);

        int index = 1;
        for (int copy = 1; copy <= copyNum; copy++) {
            for (PiCurveStandardConfig source : sourceList) {
                // 复制为新记录，清除主键
                PiCurveStandardConfig newConfig = new PiCurveStandardConfig();
                org.springframework.beans.BeanUtils.copyProperties(source, newConfig);
                newConfig.setId(null);
                newConfig.setSort(maxSortDecimal.add(BigDecimal.valueOf(index * 0.01)).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue());
                newConfig.setCreateTime(new Date());
                newConfig.setUpdateTime(null);
                // 调用新增逻辑（含拆分写入standard表）
                standardSave(newConfig);
                index++;
            }
        }
    }


    /**
     * 曲线标准基础数据抽取
     */
    @Override
    public void standardExtractData() {
        log.info("开始曲线标准基础数据抽取");
        //获取数据配方/设备/步骤
        List<RecipeExtractDto> dataList = iPiCurveStandardConfigService.queryRecipeExtractData();
        if (dataList == null || dataList.isEmpty()) {
            log.info("未查询到配方/设备/步骤数据");
            return;
        }
        log.info("查询到配方/设备/步骤数据 {} 条", dataList.size());

        // ============ 1. 配方同步 ============
        // 从抽取数据中提取去重配方 (areaNo + recipeNo 为唯一键)
        Map<String, RecipeExtractDto> extractRecipeMap = new LinkedHashMap<>();
        for (RecipeExtractDto dto : dataList) {
            String key = dto.getAreaNo() + "_" + dto.getRecipeNo();
            if (!extractRecipeMap.containsKey(key)) {
                extractRecipeMap.put(key, dto);
            }
        }

        // 获取本地配方数据(包含del_flag !=0) where data_source = 2
        List<PiCurveStandardRecipe> existingRecipes = piCurveStandardRecipeMapper.selectAllByDataSource();
        Map<String, PiCurveStandardRecipe> existingRecipeMap = new HashMap<>();
        Set<String> raRecipeIds = new HashSet<>();
        for (PiCurveStandardRecipe recipe : existingRecipes) {
            String key = recipe.getAreaNo() + "_" + recipe.getRecipeNo();
            existingRecipeMap.put(key, recipe);
            raRecipeIds.add(recipe.getId());
        }

        // 配方比较: del_flag=0更新, del_flag!=0不做操作, 不存在则新增
        Map<String, PiCurveStandardRecipe> recipeLookupMap = new HashMap<>();
        List<PiCurveStandardRecipe> recipeInsertList = new ArrayList<>();

        for (Map.Entry<String, RecipeExtractDto> entry : extractRecipeMap.entrySet()) {
            String key = entry.getKey();
            RecipeExtractDto dto = entry.getValue();
            PiCurveStandardRecipe existing = existingRecipeMap.get(key);

            if (existing != null) {
                if (existing.getDelFlag() != null && existing.getDelFlag() == 0) {
                    // del_flag = 0: 比较 判断是否更新
                    recipeLookupMap.put(key, existing);
                    if (!Objects.equals(existing.getRecipeName(), dto.getRecipeName())) {
                        existing.setRecipeName(dto.getRecipeName());
                        existing.setUpdateTime(new Date());
                        piCurveStandardRecipeService.updateById(existing);
                        log.info("更新配方: areaNo={}, recipeNo={}, recipeName={}", dto.getAreaNo(), dto.getRecipeNo(), dto.getRecipeName());
                    }
                }
                // del_flag != 0: 不做操作
            } else {
                // 新增配方
                PiCurveStandardRecipe newRecipe = new PiCurveStandardRecipe();
                newRecipe.setDataSource(2);
                newRecipe.setAreaNo(String.valueOf(dto.getAreaNo()));
                newRecipe.setRecipeNo(String.valueOf(dto.getRecipeNo()));
                newRecipe.setRecipeName(dto.getRecipeName());
                newRecipe.setSort(Double.valueOf(dto.getRecipeNo()));
                newRecipe.setCreateTime(new Date());
                recipeInsertList.add(newRecipe);
                recipeLookupMap.put(key, newRecipe);
            }
        }

        if (!recipeInsertList.isEmpty()) {
            piCurveStandardRecipeService.saveBatch(recipeInsertList);
            log.info("新增配方 {} 条", recipeInsertList.size());
        }

        // ============ 2. 设备分组同步 ============
        // 从抽取数据中提取去重分组 (recipeNo + unitNo 为唯一键)
        Map<String, RecipeExtractDto> extractGroupMap = new LinkedHashMap<>();
        for (RecipeExtractDto dto : dataList) {
            String key = dto.getRecipeNo() + "_" + dto.getUnitNo();
            if (!extractGroupMap.containsKey(key)) {
                extractGroupMap.put(key, dto);
            }
        }

        // 获取本地设备分组数据(包含del_flag !=0), 仅关注data_source=2配方关联的分组
        List<PiCurveStandardGroup> existingGroups = piCurveStandardGroupMapper.selectAllIncludingDeleted();
        Map<String, PiCurveStandardGroup> existingGroupMap = new HashMap<>();
        for (PiCurveStandardGroup group : existingGroups) {
            if (!raRecipeIds.contains(group.getRecipeId())) {
                continue;
            }
            String key = group.getRecipeId() + "_" + group.getGroupName();
            existingGroupMap.put(key, group);
        }

        // 分组比较: del_flag=0更新, del_flag!=0不做操作, 不存在则新增
        Map<String, PiCurveStandardGroup> groupLookupMap = new HashMap<>();
        List<PiCurveStandardGroup> groupInsertList = new ArrayList<>();

        for (Map.Entry<String, RecipeExtractDto> entry : extractGroupMap.entrySet()) {
            String extractKey = entry.getKey();
            RecipeExtractDto dto = entry.getValue();

            // 查找对应的配方
            String recipeKey = dto.getAreaNo() + "_" + dto.getRecipeNo();
            PiCurveStandardRecipe recipe = recipeLookupMap.get(recipeKey);
            if (recipe == null) {
                continue;
            }

            String groupName = dto.getUnitName();
            String existingKey = recipe.getId() + "_" + groupName;
            PiCurveStandardGroup existing = existingGroupMap.get(existingKey);

            if (existing != null) {
                if (existing.getDelFlag() != null && existing.getDelFlag() == 0) {
                    groupLookupMap.put(extractKey, existing);
                }
                // del_flag != 0: 不做操作
            } else {
                PiCurveStandardGroup newGroup = new PiCurveStandardGroup();
                newGroup.setRecipeId(recipe.getId());
                newGroup.setTenantId(recipe.getTenantId());
                newGroup.setFactoryCode(recipe.getFactoryCode());
                newGroup.setDeptCode(recipe.getDeptCode());
                newGroup.setLineCode(recipe.getLineCode());
                newGroup.setGroupName(groupName);
                newGroup.setSort(Double.valueOf(dto.getUnitNo()));
                newGroup.setCreateTime(new Date());
                groupInsertList.add(newGroup);
                groupLookupMap.put(extractKey, newGroup);
            }
        }

        if (!groupInsertList.isEmpty()) {
            piCurveStandardGroupService.saveBatch(groupInsertList);
            log.info("新增设备分组 {} 条", groupInsertList.size());
        }

        // ============ 3. 步骤同步 ============
        // 获取本地步骤数据(包含del_flag !=0) where data_source = 2
        List<PiCurveStandardConfig> existingConfigs = baseMapper.selectAllByDataSource2();
        Map<String, PiCurveStandardConfig> existingConfigMap = new HashMap<>();
        for (PiCurveStandardConfig config : existingConfigs) {
            String key = config.getGroupId() + "_" + config.getEopNo();
            existingConfigMap.put(key, config);
        }

        List<PiCurveStandardConfig> configInsertList = new ArrayList<>();
        int configUpdateCount = 0;

        for (RecipeExtractDto dto : dataList) {
            // 查找对应的配方
            String recipeKey = dto.getAreaNo() + "_" + dto.getRecipeNo();
            PiCurveStandardRecipe recipe = recipeLookupMap.get(recipeKey);
            if (recipe == null) {
                continue;
            }

            // 查找对应的分组
            String groupKey = dto.getRecipeNo() + "_" + dto.getUnitNo();
            PiCurveStandardGroup group = groupLookupMap.get(groupKey);
            if (group == null) {
                continue;
            }

            String configKey = group.getId() + "_" + dto.getEopNo();
            PiCurveStandardConfig existing = existingConfigMap.get(configKey);

            if (existing != null) {
                if (existing.getDelFlag() != null && existing.getDelFlag() == 0) {
                    // del_flag = 0: 比较 判断是否更新
                    boolean needUpdate = false;
                    if (!Objects.equals(existing.getRecipeName(), dto.getRecipeName())) {
                        existing.setRecipeName(dto.getRecipeName());
                        needUpdate = true;
                    }
                    if (!Objects.equals(existing.getUnitName(), dto.getUnitName())) {
                        existing.setUnitName(dto.getUnitName());
                        needUpdate = true;
                    }
                    if (!Objects.equals(existing.getEopName(), dto.getEopName())) {
                        existing.setEopName(dto.getEopName());
                        needUpdate = true;
                    }
                    Double sortVal = dto.getStepInProcedure() != null ? Double.valueOf(dto.getStepInProcedure()) : null;
                    if (!Objects.equals(existing.getSort(), sortVal)) {
                        existing.setSort(sortVal);
                        needUpdate = true;
                    }
                    if (needUpdate) {
                        existing.setUpdateTime(new Date());
                        updateById(existing);
                        configUpdateCount++;
                    }
                }
                // del_flag != 0: 不做操作
            } else {
                // 新增步骤配置
                PiCurveStandardConfig newConfig = new PiCurveStandardConfig();
                newConfig.setRecipeId(recipe.getId());
                newConfig.setGroupId(group.getId());
                newConfig.setTenantId(recipe.getTenantId());
                newConfig.setFactoryCode(recipe.getFactoryCode());
                newConfig.setDeptCode(recipe.getDeptCode());
                newConfig.setLineCode(recipe.getLineCode());
                newConfig.setDataSource(2);
                newConfig.setAreaNo(String.valueOf(dto.getAreaNo()));
                newConfig.setRecipeNo(String.valueOf(dto.getRecipeNo()));
                newConfig.setRecipeName(dto.getRecipeName());
                newConfig.setUnitNo(String.valueOf(dto.getUnitNo()));
                newConfig.setUnitName(dto.getUnitName());
                newConfig.setEopNo(String.valueOf(dto.getEopNo()));
                newConfig.setEopName(dto.getEopName());
                newConfig.setSort(dto.getStepInProcedure() != null ? Double.valueOf(dto.getStepInProcedure()) : null);
                newConfig.setCreateTime(new Date());
                newConfig.setDurationType(1);
                configInsertList.add(newConfig);
            }
        }

        if (!configInsertList.isEmpty()) {
            saveBatch(configInsertList);
            log.info("新增步骤配置 {} 条", configInsertList.size());
        }
        if (configUpdateCount > 0) {
            log.info("更新步骤配置 {} 条", configUpdateCount);
        }

        log.info("曲线标准基础数据抽取完成");
    }

    /**
     * 查询配方/设备/步骤数据(切换BatchDB数据源)
     */
    @DS("rabasesqlservice")
    @Override
    public List<RecipeExtractDto> queryRecipeExtractData() {
        return baseMapper.selectRecipeExtractData();
    }
}
