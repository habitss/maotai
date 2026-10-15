package com.keytech.modules.pi;

import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableQueryDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * pi自定义报表主表 - 单元测试
 */

@Slf4j
@SpringBootTest
public class PiCustomizeTableTest {


    @Resource
    private IPiCustomizeTableService piCustomizeTableService;

    //获取指定报表数据
    //--story=1049767 --user=贺强 01-04-PI算法模型-自定义数据表-建库维护 https://www.tapd.cn/63554897/s/1326841
    @Test
    public void getTableDataTest(){
        TableQueryDto tableQueryDto = new TableQueryDto();
        //表名
        tableQueryDto.setTable("mdm_table_002");
        //结果字段
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id");
        tableQueryDto.setFieldList(fieldList);
        //查询条件
        TableConditionDto tableConditionDto = new TableConditionDto();
        tableConditionDto.setKey("field_1");
        tableConditionDto.setVal("1");
        List<TableConditionDto> conditionList = new ArrayList<>();
        conditionList.add(tableConditionDto);
        tableQueryDto.setConditionList(conditionList);
        //查询
        List<Map<String,Object>> list = piCustomizeTableService.getTableData(tableQueryDto);
        for (Map<String, Object> map : list) {
            System.out.println(map.get("id")+"---"+map.get("field_id"));
        }
    }

    //--story=1049769 --user=贺强 01-06-PI算法模型-算法触发条件配置维护 https://www.tapd.cn/63554897/s/1327905
    @Test
    public void addTableTest(){
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setName("测试表").setTableName("table_id").setSort(1.0);
        piCustomizeTableService.save(piCustomizeTable);
    }

    //--story=1049886 --user=贺强 01-18-PI算法模型-MES3测试环境的搭建 https://www.tapd.cn/63554897/s/1327906
    @Test
    public void deleteTableTest(){
        piCustomizeTableService.removeById("1");
    }

    //--story=1049770 --user=贺强 01-07-PI算法模型-算法取数步骤配置维护 https://www.tapd.cn/63554897/s/1327907
    @Test
    public void editTableTest(){
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setId("1").setName("测试表").setTableName("table_id").setSort(1.0);
        piCustomizeTableService.updateById(piCustomizeTable);
    }

    //--story=1049892 --user=贺强 01-21-PI算法模型-唯一约束接口及数据流调试 https://www.tapd.cn/63554897/s/1327915
    @Test
    public void tableSave(){
        TableSaveDto tableSaveDto = new TableSaveDto();
        tableSaveDto.setTable("mdm_table_002");
        //值
        TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
        tableFieldValueDto.setField("field_2");
        tableFieldValueDto.setFieldVal("12345");
        List<TableFieldValueDto> fieldList = new ArrayList<>();
        fieldList.add(tableFieldValueDto);
        //约束条件
        TableConditionDto tableConditionDto = new TableConditionDto();
        tableConditionDto.setKey("field_1");
        tableConditionDto.setVal("1");
        List<TableConditionDto> tableConditionList = new ArrayList<>();
        tableConditionList.add(tableConditionDto);
        tableSaveDto.setConditionList(tableConditionList);

        String result = piCustomizeTableService.tableSave(tableSaveDto);
    }


}
