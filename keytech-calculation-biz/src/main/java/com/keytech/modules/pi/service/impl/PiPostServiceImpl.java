package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiPost;
import com.keytech.modules.pi.mapper.PiPostMapper;
import com.keytech.modules.pi.service.IPiAllocationTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiPostService;
import com.keytech.modules.pi.util.CustomizeTableUtil;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * &#064;Description: PI岗位绑定
 * &#064;Author: jeecg-boot
 * &#064;Date:   2025-07-14
 * &#064;Version: V1.0
 */
@Service
public class PiPostServiceImpl extends BaseServiceImpl<PiPostMapper, PiPost> implements IPiPostService {
    @Autowired
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;
    @Resource
    private IPiAllocationTableFieldService piAllocationTableFieldService;

    @Override
    public Map<String, Object> queryPageAppList(PiPost piPost, Integer pageNo, Integer pageSize, String tableName, HttpServletRequest req) {
        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> list = new ArrayList<>();
        int total;

        // 防止SQL注入
        SqlInjectionUtil.filterContent(tableName);
        //获取字段配置
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PiCustomizeTableField::getTableName, tableName);
        List<PiCustomizeTableField> piFieldList = piCustomizeTableFieldService.list(queryWrapper);
        //获取查询参数
        Map<String, Object> paramMap = CustomizeTableUtil.getParamMap(req, piFieldList, tableName, piPost.getRelatedField());

        //查询总数
        total = piAllocationTableFieldService.getTableDataCount(paramMap);
        if(total > 0){
            //分页查询
            paramMap.put("pageNo",(pageNo-1) * pageSize);
            paramMap.put("pageSize", pageSize);
            list = piAllocationTableFieldService.getTableData(paramMap);
        }
        result.put("records",list);
        result.put("total", total);
        return result;
    }
}
