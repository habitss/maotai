package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyRealTimeLast;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyRealTimeLastService;
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
 * @Description: 实时计算逻辑-最新记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
@Tag(name="实时计算逻辑-最新记录表")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyRealTimeLast")
@Slf4j
public class LineEfficiencyRealTimeLastController extends BaseController<ILineEfficiencyRealTimeLastService, LineEfficiencyRealTimeLast> {
	@Autowired
	private ILineEfficiencyRealTimeLastService lineEfficiencyRealTimeLastService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyRealTimeLast
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="实时计算逻辑-最新记录表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyRealTimeLast>> queryPageList(LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencyRealTimeLast> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyRealTimeLast, req.getParameterMap());
		Page<LineEfficiencyRealTimeLast> page = new Page<LineEfficiencyRealTimeLast>(pageNo, pageSize);
		IPage<LineEfficiencyRealTimeLast> pageList = lineEfficiencyRealTimeLastService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyRealTimeLast
	 * @return
	 */
	@AutoLog(value = "实时计算逻辑-最新记录表-添加")
	@Operation(summary="实时计算逻辑-最新记录表-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_real_time_last:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast) {
		lineEfficiencyRealTimeLastService.save(lineEfficiencyRealTimeLast);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyRealTimeLast
	 * @return
	 */
	@AutoLog(value = "实时计算逻辑-最新记录表-编辑")
	@Operation(summary="实时计算逻辑-最新记录表-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_real_time_last:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast) {
		lineEfficiencyRealTimeLastService.updateById(lineEfficiencyRealTimeLast);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "实时计算逻辑-最新记录表-通过id删除")
	@Operation(summary="实时计算逻辑-最新记录表-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_real_time_last:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyRealTimeLastService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "实时计算逻辑-最新记录表-批量删除")
	@Operation(summary="实时计算逻辑-最新记录表-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_real_time_last:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyRealTimeLastService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="实时计算逻辑-最新记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyRealTimeLast> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast = lineEfficiencyRealTimeLastService.getById(id);
		if(lineEfficiencyRealTimeLast==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyRealTimeLast);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyRealTimeLast
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_real_time_last:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast) {
        return super.exportXls(request, lineEfficiencyRealTimeLast, LineEfficiencyRealTimeLast.class, "实时计算逻辑-最新记录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_real_time_last:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyRealTimeLast.class);
    }

	 /**
	  * 酒损计算
	  *
	  * @return
	  */
	 @AutoLog(value = "酒损计算")
	 @Operation(summary="酒损计算")
	 @PostMapping(value = "/executeCip")
	 public Result<String> executeCip() {
		 lineEfficiencyRealTimeLastService.executeCip();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

}
