package com.keytech.modules.manage.check.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
@Mapper
public interface ManageCheckExecutionRateMapper extends BaseMapper<ManageCheckList> {

    /**
     * 执行率统计
     * @param checkDateBegin
     * @param checkDateEnd
     * @param dept
     * @param postId
     * @return
     */
    List<Map<String, Object>> getExecutionRate(@Param("checkDateBegin") String checkDateBegin,
                                          @Param("checkDateEnd") String checkDateEnd,
                                          @Param("dept") String dept,
                                          @Param("postId") String postId);

} 
