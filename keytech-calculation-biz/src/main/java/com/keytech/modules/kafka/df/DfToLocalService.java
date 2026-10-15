package com.keytech.modules.kafka.df;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 消费DF数据传输到本地kafka
 * @Author: sjw
 * @Date: 2025/1/13 9:35
 */
@Service
@Slf4j
@Profile("kafka")
public class DfToLocalService {
    @Autowired
    @Qualifier("localKafkaTemplate")
    private KafkaTemplate<String, String> localKafkaTemplate;

    @Value("${spring.kafka.local-kafka.topic}")
    private String localTopic;

    @KafkaListener(topics = "${spring.kafka.df-kafka.topic}", containerFactory = "dfKafkaListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        try {
            List<String> messagesForAnotherTopic = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                // 处理每一条记录
                messagesForAnotherTopic.add(record.value()); // 假设我们要将这些消息转发到另一个topic
            }
            // 将收集的消息批量发送到另一个topic
            localKafkaTemplate.send(localTopic, messagesForAnotherTopic.toString());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("kafka消费失败{}", e.getMessage());
        }
    }

}
