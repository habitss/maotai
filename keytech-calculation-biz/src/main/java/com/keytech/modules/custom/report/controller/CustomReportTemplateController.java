package com.keytech.modules.custom.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.custom.report.dto.SysUserDto;
import com.keytech.modules.custom.report.entity.CustomReportTemplateEntity;
import com.keytech.modules.custom.report.service.ICustomReportTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 
 * @Description: 自定义报表模板配置
 * @Author: sjw
 * @Date: 2024/12/3 15:14
 * @Param:  
 * @return:  
 */
 @Tag(name="自定义报表模板配置")
 @RestController
 @RequestMapping("/customReport/reportTemplate")
 @Slf4j
public class CustomReportTemplateController extends BaseController<ICustomReportTemplateService, CustomReportTemplateEntity> {
	@Autowired
	private ICustomReportTemplateService iCustomReportTemplateService;
	// 注入全局 ObjectMapper（用于复制配置）
	@Autowired
	private ObjectMapper globalObjectMapper;

	/**
	 * 分页列表查询
	 *
	 * @param customReportTemplateEntity
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-分页列表查询")
	@Operation(summary = "自定义报表模板配置-分页列表查询", description = "自定义报表模板配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<CustomReportTemplateEntity>> queryPageList(CustomReportTemplateEntity customReportTemplateEntity,
																   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
																   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
																   @RequestParam(name = "orderField",required = false) String orderField,
																   @RequestParam(name = "orderType",required = false) String orderType,
																   @RequestParam(name = "selectFields",required = false) String selectFields,
																   HttpServletRequest req) {
		customReportTemplateEntity.setDeptCode(null);
		Page<CustomReportTemplateEntity> pageList;
		if (customReportTemplateEntity.getReportType() != null && "5".equals(customReportTemplateEntity.getReportType())) {
			// tableName强制EQ，避免逗号被转成IN查询
			Map<String, org.jeecg.common.system.query.QueryRuleEnum> customeRuleMap = new HashMap<>();
			customeRuleMap.put("tableName", org.jeecg.common.system.query.QueryRuleEnum.EQ);
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CustomReportTemplateEntity> queryWrapper =
					org.jeecg.common.system.query.QueryGenerator.initQueryWrapper(customReportTemplateEntity, req.getParameterMap(), customeRuleMap);
			Page<CustomReportTemplateEntity> page = new Page<>(pageNo, pageSize);
			pageList = iCustomReportTemplateService.page(page, queryWrapper);
		} else {
			pageList = iCustomReportTemplateService.list(customReportTemplateEntity, pageNo, pageSize, orderField, orderType, null, req);
		}
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param customReportTemplateEntity
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-添加")
	@Operation(summary = "自定义报表模板配置-添加", description = "自定义报表模板配置-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CustomReportTemplateEntity customReportTemplateEntity) {
		iCustomReportTemplateService.save(customReportTemplateEntity);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param customReportTemplateEntity
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-编辑")
	@Operation(summary = "自定义报表模板配置-编辑", description = "自定义报表模板配置-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CustomReportTemplateEntity customReportTemplateEntity) {
		iCustomReportTemplateService.updateById(customReportTemplateEntity);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-通过id删除")
	@Operation(summary = "自定义报表模板配置-通过id删除", description = "自定义报表模板配置-通过id删除")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		iCustomReportTemplateService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-批量删除")
	@Operation(summary = "自定义报表模板配置-批量删除", description = "自定义报表模板配置-批量删除")
	@DeleteMapping(value = "/deleteDataBatch")
	public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
		iCustomReportTemplateService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-通过id查询")
	@Operation(summary = "自定义报表模板配置-通过id查询", description = "自定义报表模板配置-通过id查询")
	@GetMapping(value = "/getById")
	public Result<CustomReportTemplateEntity> getById(@RequestParam(name="id") String id) {
		return Result.ok(iCustomReportTemplateService.getById(id));
	}

	/**
	 * 查询用户字典
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-查询用户字典")
	@Operation(summary = "自定义报表模板配置-查询用户字典", description = "自定义报表模板配置-查询用户字典")
	@GetMapping(value = "/getUserAll")
	public Result<List<SysUserDto>> getUserAll() {
		return Result.ok(iCustomReportTemplateService.getUserAll());
	}

	/**
	 * 根据模板配置查询数据
	 * @return
	 */
	@AutoLog(value = "自定义报表模板配置-根据模板配置查询数据")
	@Operation(summary = "自定义报表模板配置-根据模板配置查询数据", description = "自定义报表模板配置-根据模板配置查询数据")
	@GetMapping(value = "/queryByTemplateConfig")
	public Result<?> queryByTemplateConfig(@RequestParam Map<String, String> params) throws JsonProcessingException {
		List<LinkedHashMap<String, Object>> list = iCustomReportTemplateService.queryByTemplateConfig(params);
		//创建一个ObjectMapper实例,避免忽略null字段
		ObjectMapper localMapper = globalObjectMapper.copy();
		localMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
		String json = localMapper.writeValueAsString(list);
		Map<String, Object> map = new HashMap<>();
		map.put("data", json);
		return Result.ok(map);
	}
 }
