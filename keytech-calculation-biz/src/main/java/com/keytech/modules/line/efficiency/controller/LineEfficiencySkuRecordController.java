package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencySkuRecord;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuRecordService;
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
 * @Description: sku记录
 * @Author: jeecg-boot
 * @Date:   2025-08-18
 * @Version: V1.0
 */
@Tag(name="sku记录")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencySkuRecord")
@Slf4j
public class LineEfficiencySkuRecordController extends BaseController<ILineEfficiencySkuRecordService, LineEfficiencySkuRecord> {
	@Autowired
	private ILineEfficiencySkuRecordService lineEfficiencySkuRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencySkuRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="sku记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencySkuRecord>> queryPageList(LineEfficiencySkuRecord lineEfficiencySkuRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencySkuRecord> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencySkuRecord, req.getParameterMap());
		Page<LineEfficiencySkuRecord> page = new Page<LineEfficiencySkuRecord>(pageNo, pageSize);
		IPage<LineEfficiencySkuRecord> pageList = lineEfficiencySkuRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencySkuRecord
	 * @return
	 */
	@AutoLog(value = "sku记录-添加")
	@Operation(summary="sku记录-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_record:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencySkuRecord lineEfficiencySkuRecord) {
		lineEfficiencySkuRecordService.save(lineEfficiencySkuRecord);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencySkuRecord
	 * @return
	 */
	@AutoLog(value = "sku记录-编辑")
	@Operation(summary="sku记录-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_record:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencySkuRecord lineEfficiencySkuRecord) {
		lineEfficiencySkuRecordService.updateById(lineEfficiencySkuRecord);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "sku记录-通过id删除")
	@Operation(summary="sku记录-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_record:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencySkuRecordService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "sku记录-批量删除")
	@Operation(summary="sku记录-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_record:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencySkuRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="sku记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencySkuRecord> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencySkuRecord lineEfficiencySkuRecord = lineEfficiencySkuRecordService.getById(id);
		if(lineEfficiencySkuRecord==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencySkuRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencySkuRecord
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_record:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencySkuRecord lineEfficiencySkuRecord) {
        return super.exportXls(request, lineEfficiencySkuRecord, LineEfficiencySkuRecord.class, "sku记录");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_record:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencySkuRecord.class);
    }

}
