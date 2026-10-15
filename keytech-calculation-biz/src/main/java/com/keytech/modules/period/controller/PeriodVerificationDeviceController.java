package com.keytech.modules.period.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.dto.CopScheduleJson;
import com.keytech.modules.cop.entity.CopConfig;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.impl.MdmWeekServiceImpl;
import com.keytech.modules.period.dto.ScheduleJson;
import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import com.keytech.modules.period.entity.PeriodVerificationSchedule;
import com.keytech.modules.period.service.IPeriodVerificationConfigService;
import com.keytech.modules.period.service.IPeriodVerificationDeviceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 期间核查设备信息
 * @Author: qipengbo
 * @Date:   2025-03-09
 * @Version: V1.0
 */
@Tag(name="期间核查仪器信息")
@RestController
@RequestMapping("/period/periodVerificationDevice")
@Slf4j
public class PeriodVerificationDeviceController extends BaseController<IPeriodVerificationDeviceService, PeriodVerificationDevice> {
	@Autowired
	private IPeriodVerificationDeviceService periodVerificationDeviceService;

	 @Autowired
	 private IPeriodVerificationConfigService periodVerificationConfigService;

	@Autowired
	 private IPeriodVerificationScheduleService periodVerificationScheduleService;

	@Autowired
	private MdmWeekServiceImpl mdmWeekService;

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM.dd");



