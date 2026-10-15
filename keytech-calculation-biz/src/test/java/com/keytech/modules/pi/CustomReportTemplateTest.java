package com.keytech.modules.pi;

import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableQueryDto;
import com.keytech.modules.custom.report.service.ICustomReportService;
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
public class CustomReportTemplateTest {
    @Resource
    private ICustomReportService customReportService;

    //获取指定报表数据
    //--story=1049963 --user=孙金伟 03-01-自定义报表（新汉仁）-自定义数据表 https://www.tapd.cn/63554897/s/1330776 单元测试
    @Test
    public void getTableDataTest(){
        TableQueryDto tableQueryDto = new TableQueryDto();
        //表名
        tableQueryDto.setTable("mes3_custom_001");
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
        List<Map<String,Object>> list = customReportService.getTableData(tableQueryDto);
        for (Map<String, Object> map : list) {
            System.out.println(map.get("id")+"---"+map.get("field_id"));
        }
    }
}
