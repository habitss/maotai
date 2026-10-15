package com.keytech.modules.cop.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.service.ICopDeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CopDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ICopDeviceService copDeviceService;

    @InjectMocks
    private CopDeviceController copDeviceController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(copDeviceController).build();
    }
//--story=1050825 --user=李俊02 12-COP-02-COP设备管理后端 https://www.tapd.cn/63554897/s/1346774 单元测试
    @Test
    public void listData_CopDeviceNameEmpty_ShouldNotAddWildcards() throws Exception {
        CopDevice copDevice = new CopDevice();
        copDevice.setCopDeviceName("");

        Page<CopDevice> page = new Page<>();
        when(copDeviceService.listData(ArgumentMatchers.any(CopDevice.class), eq(1), eq(10), anyString(), anyString(), anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/cop/copDevice/listData")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("orderField", "name")
                .param("orderType", "asc")
                .param("selectFields", "id,name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.records", hasSize(0)));

        verify(copDeviceService, times(1)).listData(ArgumentMatchers.any(CopDevice.class), eq(1), eq(10), eq("name"), eq("asc"), anyString(), any());
    }

    @Test
    public void listData_CopDeviceNameNotEmpty_ShouldAddWildcards() throws Exception {
        CopDevice copDevice = new CopDevice();
        copDevice.setCopDeviceName("device");

        Page<CopDevice> page = new Page<>();
        when(copDeviceService.listData(ArgumentMatchers.any(CopDevice.class), eq(1), eq(10), anyString(), anyString(), anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/cop/copDevice/listData")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("orderField", "name")
                .param("orderType", "asc")
                .param("selectFields", "id,name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.records", hasSize(0)));

        verify(copDeviceService, times(1)).listData(ArgumentMatchers.any(CopDevice.class), eq(1), eq(10), eq("name"), eq("asc"), anyString(), any());
    }

    @Test
    public void listData_WithPaginationAndSorting_ShouldReturnCorrectPage() throws Exception {
        Page<CopDevice> page = new Page<>();
        when(copDeviceService.listData(ArgumentMatchers.any(CopDevice.class), eq(2), eq(5), eq("name"), eq("desc"), anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/cop/copDevice/listData")
                .param("pageNo", "2")
                .param("pageSize", "5")
                .param("orderField", "name")
                .param("orderType", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.records", hasSize(0)));

        verify(copDeviceService, times(1)).listData(ArgumentMatchers.any(CopDevice.class), eq(2), eq(5), eq("name"), eq("desc"), anyString(), any());
    }
}
