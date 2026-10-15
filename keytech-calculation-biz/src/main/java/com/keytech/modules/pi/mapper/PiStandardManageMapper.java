package com.keytech.modules.pi.mapper;

import com.keytech.modules.pi.entity.PiStandardManage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @Description: 针对表【mes3_pi_standard_manage(PI标准管理)】的数据库操作Mapper
* @Author: sjw
* @CreateDate: 2024/12/11 14:37:06
* @Entity: generator.domain.PiStandardManage
*/
@Mapper
public interface PiStandardManageMapper extends BaseMapper<PiStandardManage> {
    /**
     * 获取最大排序
     * @param customizeId
     * @return
     */
    Double getMaxSort(String customizeId);
}




