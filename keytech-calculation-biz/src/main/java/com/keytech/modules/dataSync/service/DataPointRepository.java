package com.keytech.modules.dataSync.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.dataSync.config.DataPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SQLServer 数据访问层（视图名配置化）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@DS("sqlserver")
public class DataPointRepository {

    // 注入 SQLServer 专用 JdbcTemplate
    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate sqlServerJdbcTemplate;

    // 注入实时视图名
    private String realtimeViewName  ="[KDA].[V_DATAPOINT_LDS_YANJING_CURRENT]";

    // 注入历史视图名
    private String historyViewName = "[KDA].[V_DATAPOINT_LDS_YANJING_HISTORY]";

    /**
     * 查询实时视图最新数据（10秒一次）
     */
    public List<DataPoint> queryRealtimeData() {
        // 动态拼接SQL，使用注入的视图名
        String sql = String.format("""
                SELECT 
                    Data_Point_ID as dataPointId, 
                    Data_Point_Name as dataPointName, 
                    Equipment_ID as equipmentId, 
                    Equipment_Name as equipmentName, 
                    UOM_Name as uomName, 
                    Value as value, 
                    TIMESTAMP_FROM as timestampFrom 
                FROM %s 
                WHERE TIMESTAMP_FROM IS NOT NULL
                """, realtimeViewName);
        try {
            return sqlServerJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(DataPoint.class));
        } catch (Exception e) {
            log.error("查询实时数据失败 [视图名:{}]", realtimeViewName, e);
            return List.of();
        }
    }

    /**
     * 查询历史数据（断点续传）
     */
    public List<DataPoint> queryHistoryData(LocalDateTime startTime, LocalDateTime endTime) {
        // 动态拼接SQL，使用注入的视图名
        String sql = String.format("""
                SELECT 
                    Data_Point_ID as dataPointId, 
                    Data_Point_Name as dataPointName, 
                    Equipment_ID as equipmentId, 
                    Equipment_Name as equipmentName, 
                    UOM_Name as uomName, 
                    Value as value, 
                    TIMESTAMP_FROM as timestampFrom, 
                    TIMESTAMP_TO as timestampTo 
                FROM %s 
                WHERE TIMESTAMP_FROM >= ? AND TIMESTAMP_TO <= ? 
                ORDER BY TIMESTAMP_FROM ASC
                """, historyViewName);
        try {
            return sqlServerJdbcTemplate.query(
                    sql,
                    new Object[]{Timestamp.valueOf(startTime), Timestamp.valueOf(endTime)},
                    new BeanPropertyRowMapper<>(DataPoint.class)
            );
        } catch (Exception e) {
            log.error("查询历史数据失败 [视图名:{} start:{} end:{}]", historyViewName, startTime, endTime, e);
            return List.of();
        }
    }

    /**
     * 获取实时视图最新的时间戳（用于判断是否中断）
     */
    public LocalDateTime getRealtimeLatestTime() {
        // 动态拼接SQL，使用注入的视图名
        String sql = String.format("SELECT MAX(TIMESTAMP_FROM) FROM %s", realtimeViewName);
        try {
            return sqlServerJdbcTemplate.queryForObject(sql, LocalDateTime.class);
        } catch (Exception e) {
            log.error("获取实时数据最新时间失败 [视图名:{}]", realtimeViewName, e);
            return null;
        }
    }

    /**
     * 按点位查询历史数据
     */
    public List<DataPoint> queryHistoryDataByPoint(String dataPointId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = String.format("""
            SELECT 
                Data_Point_ID as dataPointId, 
                Data_Point_Name as dataPointName, 
                Equipment_ID as equipmentId, 
                Equipment_Name as equipmentName, 
                UOM_Name as uomName, 
                Value as value, 
                TIMESTAMP_FROM as timestampFrom, 
                TIMESTAMP_TO as timestampTo 
            FROM %s 
            WHERE Data_Point_ID = ? AND TIMESTAMP_FROM >= ? AND TIMESTAMP_TO <= ? 
            ORDER BY TIMESTAMP_FROM ASC
            """, historyViewName);
        try {
            return sqlServerJdbcTemplate.query(
                    sql,
                    new Object[]{dataPointId, Timestamp.valueOf(startTime), Timestamp.valueOf(endTime)},
                    new BeanPropertyRowMapper<>(DataPoint.class)
            );
        } catch (Exception e) {
            log.error("查询点位[{}]历史数据失败 [start:{} end:{}]", dataPointId, startTime, endTime, e);
            return List.of();
        }
    }

    /**
     * 按点位查询实时数据
     */
    public List<DataPoint> queryRealtimeDataByPoint(String dataPointId) {
        String sql = String.format("""
            SELECT 
                Data_Point_ID as dataPointId, 
                Data_Point_Name as dataPointName, 
                Equipment_ID as equipmentId, 
                Equipment_Name as equipmentName, 
                UOM_Name as uomName, 
                Value as value, 
                TIMESTAMP_FROM as timestampFrom 
            FROM %s 
            WHERE Data_Point_ID = ? 
            ORDER BY TIMESTAMP_FROM ASC
            """, realtimeViewName);
        try {
            return sqlServerJdbcTemplate.query(
                    sql,
                    new Object[]{dataPointId},
                    new BeanPropertyRowMapper<>(DataPoint.class)
            );
        } catch (Exception e) {
            log.error("查询点位[{}]实时数据失败", dataPointId, e);
            return List.of();
        }
    }
}