package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.common.QueryDeptScopeUtil;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: pi自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Tag(name="pi自定义报表")
@RestController
@RequestMapping("/pi/piCustomizeTable")
@Slf4j
public class PiCustomizeTableController extends BaseController<IPiCustomizeTableService, PiCustomizeTable> {
	@Autowired
	private IPiCustomizeTableService piCustomizeTableService;

	 /**
	  * 分页列表查询
	  * @param piCustomizeTable
	  * @param pageNo
	  * @param pageSize
	  * @param orderField
	  * @param orderType
	  * @param selectFields
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "步骤维护-分页列表查询")
	 @Operation(summary = "步骤维护-分页列表查询", description = "步骤维护-分页列表查询")
	 @GetMapping(value = "/listData")
	 public Result<IPage<PiCustomizeTable>> queryPageList(PiCustomizeTable piCustomizeTable,
												@RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
												@RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
												@RequestParam(name = "orderField",required = false) String orderField,
												@RequestParam(name = "orderType",required = false) String orderType,
												@RequestParam(name = "selectFields",required = false) String selectFields,
												HttpServletRequest req) {

		 if(StrUtil.isNotBlank(piCustomizeTable.getName())){piCustomizeTable.setName("*"+piCustomizeTable.getName()+"*");}
		 if(StrUtil.isNotBlank(piCustomizeTable.getTableName())){piCustomizeTable.setTableName("*"+piCustomizeTable.getTableName()+"*");}
//		 if(StrUtil.isNotBlank(piStep.getQueryDept())){piStep.setDeptCode(piStep.getQueryDept());}
		 //Page<PiCustomizeTable> list = baseService.list(piCustomizeTable, pageNo, pageSize, orderField, orderType, null, req);
		 QueryWrapper<PiCustomizeTable> queryWrapper = QueryGenerator.initQueryWrapper(piCustomizeTable, req.getParameterMap());
		 QueryDeptScopeUtil.initDeptScope(queryWrapper, req.getParameterMap(), piCustomizeTable);

		 Page<PiCustomizeTable> page = new Page<>(pageNo, pageSize);
		 IPage<PiCustomizeTable> list = piCustomizeTableService.page(page, queryWrapper);

		 //数据处理 isDataHandle = 1 处理
		 if(PiCommonConstants.OPEN.equals(piCustomizeTable.getIsDataHandle())){
			 list = piCustomizeTableService.statisticsData(list);
		 }
		 return Result.OK(list);
	 }


	
	/**
	 *   添加
	 *
	 * @param piCustomizeTable
	 * @return
	 */
	@AutoLog(value = "pi自定义报表主表-添加")
	@Operation(summary = "pi自定义报表主表-添加", description = "pi自定义报表主表-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiCustomizeTable piCustomizeTable) {
		Result<String> result = piCustomizeTableService.saveTable(piCustomizeTable);
		return result;
	}
	
