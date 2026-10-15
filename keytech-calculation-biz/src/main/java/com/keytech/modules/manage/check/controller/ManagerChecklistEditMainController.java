package com.keytech.modules.manage.check.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.kpi.controller.KpiReportActualWeekValueController;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.manage.check.entity.ManagerChecklistEditMain;
import com.keytech.modules.manage.check.service.IManagerChecklistEditMainService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.*;

/**
 * @Description: 分层检查-检查清单录入主表
 * @Author: jeecg-boot
 * @Date:   2025-04-10
 * @Version: V1.0
 */
@Tag(name="分层检查-检查清单录入主表")
@RestController
@RequestMapping("/manage/check/managerChecklistEditMain")
@Slf4j
public class ManagerChecklistEditMainController extends BaseController<IManagerChecklistEditMainService, ManagerChecklistEditMain> {
	private static final Logger logger = LoggerFactory.getLogger(ManagerChecklistEditMainController.class);
	@Autowired
	private IManagerChecklistEditMainService managerChecklistEditMainService;
	
	/**
	 * 分页列表查询
	 *
	 * @param managerChecklistEditMain
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="分层检查-检查清单录入主表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ManagerChecklistEditMain>> queryPageList(ManagerChecklistEditMain managerChecklistEditMain,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ManagerChecklistEditMain> queryWrapper = QueryGenerator.initQueryWrapper(managerChecklistEditMain, req.getParameterMap());
		queryWrapper.orderByDesc("record_date");
		Page<ManagerChecklistEditMain> page = new Page<ManagerChecklistEditMain>(pageNo, pageSize);
		IPage<ManagerChecklistEditMain> pageList = managerChecklistEditMainService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param managerChecklistEditMain
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入主表-添加")
	@Operation(summary="分层检查-检查清单录入主表-添加")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_main:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ManagerChecklistEditMain managerChecklistEditMain) {
		managerChecklistEditMainService.dataSave(managerChecklistEditMain);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  审核
	 *
	 * @param managerChecklistEditMain
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入主表-审核")
	@Operation(summary="分层检查-检查清单录入主表-审核")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_main:edit")
	@RequestMapping(value = "/confirmData", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> confirmData(@RequestBody ManagerChecklistEditMain managerChecklistEditMain) {
		managerChecklistEditMain.setResultConfirmTime(new Date());
		managerChecklistEditMainService.updateById(managerChecklistEditMain);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入主表-通过id删除")
	@Operation(summary="分层检查-检查清单录入主表-通过id删除")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_main:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		managerChecklistEditMainService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入主表-批量删除")
	@Operation(summary="分层检查-检查清单录入主表-批量删除")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_main:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.managerChecklistEditMainService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="分层检查-检查清单录入主表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ManagerChecklistEditMain> queryById(@RequestParam(name="id",required=true) String id) {
		ManagerChecklistEditMain managerChecklistEditMain = managerChecklistEditMainService.getById(id);
		if(managerChecklistEditMain==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(managerChecklistEditMain);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param managerChecklistEditMain
    */
    @RequiresPermissions("manage.check:mes3_manager_checklist_edit_main:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ManagerChecklistEditMain managerChecklistEditMain) {
        return super.exportXls(request, managerChecklistEditMain, ManagerChecklistEditMain.class, "分层检查-检查清单录入主表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("manage.check:mes3_manager_checklist_edit_main:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ManagerChecklistEditMain.class);
    }

	/**
	 * 获取检查项目
	 */
	@Operation(summary="分层检查-检查清单录入主表-获取检查项目")
	@GetMapping(value = "/queryInspectData")
	public Result<?> queryInspectData(ManagerChecklistEditMain managerChecklistEditMain) {
		Map<String, Object> map = managerChecklistEditMainService.queryInspectData(managerChecklistEditMain);
		return Result.OK(map);
	}

	/**
	 * 定时任务接口-检查清单推送
	 */
	@AutoLog(value = "定时任务接口-检查清单推送")
	@Operation(summary="定时任务接口-检查清单推送")
	@PostMapping(value = "/managerChecklistJob")
	public Result<?> managerChecklistJob(@RequestBody Map map) {
		logger.info("检查清单推送---ok");
		managerChecklistEditMainService.managerMonthYearCheckPush();
		logger.info("检查清单推送---ok");
		return Result.OK("操作成功");
	}


}
