package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.dto.PiRepeatCopyDto;
import com.keytech.modules.pi.entity.PiRepeatExceptionType;
import com.keytech.modules.pi.mapper.PiRepeatExceptionTypeMapper;
import com.keytech.modules.pi.service.IPiRepeatExceptionTypeService;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: pi重复异常类型配置
 * @Author: jeecg-boot
 * @Date:   2025-12-03
 * @Version: V1.0
 */
@Service
public class PiRepeatExceptionTypeServiceImpl extends BaseServiceImpl<PiRepeatExceptionTypeMapper, PiRepeatExceptionType> implements IPiRepeatExceptionTypeService {

    @Override
    @Transactional
    public void saveOrUpdateData(PiRepeatExceptionType piRepeatExceptionType) {
        //查询已存在的异常名称
        List<PiRepeatExceptionType> list = getList(
                piRepeatExceptionType.getId(),
                piRepeatExceptionType.getPiTableId(),
                piRepeatExceptionType.getPiFieldId(),
                Collections.singletonList(piRepeatExceptionType.getExceptionName())
        );
        if(CollUtil.isNotEmpty(list)){
            throw new JeecgBootException("【" + piRepeatExceptionType.getExceptionName() + "】已存在!");
        }
        this.saveOrUpdate(piRepeatExceptionType);
    }

    @Override
    @Transactional
    public void addCopy(PiRepeatCopyDto piRepeatCopyDto) {
        // 获取copyList中的exceptionName
        List<String> exceptionNames = piRepeatCopyDto.getCopyList().stream()
                .map(PiRepeatExceptionType::getExceptionName)
                .toList();
        //查询已存在的异常名称
        List<PiRepeatExceptionType> list = getList(
                null,
                piRepeatCopyDto.getPiTableId(),
                piRepeatCopyDto.getPiFieldId(),
                exceptionNames
        );
        Map<String, String> exceptionMap = list.stream()
                .collect(Collectors.toMap(
                        PiRepeatExceptionType::getExceptionName,
                        PiRepeatExceptionType::getExceptionName
                ));
        //筛选出未存在的异常名称
        List<PiRepeatExceptionType> newList = new ArrayList<>();
        for(PiRepeatExceptionType piRepeatExceptionType : piRepeatCopyDto.getCopyList()){
            if(!exceptionMap.containsKey(piRepeatExceptionType.getExceptionName())){
                PiRepeatExceptionType type = new PiRepeatExceptionType();
                BeanUtil.copyProperties(piRepeatCopyDto, type);
                type.setExceptionName(piRepeatExceptionType.getExceptionName());
                type.setSort(piRepeatExceptionType.getSort());
                newList.add(type);
            }
        }
        this.saveBatch(newList);
    }

    private List<PiRepeatExceptionType> getList(String id, String piTableId, String piFieldId, List<String> exceptionNameList) {
        //查询已存在的异常名称
        return this.lambdaQuery()
                .eq(PiRepeatExceptionType::getPiTableId, piTableId)
                .eq(PiRepeatExceptionType::getPiFieldId, piFieldId)
                .in(PiRepeatExceptionType::getExceptionName, exceptionNameList)
                .ne(StrUtil.isNotBlank(id), PiRepeatExceptionType::getId, id)
                .list();
    }
}
