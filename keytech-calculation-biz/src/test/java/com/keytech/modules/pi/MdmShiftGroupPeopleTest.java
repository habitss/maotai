package com.keytech.modules.pi;

import com.keytech.modules.mdm3.controller.MdmShiftGroupPeopleController;
import com.keytech.modules.mdm3.entity.MdmShiftGroupPeople;
import com.keytech.modules.mdm3.service.IMdmShiftGroupPeopleService;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MdmShiftGroupPeopleTest {
    //--story=1050131 --user=孙金伟 01-09-主数据管理-班次管理 https://www.tapd.cn/63554897/s/1337509 单元测试
    private MockMvc mockMvc;

    @Mock
    private IMdmShiftGroupPeopleService mdmShiftGroupPeopleService;

    @InjectMocks
    private MdmShiftGroupPeopleController mdmShiftGroupPeopleController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mdmShiftGroupPeopleController).build();
    }

    @Test
    public void testAdd() throws Exception {
        // Given
        MdmShiftGroupPeople mdmShiftGroupPeople = new MdmShiftGroupPeople();
        mdmShiftGroupPeople.setShiftGroupId("123");
        mdmShiftGroupPeople.setUserId("user1,user2,user3");

        // When
        when(mdmShiftGroupPeopleService.remove(any())).thenReturn(true);
        when(mdmShiftGroupPeopleService.saveBatch(any())).thenReturn(true);

        // Then
        mockMvc.perform(post("/mdm/mdmShiftGroupPeople/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shiftGroupId\":\"123\",\"userId\":\"user1,user2,user3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(mdmShiftGroupPeopleService, times(1)).remove(any());
        verify(mdmShiftGroupPeopleService, times(1)).saveBatch(any());
    }
}