package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.pi.entity.PiResultTransformationModelDetail;
import com.keytech.modules.pi.service.IPiResultTransformationModelDetailService;
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
import java.util.List;

/**
 * @Description: PI-结果转换模型配置
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
@Tag(name="PI-结果转换模型配置")
@RestController
@RequestMapping("/pi/piResultTransformationModelDetail")
@Slf4j
public class PiResultTransformationModelDetailController extends BaseController<IPiResultTransformationModelDetailService, PiResultTransformationModelDetail> {
	@Autowired
	private IPiResultTransformationModelDetailService piResultTransformationModelDetailService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piResultTransformationModelDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI-结果转换模型配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiResultTransformationModelDetail>> queryPageList(PiResultTransformationModelDetail piResultTransformationModelDetail,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiResultTransformationModelDetail> queryWrapper = QueryGenerator.initQueryWrapper(piResultTransformationModelDetail, req.getParameterMap());
		Page<PiResultTransformationModelDetail> page = new Page<PiResultTransformationModelDetail>(pageNo, pageSize);
		IPage<PiResultTransformationModelDetail> pageList = piResultTransformationModelDetailService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piResultTransformationModelDetail
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-添加")
	@Operation(summary="PI-结果转换模型配置-添加")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model_detail:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiResultTransformationModelDetail piResultTransformationModelDetail) {
		piResultTransformationModelDetailService.save(piResultTransformationModelDetail);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piResultTransformationModelDetail
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-编辑")
	@Operation(summary="PI-结果转换模型配置-编辑")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model_detail:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiResultTransformationModelDetail piResultTransformationModelDetail) {
		piResultTransformationModelDetailService.updateById(piResultTransformationModelDetail);
		return Result.OK("编辑成功!");
	}

	 /**
	  *  更新/编辑
	  *
	  * @param piResultTransformationModelDetail
	  * @return
	  */
	 @AutoLog(value = "PI-结果转换模型配置-编辑")
	 @Operation(summary="PI-结果转换模型配置-编辑")
	 @RequiresPermissions("pi:mes3_pi_result_transformation_model:detail")
	 @RequestMapping(value = "/saveOrUpdate", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> saveOrUpdate(@RequestBody PiResultTransformationModelDetail piResultTransformationModelDetail) {
		 baseService.saveOrUpdateData(piResultTransformationModelDetail);
		 return Result.OK("保存成功!");
	 }
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-通过id删除")
	@Operation(summary="PI-结果转换模型配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model_detail:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piResultTransformationModelDetailService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-批量删除")
	@Operation(summary="PI-结果转换模型配置-批量删除")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model_detail:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piResultTransformationModelDetailService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI-结果转换模型配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiResultTransformationModelDetail> queryById(@RequestParam(name="id",required=true) String id) {
		PiResultTransformationModelDetail piResultTransformationModelDetail = piResultTransformationModelDetailService.getById(id);
		if(piResultTransformationModelDetail==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piResultTransformationModelDetail);
	}


	 /**
	  *  获取取数规则
	  * @param modelId
	  * @return
	  */
	 @AutoLog(value = "获取取数规则")
	 @Operation(summary="获取取数规则")
	 @GetMapping(value = "/getDetailData")
	 public Result<?> getDetailData(@RequestParam(name="modelId",required=true) String modelId) {
		 LambdaQueryWrapper<PiResultTransformationModelDetail> queryWrapper = new LambdaQueryWrapper<>();
		 //算法id
		 queryWrapper.eq(PiResultTransformationModelDetail::getModelId, modelId);
		 List<PiResultTransformationModelDetail> list = baseService.list(queryWrapper);
		 return Result.OK(list);
	 }
}
