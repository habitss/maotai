package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencySkuDay;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuDayService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * @FileName LineEfficiencySkuDayControllerTest
 * @Description
 * @Author HQ
 * @date 2025-08-21
 **/
public class LineEfficiencySkuDayControllerTest {
    // 被测试的控制器实例
    @InjectMocks
    private LineEfficiencySkuDayController controller;

    // 模拟的服务层接口
    @Mock
    private ILineEfficiencySkuDayService lineEfficiencySkuDayService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常输入情况下的行为
     * --story=1057713 --user=贺强 26.01-OPI产线-SKU天记录拆分 https://www.tapd.cn/63554897/s/1389030
     */
    @Test
    void dataSplitService() {
        // 准备输入数据
        List<LineEfficiencySkuDay> dataList = new ArrayList<>();
        LineEfficiencySkuDay item = new LineEfficiencySkuDay();
        item.setId("1");
        dataList.add(item);

        // 执行方法
        Result<String> result = controller.dataSplit(dataList);

        // 验证 service 被调用一次
        verify(lineEfficiencySkuDayService, times(1)).dataSplit(dataList);

        // 验证返回结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMessage());
    }
}
