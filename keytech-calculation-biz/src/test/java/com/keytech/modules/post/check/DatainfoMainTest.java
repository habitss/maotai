package com.keytech.modules.post.check;

import cn.hutool.core.util.IdUtil;
import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoDetail;
import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoMain;
import com.keytech.modules.post.check.service.IPostCheckRecordDatainfoDetailService;
import com.keytech.modules.post.check.service.impl.PostCheckRecordDatainfoMainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
//--story=1050145 --user=孙金伟 01-04岗位记录-填报页面 https://www.tapd.cn/63554897/s/1345095 单元测试
@ExtendWith(MockitoExtension.class)
public class DatainfoMainTest {
    @Mock
    private IPostCheckRecordDatainfoDetailService iPostCheckRecordDatainfoDetailService;

    @InjectMocks
    private PostCheckRecordDatainfoMainServiceImpl postCheckRecordDatainfoMainService;

    private PostCheckRecordDatainfoMain postCheckRecordDatainfoMain;
    private List<PostCheckRecordDatainfoDetail> detailList;

    @BeforeEach
    void setUp() {
        postCheckRecordDatainfoMain = new PostCheckRecordDatainfoMain();
        detailList = new ArrayList<>();
        PostCheckRecordDatainfoDetail detail = new PostCheckRecordDatainfoDetail();
        detail.setId("temp_123");
        detail.setDatainfoId("existing_id");
        detailList.add(detail);
        postCheckRecordDatainfoMain.setDetailList(detailList);
    }

    @Test
    void testSaveDataInfo_NewRecord() {
        // Arrange
        postCheckRecordDatainfoMain.setId(null);

        // Act
        postCheckRecordDatainfoMainService.saveDataInfo(postCheckRecordDatainfoMain);

        // Assert
        assertNotNull(postCheckRecordDatainfoMain.getId());
        assertEquals("existing_id", postCheckRecordDatainfoMain.getDetailList().get(0).getDatainfoId());
        verify(iPostCheckRecordDatainfoDetailService, times(1)).saveOrUpdateBatch(any());
    }

    @Test
    void testSaveDataInfo_ExistingRecord() {
        // Arrange
        String existingId = "existing_id";
        postCheckRecordDatainfoMain.setId(existingId);

        // Act
        postCheckRecordDatainfoMainService.saveDataInfo(postCheckRecordDatainfoMain);

        // Assert
        assertEquals(existingId, postCheckRecordDatainfoMain.getId());
        assertEquals(existingId, postCheckRecordDatainfoMain.getDetailList().get(0).getDatainfoId());
        verify(iPostCheckRecordDatainfoDetailService, times(1)).saveOrUpdateBatch(any());
    }
}
