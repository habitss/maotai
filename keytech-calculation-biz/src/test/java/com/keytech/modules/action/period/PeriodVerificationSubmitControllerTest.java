package com.keytech.modules.action.period;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.period.controller.PeriodVerificationSubmitController;
import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.period.service.IPeriodVerificationSubmitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @Description: --story=1050845 --user=齐鹏波 13-期间核查-11-期间核查填报-后端 https://www.tapd.cn/63554897/s/1352253 测试类
 * --story=1055799 --user=孙金伟 24-APP-期间核查开发 https://www.tapd.cn/63554897/s/1389046 单元测试
 */
@SpringBootTest
public class PeriodVerificationSubmitControllerTest {

    @Mock
    private IPeriodVerificationSubmitService periodVerificationSubmitService;

    @Mock
    private IPeriodVerificationScheduleService periodVerificationScheduleService;

    @InjectMocks
    private PeriodVerificationSubmitController periodVerificationSubmitController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(periodVerificationSubmitController).build();
    }

    @Test
    public void testQueryPageList_success() throws Exception {
        // 准备数据
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("orderField", "id");
        req.setParameter("orderType", "asc");
        IPage<PeriodVerificationSubmit> pageList = mock(IPage.class);

        // 执行测试
        mockMvc.perform(get("/period/periodVerificationSubmit/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // 验证方法调用
    }

    @Test
    public void testAdd_success() throws Exception {
        // 准备数据
        PeriodScheduleSubmitVO scheduleSubmit = new PeriodScheduleSubmitVO();
        scheduleSubmit.setCheckDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        scheduleSubmit.setPeriodScheduleId("1");
        scheduleSubmit.setPlanWeek(3);

        // 执行测试
        mockMvc.perform(post("/period/periodVerificationSubmit/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checkDate\":\"" + scheduleSubmit.getCheckDate() + "\",\"periodScheduleId\":\"" + scheduleSubmit.getPeriodScheduleId() + "\",\"planWeek\":\"" + scheduleSubmit.getPlanWeek() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).saveOrUpdate(any(PeriodVerificationSubmit.class));
    }

    @Test
    public void testAdd_invalidDate() throws Exception {
        // 准备数据
        PeriodScheduleSubmitVO scheduleSubmit = new PeriodScheduleSubmitVO();
        scheduleSubmit.setCheckDate("invalid-date");
        scheduleSubmit.setPeriodScheduleId("1");
        scheduleSubmit.setPlanWeek(3);

        // 执行测试
        mockMvc.perform(post("/period/periodVerificationSubmit/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checkDate\":\"" + scheduleSubmit.getCheckDate() + "\",\"periodScheduleId\":\"" + scheduleSubmit.getPeriodScheduleId() + "\",\"planWeek\":\"" + scheduleSubmit.getPlanWeek() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).saveOrUpdate(any(PeriodVerificationSubmit.class));
    }

    @Test
    public void testQueryByParam_success() throws Exception {
        // 准备数据
        PeriodScheduleSubmitVO scheduleSubmit = new PeriodScheduleSubmitVO();
        scheduleSubmit.setPeriodScheduleId("1");
        scheduleSubmit.setPlanWeek(3);
        PeriodVerificationSubmit periodVerificationSubmit = new PeriodVerificationSubmit();
        when(periodVerificationSubmitService.findSubmitByParam(any(PeriodScheduleSubmitVO.class))).thenReturn(periodVerificationSubmit);

        // 执行测试
        mockMvc.perform(post("/period/periodVerificationSubmit/queryByParam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodScheduleId\":\"" + scheduleSubmit.getPeriodScheduleId() + "\",\"planWeek\":\"" + scheduleSubmit.getPlanWeek() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).findSubmitByParam(any(PeriodScheduleSubmitVO.class));
    }

    @Test
    public void testQueryByParam_noData() throws Exception {
        // 准备数据
        PeriodScheduleSubmitVO scheduleSubmit = new PeriodScheduleSubmitVO();
        scheduleSubmit.setPeriodScheduleId("1");
        scheduleSubmit.setPlanWeek(3);
        when(periodVerificationSubmitService.findSubmitByParam(any(PeriodScheduleSubmitVO.class))).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/period/periodVerificationSubmit/queryByParam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodScheduleId\":\"" + scheduleSubmit.getPeriodScheduleId() + "\",\"planWeek\":\"" + scheduleSubmit.getPlanWeek() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode()));

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).findSubmitByParam(any(PeriodScheduleSubmitVO.class));
    }



    @Test
    public void testDelete_success() throws Exception {
        // 执行测试
        mockMvc.perform(delete("/period/periodVerificationSubmit/delete")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).removeById(anyString());
    }

    @Test
    public void testDeletionBatch_success() throws Exception {
        // 执行测试
        mockMvc.perform(delete("/period/periodVerificationSubmit/deletionBatch")
                        .param("ids", "1,2,3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("批量删除成功!"));

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).removeByIds(anyList());
    }

    @Test
    public void testQueryById_success() throws Exception {
        // 准备数据
        PeriodVerificationSubmit periodVerificationSubmit = new PeriodVerificationSubmit();
        periodVerificationSubmit.setId("1");
        when(periodVerificationSubmitService.getById(anyString())).thenReturn(periodVerificationSubmit);

        // 执行测试
        mockMvc.perform(get("/period/periodVerificationSubmit/queryById")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).getById(anyString());
    }

    @Test
    public void testQueryById_noData() throws Exception {
        // 准备数据
        when(periodVerificationSubmitService.getById(anyString())).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/period/periodVerificationSubmit/queryById")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode()));

        // 验证方法调用
        verify(periodVerificationSubmitService, times(1)).getById(anyString());
    }

    @Test
    public void testExportXls_success() throws Exception {
        // 准备数据
        PeriodVerificationSubmit periodVerificationSubmit = new PeriodVerificationSubmit();
        periodVerificationSubmit.setId("1");

        // 执行测试
        mockMvc.perform(get("/period/periodVerificationSubmit/exportXls")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 验证方法调用
        verify(superExportXls(any(HttpServletRequest.class), any(PeriodVerificationSubmit.class), eq(PeriodVerificationSubmit.class), eq("期间排查排查内容提交")), times(1));
    }

    @Test
    public void testImportExcel_success() throws Exception {
        // 执行测试
        mockMvc.perform(post("/period/periodVerificationSubmit/importExcel")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 验证方法调用
        verify(superImportExcel(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(PeriodVerificationSubmit.class)), times(1));
    }

    // 模拟父类方法
    private Result<?> superExportXls(HttpServletRequest request, PeriodVerificationSubmit periodVerificationSubmit, Class<?> clazz, String title) {
        return Result.OK();
    }

    private Result<?> superImportExcel(HttpServletRequest request, HttpServletResponse response, Class<?> clazz) {
        return Result.OK();
    }
}

