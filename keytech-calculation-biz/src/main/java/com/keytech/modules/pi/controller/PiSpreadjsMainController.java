package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiSpreadjsMain;
import com.keytech.modules.pi.entity.PiSpreadjsPi;
import com.keytech.modules.pi.service.IPiSpreadjsMainService;
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
import java.util.Map;

/**
 * @Description: SpreadJS模板配置
 * @Author: jeecg-boot
 * @Date:   2025-12-23
 * @Version: V1.0
 */
@Tag(name="SpreadJS模板配置")
@RestController
@RequestMapping("/pi/piSpreadjsMain")
@Slf4j
public class PiSpreadjsMainController extends BaseController<IPiSpreadjsMainService, PiSpreadjsMain> {
	@Autowired
	private IPiSpreadjsMainService piSpreadjsMainService;

	/**
	 *   添加
	 *
	 * @param piSpreadjsMain
	 * @return
	 */
	@AutoLog(value = "SpreadJS模板配置-添加")
	@Operation(summary="SpreadJS模板配置-添加")
	@RequiresPermissions("pi:mes3_pi_spreadjs_main:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiSpreadjsMain piSpreadjsMain) {
		piSpreadjsMainService.save(piSpreadjsMain);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piSpreadjsMain
	 * @return
	 */
	@AutoLog(value = "SpreadJS模板配置-编辑")
	@Operation(summary="SpreadJS模板配置-编辑")
	@RequiresPermissions("pi:mes3_pi_spreadjs_main:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiSpreadjsMain piSpreadjsMain) {
		piSpreadjsMainService.updateById(piSpreadjsMain);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "SpreadJS模板配置-通过id删除")
	@Operation(summary="SpreadJS模板配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_spreadjs_main:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piSpreadjsMainService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "SpreadJS模板配置-批量删除")
	@Operation(summary="SpreadJS模板配置-批量删除")
	@RequiresPermissions("pi:mes3_pi_spreadjs_main:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piSpreadjsMainService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="SpreadJS模板配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiSpreadjsMain> queryById(@RequestParam(name="id",required=true) String id) {
		PiSpreadjsMain piSpreadjsMain = piSpreadjsMainService.getById(id);
		if(piSpreadjsMain==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piSpreadjsMain);
	}

	/**
	 *  存储PI配置
	 */
	@Operation(summary="SpreadJS模板配置-存储PI配置")
	@PostMapping(value = "/savePiConfig")
	public Result<String> savePiConfig(@RequestBody PiSpreadjsPi piSpreadjsPi) {
		piSpreadjsMainService.savePiConfig(piSpreadjsPi);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  获取PI配置
	 */
	@Operation(summary="SpreadJS模板配置-获取PI配置")
	@GetMapping(value = "/getPiConfig")
	public Result<?> getPiConfig(@RequestParam(name="spreadjsMainId",required=true) String spreadjsMainId) {
		List<PiSpreadjsPi>   list = piSpreadjsMainService.getPiConfig(spreadjsMainId);
		return Result.OK(list);
	}
 }
