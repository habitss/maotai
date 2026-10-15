package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiSyncConfig;
import com.keytech.modules.pi.service.IPiSyncConfigService;
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
import java.util.Map;

/**
 * @Description: PI数据同步配置
 * @Author: jeecg-boot
 * @Date:   2026-03-31
 * @Version: V1.0
 */
@Tag(name="PI数据同步配置")
@RestController
@RequestMapping("/pi/piSyncConfig")
@Slf4j
public class PiSyncConfigController extends BaseController<IPiSyncConfigService, PiSyncConfig> {
	@Autowired
	private IPiSyncConfigService piSyncConfigService;

	/**
	 * 定时任务接口-PI数据同步
	 * 参数
	 * groupKey  数据类型 1交叉进罐 2.酵母排放
	 * fewDay   源几天内数据(默认3天)
	 */
	@AutoLog(value = "定时任务接口-PI数据同步")
	@Operation(summary = "定时任务接口-PI数据同步", description = "定时任务接口-PI数据同步")
	@PostMapping(value = "/intersectionData")
	public Result<String> intersectionData(@RequestBody Map map) {
		log.info("PI数据同步开始执行---ok");
		// 通过不同groupKey 执行不同方法  1 intersectionData, 其他 sourceSyncData
		String groupKey = map.get("groupKey") != null ? map.get("groupKey").toString() : "";
		if ("1".equals(groupKey)) {
			//交叉进罐(行转列)
			piSyncConfigService.intersectionData(map);
		} else if("2".equals(groupKey)){
			//废酵母排放(其他)
			piSyncConfigService.sourceSyncData(map);
		} else if("3".equals(groupKey)){
			//酵母回收(数据同步,A同步B)
			piSyncConfigService.synchronization(map);
		}
		log.info("PI数据同步执行完成---ok,");
		return Result.OK("调用成功！");
	}

}
