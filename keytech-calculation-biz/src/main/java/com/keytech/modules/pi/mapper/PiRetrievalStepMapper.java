package com.keytech.modules.pi.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.entity.PiCalculateSignal;
import com.keytech.modules.pi.entity.PiRetrievalStep;
import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * pi-取值步骤
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-15 22:34:22
 */
@Mapper
public interface PiRetrievalStepMapper extends BaseMapper<PiRetrievalStep> {
}
