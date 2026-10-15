package com.keytech.modules.action.plan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
//--story=1055292 --user=孙金伟 19-问题来源调整，添加是否可用且不影响历史数据 https://www.tapd.cn/63554897/s/1379457 单元测试
class ActionPlanProblemTypeControllerTest {

    @InjectMocks
    private ActionPlanProblemTypeController actionPlanProblemTypeController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IActionPlanProblemTypeService actionPlanProblemTypeService;

    private ActionPlanProblemType problemType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        problemType = new ActionPlanProblemType();
    }

    @Test
    void testQueryAllTreeData_withExcludeDisabledParam() {
        // 准备测试数据
        problemType.setExcludeDisabled("true");
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        
        // 构建查询条件
        QueryWrapper<ActionPlanProblemType> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(ActionPlanProblemType.class, 
            info -> Arrays.asList("id", "tenant_id", "factory_code", 
                "dept_code", "line_code", "section", 
                "problem_name", "parent_id", "has_child", "sort")
                .contains(info.getColumn()));
        
        // 验证select语句是否正确
        assertEquals(queryWrapper.getTargetSql(), 
            "SELECT id,tenant_id,factory_code,dept_code,line_code,section,problem_name,parent_id,has_child,sort FROM mes3_action_plan_problem_type");

        // 执行测试
        verifyNoInteractions(actionPlanProblemTypeService);
    }

    @Test
    void testQueryAllTreeData_withoutExcludeDisabledParam() {
        // 准备测试数据
        problemType.setExcludeDisabled(null);
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        
        // 构建查询条件
        QueryWrapper<ActionPlanProblemType> queryWrapper = new QueryWrapper<>();
        
        // 验证查询条件
        assertNull(problemType.getExcludeDisabled());
        assertTrue(queryWrapper.getTargetSql().isEmpty());

        // 执行测试
        verifyNoInteractions(actionPlanProblemTypeService);
    }

    @Test
    void testQueryAllTreeData_withParentIdZero() {
        // 准备测试数据
        problemType.setExcludeDisabled(null);
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        
        // 创建测试数据
        List<ActionPlanProblemType> dbList = new ArrayList<>();
        ActionPlanProblemType type = new ActionPlanProblemType();
        type.setId("1");
        type.setParentId("0");
        dbList.add(type);
        
        // 模拟service返回
        when(actionPlanProblemTypeService.list(any(QueryWrapper.class))).thenReturn(dbList);
        
        // 执行方法
        Result<List<ActionPlanProblemType>> result = actionPlanProblemTypeController.queryAllTreeData(request, problemType);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getResult().size());
        assertNull(result.getResult().get(0).getParentId());
    }

    @Test
    void testQueryAllTreeData_buildTreeStructure() {
        // 准备测试数据
        problemType.setExcludeDisabled(null);
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        
        // 创建父子节点数据
        List<ActionPlanProblemType> dbList = new ArrayList<>();
        
        ActionPlanProblemType parent = new ActionPlanProblemType();
        parent.setId("1");
        parent.setParentId(null);
        
        ActionPlanProblemType child = new ActionPlanProblemType();
        child.setId("2");
        child.setParentId("1");
        
        dbList.add(parent);
        dbList.add(child);
        
        // 模拟service返回
        when(actionPlanProblemTypeService.list(any(QueryWrapper.class))).thenReturn(dbList);
        
        // 执行方法
        Result<List<ActionPlanProblemType>> result = actionPlanProblemTypeController.queryAllTreeData(request, problemType);
        
        // 验证结果
        assertNotNull(result);
        List<ActionPlanProblemType> treeData = result.getResult();
        
        // 验证树结构
        assertEquals(1, treeData.size()); // 只有一个根节点
        assertEquals("1", treeData.get(0).getId());
        assertEquals(1, treeData.get(0).getChildList().size()); // 一个子节点
        assertEquals("2", treeData.get(0).getChildList().get(0).getId());
    }

    @Test
    void testQueryAllTreeData_emptyData() {
        // 准备测试数据
        problemType.setExcludeDisabled(null);
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        
        // 模拟空数据
        when(actionPlanProblemTypeService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());
        
        // 执行方法
        Result<List<ActionPlanProblemType>> result = actionPlanProblemTypeController.queryAllTreeData(request, problemType);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getResult().isEmpty());
    }

    @Test
    void testQueryAllTreeData_multiLevelStructure() {
        // 准备测试数据
        problemType.setExcludeDisabled(null);
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);
        
        // 创建多级节点数据
        List<ActionPlanProblemType> dbList = new ArrayList<>();
        
        ActionPlanProblemType level1 = new ActionPlanProblemType();
        level1.setId("1");
        level1.setParentId(null);
        
        ActionPlanProblemType level2 = new ActionPlanProblemType();
        level2.setId("2");
        level2.setParentId("1");
        
        ActionPlanProblemType level3 = new ActionPlanProblemType();
        level3.setId("3");
        level3.setParentId("2");
        
        dbList.add(level1);
        dbList.add(level2);
        dbList.add(level3);
        
        // 模拟service返回
        when(actionPlanProblemTypeService.list(any(QueryWrapper.class))).thenReturn(dbList);
        
        // 执行方法
        Result<List<ActionPlanProblemType>> result = actionPlanProblemTypeController.queryAllTreeData(request, problemType);
        
        // 验证结果
        assertNotNull(result);
        List<ActionPlanProblemType> treeData = result.getResult();
        
        // 验证多级结构
        assertEquals(1, treeData.size()); // 根节点
        assertEquals("1", treeData.get(0).getId());
        
        // 一级子节点
        assertEquals(1, treeData.get(0).getChildList().size());
        assertEquals("2", treeData.get(0).getChildList().get(0).getId());
        
        // 二级子节点
        assertEquals(1, treeData.get(0).getChildList().get(0).getChildList().size());
        assertEquals("3", treeData.get(0).getChildList().get(0).getChildList().get(0).getId());
    }
}
