package com.keytech.modules.kafka.df;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.keytech.modules.kafka.df.config.DfSiteEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 本地kafka数据写入InfluxDB
 * @Author: sjw
 * @Date: 2025/3/26 18:10
 */
@Service
@Slf4j
@Profile("kafka")
public class LocalToInfluxDBService {

    private final WriteApi writeApi;

    @Autowired
    public LocalToInfluxDBService(InfluxDBClient influxDBClient) {
        this.writeApi = influxDBClient.makeWriteApi(); // 单例复用
    }

    @KafkaListener(topics = "${spring.kafka.local-kafka.topic}",
            containerFactory = "localKafkaListenerContainerFactory"
    )
    public void listen(List<ConsumerRecord<?, ?>> records, Acknowledgment acknowledgment) {
        try {
            //点位数据集合
            List<Point> pointList = new ArrayList<>();
            if(CollUtil.isNotEmpty(records)){
                for (ConsumerRecord<?, ?> record : records) {
                    //判断是否为字符串并且不能为空，或者是否为jsonarray对象
                    if(StrUtil.isBlankIfStr(record.value()) || !JSONUtil.isTypeJSONArray(record.value().toString())){
                        continue;
                    }

                    JSONArray jsonArray = JSONUtil.parseArray(record.value());
                    for (Object o : jsonArray) {
                        //判断是否为json对象
                        if (!JSONUtil.isTypeJSONObject(o.toString())) {
                            continue;
                        }
                        //解析数据
                        JSONObject jsonObject = JSONUtil.parseObj(o);
                        JSONObject headerObj = jsonObject.getJSONObject("header");
                        JSONObject bodyObj = jsonObject.getJSONObject("body");
                        JSONObject metaObj = jsonObject.getJSONObject("$meta");

                        //设置measurement名称
                        String measurement = DfSiteEnum.getNameByCode(headerObj.getStr("thing_id").substring(5, 9)) + "_IIOT_DATA";
                        String thingId = headerObj.getStr("thing_id");
                        for (String key : bodyObj.keySet()) {
                            //设置表
                            Point point = new Point(measurement);
                            //设置tag
                            point.addTag("thingid", thingId);
                            point.addTag("property", key);
                            //设置field
                            point.addField("insertTime", DateUtil.now());
                            point.addField("value", bodyObj.getDouble(key));
                            if (null != metaObj && null != metaObj.getJSONObject(key)) {
                                point.addField("statusCode", metaObj.getJSONObject(key).getStr("code"));
                            }
                            if (null != headerObj.getStr("timestamp")) {
                                point.time(Instant.parse(headerObj.getStr("timestamp")), WritePrecision.NS);
                            }
                            pointList.add(point);
                            if (pointList.size() == 2000) {
                                //每2000条数据写入InfluxDB一次
                                writeApi.writePoints(pointList);
                                pointList = new ArrayList<>();
                            }
                        }
                    }
                }
            }
            if(!pointList.isEmpty()){
                //将循环中剩余未的list数据写入InfluxDB数据
                writeApi.writePoints(pointList);
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("kafka消费失败{}", e.getMessage());
        }
    }

}
