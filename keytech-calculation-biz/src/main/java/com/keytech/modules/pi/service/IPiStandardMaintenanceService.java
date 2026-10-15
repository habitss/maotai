package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.CopyStandardDto;
import com.keytech.modules.pi.entity.PiStandardMaintenance;

import java.util.List;
import java.util.Map;

/**
* @Description: 针对表【mes3_pi_standard_maintenance(PI标准维护)】的数据库操作Service
* @Author: sjw
* @CreateDate: 2024/12/11 14:37:17
*/
public interface IPiStandardMaintenanceService extends BaseService<PiStandardMaintenance> {
    /**
     * 获取pi标准维护数据
     * @param piStandardMaintenance
     */
    List<PiStandardMaintenance> getPiStandardMaintenance(PiStandardMaintenance piStandardMaintenance);

    /**
     * 获取标准
     * @param piStandardMaintenance
     * @return
     */
    List<PiStandardMaintenance> queryStandard(PiStandardMaintenance piStandardMaintenance);

    /**
     * 批量新增/更新
     * @param maintenanceList
     * @return
     */
    Map<String, String> batchEditVal(List<PiStandardMaintenance> maintenanceList);

    /**
     * 批量删除
     * @param data
     * @return
     */
    boolean deleteDataBatch(String data);

    /**
     * 复制标准
     * @param copyStandardDto
     * @return
     */
    boolean copyStandard(CopyStandardDto copyStandardDto);
}
