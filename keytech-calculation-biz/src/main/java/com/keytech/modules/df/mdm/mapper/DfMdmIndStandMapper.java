package com.keytech.modules.df.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.df.mdm.entity.DfMdmIndStand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: DF主数据-指标标准
 * @Author: jeecg-boot
 * @Date:   2026-01-26
 * @Version: V1.0
 */
@Mapper
public interface DfMdmIndStandMapper extends BaseMapper<DfMdmIndStand> {
    /**
     * 批量插入或更新
     * @param list 列表
     */
    void batchInsertOrUpdate(@Param("list") List<DfMdmIndStand> list);
}
