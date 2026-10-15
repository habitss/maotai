package com.keytech.modules.line.efficiency.mapper;

import com.keytech.modules.line.efficiency.dto.ManualMessagesDto;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.line.efficiency.entity.ManualFtLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: 上位机系统手动操作记录
 * @Author: jeecg-boot
 * @Date:   2025-11-13
 * @Version: V1.0
 */
@Mapper
public interface ManualFtLogMapper extends BaseMapper<ManualFtLog> {

    /**
     * 黑龙江-上位机系统手动操作记录分页查询
     * @param lastTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Select("SELECT * FROM FTLog WITH(NOLOCK) " +
            "WHERE TimeStmp >= #{lastTime} AND TimeStmp <= #{currentTime} " +
            "and Audience = 2 and Severity = 4 and Provider = 'FactoryTalk View SATag' "+
            "ORDER BY TimeStmp,RecordID " +
            "OFFSET #{offset} ROWS " +
            "FETCH NEXT #{pageSize} ROWS ONLY")
    List<ManualFtLog> selectManualDataWithPaging(Timestamp lastTime, Timestamp currentTime, long offset, long pageSize);


    /**
     * 其他工厂-上位机系统手动操作记录分页查询
     * @param lastTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Select("SELECT * FROM sistar_rt_messages WITH(NOLOCK) " +
            "WHERE msg_time >= #{lastTime} AND msg_time <= #{currentTime} " +
            "AND (MSG_TEXT LIKE '%手动状态%' " +
            "    OR MSG_TEXT LIKE '%PID内部模式%' " +
            "    OR MSG_TEXT LIKE '%ICM切换至自动模式%' " +
            "    OR MSG_TEXT LIKE '%手动模式%' " +
            "    OR MSG_TEXT LIKE '%模拟状态%' " +
            "    OR MSG_TEXT LIKE '%控制器模式%:%自动模式开%' " +
            "    OR MSG_TEXT LIKE '%不在自动模式%' " +
            "    OR MSG_TEXT LIKE '%工厂段消息 : 步骤选择%') " +
            "ORDER BY msg_time,ds_id " +
            "OFFSET #{offset} ROWS " +
            "FETCH NEXT #{pageSize} ROWS ONLY")
    List<ManualMessagesDto> selectManualPmDataWithPaging(Timestamp lastTime, Timestamp currentTime, long offset, long pageSize);

    /**
     * 更新序号
     * @param tableName
     */
    @Update("<script>" +
            "UPDATE `${tableName}` " +
            "SET field_0001 = id " +
            "WHERE field_0001 = 0" +
            "</script>")
    void updateDateNumber(@Param("tableName") String tableName);
}
