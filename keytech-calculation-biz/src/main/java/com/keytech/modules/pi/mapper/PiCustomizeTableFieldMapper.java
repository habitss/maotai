package com.keytech.modules.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.pi.dto.SinicizedDictDto;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiStandardMaintenance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.SQLException;
import java.util.List;

/**
 * @Description: pi自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface PiCustomizeTableFieldMapper extends BaseMapper<PiCustomizeTableField> {

    void addTableField(PiCustomizeTableField piCustomizeTableField) throws SQLException;

    List<PiCustomizeTableField> getFieldByTableId(PiCustomizeTableField piCustomizeTableField);



    @Select("select  dict_group_name as label , dict_group_code as value  from mes3_mdm_dict_group where del_flag = 0")
    List<SinicizedDictDto> getDictionary();


    @Select("<script> select a.id,a.manage_id,a.manage_name,a.table_name,table_text,field_name,field_text,indicator_type,red_up,red_down,yellow_up,yellow_down,standard_value, " +
            " b.standard_name,b.enable_time,b.stop_time,b.standard_version,b.calc_time_field,b.condition_text,b.calc_time_field_name " +
            " from mes3_pi_standard_maintenance a left join  (" +
            "select m.*,c.name calc_time_field_name from mes3_pi_standard_manage m left join mes3_pi_customize_table_field c on m.customize_id = c.table_id and m.calc_time_field = c.field_name " +
            ") b on a.manage_id = b.id " +
            " where a.table_name = #{tableName} and a.field_name = #{fieldName} " +
            " and a.del_flag = 0 and b.del_flag = 0 " +
            " <if test='id!= null'> "+
            " and a.id = #{id} "+
            "</if>"+
            " order by b.sort,a.sort </script>")
    List<PiStandardMaintenance> queryStandard(PiStandardMaintenance piStandardMaintenance);


    /**
     * 创建/修改视图
     * @param viewName  视图名
     * @param viewTables  表名集合
     * @param viewConditions 条件
     */
    void view(@Param("viewName") String viewName,
              @Param("viewTables") String viewTables,
              @Param("viewConditions") String viewConditions);


    /**
     * 同步数据
     * @param subtable 子表
     * @param field  子表id
     * @param tableName 主表
     */
    void synchronizeData(@Param("subtable") String subtable,
                         @Param("field") String field,
                         @Param("tableName") String tableName);
    /**
     * 删除索引
     * @param idxName 索引名
     * @param tableName 表名
     */
    void deleteIndex(@Param("idxName") String idxName, @Param("tableName") String tableName);
    /**
     * 创建索引
     * @param idxName 索引名
     * @param tableName 表名
     * @param fields 字段
     */
    void createIndex(@Param("idxName") String idxName, @Param("tableName") String tableName, @Param("fields") String fields);

    /**
     * 查询索引
     * @param idxName 索引名
     * @param tableName 表名
     * @return 结果
     */
    int selectIndex(@Param("idxName") String idxName, @Param("tableName") String tableName);
}
