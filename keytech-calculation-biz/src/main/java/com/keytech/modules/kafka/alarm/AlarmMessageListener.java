package com.keytech.modules.kafka.alarm;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.entity.AlarmKafkaLog;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.service.IAlarmKafkaLogService;
import com.keytech.modules.alarm.service.IAlarmPointService;
import com.keytech.modules.alarm.service.IAlarmReceiveService;
import com.keytech.modules.ems.util.Java8DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 从Kafka中获取消息写入报警平台
 * @author yepeng
 * @since 2026.01.19
 */
@ConditionalOnProperty(
    prefix = "kafka.listener.alarm",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmMessageListener {
    private final IAlarmKafkaLogService alarmKafkaLogService;
    private final IAlarmReceiveService alarmReceiveService;
    private final IAlarmPointService alarmPointService;
    private final Snowflake snowflake = IdUtil.getSnowflake(3L, 3L);

    @KafkaListener(topics = "${spring.kafka.alarm-kafka.topic}", containerFactory = "alarmKafkaListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        try {
            List<AlarmKafkaLog> kafkaLogList = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                log.debug("offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
                JSONObject jsonObject = JSONObject.parse(record.value());
                JSONObject headObj = jsonObject.getJSONObject("header");
                if (headObj == null) {
                    log.warn("报警消息头为空，跳过处理：{}", record.value());
                    continue;
                }
                // 将原始消息添加到批量保存列表
                AlarmKafkaLog kafkaLog = new AlarmKafkaLog();
                kafkaLog.setDataJson(record.value());
                kafkaLog.setInstanceId(headObj.getString("instance_id"));
                kafkaLog.setThingId(headObj.getString("thing_id"));
                //转换为本地时间
                kafkaLog.setMsgTime(OffsetDateTime.parse(headObj.getString("timestamp")).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
                kafkaLog.setKfoffset(record.offset());
                kafkaLog.setCreateTime(LocalDateTime.now());
                kafkaLogList.add(kafkaLog);
            }
            // 只记录报警点位表中存在的报警点位功能位置的报警数据
            List<AlarmPoint> alarmPoints = alarmPointService.lambdaQuery().select(List.of(AlarmPoint::getAlarmPiCode))
                    .eq(AlarmPoint::getEnableFlag, 1).list();
            List<String> alarmPiThingIdList = alarmPoints.stream().map(b-> StringUtils.substringBefore(b.getAlarmPiCode(), "_")).distinct().toList();
            kafkaLogList.removeIf(log -> !alarmPiThingIdList.contains(log.getThingId()));
            if (CollectionUtils.isNotEmpty(kafkaLogList)) {
                // 批量保存到数据库
                alarmKafkaLogService.saveBatch(kafkaLogList);
                // 解析报警数据
                processAlarmData(kafkaLogList);
            }
            // 手动提交offset
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("报警Kafka消费失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 处理报警数据
     * @param kafkaLogList 报警Kafka日志列表
     */
    private void processAlarmData(List<AlarmKafkaLog> kafkaLogList) {
        List<AlarmReceiveRequest> reqList = new ArrayList<>();
        for (AlarmKafkaLog kafkaLog : kafkaLogList) {
            // 解析报警数据
            JSONObject jsonObject = JSONObject.parse(kafkaLog.getDataJson());
            JSONObject headObj = jsonObject.getJSONObject("header");
            JSONObject bodyObj = jsonObject.getJSONObject("body");
            String thingId = headObj.getString("thing_id");
            LocalDateTime msgTime = OffsetDateTime.parse(headObj.getString("timestamp")).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            // 构建报警数据接收请求参数
            for (String key : bodyObj.keySet()) {
                AlarmReceiveRequest req = new AlarmReceiveRequest();
                req.setRequestId(snowflake.nextIdStr());
                req.setAlarmPiCode(thingId + "_" + key + "_" + bodyObj.getInteger(key));
                req.setAlarmTime(Java8DateUtil.format1(msgTime));
                req.setSourceSystem("DF");
                req.setTimestamp(System.currentTimeMillis());
                reqList.add(req);
            }
        }
        // 调用批量报警接收服务
        alarmReceiveService.batchReceive(reqList);
    }

}
