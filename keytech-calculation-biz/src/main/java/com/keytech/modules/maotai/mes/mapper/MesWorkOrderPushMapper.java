package com.keytech.modules.maotai.mes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.mes.entity.MesWorkOrderPush;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单回传通知主表(API03) Mapper。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Mapper
public interface MesWorkOrderPushMapper extends BaseMapper<MesWorkOrderPush> {
}
