package com.keytech.modules.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.opl.dto.OplWriteTwoRrDto;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.entity.OplReviewPlanFile;
import com.keytech.modules.opl.service.IOplReviewPlanFileService;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import com.keytech.modules.opl.vo.OplManageVo;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
//--story=1057859 --user=孙金伟 31-OPL清单开发 https://www.tapd.cn/63554897/s/1392541 单元测试
class OplHomePageServiceImplTest {

    @InjectMocks
    private OplHomePageServiceImpl oplHomePageService;

    @Mock
    private ISysDepartService sysDepartService;

    @Mock
    private ISysUserService iSysUserService;

    @Mock
    private IOplReviewPlanService iOplReviewPlanService;

    @Mock
    private IMdmDictService dicService;

    @Mock
    private IOplReviewPlanFileService oplReviewPlanFileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetQueryPageData_NormalCase() {
        // 准备测试数据
        Page<OplHomePage> page = new Page<>(1, 10);
        OplHomePage oplPage = new OplHomePage();
        oplPage.setOplDesc("test");
        oplPage.setDeptCode("dept");
        oplPage.setDeptQuery("dept1,dept2");

        MockHttpServletRequest req = new MockHttpServletRequest();

        // Mock 依赖服务
        List<OplHomePage> oplHomePages = new ArrayList<>();
        OplHomePage homePage = new OplHomePage();
        homePage.setId("1");
        homePage.setOplDesc("test");
        oplHomePages.add(homePage);

        IPage<OplHomePage> iPage = new Page<>();
        iPage.setRecords(oplHomePages);
        iPage.setTotal(1);

        when(QueryGenerator.initQueryWrapper(any(OplHomePage.class), any(Map.class))).thenReturn(new QueryWrapper<>());
        when(oplHomePageService.page(any(Page.class), any(QueryWrapper.class))).thenReturn((Page) iPage);

        List<String> oplIds = Arrays.asList("1");
        List<OplReviewPlan> reviewPlans = new ArrayList<>();
        OplReviewPlan reviewPlan = new OplReviewPlan();
        reviewPlan.setOplId("1");
        reviewPlan.setLatestVersionNumber("1.0");
        reviewPlan.setYear(2023);
        reviewPlans.add(reviewPlan);
        when(iOplReviewPlanService.lambdaQuery().in(any(), anyList()).isNotNull(any()).orderByDesc((SFunction<OplReviewPlan, ?>) any()).list()).thenReturn(reviewPlans);

        List<OplReviewPlanFile> reviewPlanFiles = new ArrayList<>();
        OplReviewPlanFile reviewPlanFile = new OplReviewPlanFile();
        reviewPlanFile.setReviewPlanId("1");
        reviewPlanFile.setFileName("file1");
        reviewPlanFile.setCreateTime(new Date());
        reviewPlanFile.setWeek("1.1-1.10");
        reviewPlanFiles.add(reviewPlanFile);
        when(oplReviewPlanFileService.lambdaQuery().list()).thenReturn(reviewPlanFiles);

        List<String> dictList = new ArrayList<>();
        dictList.add("MES3_OPL_FREQUENCY");
        List<MdmDict> oplFrequencyList = new ArrayList<>();
        MdmDict mdmDict = new MdmDict();
        mdmDict.setItemVal("1");
        mdmDict.setItemKey("Monthly");
        oplFrequencyList.add(mdmDict);
        when(dicService.getDictAllOrByGroupCode(anyList())).thenReturn(oplFrequencyList);

        // 执行测试
        IPage<OplHomePage> result = oplHomePageService.getQueryPageData(page, oplPage, req);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("test", result.getRecords().get(0).getOplDesc());
    }

