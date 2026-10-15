package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyEhsFactory;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyEhsFactoryService;
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
import java.util.List;

/**
 * @Description: 工厂对照表
 * @Author: jeecg-boot
 * @Date:   2025-06-17
 * @Version: V1.0
 */
@Tag(name="工厂对照表")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyEhsFactory")
@Slf4j
public class LineEfficiencyEhsFactoryController extends BaseController<ILineEfficiencyEhsFactoryService, LineEfficiencyEhsFactory> {
	@Autowired
	private ILineEfficiencyEhsFactoryService lineEfficiencyEhsFactoryService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyEhsFactory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="工厂对照表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyEhsFactory>> queryPageList(LineEfficiencyEhsFactory lineEfficiencyEhsFactory,
																 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
																 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
																 @RequestParam(name = "orderField",required = false) String orderField,
																 @RequestParam(name = "orderType",required = false) String orderType,
																 @RequestParam(name = "selectFields",required = false) String selectFields,
																 HttpServletRequest req) {
        QueryWrapper<LineEfficiencyEhsFactory> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyEhsFactory, req.getParameterMap());
		Page<LineEfficiencyEhsFactory> page = new Page<LineEfficiencyEhsFactory>(pageNo, pageSize);
		IPage<LineEfficiencyEhsFactory> pageList = lineEfficiencyEhsFactoryService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyEhsFactory
	 * @return
	 */
	@AutoLog(value = "工厂对照表-添加")
	@Operation(summary="工厂对照表-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyEhsFactory lineEfficiencyEhsFactory) {
		lineEfficiencyEhsFactoryService.save(lineEfficiencyEhsFactory);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑-切换
	 *
	 * @param lineEfficiencyEhsFactory
	 * @return
	 */
	@AutoLog(value = "工厂对照表-编辑")
	@Operation(summary="工厂对照表-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyEhsFactory lineEfficiencyEhsFactory) {

		//查询当前工厂
		List<LineEfficiencyEhsFactory> oldLineEfficiencyEhsFactoryList = lineEfficiencyEhsFactoryService.lambdaQuery().eq(LineEfficiencyEhsFactory::getStatus, "1").list();

		if(null != oldLineEfficiencyEhsFactoryList && oldLineEfficiencyEhsFactoryList.size() == 1){
			LineEfficiencyEhsFactory oldLineEfficiencyEhsFactory = oldLineEfficiencyEhsFactoryList.get(0);
			oldLineEfficiencyEhsFactory.setStatus("0");
			lineEfficiencyEhsFactoryService.updateById(oldLineEfficiencyEhsFactory);
		}

		String id = lineEfficiencyEhsFactory.getId();
		lineEfficiencyEhsFactory = lineEfficiencyEhsFactoryService.getById(id);
		lineEfficiencyEhsFactory.setStatus("1");
		lineEfficiencyEhsFactoryService.updateById(lineEfficiencyEhsFactory);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "工厂对照表-通过id删除")
	@Operation(summary="工厂对照表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyEhsFactoryService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "工厂对照表-批量删除")
	@Operation(summary="工厂对照表-批量删除")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyEhsFactoryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="工厂对照表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyEhsFactory> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyEhsFactory lineEfficiencyEhsFactory = lineEfficiencyEhsFactoryService.getById(id);
		if(lineEfficiencyEhsFactory==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyEhsFactory);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyEhsFactory
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyEhsFactory lineEfficiencyEhsFactory) {
        return super.exportXls(request, lineEfficiencyEhsFactory, LineEfficiencyEhsFactory.class, "工厂对照表");
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
        return super.importExcel(request, response, LineEfficiencyEhsFactory.class);
    }

}
