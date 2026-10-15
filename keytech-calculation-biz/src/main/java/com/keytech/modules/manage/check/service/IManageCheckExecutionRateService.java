package com.keytech.modules.manage.check.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keytech.modules.manage.check.entity.ManageCheckList;

import java.util.List;
import java.util.Map;

/**
 * @Description: 执行率统计服务接口
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
public interface IManageCheckExecutionRateService extends IService<ManageCheckList> {

    List<Map> getStatistics(String checkDateBegin, String checkDateEnd, String dept, String postId, String checkerName);

}
