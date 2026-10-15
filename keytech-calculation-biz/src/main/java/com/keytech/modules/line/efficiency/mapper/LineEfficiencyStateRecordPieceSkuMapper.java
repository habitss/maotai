package com.keytech.modules.line.efficiency.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecordPieceSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 线效清零分段-按照sku记录
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Mapper
public interface LineEfficiencyStateRecordPieceSkuMapper extends BaseMapper<LineEfficiencyStateRecordPieceSku> {

}