	/**
	 *  编辑
	 *
	 * @param piCustomizeTable
	 * @return
	 */
	@AutoLog(value = "pi自定义报表主表-编辑")
	@Operation(summary = "pi自定义报表主表-分页列表查询", description = "pi自定义报表主表-分页列表查询")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiCustomizeTable piCustomizeTable) {
		piCustomizeTableService.updateById(piCustomizeTable);
		return Result.OK("编辑成功!");
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
		piCustomizeTableService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}


	/**
	 *  批量删除
	 *  删除字段/同时删除算法
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "pi自定义报表主表-批量删除")
	@Operation(summary = "pi自定义报表主表-分页列表查询", description = "pi自定义报表主表-分页列表查询")
	@DeleteMapping(value = "/deleteBatchData")
	public Result<String> deleteBatchData(@RequestParam(name="ids",required=true) String ids) {
		piCustomizeTableService.removeByIds(Arrays.asList(ids.split(",")));
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
	public Result<PiCustomizeTable> queryById(@RequestParam(name="id",required=true) String id) {
		PiCustomizeTable piCustomizeTable = piCustomizeTableService.getById(id);
		if(piCustomizeTable==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(piCustomizeTable);
	}

	/**
	 * 存储数据[本方法为调试方法]
	 * 指定表,指定字段, 指定约束条件
	 */
	@GetMapping(value = "/tableSave")
	public Result<String> tableSave() {
		TableSaveDto tableSaveDto = new TableSaveDto();
		tableSaveDto.setTable("mes3_basedata_001");
		//值
		TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
		tableFieldValueDto.setField("field_12");
		tableFieldValueDto.setFieldVal("555");

//		TableFieldValueDto tableFieldValueDto2 = new TableFieldValueDto();
//		tableFieldValueDto2.setField("field_2");
//		tableFieldValueDto2.setFieldVal("88888888");

		List<TableFieldValueDto> fieldList = new ArrayList<>();
		fieldList.add(tableFieldValueDto);
//		fieldList.add(tableFieldValueDto2);
		tableSaveDto.setFieldList(fieldList);
		//约束条件
		TableConditionDto tableConditionDto = new TableConditionDto();
		tableConditionDto.setKey("field_9");
		tableConditionDto.setVal("6");
		TableConditionDto tableConditionDto2 = new TableConditionDto();
		tableConditionDto2.setKey("field_8");
		tableConditionDto2.setVal("2024-09-25 13:19:44");
		List<TableConditionDto> tableConditionList = new ArrayList<>();
		tableConditionList.add(tableConditionDto);
		tableConditionList.add(tableConditionDto2);
		tableSaveDto.setConditionList(tableConditionList);

		String result = piCustomizeTableService.tableSave(tableSaveDto);
		// result 大于0 表示成功
		if(Integer.parseInt(result) > 0){
			return Result.OK("添加成功！", result);
		}else{
			return Result.error("添加失败！");
		}

	}

	/**查询表数据[本方法为调试方法]*/
	@GetMapping(value = "/getTableData")
	public Result<?> getTableData() {
		TableQueryDto tableQueryDto = new TableQueryDto();
		tableQueryDto.setTable("mdm_table_002");
		//值
		List<String> fieldList = new ArrayList<>();
		fieldList.add("id");
		fieldList.add("field_2");
		tableQueryDto.setFieldList(fieldList);
		//约束条件
		TableConditionDto tableConditionDto = new TableConditionDto();
		tableConditionDto.setKey("field_1");
		tableConditionDto.setVal("1");
		List<TableConditionDto> tableConditionList = new ArrayList<>();
		tableConditionList.add(tableConditionDto);
		tableQueryDto.setConditionList(tableConditionList);

		List<Map<String,Object>> list = piCustomizeTableService.getTableData(tableQueryDto);
		return Result.OK(list);

	}


	/**
	 * 获取报表集合
	 * pointCode 点位编码
	 */
	@Operation(summary = "获取报表集合", description = "获取报表集合")
	@GetMapping(value = "/getTableList")
	public Result<?> getTableList(PiCustomizeTable piCustomizeTable) {
		List<PiCustomizeTable> list = piCustomizeTableService.getTableList(piCustomizeTable);
		return Result.OK(list);
	}


	/**
	 * PI点位上图查询
	 * @return
	 */
	@AutoLog(value = "PI点位上图查询")
	@Operation(summary = "PI点位上图查询", description = "PI点位上图查询")
	@GetMapping(value = "/getPointChartData")
	public Result<Map> getPointChartData(PointChartDto pointChartDto) {
		return Result.OK(piCustomizeTableService.getPointChartData(pointChartDto));
	}

	/**
	 * pi点图查询
	 * @return 结果
	 */
	@AutoLog(value = "pi点图查询")
	@Operation(summary = "pi点图查询", description = "pi点图查询")
	@GetMapping(value = "/getPointDiagramData")
	public Result<?> getPointDiagramData(PointDiagramDto pointDiagramDto) {
		return Result.OK(piCustomizeTableService.getPointDiagramData(pointDiagramDto));
	}

	/**
	 * pi箱图查询
	 * @return 结果
	 */
	@AutoLog(value = "pi箱图查询")
	@Operation(summary = "pi箱图查询", description = "pi箱图查询")
	@GetMapping(value = "/getBoxDiagramData")
	public Result<?> getBoxDiagramData(PointDiagramDto pointDiagramDto) {
		return Result.OK(piCustomizeTableService.getBoxDiagramData(pointDiagramDto));
	}
}
