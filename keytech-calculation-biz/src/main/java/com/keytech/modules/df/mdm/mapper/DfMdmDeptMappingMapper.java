package com.keytech.modules.df.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.df.mdm.entity.DfMdmDeptMapping;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: MDM部门与系统部门映射关系表
 * @Author: jeecg-boot
 * @Date:   2025-12-25
 * @Version: V1.0
 */
@Mapper
public interface DfMdmDeptMappingMapper extends BaseMapper<DfMdmDeptMapping> {
    /**
     * 批量插入或更新
     * @since 2025.12.08
     */
    void syncMdmDeptMapping();
}
