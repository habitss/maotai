package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.dto.SinicizedDictDto;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * &#064;Description:  pi自定义报表字段
 * &#064;Author: jeecg-boot
 * &#064;Date:   2024-11-13
 * &#064;Version: V1.0
 */
//@Api(tags="pi自定义报表字段")
@Tag(name="pi自定义报表字段")
@RestController
@RequestMapping("/pi/piCustomizeTableField")
@Slf4j
public class PiCustomizeTableFieldController extends BaseController<IPiCustomizeTableFieldService,PiCustomizeTableField> {

	 @Autowired
	 private IPiCustomizeTableFieldService piCustomizeTableFieldService;

	 /**
	  * 分页列表查询
	  *
	  * @param piCustomizeTableField piCustomizeTableField
	  * @param pageNo pageNo
	  * @param pageSize pageSize
	  * @param req req
	  * @return
	  */
	 @AutoLog(value = "pi自定义报表字段-分页列表查询")
	 @Operation(summary = "pi自定义报表字段-分页列表查询", description = "pi自定义报表字段-分页列表查询")
	 @GetMapping(value = "/listData")
	 public Result<IPage<PiCustomizeTableField>> queryPageList(PiCustomizeTableField piCustomizeTableField,
															   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
															   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
															   @RequestParam(name = "orderField",required = false) String orderField,
															   @RequestParam(name = "orderType",required = false) String orderType,
															   @RequestParam(name = "selectFields",required = false) String selectFields,
															   HttpServletRequest req) {
		 piCustomizeTableField.setName("*"+piCustomizeTableField.getName()+"*");
		 Page<PiCustomizeTableField> list = baseService.list(piCustomizeTableField, pageNo, pageSize, orderField, orderType, null, req);
		 //判断是否配置算法
		 list = piCustomizeTableFieldService.statisticsAlgorithm(list);
		 return Result.OK(list);
	 }

	/**
	 * 根据参数查询所有数据
	 * @param piCustomizeTableField 查询参数
	 * @param req 请求参数
	 * @return Result<List<PiCustomizeTableField>>
	 */
	@Operation(summary="PI岗位绑定-分页列表查询")
	@GetMapping(value = "/listAll")
	public Result<List<PiCustomizeTableField>> listAll(PiCustomizeTableField piCustomizeTableField, HttpServletRequest req) {
		QueryWrapper<PiCustomizeTableField> queryWrapper = QueryGenerator.initQueryWrapper(piCustomizeTableField, req.getParameterMap());
		queryWrapper.orderByDesc("is_unique").orderByAsc("sort");

		return Result.OK(piCustomizeTableFieldService.list(queryWrapper));
	}

	 /**
	  *   添加
	  *
	  * @param piCustomizeTableField
	  * @return
	  */
	 @AutoLog(value = "pi自定义报表主表-添加")
	 @Operation(summary = "pi自定义报表主表-添加", description = "pi自定义报表主表-添加")
	 @PostMapping(value = "/add")
	 public Result<String> add(@RequestBody PiCustomizeTableField piCustomizeTableField) {
		 Result<String> result = piCustomizeTableFieldService.saveField(piCustomizeTableField);
		 return Result.OK("添加成功！");
	 }

