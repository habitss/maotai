package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.ConsistencyConfigZb;
import com.keytech.modules.pi.service.IConsistencyConfigZbService;
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
 * @Description: 一次性指数配置主表
 * @Author: jeecg-boot
 * @Date:   2025-01-13
 * @Version: V1.0
 */
@Tag(name="一次性指数配置主表")
@RestController
@RequestMapping("/pi/consistencyConfigZb")
@Slf4j
public class ConsistencyConfigZbController extends BaseController<IConsistencyConfigZbService, ConsistencyConfigZb> {
	@Autowired
	private IConsistencyConfigZbService consistencyConfigZbService;
	
	/**
	 * 分页列表查询
	 *
	 * @param consistencyConfigZb
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="一次性指数配置主表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ConsistencyConfigZb>> queryPageList(ConsistencyConfigZb consistencyConfigZb,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ConsistencyConfigZb> queryWrapper = QueryGenerator.initQueryWrapper(consistencyConfigZb, req.getParameterMap());
		Page<ConsistencyConfigZb> page = new Page<ConsistencyConfigZb>(pageNo, pageSize);
		IPage<ConsistencyConfigZb> pageList = consistencyConfigZbService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param consistencyConfigZb
	 * @return
	 */
	@AutoLog(value = "一次性指数配置主表-添加")
	@Operation(summary="一次性指数配置主表-添加")
	@RequiresPermissions("pi:mes3_consistency_config_zb:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ConsistencyConfigZb consistencyConfigZb) {
		consistencyConfigZbService.save(consistencyConfigZb);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param consistencyConfigZb
	 * @return
	 */
	@AutoLog(value = "一次性指数配置主表-编辑")
	@Operation(summary="一次性指数配置主表-编辑")
	@RequiresPermissions("pi:mes3_consistency_config_zb:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ConsistencyConfigZb consistencyConfigZb) {
		consistencyConfigZbService.updateById(consistencyConfigZb);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "一次性指数配置主表-通过id删除")
	@Operation(summary="一次性指数配置主表-通过id删除")
	@RequiresPermissions("pi:mes3_consistency_config_zb:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		consistencyConfigZbService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "一次性指数配置主表-批量删除")
	@Operation(summary="一次性指数配置主表-批量删除")
	@RequiresPermissions("pi:mes3_consistency_config_zb:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.consistencyConfigZbService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="一次性指数配置主表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ConsistencyConfigZb> queryById(@RequestParam(name="id",required=true) String id) {
		ConsistencyConfigZb consistencyConfigZb = consistencyConfigZbService.getById(id);
		if(consistencyConfigZb==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(consistencyConfigZb);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param consistencyConfigZb
    */
    @RequiresPermissions("pi:mes3_consistency_config_zb:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ConsistencyConfigZb consistencyConfigZb) {
        return super.exportXls(request, consistencyConfigZb, ConsistencyConfigZb.class, "一次性指数配置主表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_consistency_config_zb:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ConsistencyConfigZb.class);
    }

}
