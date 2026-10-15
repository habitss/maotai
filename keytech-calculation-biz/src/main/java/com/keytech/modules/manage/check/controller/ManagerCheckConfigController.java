package com.keytech.modules.manage.check.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.manage.check.entity.ManagerCheckConfig;
import com.keytech.modules.manage.check.service.IManagerCheckConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 分层检查-检查识别矩阵
 * @Author: jeecg-boot
 * @Date:   2025-04-08
 * @Version: V1.0
 */
@Tag(name="分层检查-检查识别矩阵")
@RestController
@RequestMapping("/manage/check/managerCheckConfig")
@Slf4j
public class ManagerCheckConfigController extends BaseController<IManagerCheckConfigService, ManagerCheckConfig> {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private IManagerCheckConfigService managerCheckConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param managerCheckConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="分层检查-检查识别矩阵-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ManagerCheckConfig>> queryPageList(ManagerCheckConfig managerCheckConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        // 自定义查询规则
        Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
        // 自定义多选的查询规则为：LIKE_WITH_OR
        customeRuleMap.put("cghd", QueryRuleEnum.LIKE_WITH_OR);
        QueryWrapper<ManagerCheckConfig> queryWrapper = QueryGenerator.initQueryWrapper(managerCheckConfig, req.getParameterMap(),customeRuleMap);
		if(StrUtil.isNotBlank(managerCheckConfig.getPostId()) && 1 == managerCheckConfig.getIsConfig()){
			queryWrapper.and(
					wrapper ->
							wrapper.like("gwpz_json", "\"id\":\""+managerCheckConfig.getPostId()+"\"")
									.or()
									.like("gwpz_json", "\"id\":"+managerCheckConfig.getPostId())
			);
		}
		queryWrapper.orderByAsc("sort");
		queryWrapper.orderByAsc("id");
		Page<ManagerCheckConfig> page = new Page<ManagerCheckConfig>(pageNo, pageSize);
		IPage<ManagerCheckConfig> pageList = managerCheckConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param managerCheckConfig
	 * @return
	 */
	@AutoLog(value = "分层检查-检查识别矩阵-添加")
	@Operation(summary="分层检查-检查识别矩阵-添加")
	@RequiresPermissions("manage.check:mes3_manager_check_config:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ManagerCheckConfig managerCheckConfig) {
		managerCheckConfigService.save(managerCheckConfig);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param managerCheckConfig
	 * @return
	 */
	@AutoLog(value = "分层检查-检查识别矩阵-编辑")
	@Operation(summary="分层检查-检查识别矩阵-编辑")
	@RequiresPermissions("manage.check:mes3_manager_check_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ManagerCheckConfig managerCheckConfig) {
		managerCheckConfigService.updateById(managerCheckConfig);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "分层检查-检查识别矩阵-通过id删除")
	@Operation(summary="分层检查-检查识别矩阵-通过id删除")
	@RequiresPermissions("manage.check:mes3_manager_check_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		managerCheckConfigService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "分层检查-检查识别矩阵-批量删除")
	@Operation(summary="分层检查-检查识别矩阵-批量删除")
	@RequiresPermissions("manage.check:mes3_manager_check_config:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.managerCheckConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="分层检查-检查识别矩阵-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ManagerCheckConfig> queryById(@RequestParam(name="id",required=true) String id) {
		ManagerCheckConfig managerCheckConfig = managerCheckConfigService.getById(id);
		if(managerCheckConfig==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(managerCheckConfig);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param managerCheckConfig
    */
    @RequiresPermissions("manage.check:mes3_manager_check_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ManagerCheckConfig managerCheckConfig) {
        return this.managerCheckConfigService.exportXls(request, managerCheckConfig, "分层检查-检查识别矩阵");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("manage.check:mes3_manager_check_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return this.managerCheckConfigService.importExcelBy(request, response);
    }


	/**
	 * 临时使用查询所有用户
	 */
	@Operation(summary="临时使用查询所有用户")
	@GetMapping(value = "/getUserList")
	public Result<List<SysUser>> getUserList(SysUser sysUser, HttpServletRequest req) {
		QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, req.getParameterMap());
		queryWrapper.orderByAsc("username");
		queryWrapper.select("id", "username", "realname", "work_no");
		return Result.OK(sysUserService.list(queryWrapper));
	}
}
