package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecordPiece;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateRecordPieceService;
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
 * @Description: 线效-清零分段
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Tag(name="线效-清零分段")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyStateRecordPiece")
@Slf4j
public class LineEfficiencyStateRecordPieceController extends BaseController<ILineEfficiencyStateRecordPieceService, LineEfficiencyStateRecordPiece> {
	@Autowired
	private ILineEfficiencyStateRecordPieceService lineEfficiencyStateRecordPieceService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyStateRecordPiece
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="线效-清零分段-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyStateRecordPiece>> queryPageList(LineEfficiencyStateRecordPiece lineEfficiencyStateRecordPiece,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencyStateRecordPiece> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyStateRecordPiece, req.getParameterMap());
		Page<LineEfficiencyStateRecordPiece> page = new Page<LineEfficiencyStateRecordPiece>(pageNo, pageSize);
		IPage<LineEfficiencyStateRecordPiece> pageList = lineEfficiencyStateRecordPieceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyStateRecordPiece
	 * @return
	 */
	@AutoLog(value = "线效-清零分段-添加")
	@Operation(summary="线效-清零分段-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyStateRecordPiece lineEfficiencyStateRecordPiece) {
		lineEfficiencyStateRecordPieceService.save(lineEfficiencyStateRecordPiece);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyStateRecordPiece
	 * @return
	 */
	@AutoLog(value = "线效-清零分段-编辑")
	@Operation(summary="线效-清零分段-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyStateRecordPiece lineEfficiencyStateRecordPiece) {
		lineEfficiencyStateRecordPieceService.updateById(lineEfficiencyStateRecordPiece);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "线效-清零分段-通过id删除")
	@Operation(summary="线效-清零分段-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyStateRecordPieceService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "线效-清零分段-批量删除")
	@Operation(summary="线效-清零分段-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyStateRecordPieceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="线效-清零分段-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyStateRecordPiece> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyStateRecordPiece lineEfficiencyStateRecordPiece = lineEfficiencyStateRecordPieceService.getById(id);
		if(lineEfficiencyStateRecordPiece==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyStateRecordPiece);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyStateRecordPiece
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyStateRecordPiece lineEfficiencyStateRecordPiece) {
        return super.exportXls(request, lineEfficiencyStateRecordPiece, LineEfficiencyStateRecordPiece.class, "线效-清零分段");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyStateRecordPiece.class);
    }

}
