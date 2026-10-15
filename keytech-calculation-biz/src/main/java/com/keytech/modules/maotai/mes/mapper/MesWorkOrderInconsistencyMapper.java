package com.keytech.modules.maotai.mes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.mes.entity.MesWorkOrderInconsistency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单重复校验不一致记录(API02) Mapper。
 *
 * @author maotai
 * @since 2026-08-13
 */
@Mapper
public interface MesWorkOrderInconsistencyMapper extends BaseMapper<MesWorkOrderInconsistency> {
}
