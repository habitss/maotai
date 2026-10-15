package com.keytech.modules.df.data.mapper;

import com.keytech.modules.df.data.entity.DfDataLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * DF数据日志表
 * @author yepeng
 * @since 2025.12.11
 * */
@Mapper
public interface DfDataLogMapper extends BaseMapper<DfDataLog> {

    /**
     * 批量插入或更新DF数据日志
     * @param dataList 数据列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(@Param("list") List<DfDataLog> dataList);

}
