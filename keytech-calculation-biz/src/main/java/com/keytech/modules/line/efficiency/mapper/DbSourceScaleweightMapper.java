package com.keytech.modules.line.efficiency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.line.efficiency.entity.DbScaleweight;
import com.keytech.modules.line.efficiency.entity.DbSourceScaleweight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: 地磅数据
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Mapper
public interface DbSourceScaleweightMapper extends BaseMapper<DbSourceScaleweight> {

    /**
     * 地磅数据分页查询
     * @param lastTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Select("SELECT * FROM scaleweight WITH(NOLOCK) " +
            "WHERE outime >= #{lastTime} AND outime <= #{currentTime} " +
            "ORDER BY outime,intime " +
            "OFFSET #{offset} ROWS " +
            "FETCH NEXT #{pageSize} ROWS ONLY")
    List<DbSourceScaleweight> selectScaleweightDataWithPaging(
            @Param("lastTime") Timestamp lastTime,
            @Param("currentTime") Timestamp currentTime,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);
}
