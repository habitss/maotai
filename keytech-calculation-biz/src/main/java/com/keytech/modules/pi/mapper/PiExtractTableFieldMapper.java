package com.keytech.modules.pi.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.pi.vo.ColumnInfoVo;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: PI-数据抽取配置字段表
 * @Author: jeecg-boot
 * @Date:   2025-08-26
 * @Version: V1.0
 */
@Mapper
public interface PiExtractTableFieldMapper extends BaseMapper<PiExtractTableField> {

    @DS("bmsqlservice")
    List<ColumnInfoVo> getTableField(String tableName);

    List<ColumnInfoVo> getTargetTableField(PiExtractTableField piExtractTableField);
}
