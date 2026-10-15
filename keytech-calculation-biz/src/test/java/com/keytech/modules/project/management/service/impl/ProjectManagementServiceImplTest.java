package com.keytech.modules.project.management.service.impl;

import com.keytech.modules.project.management.dto.ProjectManagementSummaryDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.project.management.mapper.ProjectManagementMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//--story=1055293 --user=孙金伟 19-RA单调整，展示有所有行动并且再RA单可以直接操作 https://www.tapd.cn/63554897/s/1379841 单元测试
class ProjectManagementServiceImplTest {

    @InjectMocks
    private ProjectManagementServiceImpl projectManagementService;

    @Mock
    private ProjectManagementMapper baseMapper;

    @Mock
    private IMdmDictService mdmDictService;

    @Mock
    private ISysUserService sysUserService;

    @Mock
    private ISysDepartService sysDepartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPersonalProjectSummary_withAllFilters() {
        // 准备测试数据
        ProjectManagement projectManagement = new ProjectManagement();
        projectManagement.setProjectLevel("level1,level2");
        projectManagement.setProjectType("type1,type2");
        projectManagement.setProjectSource("source1,source2");
        projectManagement.setUserId("user1");

        // 创建项目列表
        List<ProjectManagement> projectList = new ArrayList<>();
        ProjectManagement project1 = new ProjectManagement();
        project1.setId("project1");
        project1.setProjectLevel("level1");
        project1.setProjectType("type1");
        project1.setProjectSource("source1");
        projectList.add(project1);

        // 创建汇总数据列表
        List<ProjectManagementSummaryDto> summaryList = new ArrayList<>();
        ProjectManagementSummaryDto summaryDto = new ProjectManagementSummaryDto();
        summaryDto.setId("project1");
        summaryDto.setMonth1(100.0);
        summaryDto.setMonth2(200.0);
        summaryList.add(summaryDto);

        // 创建字典数据
        List<MdmDict> mdmDictList = new ArrayList<>();
        MdmDict dict1 = new MdmDict();
        dict1.setGroupCode("MES3_PROJECT_LEVEL");
        dict1.setItemVal("level1");
        dict1.setItemKey("Level 1");
        mdmDictList.add(dict1);

        MdmDict dict2 = new MdmDict();
        dict2.setGroupCode("MES3_PROJECT_TYPE");
        dict2.setItemVal("type1");
        dict2.setItemKey("Type 1");
        mdmDictList.add(dict2);

        MdmDict dict3 = new MdmDict();
        dict3.setGroupCode("MES3_PROJECT_SOURCE");
        dict3.setItemVal("source1");
        dict3.setItemKey("Source 1");
        mdmDictList.add(dict3);

        // 创建用户和部门数据
        List<SysUser> userList = new ArrayList<>();
        SysUser user = new SysUser();
        user.setId("user1");
        user.setRealname("User One");
        userList.add(user);

        List<SysDepart> departList = new ArrayList<>();
        SysDepart depart = new SysDepart();
        depart.setId("dept1");
        depart.setDepartName("Department One");
        departList.add(depart);

        // 设置mock行为
        when(baseMapper.queryAll(projectManagement)).thenReturn(projectList);
        when(baseMapper.getPersonalProjectSummary("project1", "user1", "2025", projectManagementService.monthList, ""))
                .thenReturn(summaryList);
        when(mdmDictService.getDictAllOrByGroupCode(Arrays.asList(
                "MES3_PROJECT_LEVEL", 
                "MES3_PROJECT_TYPE", 
                "MES3_PROJECT_SOURCE")))
                .thenReturn(mdmDictList);
        when(sysUserService.list()).thenReturn(userList);
        when(sysDepartService.list()).thenReturn(departList);

        // 执行测试
        List<ProjectManagementSummaryDto> result = projectManagementService.getPersonalProjectSummary(projectManagement);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size()); // 包括汇总行
        
        // 验证第一个结果（项目数据）
        ProjectManagementSummaryDto dto = result.get(0);
        assertEquals("project1", dto.getId());
        assertEquals(100.0, dto.getMonth1(), 0.001);
        assertEquals(200.0, dto.getMonth2(), 0.001);
        assertEquals("Level 1", dto.getProjectLevel_dictText());
        assertEquals("Type 1", dto.getProjectType_dictText());
        assertEquals("Source 1", dto.getProjectSource_dictText());
        assertEquals("User One", dto.getLeaderUserId_dictText());
        
        // 验证第二个结果（汇总数据）
        ProjectManagementSummaryDto summary = result.get(1);
        assertEquals("汇总", summary.getProjectCode());
        assertEquals(100.0, summary.getMonth1(), 0.001);
        assertEquals(200.0, summary.getMonth2(), 0.001);
        
