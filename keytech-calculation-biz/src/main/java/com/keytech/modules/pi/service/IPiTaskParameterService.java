package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.TaskDataDto;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.entity.PiTaskParameter;

import java.util.List;

/**
 * @Description: pi任务参数配置
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface IPiTaskParameterService extends BaseService<PiTaskParameter> {

    //定时任务下拉数据
    List<TaskDataDto> queryTask(TaskDataDto dto);

    /**
     * 设置报表名称
     * @param list
     * @return
     */
    Page<PiTaskParameter> setCodeName(Page<PiTaskParameter> list);
}
