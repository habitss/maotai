package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.entity.ConsistencyConfigMx;
import com.keytech.modules.pi.entity.ConsistencyConfigPi;
import com.keytech.modules.pi.service.IConsistencyConfigMxService;
import com.keytech.modules.pi.service.IConsistencyConfigPiService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
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

import java.util.Arrays;

/**
 * @Description: 一致性指数配置PI表
 * @Author: jeecg-boot
 * @Date:   2025-01-13
 * @Version: V1.0
 */
@Tag(name="一致性指数配置PI表")
@RestController
@RequestMapping("/pi/consistencyConfigPi")
@Slf4j
public class ConsistencyConfigPiController extends BaseController<IConsistencyConfigPiService, ConsistencyConfigPi> {
	@Autowired
	private IConsistencyConfigPiService consistencyConfigPiService;

	@Autowired
	private IPiCustomizeTableFieldService piCustomizeTableFieldService;

	@Autowired
	private IConsistencyConfigMxService consistencyConfigMxService;
	
	/**
	 * 分页列表查询
	 *
	 * @param consistencyConfigPi
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="一致性指数配置PI表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ConsistencyConfigPi>> queryPageList(ConsistencyConfigPi consistencyConfigPi,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ConsistencyConfigPi> queryWrapper = QueryGenerator.initQueryWrapper(consistencyConfigPi, req.getParameterMap());
		Page<ConsistencyConfigPi> page = new Page<ConsistencyConfigPi>(pageNo, pageSize);
		IPage<ConsistencyConfigPi> pageList = consistencyConfigPiService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param consistencyConfigPi
	 * @return
	 */
	@AutoLog(value = "一致性指数配置PI表-添加")
	@Operation(summary="一致性指数配置PI表-添加")
	@RequiresPermissions("pi:mes3_consistency_config_pi:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ConsistencyConfigPi consistencyConfigPi) {
		consistencyConfigPiService.save(consistencyConfigPi);
		consistencyConfigMxService.lambdaUpdate().eq(ConsistencyConfigMx::getId, consistencyConfigPi.getMxId())
				.setSql("pi_total = pi_total+1").update();
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param consistencyConfigPi
	 * @return
	 */
	@AutoLog(value = "一致性指数配置PI表-编辑")
	@Operation(summary="一致性指数配置PI表-编辑")
	@RequiresPermissions("pi:mes3_consistency_config_pi:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ConsistencyConfigPi consistencyConfigPi) {
		consistencyConfigPiService.updateById(consistencyConfigPi);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "一致性指数配置PI表-通过id删除")
	@Operation(summary="一致性指数配置PI表-通过id删除")
	@RequiresPermissions("pi:mes3_consistency_config_pi:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id, String mxId) {
		consistencyConfigPiService.removeById(id);
		consistencyConfigMxService.lambdaUpdate().eq(ConsistencyConfigMx::getId, mxId)
				.setSql("pi_total = pi_total-1").update();
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "一致性指数配置PI表-批量删除")
	@Operation(summary="一致性指数配置PI表-批量删除")
	@RequiresPermissions("pi:mes3_consistency_config_pi:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.consistencyConfigPiService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="一致性指数配置PI表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ConsistencyConfigPi> queryById(@RequestParam(name="id",required=true) String id) {
		ConsistencyConfigPi consistencyConfigPi = consistencyConfigPiService.getById(id);
		if(consistencyConfigPi==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(consistencyConfigPi);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param consistencyConfigPi
    */
    @RequiresPermissions("pi:mes3_consistency_config_pi:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ConsistencyConfigPi consistencyConfigPi) {
        return super.exportXls(request, consistencyConfigPi, ConsistencyConfigPi.class, "一致性指数配置PI表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_consistency_config_pi:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ConsistencyConfigPi.class);
    }
}
