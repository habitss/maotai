package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyEhsPoints;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyEhsPointsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @Description: 外排水点位配置
 * @Author: jeecg-boot
 * @Date:   2025-07-29
 * @Version: V1.0
 */
@Tag(name="外排水点位配置")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyEhsPoints")
@Slf4j
public class LineEfficiencyEhsPointsController extends BaseController<ILineEfficiencyEhsPointsService, LineEfficiencyEhsPoints> {
	@Autowired
	private ILineEfficiencyEhsPointsService lineEfficiencyEhsPointsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyEhsPoints
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="外排水点位配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyEhsPoints>> queryPageList(LineEfficiencyEhsPoints lineEfficiencyEhsPoints,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {

		if(StringUtils.isNoneBlank(lineEfficiencyEhsPoints.getCode())){
			lineEfficiencyEhsPoints.setCode("*" + lineEfficiencyEhsPoints.getCode().trim() + "*");
		}
		if(StringUtils.isNoneBlank(lineEfficiencyEhsPoints.getName())){
			lineEfficiencyEhsPoints.setName("*" + lineEfficiencyEhsPoints.getName().trim() + "*");
		}
		if(StringUtils.isNoneBlank(lineEfficiencyEhsPoints.getPointCode())){
			lineEfficiencyEhsPoints.setPointCode("*" + lineEfficiencyEhsPoints.getPointCode().trim() + "*");
		}
		if(StringUtils.isNoneBlank(lineEfficiencyEhsPoints.getPointName())){
			lineEfficiencyEhsPoints.setPointName("*" + lineEfficiencyEhsPoints.getPointName().trim() + "*");
		}

        QueryWrapper<LineEfficiencyEhsPoints> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyEhsPoints, req.getParameterMap());
		queryWrapper.orderByDesc("point_code");
		Page<LineEfficiencyEhsPoints> page = new Page<LineEfficiencyEhsPoints>(pageNo, pageSize);
		IPage<LineEfficiencyEhsPoints> pageList = lineEfficiencyEhsPointsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyEhsPoints
	 * @return
	 */
	@AutoLog(value = "外排水点位配置-添加")
	@Operation(summary="外排水点位配置-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyEhsPoints lineEfficiencyEhsPoints) {
		lineEfficiencyEhsPointsService.save(lineEfficiencyEhsPoints);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyEhsPoints
	 * @return
	 */
	@AutoLog(value = "外排水点位配置-编辑")
	@Operation(summary="外排水点位配置-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyEhsPoints lineEfficiencyEhsPoints) {
		lineEfficiencyEhsPointsService.updateById(lineEfficiencyEhsPoints);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "外排水点位配置-通过id删除")
	@Operation(summary="外排水点位配置-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyEhsPointsService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "外排水点位配置-批量删除")
	@Operation(summary="外排水点位配置-批量删除")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyEhsPointsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="外排水点位配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyEhsPoints> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyEhsPoints lineEfficiencyEhsPoints = lineEfficiencyEhsPointsService.getById(id);
		if(lineEfficiencyEhsPoints==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyEhsPoints);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyEhsPoints
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_ehs_points:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyEhsPoints lineEfficiencyEhsPoints) {
        return super.exportXls(request, lineEfficiencyEhsPoints, LineEfficiencyEhsPoints.class, "外排水点位配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_ehs_points:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyEhsPoints.class);
    }

}
