package com.keytech.modules.kafka.alarm.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka报警数据接入配置
 * @author yepeng
 * @since 2026.01.19
 */
@Configuration
@EnableKafka
public class KafkaAlarmConfig {
    // Kafka 报警数据接入配置
    @Value("${spring.kafka.alarm-kafka.bootstrap-servers}")
    private List<String> bootstrapServers;
    // 是否启用批量监听
    @Value("${spring.kafka.alarm-kafka.enable-batch}")
    private boolean enableBatch;
    // 接入消费者组ID
    @Value("${spring.kafka.alarm-kafka.consumer.group-id}")
    private String groupId;
    // 接入消费者自动偏移重置策略
    @Value("${spring.kafka.alarm-kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;
    // 接入消费者是否启用自动提交偏移量
    @Value("${spring.kafka.alarm-kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Bean(name = "alarmKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            @Qualifier("alarmKafkaConsumerFactory") ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(enableBatch);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean(name = "alarmKafkaConsumerFactory")
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);

        return props;
    }
}
