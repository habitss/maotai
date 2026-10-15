package com.keytech.modules.action.period;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.period.controller.PeriodVerificationConfigController;
import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.modules.period.service.IPeriodVerificationConfigService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @description: --story=1050841 --user=齐鹏波 13-期间核查-09-期间核查填报项配置-后端 https://www.tapd.cn/63554897/s/1352251 - 单元测试
 */
@Slf4j
public class PeriodVerificationConfigControllerTest {

    @Mock
    private IPeriodVerificationConfigService periodVerificationConfigService;

    @InjectMocks
    private PeriodVerificationConfigController periodVerificationConfigController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试分页列表查询的正常情况
     */
    @Test
    public void testQueryPageListHappyPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("dataId", "1");
        PeriodVerificationConfig periodVerificationConfig = new PeriodVerificationConfig();
        periodVerificationConfig.setApparatusId("1");

        QueryWrapper<PeriodVerificationConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("apparatus_id", "1");

        Page<PeriodVerificationConfig> page = new Page<>(1, 10);
        IPage<PeriodVerificationConfig> pageList = periodVerificationConfigService.page(page, queryWrapper);

        Result<IPage<PeriodVerificationConfig>> result = periodVerificationConfigController.queryPageList(periodVerificationConfig, 1, 10, null, null, null, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(pageList, result.getResult());
    }

    /**
     * 测试分页列表查询时dataId参数为空的情况
     */
    @Test
    public void testQueryPageListDataIdEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        PeriodVerificationConfig periodVerificationConfig = new PeriodVerificationConfig();

        QueryWrapper<PeriodVerificationConfig> queryWrapper = new QueryWrapper<>();

        Page<PeriodVerificationConfig> page = new Page<>(1, 10);
        IPage<PeriodVerificationConfig> pageList = periodVerificationConfigService.page(page, queryWrapper);

        Result<IPage<PeriodVerificationConfig>> result = periodVerificationConfigController.queryPageList(periodVerificationConfig, 1, 10, null, null, null, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(pageList, result.getResult());
    }

    /**
     * 测试添加正常情况
     */
    @Test
    public void testAddHappyPath() {
        PeriodVerificationConfig periodVerificationConfig = new PeriodVerificationConfig();

        Result<String> result = periodVerificationConfigController.add(periodVerificationConfig);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getResult());
        verify(periodVerificationConfigService, times(1)).save(periodVerificationConfig);
    }

    /**
     * 测试编辑正常情况
     */
    @Test
    public void testEditHappyPath() {
        PeriodVerificationConfig periodVerificationConfig = new PeriodVerificationConfig();

        Result<String> result = periodVerificationConfigController.edit(periodVerificationConfig);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("编辑成功!", result.getResult());
        verify(periodVerificationConfigService, times(1)).updateById(periodVerificationConfig);
    }

    /**
     * 测试通过id删除正常情况
     */
    @Test
    public void testDeleteByIdHappyPath() {
        String id = "1";

        Result<String> result = periodVerificationConfigController.delete(id);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getResult());
        verify(periodVerificationConfigService, times(1)).removeById(id);
    }

    /**
     * 测试通过id删除时id为空的情况
     */
    @Test
    public void testDeleteByIdIdEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            periodVerificationConfigController.delete("");
        });
    }

    /**
     * 测试批量删除正常情况
     */
    @Test
    public void testDeletionBatchHappyPath() {
        String ids = "1,2,3";

        Result<String> result = periodVerificationConfigController.deletionBatch(ids);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("批量删除成功!", result.getResult());
        verify(periodVerificationConfigService, times(1)).removeByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * 测试批量删除时ids为空的情况
     */
    @Test
    public void testDeletionBatchIdsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            periodVerificationConfigController.deletionBatch("");
        });
    }

    /**
     * 测试通过id查询正常情况
     */
    @Test
    public void testQueryByIdHappyPath() {
        String id = "1";
        PeriodVerificationConfig periodVerificationConfig = new PeriodVerificationConfig();
        periodVerificationConfig.setId(id);

        when(periodVerificationConfigService.getById(id)).thenReturn(periodVerificationConfig);

        Result<PeriodVerificationConfig> result = periodVerificationConfigController.queryById(id);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(periodVerificationConfig, result.getResult());
    }

    /**
     * 测试通过id查询时数据不存在的情况
     */
    @Test
    public void testQueryByIdDataNotFound() {
        String id = "1";

        when(periodVerificationConfigService.getById(id)).thenReturn(null);

        Result<PeriodVerificationConfig> result = periodVerificationConfigController.queryById(id);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode(), result.getCode());
    }

    /**
     * 测试通过id查询时id为空的情况
     */
    @Test
    public void testQueryByIdIdEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            periodVerificationConfigController.queryById("");
        });
    }

    /**
     * 测试导出excel正常情况
     */
    @Test
    public void testExportXlsHappyPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        PeriodVerificationConfig periodVerificationConfig = new PeriodVerificationConfig();

        ModelAndView result = periodVerificationConfigController.exportXls(request, periodVerificationConfig);

        assertNotNull(result);
        assertEquals("期间核查填报项配置", result.getModel().get("title"));
    }

    /**
     * 测试通过excel导入数据正常情况
     */
    @Test
    public void testImportExcelHappyPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Result<?> result = periodVerificationConfigController.importExcel(request, response);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}
