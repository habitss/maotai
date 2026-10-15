package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.dto.TaskDataDto;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiTaskParameter;
import com.keytech.modules.pi.service.IPiBasePointService;
import com.keytech.modules.pi.service.IPiTaskParameterService;
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
import java.util.List;

/**
 * @Description: pi任务参数配置
 * @Author: jeecg-boot
 * @Date:   2024-11-25
 * @Version: V1.0
 */
 @Tag(name="pi任务参数配置")
 @RestController
 @RequestMapping("/pi/taskParameter")
 @Slf4j
public class PiTaskParameterController extends BaseController<IPiTaskParameterService, PiTaskParameter> {
	@Autowired
	private IPiTaskParameterService piTaskParameterService;



	/**
	 * 分页列表查询
	 *
	 * @param piTaskParameter
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "pi任务参数配置-分页列表查询")
	@Operation(summary = "pi任务参数配置-分页列表查询", description = "基础点位维护-分页列表查询")
	@GetMapping(value = "/listData")
	public Result<IPage<PiTaskParameter>> queryPageList(PiTaskParameter piTaskParameter,
													@RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
													@RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
													@RequestParam(name = "orderField",required = false) String orderField,
													@RequestParam(name = "orderType",required = false) String orderType,
													@RequestParam(name = "selectFields",required = false) String selectFields,
													HttpServletRequest req) {
		piTaskParameter.setDeptCode(null);
		Page<PiTaskParameter> list = baseService.list(piTaskParameter, pageNo, pageSize, orderField, orderType, null, req);
		//匹配任务名称
		list = piTaskParameterService.setCodeName(list);
		return Result.OK(list);
	}

	/**
	 *   添加
	 *
	 * @param piTaskParameter
	 * @return
	 */
	@AutoLog(value = "pi任务参数配置-添加")
	@Operation(summary = "pi任务参数配置-添加", description = "pi任务参数配置-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiTaskParameter piTaskParameter) {
		piTaskParameterService.save(piTaskParameter);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param piTaskParameter
	 * @return
	 */
	@AutoLog(value = "pi任务参数配置-编辑")
	@Operation(summary = "pi任务参数配置-编辑", description = "pi任务参数配置-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiTaskParameter piTaskParameter) {
		piTaskParameterService.updateById(piTaskParameter);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pi任务参数配置-通过id删除")
	@Operation(summary = "pi任务参数配置-通过id删除", description = "pi任务参数配置-通过id删除")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		piTaskParameterService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "pi任务参数配置-批量删除")
	@Operation(summary = "pi任务参数配置-批量删除", description = "pi任务参数配置-批量删除")
	@DeleteMapping(value = "/deleteDataBatch")
	public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
		piTaskParameterService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 获取定时任务
	 */
	@Operation(summary = "获取定时任务", description = "获取定时任务")
	@GetMapping(value = "/queryTask")
	public Result<?> queryTask(TaskDataDto dto) {
		List<TaskDataDto> list = piTaskParameterService.queryTask(dto);
		return Result.OK(list);
	}
 }
