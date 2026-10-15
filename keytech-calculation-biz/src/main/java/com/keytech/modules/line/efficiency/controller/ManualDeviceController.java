package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.ManualDevice;
import com.keytech.modules.line.efficiency.service.IManualDeviceService;
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

 /**
 * @Description: 上位机系统手动操作设备对照
 * @Author: jeecg-boot
 * @Date:   2025-12-08
 * @Version: V1.0
 */
@Tag(name="上位机系统手动操作设备对照")
@RestController
@RequestMapping("/line/manualDevice")
@Slf4j
public class ManualDeviceController extends BaseController<IManualDeviceService, ManualDevice> {
	@Autowired
	private IManualDeviceService manualDeviceService;


	/**
	 *   添加
	 *
	 * @param manualDevice
	 * @return
	 */
	@AutoLog(value = "上位机系统手动操作设备对照-添加")
	@Operation(summary="上位机系统手动操作设备对照-添加")
	@RequiresPermissions("line:mes3_manual_device:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ManualDevice manualDevice) {
		manualDeviceService.save(manualDevice);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  编辑
	 *
	 * @param manualDevice
	 * @return
	 */
	@AutoLog(value = "上位机系统手动操作设备对照-编辑")
	@Operation(summary="上位机系统手动操作设备对照-编辑")
	@RequiresPermissions("line:mes3_manual_device:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ManualDevice manualDevice) {
		manualDeviceService.updateById(manualDevice);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "上位机系统手动操作设备对照-通过id删除")
	@Operation(summary="上位机系统手动操作设备对照-通过id删除")
	@RequiresPermissions("line:mes3_manual_device:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		manualDeviceService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "上位机系统手动操作设备对照-批量删除")
	@Operation(summary="上位机系统手动操作设备对照-批量删除")
	@RequiresPermissions("line:mes3_manual_device:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.manualDeviceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="上位机系统手动操作设备对照-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ManualDevice> queryById(@RequestParam(name="id",required=true) String id) {
		ManualDevice manualDevice = manualDeviceService.getById(id);
		if(manualDevice==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(manualDevice);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param manualDevice
    */
    @RequiresPermissions("line:mes3_manual_device:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ManualDevice manualDevice) {
        return super.exportXls(request, manualDevice, ManualDevice.class, "上位机系统手动操作设备对照");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line:mes3_manual_device:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ManualDevice.class);
    }

}
