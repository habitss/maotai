//package com.keytech.modules.dataSync.config;
//
//import org.influxdb.BatchOptions;
//import org.influxdb.InfluxDB;
//import org.influxdb.InfluxDBFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * 定时任务+InfluxDB配置
// */
//@Configuration
//@EnableScheduling
//public class TaskConfig {
//
//    @Bean
//    public InfluxDB influxDB() {
//        String url = "http://192.168.1.10:8086";
//        String username = "root";
//        String password = "123456";
//        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
//
//        // 设置数据库和保留策略
//        influxDB.setDatabase("ccr_db");
//        influxDB.setRetentionPolicy("autogen");
//
//        // 2.23版本正确的批量写入配置（无需BatchOptions）
//        influxDB.enableBatch(
//                2000,    // 每1000条数据批量提交
//                100,     // 每100毫秒自动刷新一次
//                TimeUnit.MILLISECONDS // 时间单位
//        );
//
//        // 开启Gzip压缩
//        influxDB.enableGzip();
//        return influxDB;
//    }
//}