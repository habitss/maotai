package com.keytech.modules.ems.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.ems.dto.ManualBackfillPointDto;
import com.keytech.modules.ems.service.ISystemService;
import com.keytech.modules.ems.service.impl.EmsCalculateService;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.service.IPiBasePointService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.service.ICollectPointStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 点位采集状态表
 * @author yepeng
 * @since 2025.04.29
 * */
@Tag(name="点位采集状态表")
@RestController
@RequestMapping("/ems/collectPointStatus")
@Slf4j
@RequiredArgsConstructor
public class CollectPointStatusController extends BaseController<ICollectPointStatusService, CollectPointStatus> {
	private final IPiBasePointService piBasePointService;

	/**
	 * 添加
	 *
	 * @param collectPointStatus
	 * @return
	 */
	@AutoLog(value = "点位采集状态表-添加")
	@Operation(summary="点位采集状态表-添加")
	@RequiresPermissions("ems:ems_collect_point_status:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CollectPointStatus collectPointStatus) {
		baseService.save(collectPointStatus);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param collectPointStatus
	 * @return
	 */
	@AutoLog(value = "点位采集状态表-编辑")
	@Operation(summary="点位采集状态表-编辑")
	@RequiresPermissions("ems:ems_collect_point_status:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CollectPointStatus collectPointStatus) {
		baseService.updateById(collectPointStatus);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "点位采集状态表-通过id删除")
	@Operation(summary="点位采集状态表-通过id删除")
	@RequiresPermissions("ems:ems_collect_point_status:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     *
     * @param request
     * @param collectPointStatus
     */
    @RequiresPermissions("ems:ems_collect_point_status:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CollectPointStatus collectPointStatus) {
        return super.exportXls(request, collectPointStatus, CollectPointStatus.class, "点位采集状态表");
    }

    /**
	 * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("ems:ems_collect_point_status:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CollectPointStatus.class);
    }

}
