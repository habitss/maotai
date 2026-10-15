package com.keytech.modules.exception.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.exception.entity.ExceptionReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 异常处理报告
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
@Mapper
public interface ExceptionReportMapper extends BaseMapper<ExceptionReport> {

}
