package com.keytech.modules.ems.mapper;

import com.keytech.modules.ems.entity.CollectPointStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 点位采集状态表
 * @author yepeng
 * @since 2025.04.29
 * */
public interface CollectPointStatusMapper extends BaseMapper<CollectPointStatus> {

    /**
     * 查询指定点位最近的一条完成记录
     * @param tagIds 点位编码列表
     * @param collectTime 指定采集时间
     */
    List<CollectPointStatus> selectLastHadBaseValRecordsByPoints(@Param("tagIds") List<String> tagIds, @Param("collectTime") LocalDateTime collectTime);

}