	/**
	 * 分页列表查询
	 *
	 * @param periodVerificationDevice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="期间核查设备信息-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PeriodVerificationDevice>> queryPageList(PeriodVerificationDevice periodVerificationDevice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		if (StrUtil.isNotBlank(periodVerificationDevice.getApparatusName())) {
			periodVerificationDevice.setApparatusName("*" + periodVerificationDevice.getApparatusName() + "*");
		}
        QueryWrapper<PeriodVerificationDevice> queryWrapper = QueryGenerator.initQueryWrapper(periodVerificationDevice, req.getParameterMap());
		queryWrapper.orderByAsc("sort");
		Page<PeriodVerificationDevice> page = new Page<PeriodVerificationDevice>(pageNo, pageSize);
		IPage<PeriodVerificationDevice> pageList = periodVerificationDeviceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param periodVerificationDevice
	 * @return
	 */
	@AutoLog(value = "期间核查设备信息-添加")
	@Operation(summary="期间核查设备信息-添加")
//	@RequiresPermissions("period:period_verification_device:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PeriodVerificationDevice periodVerificationDevice) {
		long count = periodVerificationDeviceService.count(new LambdaQueryWrapper<PeriodVerificationDevice>().eq(PeriodVerificationDevice::getApparatusName, periodVerificationDevice.getApparatusName()).eq(PeriodVerificationDevice::getDeptCode, periodVerificationDevice.getDeptCode()).eq(PeriodVerificationDevice::getLineCode, periodVerificationDevice.getLineCode()));
		if(count < 1) {
			periodVerificationDevice.setPlanYear(DateUtils.getCurrentYear());
			periodVerificationDeviceService.save(periodVerificationDevice);

			List<PeriodVerificationConfig> list = new ArrayList<>();
			periodVerificationDeviceService.addPeriodVerificationConfig(list, periodVerificationDevice, "3", "仪器显示值", 1.0);

			periodVerificationDeviceService.addPeriodVerificationConfig(list, periodVerificationDevice, "4", "仪器显示值图片", 2.0);

			periodVerificationDeviceService.addPeriodVerificationConfig(list, periodVerificationDevice, "3", "电脑显示值", 3.0);

			periodVerificationDeviceService.addPeriodVerificationConfig(list, periodVerificationDevice, "4", "电脑显示值图片", 4.0);

			periodVerificationDeviceService.addPeriodVerificationConfig(list, periodVerificationDevice, "3", "实际偏差", 5.0);
			periodVerificationConfigService.saveBatch(list);
		} else {
			return Result.error("新增失败，请检查该产线，工段下是否已经存在 "+periodVerificationDevice.getApparatusName());
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param periodVerificationDevice
	 * @return
	 */
	@AutoLog(value = "期间核查设备信息-编辑")
	@Operation(summary="期间核查设备信息-编辑")
//	@RequiresPermissions("period:period_verification_device:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PeriodVerificationDevice periodVerificationDevice) {
		periodVerificationDeviceService.updateById(periodVerificationDevice);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "期间核查设备信息-通过id删除")
	@Operation(summary="期间核查设备信息-通过id删除")
	//@RequiresPermissions("period:period_verification_device:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		//检查是否被绑定
		List<String> configIds = periodVerificationConfigService.list(new LambdaQueryWrapper<PeriodVerificationConfig>().eq(PeriodVerificationConfig::getApparatusId, id)).stream().map(PeriodVerificationConfig::getId).toList();
		if(CollectionUtils.isNotEmpty(configIds)) {
			periodVerificationConfigService.removeBatchByIds(configIds);
		}
		periodVerificationDeviceService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "期间核查设备信息-批量删除")
	@Operation(summary="期间核查设备信息-批量删除")
	//@RequiresPermissions("period:period_verification_device:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		List<String> configIds = periodVerificationConfigService.list(new LambdaQueryWrapper<PeriodVerificationConfig>()
				.in(PeriodVerificationConfig::getApparatusId, Arrays.asList(ids.split(",")))).stream().map(PeriodVerificationConfig::getId).toList();
		if(CollectionUtils.isNotEmpty(configIds)) {
			periodVerificationConfigService.removeBatchByIds(configIds);
		}
		periodVerificationDeviceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="期间核查设备信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PeriodVerificationDevice> queryById(@RequestParam(name="id",required=true) String id) {
		PeriodVerificationDevice periodVerificationDevice = periodVerificationDeviceService.getById(id);
		if(periodVerificationDevice==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(periodVerificationDevice);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param periodVerificationDevice
    */
//    @RequiresPermissions("period:period_verification_device:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PeriodVerificationDevice periodVerificationDevice) {
        return super.exportXls(request, periodVerificationDevice, PeriodVerificationDevice.class, "期间核查仪器配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("period:period_verification_device:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            return periodVerificationDeviceService.importExcelBy(request, response, PeriodVerificationDevice.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	@Operation(summary="期间核查设备信息-复制当前仪器及排期信息")
	@GetMapping(value = "/copyPeriod")
	public Result<?> copyPeriodVerification() {
		long count = periodVerificationDeviceService.count(new LambdaQueryWrapper<PeriodVerificationDevice>()
				.eq(PeriodVerificationDevice::getPlanYear,DateUtils.getCurrentYear()));
		if(count < 1) {
			List<PeriodVerificationDevice> list = periodVerificationDeviceService.list(new LambdaQueryWrapper<PeriodVerificationDevice>()
					.eq(PeriodVerificationDevice::getPlanYear,DateUtils.getCurrentYear()-1));
			if(CollectionUtils.isNotEmpty(list)) {
				for(PeriodVerificationDevice periodVerificationDevice : list) {
					PeriodVerificationDevice copy = new PeriodVerificationDevice();
					BeanUtils.copyProperties(periodVerificationDevice, copy);
					copy.setId(null);
					copy.setPlanYear(DateUtils.getCurrentYear());
					periodVerificationDeviceService.save(copy);
					//复制配置项
					List<PeriodVerificationConfig> configList = periodVerificationConfigService.list(new LambdaQueryWrapper<PeriodVerificationConfig>()
							.eq(PeriodVerificationConfig::getApparatusId,periodVerificationDevice.getId()));
					if(CollectionUtils.isNotEmpty(configList)) {
						for(PeriodVerificationConfig periodVerificationConfig : configList) {
							PeriodVerificationConfig copyConfig = new PeriodVerificationConfig();
							BeanUtils.copyProperties(periodVerificationConfig, copyConfig);
							copyConfig.setId(null);
							copyConfig.setApparatusId(copy.getId());
							periodVerificationConfigService.save(copyConfig);
						}
					}

					//复制排期
					List<PeriodVerificationSchedule> scheduleList = periodVerificationScheduleService.list(new LambdaQueryWrapper<PeriodVerificationSchedule>()
							.eq(PeriodVerificationSchedule::getApparatusId,periodVerificationDevice.getId())
							.eq(PeriodVerificationSchedule::getPlanYear,DateUtils.getCurrentYear()-1));
					if(CollectionUtils.isNotEmpty(scheduleList)) {
						for(PeriodVerificationSchedule periodVerificationSchedule : scheduleList) {
							PeriodVerificationSchedule copySchedule = new PeriodVerificationSchedule();
							BeanUtils.copyProperties(periodVerificationSchedule, copySchedule);
							//设置所有排期状态为计划-1
							List<ScheduleJson> weekList = JSONArray.parseArray(copySchedule.getWeekJson(), ScheduleJson.class);
							if(CollectionUtils.isNotEmpty(weekList)) {
								//用周接口获取一个模板
								MdmWeek mdmWeek = new MdmWeek();
								mdmWeek.setYear(String.valueOf(DateUtils.getCurrentYear()));
								List<MdmWeek> weeks = mdmWeekService.getWeek(mdmWeek);
								Map<Integer,MdmWeek> weekMap = weeks.stream().collect(Collectors.toMap(MdmWeek::getWeekNumber, w -> w));
								weekList.forEach(scheduleJson -> {
									MdmWeek week = weekMap.get(scheduleJson.getNum());
									scheduleJson.setData(SIMPLE_DATE_FORMAT.format(week.getStartDate()) + "-" + SIMPLE_DATE_FORMAT.format(week.getEndDate()));
									scheduleJson.setNum(week.getWeekNumber());
									scheduleJson.setYear(DateUtils.getCurrentYear());
									scheduleJson.setMonth(Integer.valueOf(week.getMonth()));
									scheduleJson.setQuarter(determineQuarter(Integer.valueOf(week.getMonth())));
									//判断上半年还是下半年
									if (scheduleJson.getMonth()<=6) {
										scheduleJson.setHalfYear(0);
									}else {
										scheduleJson.setHalfYear(1);
									}
									if (StringUtils.isNotEmpty(scheduleJson.getState())) {
										scheduleJson.setState("-1");
									}
								});
							}
							copySchedule.setId(null);
							copySchedule.setApparatusId(copy.getId());
							copySchedule.setPlanYear(DateUtils.getCurrentYear());
							copySchedule.setWeekJson(JSONArray.toJSONString(weekList));
							periodVerificationScheduleService.save(copySchedule);
						}
					}
				}
			}
		} else {
			return Result.error("已经存在当年的仪器信息！");
		}

		return Result.ok("复制成功！");
	}

	private int determineQuarter(int month) {
		if (month == 1 || month == 2 || month == 3) {
			return 1;
		} else if (month == 4 || month == 5 || month == 6) {
			return 2;
		} else if (month == 7 || month == 8 || month == 9) {
			return 3;
		} else {
			return 4;
		}
	}

}
