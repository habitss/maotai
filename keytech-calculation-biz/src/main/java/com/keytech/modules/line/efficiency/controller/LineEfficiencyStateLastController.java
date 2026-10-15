package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateLast;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateLastService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
 * @Description: 线效状态记录表-最新记录
 * @Author: jeecg-boot
 * @Date:   2025-06-05
 * @Version: V1.0
 */
@Tag(name="线效状态记录表-最新记录")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyStateLast")
@Slf4j
public class LineEfficiencyStateLastController extends BaseController<ILineEfficiencyStateLastService, LineEfficiencyStateLast> {
	@Autowired
	private ILineEfficiencyStateLastService lineEfficiencyStateLastService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyStateLast
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="线效状态记录表-最新记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyStateLast>> queryPageList(LineEfficiencyStateLast lineEfficiencyStateLast,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencyStateLast> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyStateLast, req.getParameterMap());
		Page<LineEfficiencyStateLast> page = new Page<LineEfficiencyStateLast>(pageNo, pageSize);
		IPage<LineEfficiencyStateLast> pageList = lineEfficiencyStateLastService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyStateLast
	 * @return
	 */
	@AutoLog(value = "线效状态记录表-最新记录-添加")
	@Operation(summary="线效状态记录表-最新记录-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyStateLast lineEfficiencyStateLast) {
		lineEfficiencyStateLastService.save(lineEfficiencyStateLast);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyStateLast
	 * @return
	 */
	@AutoLog(value = "线效状态记录表-最新记录-编辑")
	@Operation(summary="线效状态记录表-最新记录-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyStateLast lineEfficiencyStateLast) {
		lineEfficiencyStateLastService.updateById(lineEfficiencyStateLast);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "线效状态记录表-最新记录-通过id删除")
	@Operation(summary="线效状态记录表-最新记录-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyStateLastService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "线效状态记录表-最新记录-批量删除")
	@Operation(summary="线效状态记录表-最新记录-批量删除")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyStateLastService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="线效状态记录表-最新记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyStateLast> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyStateLast lineEfficiencyStateLast = lineEfficiencyStateLastService.getById(id);
		if(lineEfficiencyStateLast==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyStateLast);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyStateLast
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyStateLast lineEfficiencyStateLast) {
        return super.exportXls(request, lineEfficiencyStateLast, LineEfficiencyStateLast.class, "线效状态记录表-最新记录");
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
        return super.importExcel(request, response, LineEfficiencyStateLast.class);
    }

}
