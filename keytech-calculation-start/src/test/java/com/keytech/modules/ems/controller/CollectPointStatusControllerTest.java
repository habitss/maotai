package com.keytech.modules.ems.controller;

import com.keytech.modules.ems.service.ICollectPointStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 点位采集状态 Controller 测试
 */
@ExtendWith(MockitoExtension.class)
class CollectPointStatusControllerTest {

    @Mock
    private ICollectPointStatusService collectPointStatusService;

    @InjectMocks
    private CollectPointStatusController collectPointStatusController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(collectPointStatusController).build();
    }

    @Test
    void testSetManualBackfill() throws Exception {
        String userJson = "{\"startTime\":\"2025-05-07\", \"endTime\":2025-05-08}";
        mockMvc.perform(MockMvcRequestBuilders.post("/ems/collectPointStatus/setManualBackfill")
                .content(userJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }
}