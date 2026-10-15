package com.keytech.modules.line.efficiency.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.line.efficiency.entity.TbPkgClassSkuDetailOp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: OPI拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-03
 * @Version: V1.0
 */
@Mapper
@DS("opi")
public interface TbPkgClassSkuDetailOpMapper extends BaseMapper<TbPkgClassSkuDetailOp> {

}
