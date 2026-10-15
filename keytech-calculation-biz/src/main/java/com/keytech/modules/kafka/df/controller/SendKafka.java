package com.keytech.modules.kafka.df.controller;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/1/14 17:20
 */
@Service
public class SendKafka {
    @Autowired
    @Qualifier("sourceKafkaTemplate")
    private static KafkaTemplate<String, String> sourceKafkaTemplate;

    public static void main(String[] args) {
        send();
    }

    static void send(){
        // 定义日期输出格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        // 配置Kafka生产者的属性
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.3.129:9092"); // 替换为你的Kafka服务器地址
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 创建Kafka生产者实例
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            // 定义主题名称
            String topic = "test"; // 替换为实际的主题名

            // 循环写入一万条数据
            for (int i = 1; i <= 6000; i++) {
                // 获取当前UTC时间
                OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);

                ProducerRecord<String, String> record = new ProducerRecord<>(topic, Integer.toString(i), "{\"header\":{\"instance_id\":\"8708903dfe694fc0a4b995a039790c93-54994767-01-hwfc1845\",\"thing_id\":\"APAC-CN54301002-"+String.format("%04d", i)+"\",\"thing_model_id\":\"dtml:PKG-BottleWasher:321\",\"thing_model_name\":\"PKG-BottleWasher\",\"timestamp\":\""+now.format(formatter)+"\"},\"body\":{\"/AL_FL-ACV\":0.41,\"/AL_FL-INV\":0.185},\"$meta\":{\"/AL_FL-ACV\":{\"code\":\"0x00000000\",\"status\":\"GOOD\"},\"/AL_RC_PRBD-INV\":{\"code\":\"0xFF800000\",\"status\":\"Bad\"}}}");

                // 异步发送消息
                Future<RecordMetadata> future = producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception == null) {
                            System.out.println("Message sent successfully to partition " + metadata.partition() +
                                    ", offset " + metadata.offset());
                        } else {
                            exception.printStackTrace();
                        }
                    }
                });

//                 如果你想同步确认每条消息是否成功发送，可以取消注释以下两行
                try {
                    future.get(); // 等待发送结果
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 让生产者有足够的时间来发送所有消息并处理回调
            producer.flush();

            System.out.println("======================================================");
            System.out.println("======================================================");
            System.out.println("======================================================");
            System.out.println("消息发送完毕!");
            System.out.println("======================================================");
            System.out.println("======================================================");
            System.out.println("======================================================");
        }
    }



    static void send2(){
        // 获取当前系统默认时区的时间
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        // 打印完整的时间信息
        System.out.println("Current time: " + now.format(formatter));
    }
}
