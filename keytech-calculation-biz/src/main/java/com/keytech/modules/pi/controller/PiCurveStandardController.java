package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiCurveStandard;
import com.keytech.modules.pi.entity.PiCurveStandardConfig;
import com.keytech.modules.pi.entity.PiCurveStandardGroup;
import com.keytech.modules.pi.entity.PiCurveStandardRecipe;
import com.keytech.modules.pi.service.IPiCurveStandardConfigService;
import com.keytech.modules.pi.service.IPiCurveStandardGroupService;
import com.keytech.modules.pi.service.IPiCurveStandardRecipeService;
import com.keytech.modules.pi.service.IPiCurveStandardService;
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
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 曲线标准配置
 * @Author: jeecg-boot
 * @Date:   2026-01-22
 * @Version: V1.0
 */
@Tag(name="曲线标准配置")
@RestController
@RequestMapping("/pi/piCurveStandard")
@Slf4j
public class PiCurveStandardController extends BaseController<IPiCurveStandardService, PiCurveStandard> {
	@Autowired
	private IPiCurveStandardService piCurveStandardService;
	@Autowired
	private IPiCurveStandardRecipeService piCurveStandardRecipeService;
	@Autowired
	private IPiCurveStandardGroupService piCurveStandardGroupService;

	@Autowired
	private IPiCurveStandardConfigService piCurveStandardConfigService;
	/**
	 * 分页列表查询
	 *
	 * @param piCurveStandard
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="曲线标准配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiCurveStandard>> queryPageList(PiCurveStandard piCurveStandard,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiCurveStandard> queryWrapper = QueryGenerator.initQueryWrapper(piCurveStandard, req.getParameterMap());
		Page<PiCurveStandard> page = new Page<PiCurveStandard>(pageNo, pageSize);
		IPage<PiCurveStandard> pageList = piCurveStandardService.page(page, queryWrapper);
		if(pageList.getRecords() != null && pageList.getRecords().size() > 0){
			//参数转换
			baseService.convert(pageList);
		}
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piCurveStandard
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-添加")
	@Operation(summary="曲线标准配置-添加")
	@RequiresPermissions("pi:mes3_pi_curve_standard:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiCurveStandard piCurveStandard) {
		piCurveStandardService.save(piCurveStandard);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piCurveStandard
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-编辑")
	@Operation(summary="曲线标准配置-编辑")
	@RequiresPermissions("pi:mes3_pi_curve_standard:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiCurveStandard piCurveStandard) {
		piCurveStandardService.updateById(piCurveStandard);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-通过id删除")
	@Operation(summary="曲线标准配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_curve_standard:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piCurveStandardService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-批量删除")
	@Operation(summary="曲线标准配置-批量删除")
	@RequiresPermissions("pi:mes3_pi_curve_standard:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piCurveStandardService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="曲线标准配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiCurveStandard> queryById(@RequestParam(name="id",required=true) String id) {
		PiCurveStandard piCurveStandard = piCurveStandardService.getById(id);
		if(piCurveStandard==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piCurveStandard);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piCurveStandard
    */
    @RequiresPermissions("pi:mes3_pi_curve_standard:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiCurveStandard piCurveStandard) {
        return super.exportXls(request, piCurveStandard, PiCurveStandard.class, "曲线标准配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_curve_standard:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiCurveStandard.class);
    }


	 /**
	  * 获取设备
	  * @param piCurveStandard
	  * @return
	  */
	 @Operation(summary="获取设备")
	 @GetMapping(value = "/getDevice")
	 public Result<?> getDevice(@ModelAttribute PiCurveStandard piCurveStandard) {
		 return Result.OK(baseService.getDevice(piCurveStandard));
	 }

	 /**
	  * 获取配方
	  * @param piCurveStandard
	  * @return
	  */
	 @Operation(summary="获取配方")
	 @GetMapping(value = "/getRecipe")
	 public Result<?> getRecipe(@ModelAttribute PiCurveStandard piCurveStandard) {
		 return Result.OK(baseService.getRecipe(piCurveStandard));
	 }

	 /**
	  * 获取步骤
	  * @param piCurveStandard
	  * @return
	  */
	 @Operation(summary="获取步骤")
	 @GetMapping(value = "/getStep")
	 public Result<?> getStep(@ModelAttribute PiCurveStandard piCurveStandard) {
		 return Result.OK(baseService.getStep(piCurveStandard));
	 }




	 @Operation(summary="曲线标准配方-分页列表查询")
	 @GetMapping(value = "/recipeQueryPageList")
	 @AutoDict
	 public Result<IPage<PiCurveStandardRecipe>> queryPageList(PiCurveStandardRecipe piCurveStandardRecipe,
															   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															   @RequestParam(name = "orderField",required = false) String orderField,
															   @RequestParam(name = "orderType",required = false) String orderType,
															   @RequestParam(name = "selectFields",required = false) String selectFields,
															   HttpServletRequest req) {
		 QueryWrapper<PiCurveStandardRecipe> queryWrapper = QueryGenerator.initQueryWrapper(piCurveStandardRecipe, req.getParameterMap());
		 Page<PiCurveStandardRecipe> page = new Page<PiCurveStandardRecipe>(pageNo, pageSize);
		 IPage<PiCurveStandardRecipe> pageList = piCurveStandardRecipeService.page(page, queryWrapper);
		 if(pageList.getRecords() != null && pageList.getRecords().size() > 0){
			 //参数转换
			 baseService.recipeConvert(pageList);
		 }
		 return Result.OK(pageList);
	 }

	 /**
	  *   标准-配方通过id删除
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "标准-配方通过id删除")
	 @Operation(summary="标准-配方通过id删除")
	 //@RequiresPermissions("pi:mes3_pi_curve_standard:delete")
	 @DeleteMapping(value = "/recipeDelete")
	 public Result<String> recipeDelete(@RequestParam(name="id",required=true) String id) {
		 piCurveStandardRecipeService.recipeDelete(id);
		 return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	 }
	/**
	 *   配方-新增/修改
	 *
	 * @param piCurveStandardRecipe
	 * @return
	 */
	@AutoLog(value = "配方-新增/修改")
	@Operation(summary="配方-新增/修改")
	@RequestMapping(value = "/recipeSaveOrUpdate", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> recipeSaveOrUpdate(@RequestBody PiCurveStandardRecipe piCurveStandardRecipe) {
		return piCurveStandardRecipeService.recipeSaveOrUpdate(piCurveStandardRecipe);
	}

	 /**
	  * 标准-设备分类获取
	  * @param piCurveStandardGroup
	  * @return
	  */
	 @Operation(summary="标准-设备分类获取")
	 @GetMapping(value = "/loadDeviceGroupData")
	 public Result<?> loadDeviceGroupData(@ModelAttribute PiCurveStandardGroup piCurveStandardGroup) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = new ArrayList<>();
			 LambdaQueryWrapper<PiCurveStandardGroup> queryWrapper = new LambdaQueryWrapper<>();
			 queryWrapper.eq(PiCurveStandardGroup::getRecipeId, piCurveStandardGroup.getRecipeId());
			 List<PiCurveStandardGroup> list = piCurveStandardGroupService.list(queryWrapper);
			 for (PiCurveStandardGroup curveStandardGroup : list) {
				 SelectTreeModel selectTreeModel = new SelectTreeModel();
				 selectTreeModel.setKey(curveStandardGroup.getId());
				 selectTreeModel.setTitle(curveStandardGroup.getGroupName());
				 selectTreeModel.setValue(curveStandardGroup.getId());
				 selectTreeModel.setParentId(null);
				 selectTreeModel.setLeaf(false);
				 ls.add(selectTreeModel);
			 }
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }



	/**
	 *   标准-设备分组通过id删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "标准-设备分组通过id删除")
	@Operation(summary="标准-设备分组通过id删除")
	@DeleteMapping(value = "/groupDelete")
	public Result<String> groupDelete(@RequestParam(name="ids",required=true) String ids) {
		piCurveStandardGroupService.groupDelete(Arrays.asList(ids.split(",")));
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  编辑
	 *
	 * @param piCurveStandardGroup
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-编辑")
	@Operation(summary="曲线标准配置-编辑")
	@RequestMapping(value = "/groupSaveOrUpdate", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> groupSaveOrUpdate(@RequestBody PiCurveStandardGroup piCurveStandardGroup) {
		piCurveStandardGroupService.saveOrUpdate(piCurveStandardGroup);
		return Result.OK("编辑成功!");
	}


	//曲线标准配置查询
	@Operation(summary="曲线标准配置查询-分页列表查询")
	@GetMapping(value = "/configQueryPageList")
	@AutoDict
	public Result<IPage<PiCurveStandardConfig>> queryPageList(PiCurveStandardConfig piCurveStandardConfig,
															  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															  @RequestParam(name = "orderField",required = false) String orderField,
															  @RequestParam(name = "orderType",required = false) String orderType,
															  @RequestParam(name = "selectFields",required = false) String selectFields,
															  HttpServletRequest req) {
		QueryWrapper<PiCurveStandardConfig> queryWrapper = QueryGenerator.initQueryWrapper(piCurveStandardConfig, req.getParameterMap());
		Page<PiCurveStandardConfig> page = new Page<PiCurveStandardConfig>(pageNo, pageSize);
		IPage<PiCurveStandardConfig> pageList = piCurveStandardConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 *   曲线标准配置添加
	 *
	 * @param piCurveStandardConfig
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-添加")
	@Operation(summary="曲线标准配置-添加")
	@PostMapping(value = "/configAdd")
	public Result<String> add(@RequestBody PiCurveStandardConfig piCurveStandardConfig) {
		piCurveStandardConfigService.standardSave(piCurveStandardConfig);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  曲线标准配置编辑
	 *
	 * @param piCurveStandardConfig
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-编辑")
	@Operation(summary="曲线标准配置-编辑")
	@RequestMapping(value = "/configEdit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> configEdit(@RequestBody PiCurveStandardConfig piCurveStandardConfig) {
		piCurveStandardConfigService.configEdit(piCurveStandardConfig);
		return Result.OK("编辑成功!");
	}

	/**
	 *   曲线标准配置通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-通过id删除")
	@Operation(summary="曲线标准配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_curve_standard:delete")
	@DeleteMapping(value = "/configDelete")
	public Result<String> configDelete(@RequestParam(name="id",required=true) String id) {
		piCurveStandardConfigService.configDelete(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  曲线标准配置批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-批量删除")
	@Operation(summary="曲线标准配置-批量删除")
	@DeleteMapping(value = "/configDeletionBatch")
	public Result<String> configDeletionBatch(@RequestParam(name="ids",required=true) String ids) {
		piCurveStandardConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 *  曲线标准配置批量复制
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "曲线标准配置-批量复制")
	@Operation(summary="曲线标准配置-批量复制")
	@PostMapping(value = "/configBatchCopy")
	public Result<String> configBatchCopy(@RequestParam(name="ids",required=true) String ids,
										  @RequestParam(name="copyNum", defaultValue="1") Integer copyNum) {
		piCurveStandardConfigService.configBatchCopy(Arrays.asList(ids.split(",")), copyNum);
		return Result.OK("复制成功!");
	}


	/**
	 * 定时任务接口-曲线标准基础数据抽取(黑龙江)
	 */
	@AutoLog(value = "定时任务接口-曲线标准基础数据抽取")
	@Operation(summary = "定时任务接口-曲线标准基础数据抽取", description = "定时任务接口-曲线标准基础数据抽取")
	@PostMapping(value = "/standardExtractData")
	public Result<String> standardExtractData(@RequestBody Map map) {
		log.info("上位机系统手动操作记录取开始执行---ok");
		piCurveStandardConfigService.standardExtractData();
		log.info("上位机系统手动操作记录取执行完成---ok,");
		return Result.OK("调用成功！");
	}


}
