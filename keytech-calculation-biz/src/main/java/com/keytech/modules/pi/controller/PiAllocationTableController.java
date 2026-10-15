package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.common.QueryDeptScopeUtil;
import com.keytech.modules.pi.dto.RerunDto;
import com.keytech.modules.pi.entity.PiAllocationTable;
import com.keytech.modules.pi.entity.PiCustomHeading;
import com.keytech.modules.pi.service.IPiAllocationTableService;
import com.keytech.modules.pi.service.IPiCustomHeadingService;
import com.keytech.modules.pi.service.IPiStandardDataService;
import com.keytech.modules.pi.vo.PiAllocationTableVisitsVo;
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

import java.util.Arrays;
import java.util.List;

/**
 * @Description: pi报表分配
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
 @Tag(name="pi报表分配")
 @RestController
 @RequestMapping("/pi/allocationTable")
 @Slf4j
public class PiAllocationTableController extends BaseController<IPiAllocationTableService, PiAllocationTable> {
	@Autowired
	private IPiAllocationTableService iPiAllocationTableService;

	@Autowired
	private IPiStandardDataService iPiStandardDataService;

	@Autowired
	private IPiCustomHeadingService iPiCustomHeadingService;



	/**
	 * 分页列表查询
	 *
	 * @param piAllocationTable
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "pi报表分配-分页列表查询")
	@Operation(summary = "pi报表分配-分页列表查询", description = "pi报表分配-分页列表查询")
	@GetMapping(value = "/listData")
	public Result<IPage<PiAllocationTable>> queryPageList(PiAllocationTable piAllocationTable,
											   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
											   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
											   @RequestParam(name = "orderField",required = false) String orderField,
											   @RequestParam(name = "orderType",required = false) String orderType,
											   @RequestParam(name = "selectFields",required = false) String selectFields,
											   HttpServletRequest req) {
		piAllocationTable.setDeptCode(null);
		QueryWrapper<PiAllocationTable> queryWrapper = QueryGenerator.initQueryWrapper(piAllocationTable, req.getParameterMap());
		QueryDeptScopeUtil.initDeptScope(queryWrapper, req.getParameterMap(), piAllocationTable);

		Page<PiAllocationTable> page = new Page<>(pageNo, pageSize);
		IPage<PiAllocationTable> list = iPiAllocationTableService.page(page, queryWrapper);
		return Result.OK(list);
	}

	/**
	 *   添加
	 *
	 * @param piAllocationTable
	 * @return
	 */
	@AutoLog(value = "pi报表分配-添加")
	@Operation(summary = "pi报表分配-添加", description = "pi报表分配-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAllocationTable piAllocationTable) {
		iPiAllocationTableService.save(piAllocationTable);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param piAllocationTable
	 * @return
	 */
	@AutoLog(value = "pi报表分配-编辑")
	@Operation(summary = "pi报表分配-编辑", description = "pi报表分配-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAllocationTable piAllocationTable) {
		iPiAllocationTableService.updateById(piAllocationTable);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pi报表分配-通过id删除")
	@Operation(summary = "pi报表分配-通过id删除", description = "pi报表分配-通过id删除")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		iPiAllocationTableService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "pi报表分配-批量删除")
	@Operation(summary = "pi报表分配-批量删除", description = "pi报表分配-批量删除")
	@DeleteMapping(value = "/deleteDataBatch")
	public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
		iPiAllocationTableService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 *  算法重算
	 *
	 * @param rerunDto
	 * @return
	 */
	@AutoLog(value = "pi报表-重算")
	@Operation(summary = "pi报表-重算", description = "pi报表-重算")
	@PostMapping(value = "/rerun")
	public Result<String> rerun(@RequestBody RerunDto rerunDto) {
		Result<String> re = iPiAllocationTableService.rerun(rerunDto);
		return re;
	}

	/**
	 *  红黄区计算
	 *
	 * @param rerunDto
	 * @return
	 */
	@AutoLog(value = "pi报表-红黄区计算")
	@Operation(summary = "pi报表-红黄区计算", description = "pi报表-红黄区计算")
	@PostMapping(value = "/redYellowRegion")
	public Result<String> redYellowRegion(@RequestBody RerunDto rerunDto) {
		iPiStandardDataService.reSunStandardCalculate(rerunDto);
		return Result.OK("红黄区计算完成!");
	}

	/**
	 *  批量删除
	 *
	 * @return
	 */
	@AutoLog(value = "pi报表-批量删除")
	@Operation(summary = "pi报表-批量删除", description = "pi报表-批量删除")
	@DeleteMapping(value = "/deletePiDataBatch")
	public Result<String> deletePiDataBatch(@RequestParam(name="data",required=true) String data,@RequestParam(name="tableName",required=true) String tableName) {
		boolean isOk = iPiAllocationTableService.deleteDataBatch(data,tableName);
		if(isOk){
			return Result.OK("删除成功!");
		}else{
			return Result.error("删除失败!");
		}

	}


	/**
	 *  保存表头配置
	 * @param piCustomHeading
	 * @return
	 */
	@AutoLog(value = "pi报表分配-保存表头配置")
	@Operation(summary = "pi报表分配-保存表头配置", description = "pi报表分配-保存表头配置")
	@PostMapping(value = "/saveTableHead")
	public Result<String> saveTableHead(@RequestBody PiCustomHeading piCustomHeading) {
		iPiCustomHeadingService.saveOrUpdate(piCustomHeading);
		return Result.OK("添加成功！");
	}


	/**
	 * 获取表头点位配置
	 */
	@Operation(summary = "获取表头点位配置", description = "获取表头点位配置")
	@GetMapping(value = "/getTableHead")
	public Result<?> getTableHead(String allocationTableId) {
		LambdaQueryWrapper<PiCustomHeading> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(PiCustomHeading::getAllocationTableId, allocationTableId);
		List<PiCustomHeading> list = iPiCustomHeadingService.list(wrapper);
		PiCustomHeading piCustomHeadingResult = new PiCustomHeading();
		if(list != null && !list.isEmpty()){
			piCustomHeadingResult = list.get(0);
		}
		return Result.OK(piCustomHeadingResult);
	}

	/**
	 *  删除表头配置
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pi报表表头配置-删除表头配置")
	@Operation(summary = "pi报表表头配置-删除表头配置", description = "pi报表表头配置-删除表头配置")
	@DeleteMapping(value = "/delTableHead")
	public Result<String> delTableHead(@RequestParam(name="id",required=true) String id) {
		iPiCustomHeadingService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}



	/**
	 * 统计报表访问次数
	 * @param piAllocationTableVisitsVo 参数
	 * @return 统计结果
	 */
	@AutoLog(value = "统计PI报表访问次数")
	@Operation(summary = "统计PI报表访问次数", description = "统计PI报表访问次数")
	@GetMapping(value = "/queryAllocationTableVisits")
	public Result<List<PiAllocationTableVisitsVo>> queryAllocationTableVisits(PiAllocationTableVisitsVo piAllocationTableVisitsVo) {
		return Result.OK(iPiAllocationTableService.queryAllocationTableVisits(piAllocationTableVisitsVo));
	}
 }
