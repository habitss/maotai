package com.keytech.modules.df.mdm.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.keytech.modules.alarm.job.AlarmReqTsmJobHandler;
import com.keytech.modules.df.mdm.job.SyncDfMdmDataJob;
import com.keytech.modules.df.mdm.service.ISyncDfMdmDataService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MDM-定时任务
 * @author yepeng
 * @since 2025.12.08
 * */
@Tag(name="MDM-定时任务")
@RestController
@RequestMapping("/df/mdm/syncJob")
@Slf4j
public class DfMdmJobController {
	@Autowired
	private SyncDfMdmDataJob syncDfMdmDataJob;
	@Autowired
	private ISyncDfMdmDataService syncDfMdmDataService;
	@Autowired
	private AlarmReqTsmJobHandler alarmReqTsmJobHandler;


	 /**
	  * 定时任务-同步指标数据
	  * @param paramJson 定时任务参数
	  * @return 执行结果
	  */
	 @PostMapping(value = "/doSyncMdmIndicatorJob")
	 public ReturnT<String> doSyncMdmIndicatorJob(@RequestBody String paramJson) {
		 JSONObject jsonObject = JSONUtil.parseObj(paramJson);
         return syncDfMdmDataJob.doSyncMdmIndicatorJob(jsonObject.getStr("param"));
	 }
	 /**
	  * 定时任务-同步配方数据
	  * @param paramJson 定时任务参数
	  * @return 执行结果
	  */
	 @PostMapping(value = "/doSyncMdmRecipeJob")
	 public ReturnT<String> doSyncMdmRecipeJob(@RequestBody String paramJson) {
		 JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		 return syncDfMdmDataJob.doSyncMdmRecipeJob(jsonObject.getStr("param"));
	 }

	 /**
	  * 定时任务-同步设备数据
	  * @param paramJson 定时任务参数
	  * @return 执行结果
	  */
	 @PostMapping(value = "/doSyncMdmDeviceJob")
	 public ReturnT<String> doSyncMdmDeviceJob(@RequestBody String paramJson) {
		 JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		 String params = jsonObject.getStr("param");
		 if(params == null) {
			 params = XxlJobHelper.getJobParam();
			 log.info("参数：{}", params);
		 }
		 String[] args = params.split(",");
		 Integer tenantId = Integer.parseInt(args[0]);
		 return syncDfMdmDataService.syncMdmDevice(tenantId);
	 }

	/**
	 * 定时任务-同步指标数据
	 * @param paramJson 定时任务参数
	 * @return 执行结果
	 */
	@PostMapping(value = "/doSyncMdmIndStandJob")
	public ReturnT<String> doSyncMdmIndStandJob(@RequestBody String paramJson) {
		JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		return syncDfMdmDataJob.syncMdmDataJob(jsonObject.getStr("param"));
	}

	/**
	 * 定时任务-每小时执行一次MDM报警点位主数据的同步
	 * @param paramJson 定时任务参数
	 * @return 执行结果
	 */
	@PostMapping(value = "/doSyncMdmAlarmtagJob")
	public ReturnT<String> doSyncMdmAlarmtagJob(@RequestBody String paramJson) {
		JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		return syncDfMdmDataJob.doSyncMdmAlarmtagJob(jsonObject.getStr("param"));
	}

	/**
	 * 报警事实-TSM请求 定时任务（每2分钟执行一次）
	 * 每次查询时间最早的50条报警事件，推送到TSM，推送成功后，将报警事件状态修改为已响应
	 * @param paramJson 定时任务参数
	 * @return 执行结果
	 */
	@PostMapping(value = "/pushAlarmToTsmJob")
	public ReturnT<String> pushAlarmToTsmJob(@RequestBody String paramJson) {
		JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		return alarmReqTsmJobHandler.handlePushAlarmToTsmJob(jsonObject.getStr("param"));
	}

	/**
	 * 报警事实-TSM任务状态查询 定时任务（每2分钟执行一次）
	 * 每次查询时间最早的50条已响应的报警事件，通过TSM查询接口查询，将报警事件状态修改为处理中/已处理
	 * @param paramJson 定时任务参数
	 * @return 执行结果
	 */
	@PostMapping(value = "/queryAlarmTsmStateJob(")
	public ReturnT<String> queryAlarmTsmStateJob(@RequestBody String paramJson) {
		JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		return alarmReqTsmJobHandler.handleQueryAlarmTsmStateJob(jsonObject.getStr("param"));
	}
}
