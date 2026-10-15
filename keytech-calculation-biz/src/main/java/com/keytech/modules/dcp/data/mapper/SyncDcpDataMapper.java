package com.keytech.modules.dcp.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * DCP视图数据 Mapper
 * */
@Mapper
public interface SyncDcpDataMapper {

    /**
     * 批量新增数据
     * @param dataList 数据列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(@Param("list") List<Map<String, Object>> dataList);

    /**
     * 批量修改数据
     * @param dataList 数据列表
     * @return 影响行数
     */
    int batchUpdateData(@Param("list") List<Map<String, Object>> dataList);

    /**
     * 查询数据
     * @param thingId thingid
     * @param recipeId recipeId品牌id
     * @param bbtMtNo bbtMtNo BBT罐号
     * @param flagPoint 标记点
     * @return 查询的数据
     */
    List<Map<String, Object>> queryData(String thingId, String recipeId, String bbtMtNo, String flagPoint);
}
