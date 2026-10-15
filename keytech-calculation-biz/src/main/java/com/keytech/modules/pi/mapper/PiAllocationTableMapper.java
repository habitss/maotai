package com.keytech.modules.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.entity.PiAllocationTable;
import com.keytech.modules.pi.vo.PiAllocationTableVisitsVo;

import java.util.List;

/**
 * @Description: pi报表分配
 * @Author: jeecg-boot
 * @Date:   2024-11-26
 * @Version: V1.0
 */
public interface PiAllocationTableMapper extends BaseMapper<PiAllocationTable> {
    List<PiAllocationTableVisitsVo> queryAllocationTableVisits(PiAllocationTableVisitsVo piAllocationTableVisitsVo);
}
