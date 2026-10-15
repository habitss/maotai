package com.keytech.modules.df.mdm.mapper;

import com.keytech.modules.df.mdm.entity.DfMdmAlarmtag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * MDM-报警点位主数据表
 * @author yepeng
 * @since 2025.12.24
 * */
@Mapper
public interface DfMdmAlarmtagMapper extends BaseMapper<DfMdmAlarmtag> {
    /**
     * 批量插入或更新
     * @param list 列表
     * @since 2025.12.08
     */
    void batchInsertOrUpdate(@Param("list") List<DfMdmAlarmtag> list);
}
