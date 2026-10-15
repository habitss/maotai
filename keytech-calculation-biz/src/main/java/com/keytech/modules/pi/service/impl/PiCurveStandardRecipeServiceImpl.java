package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.pi.entity.PiCurveStandardGroup;
import com.keytech.modules.pi.entity.PiCurveStandardRecipe;
import com.keytech.modules.pi.mapper.PiCurveStandardRecipeMapper;
import com.keytech.modules.pi.service.IPiCurveStandardGroupService;
import com.keytech.modules.pi.service.IPiCurveStandardRecipeService;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 曲线标准-配方
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
@Service
public class PiCurveStandardRecipeServiceImpl extends BaseServiceImpl<PiCurveStandardRecipeMapper, PiCurveStandardRecipe> implements IPiCurveStandardRecipeService {

    @Autowired
    private IPiCurveStandardGroupService piCurveStandardGroupService;

    @Override
    public Result<String> recipeSaveOrUpdate(PiCurveStandardRecipe piCurveStandardRecipe) {
        //判断配方是否存在
        LambdaQueryWrapper<PiCurveStandardRecipe> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PiCurveStandardRecipe::getRecipeNo, piCurveStandardRecipe.getRecipeNo());
        queryWrapper.eq(PiCurveStandardRecipe::getAreaNo, piCurveStandardRecipe.getAreaNo());
        queryWrapper.eq(PiCurveStandardRecipe::getRecipeName, piCurveStandardRecipe.getRecipeName());
        List<PiCurveStandardRecipe> list = list(queryWrapper);
        if(list.size() > 0){
            return Result.error("配方已存在");
        }
        saveOrUpdate(piCurveStandardRecipe);
        return Result.ok("保存成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recipeDelete(String id) {
        //1.查询配方关联的分组
        List<PiCurveStandardGroup> groupList = piCurveStandardGroupService.list(
                new LambdaQueryWrapper<PiCurveStandardGroup>()
                        .eq(PiCurveStandardGroup::getRecipeId, id)
        );

        //2.删除分组(级联删除配置和拆分数据)
        if (!groupList.isEmpty()) {
            List<String> groupIds = groupList.stream()
                    .map(PiCurveStandardGroup::getId)
                    .collect(Collectors.toList());
            piCurveStandardGroupService.groupDelete(groupIds);
        }

        //3.删除配方
        removeById(id);
    }
}
