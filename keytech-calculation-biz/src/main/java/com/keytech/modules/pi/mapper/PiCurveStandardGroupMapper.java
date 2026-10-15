package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.keytech.modules.pi.entity.PiCurveStandardGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: 曲线标准-设备分组
 * @Author: jeecg-boot
 * @Date:   2026-03-05
 * @Version: V1.0
 */
@Mapper
public interface PiCurveStandardGroupMapper extends BaseMapper<PiCurveStandardGroup> {

    /**
     * 查询所有设备分组(包含已删除)
     */
    @Select("SELECT * FROM mes3_pi_curve_standard_group")
    List<PiCurveStandardGroup> selectAllIncludingDeleted();
}
