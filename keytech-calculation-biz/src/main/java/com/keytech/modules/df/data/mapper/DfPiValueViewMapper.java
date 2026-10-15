package com.keytech.modules.df.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.df.data.vo.DfPiValueView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DF视图数据 Mapper
 * @author yepeng
 * @since 2025.12.12
 * */
@Mapper
public interface DfPiValueViewMapper extends BaseMapper<DfPiValueView> {

    /**
     * 查询指定时间数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据列表
     */
    List<DfPiValueView> selectList(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

}
