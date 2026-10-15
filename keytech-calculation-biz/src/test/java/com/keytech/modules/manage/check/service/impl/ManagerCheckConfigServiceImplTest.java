package com.keytech.modules.manage.check.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.manage.check.entity.ManagerCheckConfig;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmPost;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmPostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.view.JeecgMapExcelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ManagerCheckConfigServiceImplTest {

    @InjectMocks
    private ManagerCheckConfigServiceImpl managerCheckConfigService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private IMdmPostService iMdmPostService;

    @Mock
    private IMdmDictService mdmDictService;

    @Mock
    private ISysDepartService sysDepartService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //--story=1055656 --user=孙金伟 15-包头工厂现场优化改造 https://www.tapd.cn/63554897/s/1382216 单元测试
    @Test
    public void testExportXls_withSelectionsAndPostData() {
        // Arrange
        ManagerCheckConfig managerCheckConfig = new ManagerCheckConfig();
        String title = "测试导出";

        // 模拟 request 参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("selections")).thenReturn("1,2,3");

        // 模拟 listMaps 查询结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", "1");
        dataMap.put("factory_code", "F001");
        dataMap.put("dept_code", "D001");
        dataMap.put("line_code", "L001");
        dataMap.put("jcxly_dm", "S001");
        dataMap.put("jctx_dm", "T001");
        dataMap.put("if_zx", "1");
        dataMap.put("gwpz_json", "[{\"id\":\"P001\",\"plName\":\"每天\",\"weekName\":\"每周\"}]");
        mapList.add(dataMap);

        when(managerCheckConfigService.listMaps(any(QueryWrapper.class))).thenReturn(mapList);

        // 模拟 sysDepartService.list()
        List<SysDepart> allDeptList = new ArrayList<>();
        SysDepart dept = new SysDepart();
        dept.setOrgCode("F001");
        dept.setDepartName("工厂A");
        allDeptList.add(dept);
        dept = new SysDepart();
        dept.setOrgCode("D001");
        dept.setDepartName("部门A");
        allDeptList.add(dept);
        dept = new SysDepart();
        dept.setOrgCode("L001");
        dept.setDepartName("产线A");
        allDeptList.add(dept);
        when(sysDepartService.list()).thenReturn(allDeptList);

        // 模拟岗位数据
        List<MdmPost> postList = new ArrayList<>();
        MdmPost post = new MdmPost();
        post.setId(1);
        post.setPostName("岗位A");
        post.setDeptCode("D001");
        post.setPostType(1);
        postList.add(post);
        when(iMdmPostService.list(any(LambdaQueryWrapper.class))).thenReturn(postList);

        // 模拟字典数据
        List<MdmDict> mdmDictList = new ArrayList<>();
        MdmDict dict = new MdmDict();
        dict.setGroupCode("MES3_MANAGE_CHECK_SOURCE");
        dict.setItemVal("S001");
        dict.setItemKey("来源A");
        mdmDictList.add(dict);
        dict = new MdmDict();
        dict.setGroupCode("MES3_MANAGE_CHECK_TX");
        dict.setItemVal("T001");
        dict.setItemKey("体系A");
        mdmDictList.add(dict);
        when(mdmDictService.getDictAllOrByGroupCode(anyList())).thenReturn(mdmDictList);

        // 模拟登录用户
        LoginUser loginUser = new LoginUser();
        loginUser.setRealname("测试用户");
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getSubject).thenThrow(new RuntimeException("Mocked Subject"));
            // 这里简化处理，实际项目中应使用真实 SecurityContext
        }

        // Act
        ModelAndView result = managerCheckConfigService.exportXls(request, managerCheckConfig, title);

        // Assert
        assertNotNull(result);
        assertEquals(JeecgMapExcelView.class, result.getView().getClass());
        assertEquals(title, result.getModel().get(NormalExcelConstants.FILE_NAME));
        assertEquals(mapList, result.getModel().get(NormalExcelConstants.MAP_LIST));
    }
}
