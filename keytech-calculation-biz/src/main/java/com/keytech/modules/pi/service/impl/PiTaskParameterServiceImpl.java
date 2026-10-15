package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.dto.TaskDataDto;
import com.keytech.modules.pi.entity.PiTaskParameter;
import com.keytech.modules.pi.mapper.PiBasePointMapper;
import com.keytech.modules.pi.mapper.PiTaskParameterMapper;
import com.keytech.modules.pi.service.IPiTaskParameterService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: pi任务参数配置
 * @Author: jeecg-boot
 * @Date:   2024-11-25
 * @Version: V1.0
 */
@Service
public class PiTaskParameterServiceImpl extends BaseServiceImpl<PiTaskParameterMapper, PiTaskParameter> implements IPiTaskParameterService {

    @Resource
    PiTaskParameterMapper piTaskParameterMapper;
    //定时任务库名称
    @Value("${mes-system.db-task}")
    private String dbTask;



    //定时任务下拉数据
    @Override
    public List<TaskDataDto> queryTask(TaskDataDto dto) {
        return piTaskParameterMapper.queryTask(dto,dbTask);
    }


    //匹配定时任务名称
    @Override
    public Page<PiTaskParameter> setCodeName(Page<PiTaskParameter> list) {
        //定时任务集合
        List<TaskDataDto> taskList =  piTaskParameterMapper.getTask(null,dbTask);
        List<PiTaskParameter> taskParameterList = list.getRecords();
        Map<String, TaskDataDto> taskMap = taskList.stream()
                .collect(Collectors.toMap(TaskDataDto::getValue, t -> t));
        taskParameterList.stream()
                .forEach(parameter -> parameter.setTaskName(taskMap.get(parameter.getTaskId()).getLabel()));
        return list;
    }
}
