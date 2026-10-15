package com.keytech.modules.pi;

import com.keytech.modules.custom.report.entity.CustomReportField;
import com.keytech.modules.custom.report.service.ICustomReportFieldService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class CustomReportFieldTest {

    @Resource
    private ICustomReportFieldService customReportFieldService;


    //--story=1049963 --user=孙金伟 03-01-自定义报表（新汉仁）-自定义数据表 https://www.tapd.cn/63554897/s/1330776 单元测试
    @Test
    public void getTableDataTest(){
        CustomReportField customReportField = new CustomReportField();
        customReportField.setTableId("1").setType("1").setFieldLength(50);
        customReportFieldService.saveField(customReportField);
    }

    //--story=1049966 --user=孙金伟 03-04-自定义报表（新汉仁）-统计查询 https://www.tapd.cn/63554897/s/1332329 单元测试
    @Test
    public void queryHeaderTest(){
        customReportFieldService.queryHeaderByTemplateId("1864571690143612930");
    }

    //--story=1049966 --user=孙金伟 03-04-自定义报表（新汉仁）-统计查询 https://www.tapd.cn/63554897/s/1332329 单元测试
    @Test
    public void queryDataTest(){
        // 创建 HttpServletRequest 的 mock 实例
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        customReportFieldService.queryData(1, 10, "", "", "1864571690143612930", request);
    }

}
