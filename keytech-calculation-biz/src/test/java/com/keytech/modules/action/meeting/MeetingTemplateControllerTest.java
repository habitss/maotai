package com.keytech.modules.action.meeting;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.meeting.controller.MeetingTemplateController;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import com.keytech.modules.meeting.service.IMeetingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName MeetingTemplateControllerTest
 * @Description --story=1050921 --user=齐鹏波 05-有效会议-03-会议模板管理 https://www.tapd.cn/63554897/s/1353201
 * @createTime 2025年03月18日 21:42:00
 */
public class MeetingTemplateControllerTest {

    @Mock
    private IMeetingTemplateService meetingTemplateService;

    @InjectMocks
    private MeetingTemplateController meetingTemplateController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Operation(summary="测试分页列表查询-正常情况")
    public void testQueryPageList_HappyPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("dataId", "testType");
        request.addParameter("orderField", "testField");
        request.addParameter("orderType", "asc");
        request.addParameter("selectFields", "field1,field2");

        Page<MeetingTemplate> page = new Page<>(1, 20);
        IPage<MeetingTemplate> pageList = new Page<>();
        pageList.setRecords(Collections.singletonList(new MeetingTemplate()));


        Result<IPage<MeetingTemplate>> result = meetingTemplateController.queryPageList(new MeetingTemplate(), 1, 20, "testField", "asc", "field1,field2", request);

        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(1, result.getResult().getRecords().size());
    }

    @Test
    @Operation(summary="测试分页列表查询-无查询参数")
    public void testQueryPageList_NoParams() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Page<MeetingTemplate> page = new Page<>(1, 20);
        IPage<MeetingTemplate> pageList = new Page<>();
        pageList.setRecords(Collections.singletonList(new MeetingTemplate()));


        Result<IPage<MeetingTemplate>> result = meetingTemplateController.queryPageList(new MeetingTemplate(), 1, 20, null, null, null, request);

        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(1, result.getResult().getRecords().size());
    }

    @Test
    @Operation(summary="测试添加-正常情况")
    public void testAdd_HappyPath() {
        MeetingTemplate meetingTemplate = new MeetingTemplate();
        meetingTemplate.setId("1");

        Result<String> result = meetingTemplateController.add(meetingTemplate);

        verify(meetingTemplateService).save(eq(meetingTemplate));
        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @Operation(summary="测试添加-空对象")
    public void testAdd_EmptyObject() {
        MeetingTemplate meetingTemplate = new MeetingTemplate();

        Result<String> result = meetingTemplateController.add(meetingTemplate);

        verify(meetingTemplateService).save(eq(meetingTemplate));
        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @Operation(summary="测试编辑-正常情况")
    public void testEdit_HappyPath() {
        MeetingTemplate meetingTemplate = new MeetingTemplate();
        meetingTemplate.setId("1");

        Result<String> result = meetingTemplateController.edit(meetingTemplate);

        verify(meetingTemplateService).updateById(eq(meetingTemplate));
        assertEquals(Result.class, result.getClass());
        assertEquals("编辑成功!", result.getMessage());
    }

    @Test
    @Operation(summary="测试编辑-空对象")
    public void testEdit_EmptyObject() {
        MeetingTemplate meetingTemplate = new MeetingTemplate();

        Result<String> result = meetingTemplateController.edit(meetingTemplate);

        verify(meetingTemplateService).updateById(eq(meetingTemplate));
        assertEquals(Result.class, result.getClass());
        assertEquals("编辑成功!", result.getMessage());
    }

    @Test
    @Operation(summary="测试通过id删除-正常情况")
    public void testDeleteById_HappyPath() {
        String id = "1";

        Result<String> result = meetingTemplateController.delete(id);

        verify(meetingTemplateService).removeById(eq(id));
        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @Operation(summary="测试通过id删除-空id")
    public void testDeleteById_EmptyId() {
        String id = "";

        Result<String> result = meetingTemplateController.delete(id);

        verify(meetingTemplateService).removeById(eq(id));
        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @Operation(summary="测试批量删除-正常情况")
    public void testDeletionBatch_HappyPath() {
        String ids = "1,2,3";

        Result<String> result = meetingTemplateController.deletionBatch(ids);

        verify(meetingTemplateService).removeByIds(eq(Arrays.asList("1", "2", "3")));
        assertEquals(Result.class, result.getClass());
        assertEquals("批量删除成功!", result.getMessage());
    }

    @Test
    @Operation(summary="测试批量删除-空ids")
    public void testDeletionBatch_EmptyIds() {
        String ids = "";

        Result<String> result = meetingTemplateController.deletionBatch(ids);

        verify(meetingTemplateService).removeByIds(eq(Collections.emptyList()));
        assertEquals(Result.class, result.getClass());
        assertEquals("批量删除成功!", result.getMessage());
    }

    @Test
    @Operation(summary="测试通过id查询-正常情况")
    public void testQueryById_HappyPath() {
        int id = 1;
        MeetingTemplate meetingTemplate = new MeetingTemplate();
        meetingTemplate.setId(String.valueOf(id));

        when(meetingTemplateService.getById(eq(id))).thenReturn(meetingTemplate);

        Result<MeetingTemplate> result = meetingTemplateController.queryById(String.valueOf(id));

        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(meetingTemplate, result.getResult());
    }

    @Test
    @Operation(summary="测试通过id查询-无数据")
    public void testQueryById_NoData() {
        String id = "1";

        when(meetingTemplateService.getById(eq(id))).thenReturn(null);

        Result<MeetingTemplate> result = meetingTemplateController.queryById(id);

        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode(), result.getCode());
    }

    @Test
    @Operation(summary="测试通过id查询-空id")
    public void testQueryById_EmptyId() {
        String id = "";

        when(meetingTemplateService.getById(eq(id))).thenReturn(null);

        Result<MeetingTemplate> result = meetingTemplateController.queryById(id);

        assertEquals(Result.class, result.getClass());
        assertEquals(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode(), result.getCode());
    }

    @Test
    @Operation(summary="测试导出excel-正常情况")
    public void testExportXls_HappyPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ModelAndView result = meetingTemplateController.exportXls(request, new MeetingTemplate());

        assertEquals(ModelAndView.class, result.getClass());
    }

    @Test
    @Operation(summary="测试导入excel-正常情况")
    public void testImportExcel_HappyPath() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Result<?> result = meetingTemplateController.importExcel(request, response);

        verify(meetingTemplateService).importExcelBy(eq(request), eq(response), eq(MeetingTemplate.class));
        assertEquals(Result.class, result.getClass());
    }
}


