package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.mapper.MdmDictMapper;
import com.keytech.modules.mdm3.service.impl.MdmDictServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class MdmDictServiceImplTest {

    @Mock
    private MdmDictMapper baseMapper; // 被测类依赖的基类Mapper

    @InjectMocks
    private MdmDictServiceImpl mdmDictServiceImpl; // 被测类

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //--story=1050089 --user=孙金伟 01-02-主数据管理-代码表管理 https://www.tapd.cn/63554897/s/1334572 单元测试
    @Test
    public void testGetDictAllOrByGroupCode() {
        // 准备测试数据
        List<String> groupCodes = Arrays.asList("group1", "group2");
        MdmDict dict1 = new MdmDict();
        dict1.setId(1);
        dict1.setItemKey("1");
        dict1.setItemVal("Dict1");
        MdmDict dict2 = new MdmDict();
        dict2.setId(2);
        dict2.setItemKey("2");
        dict2.setItemVal("Dict2");
        List<MdmDict> expectedDicts = Arrays.asList(dict1, dict2);

        // 模拟BaseMapper的行为
        when(baseMapper.getDictAllOrByGroupCode(groupCodes)).thenReturn(expectedDicts);

        // 调用被测方法
        List<MdmDict> actualDicts = mdmDictServiceImpl.getDictAllOrByGroupCode(groupCodes);
        System.out.println(actualDicts.toString());
        System.out.println(actualDicts.size());
        // 断言结果
        assertEquals(expectedDicts, actualDicts);
    }
}