package com.keytech.modules.line.efficiency.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecordPieceSku;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateRecordPieceSkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 线效清零分段-按照sku记录
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Tag(name="线效清零分段-按照sku记录")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyStateRecordPieceSku")
@Slf4j
public class LineEfficiencyStateRecordPieceSkuController extends BaseController<ILineEfficiencyStateRecordPieceSkuService, LineEfficiencyStateRecordPieceSku> {

	 private static final String QUERY_TYPE_EFFICIENCY = "1";
	@Autowired
	private ILineEfficiencyStateRecordPieceSkuService lineEfficiencyStateRecordPieceSkuService;
	
	/**
	 * 分页列表查询
	 *
	 * @param stateRecordPieceSku
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="线效清零分段-按照sku记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyStateRecordPieceSku>> queryPageList(LineEfficiencyStateRecordPieceSku stateRecordPieceSku,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {

		Date startTime = null;
		Date endTime = null;
		String lineCode = "";
		if(QUERY_TYPE_EFFICIENCY.equals(stateRecordPieceSku.getQueryType())){
			startTime = stateRecordPieceSku.getStartTime();
			endTime = stateRecordPieceSku.getEndTime();
			lineCode = stateRecordPieceSku.getLineCode();
			stateRecordPieceSku.setStartTime(null);
			stateRecordPieceSku.setEndTime(null);
			stateRecordPieceSku.setLineCode(null);
		}
        QueryWrapper<LineEfficiencyStateRecordPieceSku> queryWrapper = QueryGenerator.initQueryWrapper(stateRecordPieceSku, req.getParameterMap());
		Page<LineEfficiencyStateRecordPieceSku> page = new Page<LineEfficiencyStateRecordPieceSku>(pageNo, pageSize);
		if(QUERY_TYPE_EFFICIENCY.equals(stateRecordPieceSku.getQueryType())){
			//判断开始时间  结束时间 产线 是否为空
			if(null !=startTime && null != endTime && StrUtil.isNotBlank(lineCode)){
				//时间段
				queryWrapper.ge("start_time", startTime);
				queryWrapper.le("end_time", endTime);
				queryWrapper.eq("line_code", lineCode);
			}else{
				queryWrapper.eq("id","");
			}
		}
		IPage<LineEfficiencyStateRecordPieceSku> pageList = lineEfficiencyStateRecordPieceSkuService.page(page, queryWrapper);
		//计算OPI
		if(QUERY_TYPE_EFFICIENCY.equals(stateRecordPieceSku.getQueryType())){
			lineEfficiencyStateRecordPieceSkuService.calculateOpi(pageList);
		}
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyStateRecordPieceSku
	 * @return
	 */
	@AutoLog(value = "线效清零分段-按照sku记录-添加")
	@Operation(summary="线效清零分段-按照sku记录-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece_sku:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyStateRecordPieceSku lineEfficiencyStateRecordPieceSku) {
		lineEfficiencyStateRecordPieceSkuService.save(lineEfficiencyStateRecordPieceSku);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyStateRecordPieceSku
	 * @return
	 */
	@AutoLog(value = "线效清零分段-按照sku记录-编辑")
	@Operation(summary="线效清零分段-按照sku记录-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece_sku:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyStateRecordPieceSku lineEfficiencyStateRecordPieceSku) {
		lineEfficiencyStateRecordPieceSkuService.updateById(lineEfficiencyStateRecordPieceSku);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "线效清零分段-按照sku记录-通过id删除")
	@Operation(summary="线效清零分段-按照sku记录-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece_sku:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyStateRecordPieceSkuService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "线效清零分段-按照sku记录-批量删除")
	@Operation(summary="线效清零分段-按照sku记录-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece_sku:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyStateRecordPieceSkuService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="线效清零分段-按照sku记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyStateRecordPieceSku> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyStateRecordPieceSku lineEfficiencyStateRecordPieceSku = lineEfficiencyStateRecordPieceSkuService.getById(id);
		if(lineEfficiencyStateRecordPieceSku==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyStateRecordPieceSku);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param stateRecordPieceSku
    */
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece_sku:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyStateRecordPieceSku stateRecordPieceSku) {
        Date startTime = null;
        Date endTime = null;
        String lineCode = "";
        if(QUERY_TYPE_EFFICIENCY.equals(stateRecordPieceSku.getQueryType())){
            startTime = stateRecordPieceSku.getStartTime();
            endTime = stateRecordPieceSku.getEndTime();
            lineCode = stateRecordPieceSku.getLineCode();
            stateRecordPieceSku.setStartTime(null);
            stateRecordPieceSku.setEndTime(null);
            stateRecordPieceSku.setLineCode(null);
        }
        QueryWrapper<LineEfficiencyStateRecordPieceSku> queryWrapper = QueryGenerator.initQueryWrapper(stateRecordPieceSku, request.getParameterMap());
        if(QUERY_TYPE_EFFICIENCY.equals(stateRecordPieceSku.getQueryType())){
            if(null != startTime && null != endTime && StrUtil.isNotBlank(lineCode)){
                queryWrapper.ge("start_time", startTime);
                queryWrapper.le("end_time", endTime);
                queryWrapper.eq("line_code", lineCode);
            }else{
                queryWrapper.eq("id","");
            }
        }
        List<LineEfficiencyStateRecordPieceSku> exportList = lineEfficiencyStateRecordPieceSkuService.list(queryWrapper);
        //计算OPI
        if(QUERY_TYPE_EFFICIENCY.equals(stateRecordPieceSku.getQueryType())){
            Page<LineEfficiencyStateRecordPieceSku> page = new Page<>(1, exportList.size());
            page.setRecords(exportList);
            page.setTotal(exportList.size());
            lineEfficiencyStateRecordPieceSkuService.calculateOpi(page);
        }
        //获取当前登录用户
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "分SKU批次数据");
        mv.addObject(NormalExcelConstants.CLASS, LineEfficiencyStateRecordPieceSku.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("分SKU批次数据", "导出人:" + sysUser.getRealname(), "分SKU批次数据"));
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        return mv;
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_piece_sku:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyStateRecordPieceSku.class);
    }


	/**
	 *  速度编辑,重新计算SKU-OPI
	 * @param lineEfficiencyStateRecordPieceSku
	 * @return
	 */
	@AutoLog(value = "线效清零分段-按照sku记录-速度编辑")
	@Operation(summary="线效清零分段-按照sku记录-速度编辑")
	@RequestMapping(value = "/speedEdit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> speedEdit(@RequestBody LineEfficiencyStateRecordPieceSku lineEfficiencyStateRecordPieceSku) {
		lineEfficiencyStateRecordPieceSkuService.speedEdit(lineEfficiencyStateRecordPieceSku);
		return Result.OK("编辑成功!");
	}

}
