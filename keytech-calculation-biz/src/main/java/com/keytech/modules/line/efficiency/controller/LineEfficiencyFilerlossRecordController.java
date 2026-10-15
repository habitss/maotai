package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyFilerlossRecord;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyFilerlossRecordService;
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
 * @Description: 酒损记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
@Tag(name="酒损记录表")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyFilerlossRecord")
@Slf4j
public class LineEfficiencyFilerlossRecordController extends BaseController<ILineEfficiencyFilerlossRecordService, LineEfficiencyFilerlossRecord> {
	@Autowired
	private ILineEfficiencyFilerlossRecordService lineEfficiencyFilerlossRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyFilerlossRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="酒损记录表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyFilerlossRecord>> queryPageList(LineEfficiencyFilerlossRecord lineEfficiencyFilerlossRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencyFilerlossRecord> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyFilerlossRecord, req.getParameterMap());
		Page<LineEfficiencyFilerlossRecord> page = new Page<LineEfficiencyFilerlossRecord>(pageNo, pageSize);
		IPage<LineEfficiencyFilerlossRecord> pageList = lineEfficiencyFilerlossRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyFilerlossRecord
	 * @return
	 */
	@AutoLog(value = "酒损记录表-添加")
	@Operation(summary="酒损记录表-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_filerloss_record:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyFilerlossRecord lineEfficiencyFilerlossRecord) {
		lineEfficiencyFilerlossRecordService.save(lineEfficiencyFilerlossRecord);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyFilerlossRecord
	 * @return
	 */
	@AutoLog(value = "酒损记录表-编辑")
	@Operation(summary="酒损记录表-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_filerloss_record:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyFilerlossRecord lineEfficiencyFilerlossRecord) {
		lineEfficiencyFilerlossRecordService.updateById(lineEfficiencyFilerlossRecord);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "酒损记录表-通过id删除")
	@Operation(summary="酒损记录表-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_filerloss_record:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyFilerlossRecordService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "酒损记录表-批量删除")
	@Operation(summary="酒损记录表-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_filerloss_record:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyFilerlossRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="酒损记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyFilerlossRecord> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyFilerlossRecord lineEfficiencyFilerlossRecord = lineEfficiencyFilerlossRecordService.getById(id);
		if(lineEfficiencyFilerlossRecord==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyFilerlossRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyFilerlossRecord
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_filerloss_record:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyFilerlossRecord lineEfficiencyFilerlossRecord) {
        return super.exportXls(request, lineEfficiencyFilerlossRecord, LineEfficiencyFilerlossRecord.class, "酒损记录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_filerloss_record:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyFilerlossRecord.class);
    }

}
