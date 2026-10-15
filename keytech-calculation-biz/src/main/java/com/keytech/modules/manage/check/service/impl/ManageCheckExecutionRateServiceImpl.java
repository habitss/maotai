package com.keytech.modules.manage.check.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keytech.modules.manage.check.mapper.ManageCheckExecutionRateMapper;
import com.keytech.modules.manage.check.service.IManageCheckExecutionRateService;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 执行率统计服务实现
 * @Author: jeecg-boot
 * @Date:   2025-03-25
 * @Version: V1.0
 */
@Service
@Slf4j
public class ManageCheckExecutionRateServiceImpl extends ServiceImpl<ManageCheckExecutionRateMapper, ManageCheckList> implements IManageCheckExecutionRateService {
    @Autowired
    private ManageCheckExecutionRateMapper manageCheckExecutionRateMapper;

    @Override
    public List<Map> getStatistics(String checkDateBegin, String checkDateEnd, String dept, String postId, String checkerName) {
        // 从数据库查询数据，使用 left join 实现中文名称转换
        List<Map<String, Object>> results = manageCheckExecutionRateMapper.getExecutionRate(checkDateBegin, checkDateEnd, dept, postId);
        
        // 处理查询结果
        List<Map> list = new ArrayList<>();
        int totalPlanned = 0;
        int totalActual = 0;
        
        for (Map<String, Object> result : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("deptCode", result.get("dept_code"));
            data.put("postId", result.get("post_id"));
            data.put("inspectedBy", result.get("inspected_by"));
            data.put("deptName", result.get("deptName"));
            data.put("postName", result.get("postName"));
            data.put("realName", result.get("realName"));
            data.put("plannedCount", result.get("plannedCount"));
            data.put("actualCount", result.get("actualCount"));
            
            // 计算执行率
            int plannedCount = ((Number) result.get("plannedCount")).intValue();
            int actualCount = ((Number) result.get("actualCount")).intValue();
            double rate = plannedCount > 0 ? (double) actualCount / plannedCount * 100 : 0;
            data.put("executionRate", String.format("%.2f%%", rate));
            
            list.add(data);
            
            totalPlanned += plannedCount;
            totalActual += actualCount;
        }
        
        // 添加汇总行
        Map<String, Object> sumMap = new HashMap<>();
        sumMap.put("deptName", "汇总");
        sumMap.put("postName", "");
        sumMap.put("realName", "");
        sumMap.put("plannedCount", totalPlanned);
        sumMap.put("actualCount", totalActual);
        double totalRate = totalPlanned > 0 ? (double) totalActual / totalPlanned * 100 : 0;
        sumMap.put("executionRate", String.format("%.2f%%", totalRate));
        list.add(sumMap);
        
        return list;
    }

}
