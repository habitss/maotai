package com.keytech.modules.ems.mapper;

import com.keytech.modules.ems.entity.AggregationStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 聚合计算状态表
 * @author yepeng
 * @since 2025.05.12
 * */
public interface AggregationStatusMapper extends BaseMapper<AggregationStatus> {

    /**
     * 批量插入或修改聚合任务状态
     * @param list 列表
     * @since 2025.05.14
     */
    void batchInsertOrUpdateAggregationStatus(@Param("list") List<AggregationStatus> list);

}
