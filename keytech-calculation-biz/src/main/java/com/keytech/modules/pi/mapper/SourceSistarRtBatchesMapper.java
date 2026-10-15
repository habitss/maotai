package com.keytech.modules.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.entity.SourceSistarRtBatches;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter数据-步骤明细
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Mapper
public interface SourceSistarRtBatchesMapper extends BaseMapper<SourceSistarRtBatches> {


    /**
     * @Description: 获取增量数据
     * @param lastTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    // 使用参数分页 ORDER BY insert_timestamp, id
    @Select("SELECT * FROM sistar_rt_batches WITH(NOLOCK) " +
            "WHERE insert_timestamp > #{lastTime} AND insert_timestamp <= #{currentTime} " +
            "ORDER BY insert_timestamp,batch_no " +
            "OFFSET #{offset} ROWS " +
            "FETCH NEXT #{pageSize} ROWS ONLY")
    List<SourceSistarRtBatches> selectIncrementalDataWithPaging(
            @Param("lastTime") Timestamp lastTime,
            @Param("currentTime") Timestamp currentTime,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);
}
