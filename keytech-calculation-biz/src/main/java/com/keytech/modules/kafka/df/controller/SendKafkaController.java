package com.keytech.modules.kafka.df.controller;

import cn.hutool.core.util.IdUtil;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.entity.GapAnalysis;
import com.keytech.modules.kpi.service.IGapAnalysisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: 差距分析
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="差距分析")
@RestController
@RequestMapping("/kafka/send")
@Slf4j
public class SendKafkaController extends BaseController<IGapAnalysisService, GapAnalysis> {
	@Autowired
	@Qualifier("dfKafkaTemplate")
	private KafkaTemplate<String, String> dfKafkaTemplate;

	@GetMapping(value = "/send")
	@Async
	public CompletableFuture<Result<?>> send() {
		// 定义日期输出格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		for (int i = 1; i <= 2000; i++) {
			// 获取当前UTC时间
			OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);
			String id = IdUtil.getSnowflakeNextIdStr();
			String message = "{" +
					"    \"header\": {" +
					"        \"instance_id\": \"8708903dfe694fc0a4b995a039790c93-54994767-01-hwfc1845\"," +
					"        \"thing_id\": \"APAC-CN48301002-"+id+"\"," +
					"        \"thing_model_id\": \"dtml:PKG-BottleWasher:321\"," +
					"        \"thing_model_name\": \"PKG-BottleWasher\"," +
					"        \"timestamp\": \""+now.format(formatter)+"\"" +
					"    }," +
					"    \"body\": {" +
					"        \"/AL_FL-ACV\": 0.41," +
					"        \"/AL_FL-INV\": 0.185" +
					"    }," +
					"    \"$meta\": {" +
					"        \"/AL_FL-ACV\": {" +
					"            \"code\": \"0x00000000\"," +
					"            \"status\": \"GOOD\"" +
					"        }," +
					"        \"/AL_FL-INV\": {" +
					"            \"code\": \"0xFF800000\"," +
					"            \"status\": \"Bad\"" +
					"        }" +
					"    }" +
					"}";
			dfKafkaTemplate.send("test", message);
		}
		System.out.println("--------df发送完成---------");
		return CompletableFuture.completedFuture(Result.OK());
	}
}
