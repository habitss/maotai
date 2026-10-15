package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.common.CommonConstants;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.TableDataDto;
import com.keytech.modules.pi.dto.TransferDataDto;
import com.keytech.modules.pi.entity.PiAllocationTable;
import com.keytech.modules.pi.entity.PiAllocationTableCondition;
import com.keytech.modules.pi.entity.PiAllocationTableField;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.service.IPiAllocationTableConditionService;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.IPiAllocationTableFieldService;
import com.keytech.modules.pi.service.IPiAllocationTableService;
import com.keytech.modules.pi.service.IPiCalculateSignalService;
import com.keytech.modules.pi.service.IPiComputationModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 报表字段分配
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
 @Tag(name="报表字段分配")
 @RestController
 @RequestMapping("/pi/allocationTableField")
 @Slf4j
public class PiAllocationTableFieldController extends BaseController<IPiAllocationTableService, PiAllocationTable> {
	@Autowired
	private IPiAllocationTableFieldService iPiAllocationTableFieldService;
	@Autowired
	private IPiAllocationTableConditionService iPiAllocationTableConditionService;

	@Autowired
	private IPiCalculateSignalService iPiCalculateSignalService;
	@Autowired
	private IPiComputationModelService iPiComputationModelService;

	/**
	 * 分页列表查询
	 *
	 * @param piAllocationTableField
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "报表字段分配-分页列表查询")
	@Operation(summary = "基础点位维护-分页列表查询", description = "基础点位维护-分页列表查询")
	@GetMapping(value = "/listData")
	public Result<IPage<PiAllocationTableField>> queryPageList(PiAllocationTableField piAllocationTableField,
														  @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
														  @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
														  @RequestParam(name = "orderField",required = false) String orderField,
														  @RequestParam(name = "orderType",required = false) String orderType,
														  @RequestParam(name = "selectFields",required = false) String selectFields,
														  HttpServletRequest req) {
		piAllocationTableField.setDeptCode(null);
		piAllocationTableField.setName("*"+piAllocationTableField.getName()+"*");
		Page<PiAllocationTableField> list = iPiAllocationTableFieldService.list(piAllocationTableField, pageNo, pageSize, orderField, orderType, null, req);
		return Result.OK(list);
	}

	/**
	 *   添加
	 *
	 * @param piAllocationTableField
	 * @return
	 */
	@AutoLog(value = "报表字段分配-添加")
	@Operation(summary = "报表字段分配-添加", description = "报表字段分配-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAllocationTableField piAllocationTableField) {
		iPiAllocationTableFieldService.fieldSave(piAllocationTableField);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param piAllocationTableField
	 * @return
	 */
	@AutoLog(value = "报表字段分配-编辑")
	@Operation(summary = "报表字段分配-编辑", description = "报表字段分配-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody PiAllocationTableField piAllocationTableField) {
		return iPiAllocationTableFieldService.edit(piAllocationTableField);
	}

	/**
	 *  批量编辑
	 * @param list
	 * @return
	 */
	@AutoLog(value = "报表字段分配-批量编辑")
	@Operation(summary = "报表字段分配-批量编辑", description = "报表字段分配-批量编辑")
	@RequestMapping(value = "/editBatch", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> editBatch(@RequestBody List<PiAllocationTableField> list) {
		iPiAllocationTableFieldService.updateBatchById(list);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报表字段分配-通过id删除")
	@Operation(summary = "报表字段分配-通过id删除", description = "报表字段分配-通过id删除")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		iPiAllocationTableFieldService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "报表字段分配-批量删除")
	@Operation(summary = "报表字段分配-批量删除", description = "报表字段分配-批量删除")
	@DeleteMapping(value = "/deleteDataBatch")
	public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
		iPiAllocationTableFieldService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}


	/**
	 * 获取分配的字段
	 */
	@Operation(summary = "获取分配的字段", description = "获取分配的字段")
	@GetMapping(value = "/getAllocationField")
	public Result<?> getAllocationField(PiAllocationTable piAllocationTable) {
		List<TransferDataDto> list = iPiAllocationTableFieldService.getAllocationField(piAllocationTable);
		return Result.OK(list);
	}


	/**
	 * 获取表头
	 */
	@Operation(summary = "获取获取表头", description = "获取获取表头")
	@GetMapping(value = "/getHead")
	public Result<?> getHead(PiAllocationTableField piAllocationTableField) {
		LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
		//重复引用功能
		if(StrUtil.isNotBlank(piAllocationTableField.getCiteField())){
			queryWrapper.eq(PiAllocationTableField::getIsUnique, 1);
		}
		queryWrapper.eq(PiAllocationTableField::getAllocationTableId, piAllocationTableField.getAllocationTableId());
		queryWrapper.eq(PiAllocationTableField::getIsList, "1");
		queryWrapper.orderByDesc(PiAllocationTableField::getIsFixed).orderByAsc(PiAllocationTableField::getSort);
		List<PiAllocationTableField> list = iPiAllocationTableFieldService.list(queryWrapper);

		return Result.OK(list);
	}

	/**
	 * 获取报表数据
	 */
	@AutoLog(value = "获取报表数据-分页列表查询")
	@Operation(summary = "获取报表数据-分页列表查询", description = "获取报表数据-分页列表查询")
	@GetMapping(value = "/queryData")
	public Result<?> queryData(PiAllocationTableField piAllocationTableField,
															   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
															   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
															   @RequestParam(name = "orderField",required = false) String orderField,
															   @RequestParam(name = "orderType",required = false) String orderType,
															   @RequestParam(name = "selectFields",required = false) String selectFields,
															   HttpServletRequest req) {

		Map<String,Object> mapR = iPiAllocationTableFieldService.queryData(pageNo, pageSize,req,piAllocationTableField);
		return Result.OK(mapR);
	}

	/**
	 * 报表数据查询-导出excel
	 *
	 * @param request
	 * @param piAllocationTableField
	 */
//	@RequestMapping(value = "/exportXls")
//	public ModelAndView exportXls(HttpServletRequest request, PiAllocationTableField piAllocationTableField, HttpServletResponse response) {
//		// 创建一个新的Excel工作簿
//		Workbook workbook = new XSSFWorkbook();
//		// 创建一个工作表
//		Sheet sheet = workbook.createSheet("Sheet1");
//		// 创建标题行
//		Row headerRow = sheet.createRow(0);
//		Cell headerCell1 = headerRow.createCell(0);
//		headerCell1.setCellValue("数据1");
//		Cell headerCell2 = headerRow.createCell(1);
//		headerCell2.setCellValue("数据2");
//		// 准备数据
//		List<String[]> dataList = new ArrayList<>();
//		dataList.add(new String[]{"值1-1", "值1-2"});
//		dataList.add(new String[]{"值2-1", "值2-2"});
//		// 将数据写入工作表
//		int rowNum = 1;
//		for (String[] data : dataList) {
//			Row row = sheet.createRow(rowNum++);
//			Cell cell1 = row.createCell(0);
//			cell1.setCellValue(data[0]);
//			Cell cell2 = row.createCell(1);
//			cell2.setCellValue(data[1]);
//		}
//		// 设置响应头，告诉浏览器这是一个Excel文件
//		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//		response.setHeader("Content - Disposition", "attachment;filename=example.xlsx");
//		try {
//			// 将Excel工作簿写入输出流
//			OutputStream outputStream = response.getOutputStream();
//			workbook.write(outputStream);
//			outputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		// 返回一个空的ModelAndView，因为数据已经通过响应流发送
//		return new ModelAndView();
//	}

	/**
	 *  更新报表值
	 * @param
	 * 'id 值id
	 * field_n 值
	 * table_id 报表id
	 * @return
	 */
	@AutoLog(value = "批量更新报表值")
	@Operation(summary = "批量更新报表值", description = "批量更新报表值")
	@RequestMapping(value = "/editVal", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> editVal(@RequestBody Map map) {
		if(iPiAllocationTableFieldService.editVal(map)){
			return Result.OK("修改成功!");
		}else{
			return Result.error("修改失败!");
		}
	}

	/**
	 *  批量更新报表值
	 * @param
	 * @return
	 */
	@AutoLog(value = "更新报表值")
	@Operation(summary = "更新报表值", description = "更新报表值")
	@RequestMapping(value = "/batchEditVal", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> batchEditVal(@RequestBody Map map) {
		return getBatchEditValResult(map);
	}

	/**
	 *  App批量更新报表值
	 */
	@AutoLog(value = "App批量更新报表值")
	@Operation(summary = "App批量更新报表值", description = "App批量更新报表值")
	@RequestMapping(value = "/batchEditValFromApp", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> batchEditValFromApp(@RequestBody Map map) {
		return getBatchEditValResult(map);
	}

	@NotNull
	private Result<String> getBatchEditValResult(Map map) {
		Map<String,String> result= iPiAllocationTableFieldService.batchEditVal(map);
		if("0".equals(result.get("code"))){
			return Result.OK("保存成功!");
		}else if("3".equals(result.get("code"))){
			return Result.error("无可修改的数据!");
		}else if("4".equals(result.get("code"))){
			return Result.error("唯一约束不为空!");
		}else{
			return Result.error("保存失败!");
		}
	}


	/**
	 * 获取表头
	 */
	@Operation(summary = "获取获取表头", description = "获取获取表头")
	@GetMapping(value = "/tableHead")
	public Result<?> tableHead(TableDataDto tableDataDto) {

		List<?> list = iPiAllocationTableFieldService.tableHead(tableDataDto);
		return Result.OK(list);
	}


	/**
	 * 获取配置算法PI(字段)
	 */
	@Operation(summary = "获取配置算法PI", description = "获取配置算法PI")
	@GetMapping(value = "/getAlgorithmField")
	public Result<?> getAlgorithmField(PiAllocationTableField piAllocationTableField) {
		List<?> list = iPiAllocationTableFieldService.getAlgorithmField(piAllocationTableField);
		return Result.OK(list);
	}


	/**
	 *  批量修改PI字段是否可编辑(配置算法的字段)
	 *
	 * @param allocationTableId
	 * @return
	 */
	@AutoLog(value = "报表字段分配-批量修改PI字段是否可编辑")
	@Operation(summary = "报表字段分配-批量修改PI字段是否可编辑", description = "报表字段分配-批量修改PI字段是否可编辑")
	@DeleteMapping(value = "/openOrCloseEdit")
	public Result<String> openOrCloseEdit(@RequestParam(name="allocationTableId",required=true) String allocationTableId, @RequestParam(name="isEdit",required=true) String isEdit) {
		iPiAllocationTableFieldService.openOrCloseEdit(allocationTableId,isEdit);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 获取需要时间格式字段
	 * @param piAllocationTableField
	 * @return
	 */
	@Operation(summary = "获取需要时间格式字段", description = "获取需要时间格式字段")
	@GetMapping(value = "/getTimeFormat")
	public Result<?> getTimeFormat(PiAllocationTableField piAllocationTableField) {
		Map<String, String> map = new HashMap<>();
		LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiAllocationTableField::getAllocationTableId, piAllocationTableField.getAllocationTableId());
		queryWrapper.eq(PiAllocationTableField::getType, "6");
		queryWrapper.isNotNull(PiAllocationTableField::getTimeFormat)
				.ne(PiAllocationTableField::getTimeFormat, ""); //时间展示格式
		List<PiAllocationTableField> list = iPiAllocationTableFieldService.list(queryWrapper);
		//获取map key为 filedName value为timeFormat
		list.forEach(item -> map.put(item.getFieldName(), item.getTimeFormat()));
		return Result.OK(map);
	}


	/**
	 * 获取获取字段最新10条数据
	 * @param piAllocationTableField
	 * @return
	 */
	@Operation(summary = "获取获取表头", description = "获取获取表头")
	@GetMapping(value = "/getTableFieldData")
	public Result<?> getTableFieldData(PiAllocationTableField piAllocationTableField) {
		return Result.OK(iPiAllocationTableFieldService.getTableFieldData(piAllocationTableField));
	}


	/**
	 * 获取多表对照关系
	 */
	@Operation(summary = "获取多表对照关系", description = "获取多表对照关系")
	@GetMapping(value = "/getTableCondition")
	public Result<?> getTableCondition(PiAllocationTable piAllocationTable) {
		LambdaQueryWrapper<PiAllocationTableCondition> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiAllocationTableCondition::getAllocationTableId, piAllocationTable.getId());
		List<PiAllocationTableCondition> list = iPiAllocationTableConditionService.list(queryWrapper);
		return Result.OK(list);
	}
 }
