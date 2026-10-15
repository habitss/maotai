package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiStandardMaintenance;
import com.keytech.modules.pi.entity.PiStandardManage;
import com.keytech.modules.pi.mapper.PiStandardManageMapper;
import com.keytech.modules.pi.service.IPiStandardMaintenanceService;
import com.keytech.modules.pi.service.IPiStandardManageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @Description: 针对表【mes3_pi_standard_manage(PI标准管理)】的数据库操作Service实现
* @Author: sjw
* @CreateDate: 2024/12/11 14:37:06
*/
@Service
public class PiStandardManageServiceImpl extends BaseServiceImpl<PiStandardManageMapper, PiStandardManage>
    implements IPiStandardManageService {

    @Resource
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;

    @Override
    public Double getMaxSort(String customizeId) {
        return baseMapper.getMaxSort(customizeId);
    }


    /**
     * 标准复制方法。
     * 1.复制标准主表
     * 2.复制标准明细
     * @param id 要复制的对象的ID
     * @return 如果复制成功，则返回true；如果复制失败或未实现此方法，则返回null
     */
    @Override
    public Boolean standardCopy(String id) {
        Boolean isOk = true;
        //1.复制标准主表
        PiStandardManage piStandardManage = getById(id);
        piStandardManage.setId(null);
        piStandardManage.setSort(0.001);
        piStandardManage.setStandardName(piStandardManage.getStandardName()+"_复制");
        save(piStandardManage);
        //2.复制标准明细
        LambdaQueryWrapper<PiStandardMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PiStandardMaintenance::getManageId, id);
        List<PiStandardMaintenance> maintenanceList = iPiStandardMaintenanceService.list(queryWrapper);

        for (PiStandardMaintenance piStandardMaintenance : maintenanceList) {
            piStandardMaintenance.setId(null);
            piStandardMaintenance.setManageId(piStandardManage.getId());
            piStandardMaintenance.setManageName(piStandardManage.getStandardName());
        }
        iPiStandardMaintenanceService.saveBatch(maintenanceList);
        return isOk;
    }
}