	/**
	 *  编辑
	 *
	 * @param piCustomizeTableField
	 * @return
	 */
	@AutoLog(value = "pi自定义报表主表-编辑")
	@Operation(summary = "pi自定义报表主表-分页列表查询", description = "pi自定义报表主表-分页列表查询")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody PiCustomizeTableField piCustomizeTableField) {
		//更新基础报表同时更新PI报表
		return piCustomizeTableFieldService.updateByData(piCustomizeTableField);
	}

	 /**
	  *   通过id删除
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "pi自定义报表主表-通过id删除")
	 @Operation(summary = "pi自定义报表主表-分页列表查询", description = "pi自定义报表主表-分页列表查询")
	 @DeleteMapping(value = "/deleteData")
	 public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		 List<String> list = Collections.singletonList(id);
		 piCustomizeTableFieldService.deleteBatchData(list);
		 return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	 }


	 /**
	  *  批量删除
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "pi自定义报表主表-批量删除")
	 @Operation(summary = "pi自定义报表主表-分页列表查询", description = "pi自定义报表主表-分页列表查询")
	 @DeleteMapping(value = "/deleteBatchData")
	 public Result<String> deleteBatchData(@RequestParam(name="ids",required=true) String ids) {
		 //this.piCustomizeTableFieldService.removeByIds(Arrays.asList(ids.split(",")));
		 piCustomizeTableFieldService.deleteBatchData(Arrays.asList(ids.split(",")));
		 return Result.OK("批量删除成功!");
	 }

	 /**
	  * 通过id查询
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "pi自定义报表主表-通过id查询")
	 @Operation(summary = "pi自定义报表主表-分页列表查询", description = "pi自定义报表主表-分页列表查询")
	 @GetMapping(value = "/queryById")
	 public Result<PiCustomizeTableField> queryById(@RequestParam(name="id",required=true) String id) {
		 PiCustomizeTableField piCustomizeTableField = piCustomizeTableFieldService.getById(id);
		 if(piCustomizeTableField==null) {
			 return Result.error("未找到对应数据");
		 }
		 return Result.OK(piCustomizeTableField);
	 }


	 /**
	  * 获取字段(唯一约束)
	  */
	 @Operation(summary = "点位查询", description = "点位查询")
	 @GetMapping(value = "/getFieldByTableId")
	 public Result<?> getFieldByTableId(PiCustomizeTableField piCustomizeTableField, HttpServletRequest req) {
		 LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
		 queryWrapper.eq(PiCustomizeTableField::getTableId,piCustomizeTableField.getTableId());
		 queryWrapper.eq(null !=piCustomizeTableField.getIsUnique(),PiCustomizeTableField::getIsUnique,piCustomizeTableField.getIsUnique());
		 List<PiCustomizeTableField> list = piCustomizeTableFieldService.list(queryWrapper);
//		 List<PiCustomizeTableField> list = null;
		 return Result.OK(list);
	 }


	 /**
	  * 算法保存
	  *
	  * @param algorithmDataDto
	  * @return
	  */
	 @AutoLog(value = "算法-添加")
	 @Operation(summary = "算法-添加", description = "算法-添加")
	 @PostMapping(value = "/algorithmAdd")
	 public Result<String> algorithmAdd(@RequestBody AlgorithmDataDto algorithmDataDto) {
		 piCustomizeTableFieldService.algorithmAdd(algorithmDataDto);
		 return Result.OK("添加成功！");
	 }


	 /**
	  * 获取字典编码(汉化模型)
	  */
	 @Operation(summary = "获取字典编码", description = "获取字典编码")
	 @GetMapping(value = "/getDictionary")
	 public Result<?> getDictionary() {
		 List<SinicizedDictDto> list = piCustomizeTableFieldService.getDictionary();
		 return Result.OK(list);
	 }


	/**
	 * 获取表头
	 */
	@Operation(summary = "获取获取表头", description = "获取获取表头")
	@GetMapping(value = "/getHead")
	public Result<?> getHead(PiCustomizeTableField piCustomizeTableField) {
		LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiCustomizeTableField::getTableId, piCustomizeTableField.getTableId());
		queryWrapper.eq(PiCustomizeTableField::getIsList, "1");
		queryWrapper.orderByAsc(PiCustomizeTableField::getSort);
		List<PiCustomizeTableField> list = piCustomizeTableFieldService.list(queryWrapper);
		return Result.OK(list);
	}

	/**
	 * 获取报表数据
	 */
	@AutoLog(value = "获取报表数据-分页列表查询")
	@Operation(summary = "获取报表数据-分页列表查询", description = "获取报表数据-分页列表查询")
	@GetMapping(value = "/queryData")
	public Result<?> queryData(PiCustomizeTableField piCustomizeTableField,
							   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
							   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
							   @RequestParam(name = "orderField",required = false) String orderField,
							   @RequestParam(name = "orderType",required = false) String orderType,
							   @RequestParam(name = "selectFields",required = false) String selectFields,
							   HttpServletRequest req) {

		Map<String,Object> mapR = piCustomizeTableFieldService.queryData(pageNo, pageSize,req,piCustomizeTableField);
		return Result.OK(mapR);
	}

	/**
	 *  更新报表值
	 * @param
	 * 'id 值id
	 * field_n 值
	 * table_id 报表id
	 * @return
	 */
	@AutoLog(value = "更新报表值")
	@Operation(summary = "更新报表值", description = "更新报表值")
	@RequestMapping(value = "/editVal", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> editVal(@RequestBody Map map) {
		if(piCustomizeTableFieldService.editVal(map)){
			return Result.OK("修改成功!");
		}else{
			return Result.error("修改失败!");
		}
	}


	/**
	 * 获取自定义表字段
	 * 排除定时任务分配过的字段
	 * @param piCustomizeTableField
	 * @param req
	 * @return
	 */
	@AutoLog(value = "获取自定义表字段")
	@Operation(summary = "获取自定义表字段", description = "获取自定义表字段")
	@GetMapping(value = "/fieldByTableIdData")
	public Result<List<PiCustomizeTableField>> fieldByTableIdData(PiCustomizeTableField piCustomizeTableField, HttpServletRequest req) {
		List<PiCustomizeTableField> list = baseService.fieldByTableIdData(piCustomizeTableField);
		return Result.OK(list);
	}


	/**
	 *   算法复制
	 * @param piCustomizeTableField
	 * @return
	 */
	@AutoLog(value = "算法复制")
	@Operation(summary = "算法复制", description = "算法复制")
	@PostMapping(value = "/algorithmCopy")
	public Result<String> algorithmCopy(@RequestBody PiCustomizeTableField piCustomizeTableField) {
		Result<String> result = piCustomizeTableFieldService.algorithmCopy(piCustomizeTableField);
		return Result.OK("复制成功！");
	}


	/**
	 * 获取排序(当前报表字段最大序号)
	 * @param tableId
	 * @return
	 */
	@AutoLog(value = "获取排序")
	@Operation(summary = "获取排序", description = "获取排序")
	@GetMapping(value = "/getSort")
	public Result<List<PiCustomizeTableField>> getSort(@RequestParam(name="tableId",required=true) String tableId) {
		List<PiCustomizeTableField> piCustomizeTableField = piCustomizeTableFieldService.getSort(tableId);
		return Result.OK(piCustomizeTableField);
	}


	/**
	 *  批量修改PI字段是否可编辑(配置算法的字段)
	 *
	 * @param tableId
	 * @return
	 */
	@AutoLog(value = "报表字段-批量修改PI字段是否可编辑")
	@Operation(summary = "报表字段-批量修改PI字段是否可编辑", description = "报表字段-批量修改PI字段是否可编辑")
	@DeleteMapping(value = "/openOrCloseEdit")
	public Result<String> openOrCloseEdit(@RequestParam(name="tableId",required=true) String tableId, @RequestParam(name="isEdit",required=true) String isEdit) {

		piCustomizeTableFieldService.openOrCloseEdit(tableId, isEdit);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}


	/**
	 * 获取报表字段(标记是否配置标准)
	 */
	@Operation(summary="获取报表字段")
	@GetMapping(value = "/getStandardFieldByTableId")
	public Result<List<PiCustomizeTableField>> getStandardFieldByTableId(PiCustomizeTableField piCustomizeTableField, HttpServletRequest req) {
		return Result.OK(piCustomizeTableFieldService.getStandardFieldByTableId(piCustomizeTableField, req.getParameterMap()));
	}

	/**
	 * 字段和算法合并到指定表
	 */
	@Operation(summary="获取报表字段")
	@GetMapping(value = "/mergingFieldsAndAlgorithms")
	public Result<?> mergingFieldsAndAlgorithms() {
		return Result.OK(piCustomizeTableFieldService.mergingFieldsAndAlgorithms());
	}
}
