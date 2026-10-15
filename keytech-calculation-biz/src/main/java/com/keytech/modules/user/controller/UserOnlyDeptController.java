package com.keytech.modules.user.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.user.UserOnlyDept;
import com.keytech.modules.user.service.IUserOnlyDeptService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 用户唯一部门
 * @Author: jeecg-boot
 * @Date:   2025-05-16
 * @Version: V1.0
 */
@Tag(name="用户唯一部门")
@RestController
@RequestMapping("/mes3User/userOnlyDept")
@Slf4j
public class UserOnlyDeptController extends BaseController<IUserOnlyDeptService, UserOnlyDept> {
	@Autowired
	private IUserOnlyDeptService userOnlyDeptService;
	 @Autowired
	 private ISysUserService iSysUserService;
	
	/**
	 * 分页列表查询
	 *
	 * @param userOnlyDept
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="用户唯一部门-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<UserOnlyDept>> queryPageList(UserOnlyDept userOnlyDept,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<UserOnlyDept> queryWrapper = QueryGenerator.initQueryWrapper(userOnlyDept, req.getParameterMap());
		queryWrapper.orderByAsc("user_dept_code");
		Page<UserOnlyDept> page = new Page<>(pageNo, pageSize);
		IPage<UserOnlyDept> pageList = userOnlyDeptService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param userOnlyDept
	 * @return
	 */
	@AutoLog(value = "用户唯一部门-添加")
	@Operation(summary="用户唯一部门-添加")
	@RequiresPermissions("user:mes3_user_only_dept:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody UserOnlyDept userOnlyDept) {
		userOnlyDeptService.saveOrUpdateData(userOnlyDept);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param userOnlyDept
	 * @return
	 */
	@AutoLog(value = "用户唯一部门-编辑")
	@Operation(summary="用户唯一部门-编辑")
	@RequiresPermissions("user:mes3_user_only_dept:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody UserOnlyDept userOnlyDept) {
		userOnlyDeptService.saveOrUpdateData(userOnlyDept);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "用户唯一部门-通过id删除")
	@Operation(summary="用户唯一部门-通过id删除")
	@RequiresPermissions("user:mes3_user_only_dept:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		userOnlyDeptService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "用户唯一部门-批量删除")
	@Operation(summary="用户唯一部门-批量删除")
	@RequiresPermissions("user:mes3_user_only_dept:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.userOnlyDeptService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="用户唯一部门-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<UserOnlyDept> queryById(@RequestParam(name="id",required=true) String id) {
		UserOnlyDept userOnlyDept = userOnlyDeptService.getById(id);
		if(userOnlyDept==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(userOnlyDept);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param userOnlyDept
    */
    @RequiresPermissions("user:mes3_user_only_dept:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UserOnlyDept userOnlyDept) {
        return super.exportXls(request, userOnlyDept, UserOnlyDept.class, "用户唯一部门");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("user:mes3_user_only_dept:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, UserOnlyDept.class);
    }


	 /**
	  * 查询不存在用户管理中的数据
	  * @param isExists
	  * @return
	  */
	 @GetMapping(value = "/queryNoExistsUser")
	 public Result<List<SysUser>> queryPageList( @RequestParam(name="isExists", defaultValue = "false") Boolean isExists) {
		 LambdaQueryWrapper queryWrapper = new LambdaQueryWrapper<SysUser>();
		 queryWrapper.notExists(true == isExists,"select 1 from mes3_user_only_dept o where o.user_id = sys_user.id");
		 List<SysUser> list = iSysUserService.list(queryWrapper);
		 return Result.OK(list);
	 }
}
