package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.dto.ProductionDto;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyHandle;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyHandleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 线效率-产量/线效状态点位配置
 * @Author: jeecg-boot
 * @Date:   2025-05-26
 * @Version: V1.0
 */
@Tag(name="线效率-产量/线效状态点位配置")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyHandle")
@Slf4j
public class LineEfficiencyHandleController extends BaseController<ILineEfficiencyHandleService, LineEfficiencyHandle> {

	@Resource
	private ILineEfficiencyHandleService lineEfficiencyHandleService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyHandle
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="线效率-产量/线效状态点位配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyHandle>> queryPageList(LineEfficiencyHandle lineEfficiencyHandle,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		//产线名称设置模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyHandle.getLineName())){
			lineEfficiencyHandle.setLineName("*"+lineEfficiencyHandle.getLineName()+"*");
		}

		//产线名称设置模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyHandle.getSbName())){
			lineEfficiencyHandle.setSbName("*"+lineEfficiencyHandle.getSbName()+"*");
		}

		QueryWrapper<LineEfficiencyHandle> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyHandle, req.getParameterMap());

		Page<LineEfficiencyHandle> page = new Page<LineEfficiencyHandle>(pageNo, pageSize);
		IPage<LineEfficiencyHandle> pageList = lineEfficiencyHandleService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * OEE列表查询（单独定制）
	 *
	 * @param lineEfficiencyHandle
	 * @param req
	 * @return
	 */
	@Operation(summary="OEE列表查询（单独定制）")
	@GetMapping(value = "/queryListForOee")
	public Result<List<LineEfficiencyHandle>> queryListForOee(LineEfficiencyHandle lineEfficiencyHandle,
															 HttpServletRequest req) {
		//产线名称设置模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyHandle.getLineName())){
			lineEfficiencyHandle.setLineName("*"+lineEfficiencyHandle.getLineName()+"*");
		}

		//产线名称设置模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyHandle.getSbName())){
			lineEfficiencyHandle.setSbName("*"+lineEfficiencyHandle.getSbName()+"*");
		}

		QueryWrapper<LineEfficiencyHandle> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyHandle, req.getParameterMap());
		queryWrapper.in("handle_type", Arrays.asList(1,2));
		queryWrapper.orderByDesc("sb_id");

		List<LineEfficiencyHandle>  list = lineEfficiencyHandleService.list(queryWrapper);

		//按照设备id进行去重处理，形成新的集合
		Map<String, List<LineEfficiencyHandle>> groupedByLineCode = list.stream()
				.collect(Collectors.groupingBy(LineEfficiencyHandle::getLineCode));

		Set<String> keys = groupedByLineCode.keySet();
		List<LineEfficiencyHandle>  listNew = new ArrayList<>();
		for(String lineCode : keys ){
			//只会存在handle_type 1,2各一条数据
			LineEfficiencyHandle handle = new LineEfficiencyHandle();
			List<LineEfficiencyHandle> handleList = groupedByLineCode.get(lineCode);

			Optional<LineEfficiencyHandle> handleOptionalStart = handleList.stream().filter(item -> item.getHandleType() == 1).findFirst();
			Optional<LineEfficiencyHandle> handleOptionalEnd = handleList.stream().filter(item -> item.getHandleType() == 2).findFirst();
			if(handleOptionalStart.isPresent()){
				handle = handleOptionalStart.get();
			}
			if(handleOptionalEnd.isPresent()){
				handle.setCipCode(handleOptionalEnd.get().getPointCode());
				handle.setCipName(handleOptionalEnd.get().getPointName());

				handle.setJjwdCode(handleOptionalEnd.get().getPointCodeNew());
				handle.setJjwdName(handleOptionalEnd.get().getPointNameNew());
			}
			if(StringUtils.isNoneBlank(handle.getLineCode())){
				listNew.add(handle);
			}
		}
		return Result.OK(listNew);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyHandle
	 * @return
	 */
	@AutoLog(value = "线效率-产量/线效状态点位配置-添加")
	@Operation(summary="线效率-产量/线效状态点位配置-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyHandle lineEfficiencyHandle) {
		lineEfficiencyHandleService.save(lineEfficiencyHandle);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyHandle
	 * @return
	 */
	@AutoLog(value = "线效率-产量/线效状态点位配置-编辑")
	@Operation(summary="线效率-产量/线效状态点位配置-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyHandle lineEfficiencyHandle) {
		lineEfficiencyHandleService.updateById(lineEfficiencyHandle);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "线效率-产量/线效状态点位配置-通过id删除")
	@Operation(summary="线效率-产量/线效状态点位配置-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyHandleService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "线效率-产量/线效状态点位配置-批量删除")
	@Operation(summary="线效率-产量/线效状态点位配置-批量删除")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyHandleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="线效率-产量/线效状态点位配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyHandle> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyHandle lineEfficiencyHandle = lineEfficiencyHandleService.getById(id);
		if(lineEfficiencyHandle==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyHandle);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyHandle
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyHandle lineEfficiencyHandle) {
        return super.exportXls(request, lineEfficiencyHandle, LineEfficiencyHandle.class, "线效率-产量/线效状态点位配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyHandle.class);
    }

	 /**
	  * 计算线效状态
	  * @return
	  */
	 @AutoLog(value = "计算线效状态")
	 @Operation(summary="计算线效状态")
	 @PostMapping(value = "/lineEfficiencyState")
	 public Result<String> lineEfficiencyState() {
		 lineEfficiencyHandleService.lineEfficiencyState();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

	 /**
	  * 设备运行时长（按照设备开机信号点来进行计算，计算出每次开机关机的时间）
	  * @return
	  */
	 @AutoLog(value = "设备运行时长（按照设备开机信号点来进行计算，计算出每次开机关机的时间）")
	 @Operation(summary="设备运行时长（按照设备开机信号点来进行计算，计算出每次开机关机的时间）")
	 @PostMapping(value = "/machineMinute")
	 public Result<String> machineMinute() {
		 lineEfficiencyHandleService.machineMinute();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

	 /**
	  * 报警（按照报警信号点来进行计算，计算出每次报警开始的时间）
	  * @return
	  */
	 @AutoLog(value = "报警（按照报警信号点来进行计算，计算出每次报警开始的时间）")
	 @Operation(summary="报警（按照报警信号点来进行计算，计算出每次报警开始的时间）")
	 @PostMapping(value = "/alarmMinute")
	 public Result<String> alarmMinute() {
		 lineEfficiencyHandleService.alarmMinute();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

	/**
	 * 点位运行状态（按照点位信号点来进行计算，计算出每次运行开始的时间）
	 * @return
	 */
	@AutoLog(value = "点位运行状态（按照点位信号点来进行计算，计算出每次运行开始的时间）")
	@Operation(summary="点位运行状态（按照点位信号点来进行计算，计算出每次运行开始的时间）")
	@PostMapping(value = "/operatingDurationForPoint")
	public Result<String> operatingDurationForPoint() {
		lineEfficiencyHandleService.operatingDurationForPoint();
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	 /**
	  * 查询当前时间的外排水数据，然后发送给第三方
	  * @return
	  */
	 @AutoLog(value = "查询当前时间的外排水数据，然后发送给第三方")
	 @Operation(summary="查询当前时间的外排水数据，然后发送给第三方")
	 @PostMapping(value = "/queryEhsData")
	 public Result<String> queryEhsData() {
		 lineEfficiencyHandleService.queryEhsData();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }


	/**
	 *   OEE配置添加
	 *
	 * @param lineEfficiencyHandle
	 * @return
	 */
	@AutoLog(value = "线效率-OEE配置-添加")
	@Operation(summary="线效率-OEE配置-添加")
	@PostMapping(value = "/addOee")
	public Result<String> addOee(@RequestBody LineEfficiencyHandle lineEfficiencyHandle) {

		String sbId = lineEfficiencyHandle.getSbId();
		List<LineEfficiencyHandle> handleEndList = lineEfficiencyHandleService.lambdaQuery()
				.eq(LineEfficiencyHandle::getSbId,sbId)
				.in(LineEfficiencyHandle::getHandleType,Arrays.asList(1,2))
				.list();
		if(handleEndList.size() > 0){
			return Result.error(GlobalErrorEnum.ALREADY_EXIST.getCode());
		}
		lineEfficiencyHandle.setHandleType(1);
		lineEfficiencyHandleService.save(lineEfficiencyHandle);

		lineEfficiencyHandle.setId(null);
		lineEfficiencyHandle.setHandleType(2);
		lineEfficiencyHandle.setPointCode(lineEfficiencyHandle.getCipCode());
		lineEfficiencyHandle.setPointName(lineEfficiencyHandle.getCipName());
		lineEfficiencyHandle.setPointCodeNew(lineEfficiencyHandle.getJjwdCode());
		lineEfficiencyHandle.setPointNameNew(lineEfficiencyHandle.getJjwdName());
		lineEfficiencyHandleService.save(lineEfficiencyHandle);

		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  OEE配置编辑
	 *
	 * @param lineEfficiencyHandle
	 * @return
	 */
	@AutoLog(value = "线效率-OEE配置-编辑")
	@Operation(summary="线效率-OEE配置-编辑")
	@RequestMapping(value = "/editOee", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> editOee(@RequestBody LineEfficiencyHandle lineEfficiencyHandle) {

		//获取设备编码、cip、进酒温度
		String sbId = lineEfficiencyHandle.getSbId();
		String cipCode = lineEfficiencyHandle.getCipCode();
		String cipName = lineEfficiencyHandle.getCipName();
		String jjwdCode = lineEfficiencyHandle.getJjwdCode();
		String jjwdName = lineEfficiencyHandle.getJjwdName();

		//OEE编辑会传入lineEfficiencyHandle对象，默认传入的是开始配置
		lineEfficiencyHandle.setHandleType(1);
		lineEfficiencyHandleService.updateById(lineEfficiencyHandle);

		//查询结束配置
		List<LineEfficiencyHandle> handleEndList = lineEfficiencyHandleService.lambdaQuery()
				.eq(LineEfficiencyHandle::getSbId,sbId)
				.eq(LineEfficiencyHandle::getHandleType,2)
				.list();
		if(handleEndList.size() == 1){
			String endId = handleEndList.get(0).getId();
			lineEfficiencyHandle.setId(endId);
			lineEfficiencyHandle.setHandleType(2);
			lineEfficiencyHandle.setPointCode(cipCode);
			lineEfficiencyHandle.setPointName(cipName);
			lineEfficiencyHandle.setPointCodeNew(jjwdCode);
			lineEfficiencyHandle.setPointNameNew(jjwdName);
			lineEfficiencyHandleService.updateById(lineEfficiencyHandle);
		}
		return Result.OK("编辑成功!");
	}


	/**
	 * 通过sbId删除OEE配置
	 *
	 * @param sbId
	 * @return
	 */
	@AutoLog(value = "线效率-OEE配置-通过sbId删除")
	@Operation(summary="线效率-OEE配置-通过sbId删除")
	@DeleteMapping(value = "/deleteOee")
	public Result<String> deleteOee(@RequestParam(name="sbId",required=true) String sbId) {

		//删除该设备下所有的oee配置
		//查询结束配置
		List<LineEfficiencyHandle> handleEndList = lineEfficiencyHandleService.lambdaQuery()
				.eq(LineEfficiencyHandle::getSbId,sbId)
				.in(LineEfficiencyHandle::getHandleType,Arrays.asList(1,2))
				.list();
		List<String> ids = handleEndList.stream().map(LineEfficiencyHandle::getId).collect(Collectors.toList());
		lineEfficiencyHandleService.removeByIds(ids);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 查询产量
	 * @return
	 */
	@AutoLog(value = "查询产量")
	@Operation(summary="查询产量")
	@PostMapping(value = "/getProductionByTime")
	public Result<String> getProductionByTime() {

		ProductionDto productionDto = new ProductionDto();
		productionDto.setProductionCode("400082");

		LocalDateTime startLocalTime = LocalDateTime.of(2026, 3, 19, 8, 0, 0);
		LocalDateTime endLocalTime = LocalDateTime.of(2026, 3, 19, 20, 0, 0);
		productionDto = lineEfficiencyHandleService.getProductionByTime(startLocalTime,  endLocalTime,productionDto,0.0);
		System.out.println("19号白班:" + productionDto.getProduction());

		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

}
