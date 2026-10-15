package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiResultTransformationModel;
import com.keytech.modules.pi.entity.PiResultTransformationModelDetail;
import com.keytech.modules.pi.service.IPiResultTransformationModelService;
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
import java.util.List;

/**
 * @Description: PI-结果转换模型配置-主表
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
@Tag(name="PI-结果转换模型配置-主表")
@RestController
@RequestMapping("/pi/piResultTransformationModel")
@Slf4j
public class PiResultTransformationModelController extends BaseController<IPiResultTransformationModelService, PiResultTransformationModel> {
	@Autowired
	private IPiResultTransformationModelService piResultTransformationModelService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piResultTransformationModel
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI-结果转换模型配置-主表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiResultTransformationModel>> queryPageList(PiResultTransformationModel piResultTransformationModel,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiResultTransformationModel> queryWrapper = QueryGenerator.initQueryWrapper(piResultTransformationModel, req.getParameterMap());
		Page<PiResultTransformationModel> page = new Page<PiResultTransformationModel>(pageNo, pageSize);
		IPage<PiResultTransformationModel> pageList = piResultTransformationModelService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piResultTransformationModel
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-主表-添加")
	@Operation(summary="PI-结果转换模型配置-主表-添加")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiResultTransformationModel piResultTransformationModel) {
		//判断名称是否存在
		piResultTransformationModelService.saveOrUpdateData(piResultTransformationModel);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piResultTransformationModel
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-主表-编辑")
	@Operation(summary="PI-结果转换模型配置-主表-编辑")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiResultTransformationModel piResultTransformationModel) {
		piResultTransformationModelService.saveOrUpdateData(piResultTransformationModel);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-主表-通过id删除")
	@Operation(summary="PI-结果转换模型配置-主表-通过id删除")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
//		piResultTransformationModelService.removeById(id);
		piResultTransformationModelService.delete(Arrays.asList(id));
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-主表-批量删除")
	@Operation(summary="PI-结果转换模型配置-主表-批量删除")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
//		this.piResultTransformationModelService.removeByIds(Arrays.asList(ids.split(",")));
		this.piResultTransformationModelService.delete(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI-结果转换模型配置-主表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiResultTransformationModel> queryById(@RequestParam(name="id",required=true) String id) {
		PiResultTransformationModel piResultTransformationModel = piResultTransformationModelService.getById(id);
		if(piResultTransformationModel==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piResultTransformationModel);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piResultTransformationModel
    */
    @RequiresPermissions("pi:mes3_pi_result_transformation_model:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiResultTransformationModel piResultTransformationModel) {
        return super.exportXls(request, piResultTransformationModel, PiResultTransformationModel.class, "PI-结果转换模型配置-主表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_result_transformation_model:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiResultTransformationModel.class);
    }

	 /**
	  * 列表查询
	  *
	  * @param orderField
	  * @param orderType
	  * @return
	  */
	 @Operation(summary="PI-结果转换模型配置-列表查询")
	 @GetMapping(value = "/getResultModel")
	 public Result<List<PiResultTransformationModel>> getResultModel(@RequestParam(name = "orderField",required = false) String orderField,
																	@RequestParam(name = "orderType",required = false) String orderType) {
		 QueryWrapper<PiResultTransformationModel> queryWrapper = new QueryWrapper<>();
		 // 修复空指针：先判断 orderType 是否为 null
		 if (StringUtils.isNotBlank(orderField) && StringUtils.isNotBlank(orderType)) {
			 boolean isAsc = "asc".equalsIgnoreCase(orderType);
			 queryWrapper.orderBy(true, isAsc, orderField);
		 }
		 return Result.OK(baseService.list(queryWrapper));
	 }
}
