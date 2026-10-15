package com.keytech.modules.custom.report.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.custom.report.entity.CustomReport;
import com.keytech.modules.custom.report.service.ICustomReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * @Description: 自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Tag(name="自定义报表")
@RestController
@RequestMapping("/customReport/customReport")
@Slf4j
public class CustomReportController extends BaseController<ICustomReportService, CustomReport> {
	@Autowired
	private ICustomReportService customReportService;

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
	 @AutoLog(value = "自定义报表配置-分页列表查询")
	 @Operation(summary = "自定义报表配置-分页列表查询", description = "自定义报表配置-分页列表查询")
	 @GetMapping(value = "/listData")
	 public Result<IPage<CustomReport>> queryPageList(CustomReport piCustomizeTable,
												@RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
												@RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
												@RequestParam(name = "orderField",required = false) String orderField,
												@RequestParam(name = "orderType",required = false) String orderType,
												@RequestParam(name = "selectFields",required = false) String selectFields,
												HttpServletRequest req) {

		 if(StrUtil.isNotBlank(piCustomizeTable.getName())){piCustomizeTable.setName("*"+piCustomizeTable.getName()+"*");}
		 if(StrUtil.isNotBlank(piCustomizeTable.getTableName())){piCustomizeTable.setTableName("*"+piCustomizeTable.getTableName()+"*");}
		 //if(StrUtil.isNotBlank(piStep.getQueryDept())){piStep.setDeptCode(piStep.getQueryDept());}
		 Page<CustomReport> list = baseService.list(piCustomizeTable, pageNo, pageSize, orderField, orderType, null, req);
		 return Result.OK(list);
	 }


	
	/**
	 * 添加
	 * @param piCustomizeTable
	 * @return
	 */
	@AutoLog(value = "自定义报表配置-添加")
	@Operation(summary = "自定义报表配置-添加", description = "自定义报表配置-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CustomReport piCustomizeTable) {
		return customReportService.saveTable(piCustomizeTable);
	}
	
	/**
	 * 编辑
	 * @param piCustomizeTable
	 * @return
	 */
	@AutoLog(value = "自定义报表配置-编辑")
	@Operation(summary = "自定义报表配置-编辑", description = "自定义报表配置-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CustomReport piCustomizeTable) {
		customReportService.updateById(piCustomizeTable);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 * @param id
	 * @return
	 */
	@AutoLog(value = "自定义报表配置-通过id删除")
	@Operation(summary = "自定义报表配置-通过id删除", description = "自定义报表配置-通过id删除")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		customReportService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}


	/**
	 *  批量删除
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "自定义报表配置-批量删除")
	@Operation(summary = "自定义报表配置-批量删除", description = "自定义报表配置-批量删除")
	@DeleteMapping(value = "/deleteBatchData")
	public Result<String> deleteBatchData(@RequestParam(name="ids",required=true) String ids) {
		this.customReportService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "自定义报表配置-通过id查询")
	@Operation(summary = "自定义报表配置-通过id查询", description = "自定义报表配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CustomReport> queryById(@RequestParam(name="id",required=true) String id) {
		CustomReport piCustomizeTable = customReportService.getById(id);
		if(piCustomizeTable==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(piCustomizeTable);
	}
}
