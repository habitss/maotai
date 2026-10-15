package com.keytech.modules.manage.check.controller;

import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.manage.check.dto.ManageCheckListDto;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import com.keytech.modules.manage.check.service.IManageCheckListService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 管理者检查清单-分层检查清单
 * @Author: jeecg-boot
 * @Date:   2025-03-31
 * @Version: V1.0
 */
@Tag(name="管理者检查清单-分层检查清单")
@RestController
@RequestMapping("/manage/check/manageCheckList")
@Slf4j
public class ManageCheckListController extends BaseController<IManageCheckListService, ManageCheckList> {
	@Autowired
	private IManageCheckListService manageCheckListService;
	
	/**
     * 分页列表查询
     *
     * @param manageCheckList
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
	@Operation(summary="管理者检查清单-分层检查清单-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ManageCheckList>> queryPageList(ManageCheckList manageCheckList,
                                                        @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                        @RequestParam(name = "orderField",required = false) String orderField,
                                                        @RequestParam(name = "orderType",required = false) String orderType,
                                                        @RequestParam(name = "selectFields",required = false) String selectFields,
                                                        HttpServletRequest req) {
		return Result.OK(manageCheckListService.queryPageList(manageCheckList, pageNo, pageSize));
	}
	
	/**
	 *   添加
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查清单-添加")
	@Operation(summary="管理者检查清单-分层检查清单-添加")
	@RequiresPermissions("manage.check:mes3_manage_check_list:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ManageCheckListDto dto) {
		return manageCheckListService.saveManageCheckList(dto);
	}
	
	/**
	 *  编辑
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查清单-编辑")
	@Operation(summary="管理者检查清单-分层检查清单-编辑")
	@RequiresPermissions("manage.check:mes3_manage_check_list:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ManageCheckListDto dto) {
		return manageCheckListService.updateManageCheckList(dto);
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查清单-通过id删除")
	@Operation(summary="管理者检查清单-分层检查清单-通过id删除")
	@RequiresPermissions("manage.check:mes3_manage_check_list:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		manageCheckListService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param deleteList
	 * @return
	 */
	@AutoLog(value = "管理者检查清单-分层检查清单-批量删除")
	@Operation(summary="管理者检查清单-分层检查清单-批量删除")
	@RequiresPermissions("manage.check:mes3_manage_check_list:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestBody List<ManageCheckList> deleteList) {
		manageCheckListService.deleteBatch(deleteList);
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="管理者检查清单-分层检查清单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ManageCheckList> queryById(@RequestParam(name="id",required=true) String id) {
		ManageCheckList manageCheckList = manageCheckListService.getById(id);
		if(manageCheckList==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(manageCheckList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param manageCheckList
    */
    @RequiresPermissions("manage.check:mes3_manage_check_list:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ManageCheckList manageCheckList) {
        return super.exportXls(request, manageCheckList, ManageCheckList.class, "管理者检查清单-分层检查清单");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("manage.check:mes3_manage_check_list:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return manageCheckListService.importData(request, response, ManageCheckList.class);
    }

	/**
	 * 分层检查汇总
	 * @return
	 */
	@Operation(summary="管理者检查清单-分层检查汇总-列表查询")
	@GetMapping(value = "/querySummaryList")
	public Result<?> querySummaryList(ManageCheckList manageCheckList) {
		return Result.OK(manageCheckListService.querySummaryList(manageCheckList));
	}

	/**
	 * 分层检查汇总-首页
	 * @return
	 */
	@Operation(summary="管理者检查清单-首页分层检查汇总")
	@GetMapping(value = "/queryIndexSummary")
	public Result<?> queryIndexSummary(WSActionStatisticsDto dto) {
		return Result.OK(manageCheckListService.queryIndexSummary(dto));
	}

	/**
	 * 分层检查汇总-首页
	 * @return
	 */
	@Operation(summary="管理者检查清单-首页分层检查汇总")
	@GetMapping(value = "/queryIndexSummaryList")
	public Result<?> queryIndexSummaryList(WSActionStatisticsDto dto) {
		return Result.OK(manageCheckListService.queryIndexSummaryList(dto));
	}
}
