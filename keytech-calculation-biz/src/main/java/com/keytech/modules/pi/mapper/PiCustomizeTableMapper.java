package com.keytech.modules.pi.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.keytech.modules.pi.dto.PointChartDto;
import com.keytech.modules.pi.dto.PointDiagramDto;
import com.keytech.modules.pi.dto.SinicizedDictDto;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUser;

/**
 * @Description: pi自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface PiCustomizeTableMapper extends BaseMapper<PiCustomizeTable> {

    void createNewTable(PiCustomizeTable piCustomizeTable);

    List<Map<String, Object>> getTableData(String field, String table, String condition, String sort, int rowsNumber);

    /**
     * 动态表插入数据并返回自增ID
     * @param fieldStr 字段列表，逗号分隔
     * @param fieldValStr 值列表，逗号分隔
     * @param table  表名
     */
    @Options(useGeneratedKeys = true, keyProperty = "mainId.id", keyColumn = "id")
    void tableSave(@Param("fieldStr") String fieldStr,
                   @Param("fieldValStr") String fieldValStr,
                   @Param("table") String table,
                   @Param("mainId") Map<String, Long> mainId);

    void tableUpdate(String fieldValStr, String table, String condition);

    /**
     * 获取基础数据表数量
     * @param dbName
     * @return
     */
    int getBasedataTableNum(@Param("dbName") String dbName);

    List<Map> getPointChartData(PointChartDto dto);

    List<PointDiagramDto> queryPointDiagram(PointDiagramDto dto);

    List<PointDiagramDto> queryBoxDiagram(PointDiagramDto dto);

    @Select("select login_tenant_id from sys_user a where username = 'admin' and del_flag = 0")
    SysUser getAdminUser();

    @InterceptorIgnore(tenantLine = "true")
    List<Map<String, Object>> getDataByJson(String field, String table, String condition, String sort, int rowsNumber);

    /**
     * 创建子表
     * @param piCustomizeTable
     */
    void createExtendTable(PiCustomizeTable piCustomizeTable);

    void createNewTablePg(PiCustomizeTable piCustomizeTable);

    void updateTableCommentsPg(PiCustomizeTable piCustomizeTable);
}
