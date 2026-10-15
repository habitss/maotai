package com.keytech.modules.df.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.df.mdm.entity.DfMdmFunloc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: DF主数据-功能位置
 * @Author: jeecg-boot
 * @Date:   2026-01-28
 * @Version: V1.0
 */
@Mapper
public interface DfMdmFunlocMapper extends BaseMapper<DfMdmFunloc> {
    /**
     * 批量插入或更新
     * @param list 列表
     */
    void batchInsertOrUpdateFunloc(@Param("list") List<DfMdmFunloc> list);
}
