package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.LineEfficiencySku;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 杭州线效需求开发
 * @Author: jeecg-boot
 * @Date:   2025-08-13
 * @Version: V1.0
 */
@Tag(name="sku配置")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencySku")
@Slf4j
public class LineEfficiencySkuController extends BaseController<ILineEfficiencySkuService, LineEfficiencySku> {
	private static final Logger logger = LoggerFactory.getLogger(LineEfficiencySkuController.class);
	@Autowired
	private ILineEfficiencySkuService lineEfficiencySkuService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencySku
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="sku配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencySku>> queryPageList(LineEfficiencySku lineEfficiencySku,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencySku> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencySku, req.getParameterMap());
		Page<LineEfficiencySku> page = new Page<LineEfficiencySku>(pageNo, pageSize);
		IPage<LineEfficiencySku> pageList = lineEfficiencySkuService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencySku
	 * @return
	 */
	@AutoLog(value = "sku配置-添加")
	@Operation(summary="sku配置-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencySku lineEfficiencySku) {
		lineEfficiencySkuService.save(lineEfficiencySku);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencySku
	 * @return
	 */
	@AutoLog(value = "sku配置-编辑")
	@Operation(summary="sku配置-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencySku lineEfficiencySku) {
		lineEfficiencySkuService.updateById(lineEfficiencySku);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "sku配置-通过id删除")
	@Operation(summary="sku配置-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencySkuService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "sku配置-批量删除")
	@Operation(summary="sku配置-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencySkuService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="sku配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencySku> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencySku lineEfficiencySku = lineEfficiencySkuService.getById(id);
		if(lineEfficiencySku==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencySku);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencySku
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_sku:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencySku lineEfficiencySku) {
        return super.exportXls(request, lineEfficiencySku, LineEfficiencySku.class, "sku配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line.efficiency:mes3_line_efficiency_sku:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencySku.class);
    }



	 /**
	  * 查询时间段内的产量信息
	  *
	  * @return
	  */
	 @AutoLog(value = "sku配置-查询时间段内的产量信息")
	 @Operation(summary="sku配置-查询时间段内的产量信息")
	 @PostMapping(value = "/queryDataProduction")
	 public Result<String> queryDataProduction() {
		 lineEfficiencySkuService.getLineEfficiencySkuListByHour();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

	 /**
	  * 根据小时来获取对应的产量及SKU的信息（小时定时任务）
	  *
	  */
	@AutoLog(value = "定时任务接口-根据小时来获取对应的产量及SKU的信息")
	@Operation(summary = "定时任务接口-根据小时来获取对应的产量及SKU的信息", description = "定时任务接口-根据小时来获取对应的产量及SKU的信息")
	@PostMapping(value = "/getLineEfficiencySkuListByHour")
	public Result<String> getLineEfficiencySkuListByHour(@RequestBody Map map) {
		logger.info("根据小时来获取对应的产量及SKU的信息执行---ok");
		lineEfficiencySkuService.getLineEfficiencySkuListByHour();
		logger.info("根据小时来获取对应的产量及SKU的信息推送---ok,");
		return Result.OK("调用成功！");
	}
}
