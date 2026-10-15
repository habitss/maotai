package com.keytech.modules.pi.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.pi.vo.TableInfoVo;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiExtractTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @Description: PI-数据抽取配置主表
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Mapper
public interface PiExtractTableMapper extends BaseMapper<PiExtractTable> {

    //指定bmsqlservice数据源
    @DS("bmsqlservice")
    List<Map<String, Object>> extractTest(String extractSql, String dataSourceName);


    /**
     * 获取库表信息
     * @return
     */
    @DS("bmsqlservice")
    List<TableInfoVo> getTable();

    /**
     * 获取目标表
     * @return
     */
    List<TableInfoVo> getTargetTable();
}
