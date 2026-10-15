package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.RerunDto;
import com.keytech.modules.pi.entity.PiAllocationTable;
import com.keytech.modules.pi.vo.PiAllocationTableVisitsVo;
import org.jeecg.common.api.vo.Result;

import java.util.List;

/**
 * @Description: pi报表分配
 * @Author: jeecg-boot
 * @Date:   2024-11-26
 * @Version: V1.0
 */
public interface IPiAllocationTableService extends BaseService<PiAllocationTable> {


    /**
     * pi报表重算
     * @param rerunDto
     * @return
     */
    Result<String> rerun(RerunDto rerunDto);

    /**
     * pi报表批量删除
     * @param data
     * @param tableName
     * @return
     */
    boolean deleteDataBatch(String data, String tableName);

    /**
     * 统计报表访问次数
     * @param piAllocationTableVisitsVo 参数
     * @return 统计结果
     */
    List<PiAllocationTableVisitsVo> queryAllocationTableVisits(PiAllocationTableVisitsVo piAllocationTableVisitsVo);

}
