package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.ConsistencyConfigMx;
import com.keytech.modules.pi.service.IConsistencyConfigMxService;
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
 * @Description: 一次性指数配置明细表
 * @Author: jeecg-boot
 * @Date:   2025-01-13
 * @Version: V1.0
 */
@Tag(name="一次性指数配置明细表")
@RestController
@RequestMapping("/pi/consistencyConfigMx")
@Slf4j
public class ConsistencyConfigMxController extends BaseController<IConsistencyConfigMxService, ConsistencyConfigMx> {
	@Autowired
	private IConsistencyConfigMxService consistencyConfigMxService;
	
	/**
	 * 分页列表查询
	 *
	 * @param consistencyConfigMx
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="一次性指数配置明细表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ConsistencyConfigMx>> queryPageList(ConsistencyConfigMx consistencyConfigMx,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ConsistencyConfigMx> queryWrapper = QueryGenerator.initQueryWrapper(consistencyConfigMx, req.getParameterMap());
		Page<ConsistencyConfigMx> page = new Page<ConsistencyConfigMx>(pageNo, pageSize);
		IPage<ConsistencyConfigMx> pageList = consistencyConfigMxService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param consistencyConfigMx
	 * @return
	 */
	@AutoLog(value = "一次性指数配置明细表-添加")
	@Operation(summary="一次性指数配置明细表-添加")
	@RequiresPermissions("pi:mes3_consistency_config_mx:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ConsistencyConfigMx consistencyConfigMx) {
		consistencyConfigMxService.save(consistencyConfigMx);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param consistencyConfigMx
	 * @return
	 */
	@AutoLog(value = "一次性指数配置明细表-编辑")
	@Operation(summary="一次性指数配置明细表-编辑")
	@RequiresPermissions("pi:mes3_consistency_config_mx:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ConsistencyConfigMx consistencyConfigMx) {
		consistencyConfigMxService.updateById(consistencyConfigMx);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "一次性指数配置明细表-通过id删除")
	@Operation(summary="一次性指数配置明细表-通过id删除")
	@RequiresPermissions("pi:mes3_consistency_config_mx:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		consistencyConfigMxService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "一次性指数配置明细表-批量删除")
	@Operation(summary="一次性指数配置明细表-批量删除")
	@RequiresPermissions("pi:mes3_consistency_config_mx:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.consistencyConfigMxService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="一次性指数配置明细表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ConsistencyConfigMx> queryById(@RequestParam(name="id",required=true) String id) {
		ConsistencyConfigMx consistencyConfigMx = consistencyConfigMxService.getById(id);
		if(consistencyConfigMx==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(consistencyConfigMx);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param consistencyConfigMx
    */
    @RequiresPermissions("pi:mes3_consistency_config_mx:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ConsistencyConfigMx consistencyConfigMx) {
        return super.exportXls(request, consistencyConfigMx, ConsistencyConfigMx.class, "一次性指数配置明细表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_consistency_config_mx:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ConsistencyConfigMx.class);
    }

}
