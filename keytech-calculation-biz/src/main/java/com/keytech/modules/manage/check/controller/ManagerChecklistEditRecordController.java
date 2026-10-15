package com.keytech.modules.manage.check.controller;

import com.keytech.modules.manage.check.entity.ManagerChecklistEditRecord;
import com.keytech.modules.manage.check.service.IManagerChecklistEditRecordService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;

 /**
 * @Description: 分层检查-检查清单录入记录表
 * @Author: jeecg-boot
 * @Date:   2025-04-10
 * @Version: V1.0
 */
@Tag(name="分层检查-检查清单录入记录表")
@RestController
@RequestMapping("/manage/check/managerChecklistEditRecord")
@Slf4j
public class ManagerChecklistEditRecordController extends BaseController<IManagerChecklistEditRecordService, ManagerChecklistEditRecord> {
	@Autowired
	private IManagerChecklistEditRecordService managerChecklistEditRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param managerChecklistEditRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="分层检查-检查清单录入记录表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ManagerChecklistEditRecord>> queryPageList(ManagerChecklistEditRecord managerChecklistEditRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ManagerChecklistEditRecord> queryWrapper = QueryGenerator.initQueryWrapper(managerChecklistEditRecord, req.getParameterMap());
		Page<ManagerChecklistEditRecord> page = new Page<ManagerChecklistEditRecord>(pageNo, pageSize);
		IPage<ManagerChecklistEditRecord> pageList = managerChecklistEditRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param managerChecklistEditRecord
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入记录表-添加")
	@Operation(summary="分层检查-检查清单录入记录表-添加")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_record:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ManagerChecklistEditRecord managerChecklistEditRecord) {
		managerChecklistEditRecordService.save(managerChecklistEditRecord);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param managerChecklistEditRecord
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入记录表-编辑")
	@Operation(summary="分层检查-检查清单录入记录表-编辑")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_record:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ManagerChecklistEditRecord managerChecklistEditRecord) {
		managerChecklistEditRecordService.updateById(managerChecklistEditRecord);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入记录表-通过id删除")
	@Operation(summary="分层检查-检查清单录入记录表-通过id删除")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_record:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		managerChecklistEditRecordService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "分层检查-检查清单录入记录表-批量删除")
	@Operation(summary="分层检查-检查清单录入记录表-批量删除")
	@RequiresPermissions("manage.check:mes3_manager_checklist_edit_record:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.managerChecklistEditRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="分层检查-检查清单录入记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ManagerChecklistEditRecord> queryById(@RequestParam(name="id",required=true) String id) {
		ManagerChecklistEditRecord managerChecklistEditRecord = managerChecklistEditRecordService.getById(id);
		if(managerChecklistEditRecord==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(managerChecklistEditRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param managerChecklistEditRecord
    */
    @RequiresPermissions("manage.check:mes3_manager_checklist_edit_record:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ManagerChecklistEditRecord managerChecklistEditRecord) {
        return super.exportXls(request, managerChecklistEditRecord, ManagerChecklistEditRecord.class, "分层检查-检查清单录入记录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("manage.check:mes3_manager_checklist_edit_record:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ManagerChecklistEditRecord.class);
    }

}
