package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiAdapterAlgorithmCondition;
import com.keytech.modules.pi.service.IPiAdapterAlgorithmConditionService;
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
 * @Description: Adapter-算法约束条件
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Tag(name="Adapter-算法约束条件")
@RestController
@RequestMapping("/pi/piAdapterAlgorithmCondition")
@Slf4j
public class PiAdapterAlgorithmConditionController extends BaseController<IPiAdapterAlgorithmConditionService, PiAdapterAlgorithmCondition> {
	@Autowired
	private IPiAdapterAlgorithmConditionService piAdapterAlgorithmConditionService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piAdapterAlgorithmCondition
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Adapter-算法约束条件-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiAdapterAlgorithmCondition>> queryPageList(PiAdapterAlgorithmCondition piAdapterAlgorithmCondition,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiAdapterAlgorithmCondition> queryWrapper = QueryGenerator.initQueryWrapper(piAdapterAlgorithmCondition, req.getParameterMap());
		Page<PiAdapterAlgorithmCondition> page = new Page<PiAdapterAlgorithmCondition>(pageNo, pageSize);
		IPage<PiAdapterAlgorithmCondition> pageList = piAdapterAlgorithmConditionService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piAdapterAlgorithmCondition
	 * @return
	 */
	@AutoLog(value = "Adapter-算法约束条件-添加")
	@Operation(summary="Adapter-算法约束条件-添加")
	@RequiresPermissions("pi:mes3_pi_adapter_algorithm_condition:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAdapterAlgorithmCondition piAdapterAlgorithmCondition) {
		piAdapterAlgorithmConditionService.save(piAdapterAlgorithmCondition);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piAdapterAlgorithmCondition
	 * @return
	 */
	@AutoLog(value = "Adapter-算法约束条件-编辑")
	@Operation(summary="Adapter-算法约束条件-编辑")
	@RequiresPermissions("pi:mes3_pi_adapter_algorithm_condition:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAdapterAlgorithmCondition piAdapterAlgorithmCondition) {
		piAdapterAlgorithmConditionService.updateById(piAdapterAlgorithmCondition);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Adapter-算法约束条件-通过id删除")
	@Operation(summary="Adapter-算法约束条件-通过id删除")
	@RequiresPermissions("pi:mes3_pi_adapter_algorithm_condition:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piAdapterAlgorithmConditionService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Adapter-算法约束条件-批量删除")
	@Operation(summary="Adapter-算法约束条件-批量删除")
	@RequiresPermissions("pi:mes3_pi_adapter_algorithm_condition:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piAdapterAlgorithmConditionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Adapter-算法约束条件-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiAdapterAlgorithmCondition> queryById(@RequestParam(name="id",required=true) String id) {
		PiAdapterAlgorithmCondition piAdapterAlgorithmCondition = piAdapterAlgorithmConditionService.getById(id);
		if(piAdapterAlgorithmCondition==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piAdapterAlgorithmCondition);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piAdapterAlgorithmCondition
    */
    @RequiresPermissions("pi:mes3_pi_adapter_algorithm_condition:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiAdapterAlgorithmCondition piAdapterAlgorithmCondition) {
        return super.exportXls(request, piAdapterAlgorithmCondition, PiAdapterAlgorithmCondition.class, "Adapter-算法约束条件");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_adapter_algorithm_condition:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiAdapterAlgorithmCondition.class);
    }

}
