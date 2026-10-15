package com.keytech.modules.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.entity.PiAllocationTableField;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description: 报表字段分配
 * @Author: jeecg-boot
 * @Date:   2024-11-27
 * @Version: V1.0
 */
public interface PiAllocationTableFieldMapper extends BaseMapper<PiAllocationTableField> {

    int getTableDataCount(Map<String, Object> paramMap);

    List<Map<String, Object>> getTableData(Map<String, Object> paramMap);

    List<Map<String, Object>> getTableDataPg(Map<String, Object> paramMap);

    //获取最新10条数据
    List<Map<String, Object>> getTableFieldData(Map<String, Object> paramMap);

    /**
     * 创建/修改视图
     * @param viewName  视图名
     * @param viewFields 字段
     * @param viewTables  表名集合
     * @param viewConditions 条件
     */
    void view(@Param("viewName") String viewName,
              @Param("viewFields") String viewFields,
              @Param("viewTables") String viewTables,
              @Param("viewConditions") String viewConditions);
}
