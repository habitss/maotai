package com.keytech.modules.pi;

import com.keytech.modules.custom.report.dto.CustomReportFillInDto;
import com.keytech.modules.custom.report.service.ICustomReportFillInService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 自定义报表填报 - 单元测试
 */

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class CustomReportFillInTest {
    @Resource
    private ICustomReportFillInService customReportFillInService;

    //新增填报数据测试
    //--story=1049967 --user=孙金伟 03-05-自定义报表（新汉仁）-填报 https://www.tapd.cn/63554897/s/1332330 单元测试
    @Test
    public void addDataTest(){
        CustomReportFillInDto dto = new CustomReportFillInDto();
        dto.setFillInList("");
        dto.setTableName("mes3_custom_001");
        dto.setTemplateId("1864571690143612930");
        customReportFillInService.addData(dto);
    }

    //编辑填报数据测试
    //--story=1049967 --user=孙金伟 03-05-自定义报表（新汉仁）-填报 https://www.tapd.cn/63554897/s/1332330 单元测试
    @Test
    public void updateDataTest(){
        CustomReportFillInDto dto = new CustomReportFillInDto();
        dto.setFillInList("");
        dto.setTableName("mes3_custom_001");
        dto.setTemplateId("1864571690143612930");
        dto.setRecordId("9");
        customReportFillInService.updateData(dto);
    }

    //删除填报数据测试
    //--story=1049967 --user=孙金伟 03-05-自定义报表（新汉仁）-填报 https://www.tapd.cn/63554897/s/1332330 单元测试
    @Test
    public void removeByIdTest(){
        CustomReportFillInDto dto = new CustomReportFillInDto();
        dto.setTableName("mes3_custom_001");
        dto.setTemplateId("1864571690143612930");
        dto.setRecordId("9");
        customReportFillInService.removeById(dto);
    }
}