        // 验证交互次数
        verify(baseMapper, times(1)).queryAll(projectManagement);
        verify(baseMapper, times(1)).getPersonalProjectSummary("project1", "user1", "2025", projectManagementService.monthList, "");
        verify(mdmDictService, times(1)).getDictAllOrByGroupCode(Arrays.asList(
                "MES3_PROJECT_LEVEL", 
                "MES3_PROJECT_TYPE", 
                "MES3_PROJECT_SOURCE"));
        verify(sysUserService, times(1)).list();
        verify(sysDepartService, times(1)).list();
    }

    @Test
    void testGetPersonalProjectSummary_withoutFilters() {
        // 准备测试数据
        ProjectManagement projectManagement = new ProjectManagement();
        projectManagement.setUserId("user1");

        // 创建项目列表
        List<ProjectManagement> projectList = new ArrayList<>();
        ProjectManagement project1 = new ProjectManagement();
        project1.setId("project1");
        project1.setProjectLevel("level1");
        project1.setProjectType("type1");
        project1.setProjectSource("source1");
        projectList.add(project1);

        // 创建汇总数据列表
        List<ProjectManagementSummaryDto> summaryList = new ArrayList<>();
        ProjectManagementSummaryDto summaryDto = new ProjectManagementSummaryDto();
        summaryDto.setId("project1");
        summaryDto.setMonth1(100.0);
        summaryDto.setMonth2(200.0);
        summaryList.add(summaryDto);

        // 创建字典数据
        List<MdmDict> mdmDictList = new ArrayList<>();
        MdmDict dict1 = new MdmDict();
        dict1.setGroupCode("MES3_PROJECT_LEVEL");
        dict1.setItemVal("level1");
        dict1.setItemKey("Level 1");
        mdmDictList.add(dict1);

        MdmDict dict2 = new MdmDict();
        dict2.setGroupCode("MES3_PROJECT_TYPE");
        dict2.setItemVal("type1");
        dict2.setItemKey("Type 1");
        mdmDictList.add(dict2);

        MdmDict dict3 = new MdmDict();
        dict3.setGroupCode("MES3_PROJECT_SOURCE");
        dict3.setItemVal("source1");
        dict3.setItemKey("Source 1");
        mdmDictList.add(dict3);

        // 创建用户和部门数据
        List<SysUser> userList = new ArrayList<>();
        SysUser user = new SysUser();
        user.setId("user1");
        user.setRealname("User One");
        userList.add(user);

        List<SysDepart> departList = new ArrayList<>();
        SysDepart depart = new SysDepart();
        depart.setId("dept1");
        depart.setDepartName("Department One");
        departList.add(depart);

        // 设置mock行为
        when(baseMapper.queryAll(projectManagement)).thenReturn(projectList);
        when(baseMapper.getPersonalProjectSummary("project1", "user1", "2025", projectManagementService.monthList, ""))
                .thenReturn(summaryList);
        when(mdmDictService.getDictAllOrByGroupCode(Arrays.asList(
                "MES3_PROJECT_LEVEL", 
                "MES3_PROJECT_TYPE", 
                "MES3_PROJECT_SOURCE")))
                .thenReturn(mdmDictList);
        when(sysUserService.list()).thenReturn(userList);
        when(sysDepartService.list()).thenReturn(departList);

        // 执行测试
        List<ProjectManagementSummaryDto> result = projectManagementService.getPersonalProjectSummary(projectManagement);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size()); // 包括汇总行
        
        // 验证第一个结果（项目数据）
        ProjectManagementSummaryDto dto = result.get(0);
        assertEquals("project1", dto.getId());
        assertEquals(100.0, dto.getMonth1(), 0.001);
        assertEquals(200.0, dto.getMonth2(), 0.001);
        assertEquals("Level 1", dto.getProjectLevel_dictText());
        assertEquals("Type 1", dto.getProjectType_dictText());
        assertEquals("Source 1", dto.getProjectSource_dictText());
        assertEquals("User One", dto.getLeaderUserId_dictText());
        
        // 验证第二个结果（汇总数据）
        ProjectManagementSummaryDto summary = result.get(1);
        assertEquals("汇总", summary.getProjectCode());
        assertEquals(100.0, summary.getMonth1(), 0.001);
        assertEquals(200.0, summary.getMonth2(), 0.001);
        
        // 验证交互次数
        verify(baseMapper, times(1)).queryAll(projectManagement);
        verify(baseMapper, times(1)).getPersonalProjectSummary("project1", "user1", "2025", projectManagementService.monthList, "");
        verify(mdmDictService, times(1)).getDictAllOrByGroupCode(Arrays.asList(
                "MES3_PROJECT_LEVEL", 
                "MES3_PROJECT_TYPE", 
                "MES3_PROJECT_SOURCE"));
        verify(sysUserService, times(1)).list();
        verify(sysDepartService, times(1)).list();
    }

    @Test
    void testGetPersonalProjectSummary_noResults() {
        // 准备测试数据
        ProjectManagement projectManagement = new ProjectManagement();
        projectManagement.setUserId("user1");

        // 设置mock行为
        when(baseMapper.queryAll(projectManagement)).thenReturn(new ArrayList<>());

        // 执行测试
        List<ProjectManagementSummaryDto> result = projectManagementService.getPersonalProjectSummary(projectManagement);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证交互次数
        verify(baseMapper, times(1)).queryAll(projectManagement);
        verify(baseMapper, never()).getPersonalProjectSummary(anyString(), anyString(), anyString(), anyList(), "");
        verify(mdmDictService, never()).getDictAllOrByGroupCode(anyList());
        verify(sysUserService, never()).list();
        verify(sysDepartService, never()).list();
    }
}
