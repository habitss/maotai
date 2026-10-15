package com.keytech.modules.pi.service.impl;

import com.keytech.modules.pi.entity.PiSistarEngReccats;
import com.keytech.modules.pi.service.IPiSistarEngReccatsService;
import com.keytech.modules.pi.service.ISourceSistarRtStepsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * PiSistarRtStepsServiceImpl 的单元测试类
 */
@ExtendWith(MockitoExtension.class)
public class PiSistarRtStepsServiceImplTest {

    @InjectMocks
    private PiSistarRtStepsServiceImpl piSistarRtStepsService;

    @Mock
    private ISourceSistarRtStepsService iSourceSistarRtStepsService;

    @Mock
    private IPiSistarEngReccatsService iPiSistarEngReccatsService;


    /**
     * 测试用例：getReccats 返回空列表
     */
    @Test
    public void testDictionaryData_EmptyList() {
        // 模拟 getReccats 返回空列表
        when(iSourceSistarRtStepsService.getReccats()).thenReturn(new ArrayList<>());

        // 执行被测方法
        piSistarRtStepsService.dictionaryData();

        // 验证 deleteData 被调用
        verify(iSourceSistarRtStepsService, times(1)).deleteData("mes3_pi_sistar_eng_reccats");

        // 验证 saveBatch 被调用，传入空列表
        verify(iPiSistarEngReccatsService, times(1)).saveBatch(anyList());
    }

    /**
     * 测试用例：getReccats 返回非空列表
     */
    @Test
    public void testDictionaryData_NonEmptyList() {
        // 构造测试数据
        List<PiSistarEngReccats> mockList = new ArrayList<>();
        PiSistarEngReccats item1 = new PiSistarEngReccats();
        PiSistarEngReccats item2 = new PiSistarEngReccats();
        mockList.add(item1);
        mockList.add(item2);

        // 模拟 getReccats 返回数据
        when(iSourceSistarRtStepsService.getReccats()).thenReturn(mockList);

        // 执行被测方法
        piSistarRtStepsService.dictionaryData();

        // 验证 deleteData 被调用
        verify(iSourceSistarRtStepsService, times(1)).deleteData("mes3_pi_sistar_eng_reccats");

        // 验证 saveBatch 被调用
        ArgumentCaptor<List<PiSistarEngReccats>> captor = ArgumentCaptor.forClass(List.class);
        verify(iPiSistarEngReccatsService, times(1)).saveBatch(captor.capture());

        // 验证 tenantId 是否设置正确
        List<PiSistarEngReccats> capturedList = captor.getValue();
        for (PiSistarEngReccats item : capturedList) {
            assert item.getTenantId().equals(1001);
        }
    }
}
