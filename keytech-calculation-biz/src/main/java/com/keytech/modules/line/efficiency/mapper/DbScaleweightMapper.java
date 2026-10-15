package com.keytech.modules.line.efficiency.mapper;

import com.keytech.modules.line.efficiency.entity.DbSourceScaleweight;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.line.efficiency.entity.DbScaleweight;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface DbScaleweightMapper extends BaseMapper<DbScaleweight> {

}
