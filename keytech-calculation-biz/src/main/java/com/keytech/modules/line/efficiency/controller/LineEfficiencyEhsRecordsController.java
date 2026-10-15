package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyEhsRecords;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyEhsRecordsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @Description: 外排水-推送记录
 * @Author: jeecg-boot
 * @Date:   2025-06-17
 * @Version: V1.0
 */
@Tag(name="外排水-推送记录")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyEhsRecords")
@Slf4j
public class LineEfficiencyEhsRecordsController extends BaseController<ILineEfficiencyEhsRecordsService, LineEfficiencyEhsRecords> {
	@Autowired
	private ILineEfficiencyEhsRecordsService lineEfficiencyEhsRecordsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyEhsRecords
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="外排水-推送记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyEhsRecords>> queryPageList(LineEfficiencyEhsRecords lineEfficiencyEhsRecords,
																 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
																 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
																 @RequestParam(name = "orderField",required = false) String orderField,
																 @RequestParam(name = "orderType",required = false) String orderType,
																 @RequestParam(name = "selectFields",required = false) String selectFields,
																 HttpServletRequest req) {

		//做模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyEhsRecords.getParamName())){
			lineEfficiencyEhsRecords.setParamName("*" + lineEfficiencyEhsRecords.getParamName().trim() + "*");
		}

        QueryWrapper<LineEfficiencyEhsRecords> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyEhsRecords, req.getParameterMap());
		Page<LineEfficiencyEhsRecords> page = new Page<LineEfficiencyEhsRecords>(pageNo, pageSize);
		IPage<LineEfficiencyEhsRecords> pageList = lineEfficiencyEhsRecordsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyEhsRecords
	 * @return
	 */
	@AutoLog(value = "外排水-推送记录-添加")
	@Operation(summary="外排水-推送记录-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyEhsRecords lineEfficiencyEhsRecords) {
		lineEfficiencyEhsRecordsService.save(lineEfficiencyEhsRecords);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyEhsRecords
	 * @return
	 */
	@AutoLog(value = "外排水-推送记录-编辑")
	@Operation(summary="外排水-推送记录-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyEhsRecords lineEfficiencyEhsRecords) {
		lineEfficiencyEhsRecordsService.updateById(lineEfficiencyEhsRecords);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "外排水-推送记录-通过id删除")
	@Operation(summary="外排水-推送记录-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyEhsRecordsService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "外排水-推送记录-批量删除")
	@Operation(summary="外排水-推送记录-批量删除")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyEhsRecordsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="外排水-推送记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyEhsRecords> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyEhsRecords lineEfficiencyEhsRecords = lineEfficiencyEhsRecordsService.getById(id);
		if(lineEfficiencyEhsRecords==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyEhsRecords);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyEhsRecords
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyEhsRecords lineEfficiencyEhsRecords) {
        return super.exportXls(request, lineEfficiencyEhsRecords, LineEfficiencyEhsRecords.class, "外排水-推送记录");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyEhsRecords.class);
    }

}
