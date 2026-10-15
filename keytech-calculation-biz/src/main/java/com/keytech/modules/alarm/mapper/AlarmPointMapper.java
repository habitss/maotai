package com.keytech.modules.alarm.mapper;

import com.keytech.modules.alarm.entity.AlarmPoint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 报警点位
 * @author liu
 * @since 2026.01.12
 * */
@Mapper
public interface AlarmPointMapper extends BaseMapper<AlarmPoint> {
    /**
     * 批量插入或更新
     * @param list 列表
     * @since 2025.12.08
     */
    void batchInsertOrUpdate(@Param("list") List<AlarmPoint> list);
}
