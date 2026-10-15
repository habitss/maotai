package com.keytech.modules.dcp.data.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.keytech.modules.dcp.data.service.ISyncDcpDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * DF数据日志表
 * @author yepeng
 * @since 2025.12.11
 * */
@Tag(name="DF数据日志表")
@RestController
@RequestMapping("/dcp/dcpData")
@Slf4j
public class DcpDataController{
	@Autowired
	private ISyncDcpDataService syncDcpDataService;

	@PostMapping(value = "/syncDcpDataJob")
	public Result<String> syncDcpDataJob(@RequestBody String paramJson) {
		JSONObject jsonObject = JSONUtil.parseObj(paramJson);
		syncDcpDataService.syncDcpData(1006, jsonObject.getStr("sDate"), jsonObject.getStr("eDate"));
//		Date sd = DateUtil.parse("2026-02-09 08:00:00");
//		Date ed = DateUtil.parse("2026-02-09 09:00:00");
//		for(int i=0;i<36;i++){
//			syncDcpDataService.syncDcpData(
//					1006,
//					DateUtil.format(DateUtil.offsetHour(sd, i), "yyyy-MM-dd HH:mm:ss"),
//					DateUtil.format(DateUtil.offsetHour(ed, i), "yyyy-MM-dd HH:mm:ss")
//			);
//		}
		return Result.OK("添加成功！");
	}

}
