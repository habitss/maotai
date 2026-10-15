package com.keytech.modules.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.dto.TaskDataDto;
import com.keytech.modules.pi.entity.PiTaskParameter;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: pi任务参数配置
 * @Author: jeecg-boot
 * @Date:   2024-11-25
 * @Version: V1.0
 */
public interface PiTaskParameterMapper extends BaseMapper<PiTaskParameter> {
    List<TaskDataDto> getTask(@Param("groupId")String groupId, @Param("dbTask")String dbTask);

    List<TaskDataDto> queryTask(@Param("dto") TaskDataDto dto,
                                @Param("dbTask") String dbTask);
}
