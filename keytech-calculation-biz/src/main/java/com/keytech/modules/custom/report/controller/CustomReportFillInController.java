package com.keytech.modules.custom.report.controller;

import com.keytech.modules.custom.report.dto.CustomReportFillInDto;
import com.keytech.modules.custom.report.service.ICustomReportFillInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 自定义报表填报
 * @Author: sjw
 * @Date: 2024/12/9 17:34
 * @Param:
 * @return:
 */
 @Tag(name="自定义报表填报")
 @RestController
 @RequestMapping("/customReport/fuillIn")
 @Slf4j
public class CustomReportFillInController {

	@Autowired
	 private ICustomReportFillInService iCustomReportFillInService;

	/**
	 *	新增
	 * @param customReportFillInDto
	 * @return
	 */
	@AutoLog(value = "自定义报表填报-添加")
	@Operation(summary = "自定义报表填报-添加", description = "自定义报表填报-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CustomReportFillInDto customReportFillInDto) {
		iCustomReportFillInService.addData(customReportFillInDto);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 * @param customReportFillInDto
	 * @return
	 */
	@AutoLog(value = "自定义报表填报-编辑")
	@Operation(summary = "自定义报表填报-编辑", description = "自定义报表填报-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CustomReportFillInDto customReportFillInDto) {
		iCustomReportFillInService.updateData(customReportFillInDto);
		return Result.OK("编辑成功!");
	}

	/**
	 * 通过id删除
	 * @param customReportFillInDto
	 * @return
	 */
	@AutoLog(value = "自定义报表填报-通过id删除")
	@Operation(summary = "自定义报表填报-通过id删除", description = "自定义报表填报-通过id删除")
	@RequestMapping(value = "/deleteData", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> deleteData(@RequestBody CustomReportFillInDto customReportFillInDto) {
		iCustomReportFillInService.removeById(customReportFillInDto);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
 }
