package com.keytech.modules.pi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.keytech.modules.pi.controller.PiStandardMaintenanceController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.keytech.modules.pi.service.IPiStandardMaintenanceService;
import com.keytech.modules.pi.entity.PiStandardMaintenance;

@WebMvcTest(PiStandardMaintenanceController.class)
@Slf4j
public class PiStandardTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;

    private PiStandardMaintenance testObject;

    @BeforeEach
    void setUp() {
        testObject = new PiStandardMaintenance();
        testObject.setId("testid-1");
        testObject.setTableName("testTableName");
    }

    //--story=1049988 --user=孙金伟 02-09-PI日报-PI红黄区配置 https://www.tapd.cn/63554897/s/1333376
    @Test
    public void getDataTest() throws Exception {
        List<PiStandardMaintenance> list = new ArrayList<>();
        list.add(testObject);
        when(iPiStandardMaintenanceService.list()).thenReturn(list);

        mockMvc.perform(post("/api/pi/standard/queryPageList") // 确认URL路径
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableName\":\"testTableName\"}")) // 确认请求体内容与业务逻辑一致
                .andExpect(status().isOk()) // 如果是查询操作，通常返回状态码应为 200 OK 而不是 201 Created
                .andExpect(jsonPath("$.length()").value(1)) // 假设返回的是一个数组
                .andExpect(jsonPath("$[0].id").value("testid-1"))
                .andExpect(jsonPath("$[0].tableName").value("testTableName"));
    }
}