    @Test
    void testGetQueryPageData_EmptyResult() {
        // 准备测试数据
        Page<OplHomePage> page = new Page<>(1, 10);
        OplHomePage oplPage = new OplHomePage();
        MockHttpServletRequest req = new MockHttpServletRequest();

        // Mock 依赖服务
        IPage<OplHomePage> iPage = new Page<>();
        iPage.setRecords(new ArrayList<>());
        iPage.setTotal(0);
        when(QueryGenerator.initQueryWrapper(any(OplHomePage.class), any(Map.class))).thenReturn(new QueryWrapper<>());
        when(oplHomePageService.page(any(Page.class), any(QueryWrapper.class))).thenReturn((Page) iPage);

        // 执行测试
        IPage<OplHomePage> result = oplHomePageService.getQueryPageData(page, oplPage, req);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testGetQueryPageData_WriteTwoRrNotEmpty() {
        // 准备测试数据
        Page<OplHomePage> page = new Page<>(1, 10);
        OplHomePage oplPage = new OplHomePage();
        oplPage.setWriteTwoRr("[{\"oplTitle\":\"file1\",\"r1\":\"user1\"}]");

        MockHttpServletRequest req = new MockHttpServletRequest();

        // Mock 依赖服务
        List<OplHomePage> oplHomePages = new ArrayList<>();
        OplHomePage homePage = new OplHomePage();
        homePage.setId("1");
        homePage.setWriteTwoRr("[{\"oplTitle\":\"file1\",\"r1\":\"user1\"}]");
        oplHomePages.add(homePage);

        IPage<OplHomePage> iPage = new Page<>();
        iPage.setRecords(oplHomePages);
        iPage.setTotal(1);

        when(QueryGenerator.initQueryWrapper(any(OplHomePage.class), any(Map.class))).thenReturn(new QueryWrapper<>());
        when(oplHomePageService.page(any(Page.class), any(QueryWrapper.class))).thenReturn((Page) iPage);

        List<OplReviewPlan> reviewPlans = new ArrayList<>();
        OplReviewPlan reviewPlan = new OplReviewPlan();
        reviewPlan.setOplId("1");
        reviewPlan.setLatestVersionNumber("1.0");
        reviewPlan.setYear(2023);
        reviewPlans.add(reviewPlan);
        when(iOplReviewPlanService.lambdaQuery().in(any(), anyList()).isNotNull(any()).orderByDesc((SFunction<OplReviewPlan, ?>) any()).list()).thenReturn(reviewPlans);

        List<OplReviewPlanFile> reviewPlanFiles = new ArrayList<>();
        OplReviewPlanFile reviewPlanFile = new OplReviewPlanFile();
        reviewPlanFile.setReviewPlanId("1");
        reviewPlanFile.setFileName("file1");
        reviewPlanFile.setCreateTime(new Date());
        reviewPlanFile.setWeek("1.1-1.10");
        reviewPlanFiles.add(reviewPlanFile);
        when(oplReviewPlanFileService.lambdaQuery().list()).thenReturn(reviewPlanFiles);

        List<String> dictList = new ArrayList<>();
        dictList.add("MES3_OPL_FREQUENCY");
        List<MdmDict> oplFrequencyList = new ArrayList<>();
        MdmDict mdmDict = new MdmDict();
        mdmDict.setItemVal("1");
        mdmDict.setItemKey("Monthly");
        oplFrequencyList.add(mdmDict);
        when(dicService.getDictAllOrByGroupCode(anyList())).thenReturn(oplFrequencyList);

        // 执行测试
        IPage<OplHomePage> result = oplHomePageService.getQueryPageData(page, oplPage, req);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        List<OplWriteTwoRrDto> writeTwoRrList = result.getRecords().get(0).getWriteTwoRrList();
        assertNotNull(writeTwoRrList);
        assertEquals(1, writeTwoRrList.size());
        assertEquals("user1", result.getRecords().get(0).getPutIntoNames());
    }

    //--story=1057865 --user=孙金伟 31-OPL管理回顾 https://www.tapd.cn/63554897/s/1392543 单元测试
    @Test
    void testGetOplManage_NormalCase() {
        // 准备测试数据
        String year = "2023";
        String month = "01";
        String depts = "dept1";
        String dept = "dept1";

        // 模拟部门查询结果
        SysDepart sysDepart = new SysDepart();
        sysDepart.setId("1");
        sysDepart.setOrgCode("dept1");
        sysDepart.setDepartName("测试部门");
        List<SysDepart> departList = Arrays.asList(sysDepart);
        when(sysDepartService.list(any(LambdaQueryWrapper.class))).thenReturn(departList);

        // 模拟OPL总数统计
        OplManageVo totalVo = new OplManageVo();
        totalVo.setDept("dept1");
        totalVo.setTotal(10);
        when(iOplReviewPlanService.getOpltotalByDept()).thenReturn(Arrays.asList(totalVo));

        // 模拟回顾计划数据
        OplReviewPlan reviewPlan = new OplReviewPlan();
        reviewPlan.setDeptCode("dept1");
        reviewPlan.setResultData("{\"01.1\":\"3\",\"01.2\":\"1\"}");
        when(iOplReviewPlanService.getOplReviewByDept(year)).thenReturn(Arrays.asList(reviewPlan));

        // 执行测试
        List<OplManageVo> result = oplHomePageService.getOplManage(year, month, depts, dept);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        OplManageVo vo = result.get(0);
        assertEquals("1", vo.getKey());
        assertEquals("测试部门", vo.getTitle());
        assertEquals(10, vo.getTotal());
        assertEquals(2, vo.getPlanReview()); // 01.1 和 01.2 都是计划回顾
        assertEquals(1, vo.getRealityReview()); // 01.1 是实际回顾
        assertEquals("50%", vo.getReviewRate()); // 完成率 1/2 = 50%
        assertEquals(2, vo.getMonthPlanReview());
        assertEquals(1, vo.getMonthRealityReview());
        assertEquals("50%", vo.getMonthReviewRate());
    }

    @Test
    void testGetOplManage_EmptyDepts() {
        // 准备测试数据
        String year = "2023";
        String month = "01";
        String depts = "";
        String dept = "dept1";

        // 模拟部门查询结果
        SysDepart sysDepart = new SysDepart();
        sysDepart.setId("1");
        sysDepart.setOrgCode("dept1");
        sysDepart.setDepartName("测试部门");
        List<SysDepart> departList = Arrays.asList(sysDepart);
        when(sysDepartService.list(any(LambdaQueryWrapper.class))).thenReturn(departList);

        // 模拟OPL总数统计
        OplManageVo totalVo = new OplManageVo();
        totalVo.setDept("dept1");
        totalVo.setTotal(10);
        when(iOplReviewPlanService.getOpltotalByDept()).thenReturn(Arrays.asList(totalVo));

        // 模拟回顾计划数据
        OplReviewPlan reviewPlan = new OplReviewPlan();
        reviewPlan.setDeptCode("dept1");
        reviewPlan.setResultData("{\"01.1\":\"3\",\"01.2\":\"1\"}");
        when(iOplReviewPlanService.getOplReviewByDept(year)).thenReturn(Arrays.asList(reviewPlan));

        // 执行测试
        List<OplManageVo> result = oplHomePageService.getOplManage(year, month, depts, dept);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetOplManage_NoDepartData() {
        // 准备测试数据
        String year = "2023";
        String month = "01";
        String depts = "";
        String dept = "dept1";

        // 模拟无部门数据
        when(sysDepartService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // 执行测试
        List<OplManageVo> result = oplHomePageService.getOplManage(year, month, depts, dept);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOplManage_NoReviewPlanData() {
        // 准备测试数据
        String year = "2023";
        String month = "01";
        String depts = "";
        String dept = "dept1";

        // 模拟部门查询结果
        SysDepart sysDepart = new SysDepart();
        sysDepart.setId("1");
        sysDepart.setOrgCode("dept1");
        sysDepart.setDepartName("测试部门");
        List<SysDepart> departList = Arrays.asList(sysDepart);
        when(sysDepartService.list(any(LambdaQueryWrapper.class))).thenReturn(departList);

        // 模拟OPL总数统计
        OplManageVo totalVo = new OplManageVo();
        totalVo.setDept("dept1");
        totalVo.setTotal(10);
        when(iOplReviewPlanService.getOpltotalByDept()).thenReturn(Arrays.asList(totalVo));

        // 模拟无回顾计划数据
        when(iOplReviewPlanService.getOplReviewByDept(year)).thenReturn(Collections.emptyList());

        // 执行测试
        List<OplManageVo> result = oplHomePageService.getOplManage(year, month, depts, dept);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        OplManageVo vo = result.get(0);
        assertEquals(10, vo.getTotal());
        assertEquals(0, vo.getPlanReview());
        assertEquals(0, vo.getRealityReview());
        assertEquals("0%", vo.getReviewRate());
    }
}
