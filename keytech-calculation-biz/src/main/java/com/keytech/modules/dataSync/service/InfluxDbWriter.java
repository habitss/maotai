//package com.keytech.modules.dataSync.service;
//
//import com.keytech.modules.dataSync.config.DataPoint;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.influxdb.InfluxDB;
//import org.influxdb.dto.BatchPoints;
//import org.influxdb.dto.Point;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class InfluxDbWriter {
//    private final InfluxDB influxDB;
//    private String dbName = "ccr_db";
//    private String rpName = "autogen";
//    private String measurement = "ccr_table";
//
//    /**
//     * 单个写入
//     */
//    public void writeRealtimeData(DataPoint dataPoint) {
//        if (dataPoint.getTimestampFrom() == null || dataPoint.getValue() == null) {
//            log.warn("实时数据字段不全，跳过写入: {}", dataPoint);
//            return;
//        }
//        try {
//            // 转换为毫秒级时间戳
//            long timestamp = dataPoint.getTimestampFrom()
//                    .toInstant(ZoneOffset.UTC)
//                    .toEpochMilli();
//
//            Point point = Point.measurement(measurement) // 使用注入的测量值名
//                    .time(timestamp, TimeUnit.MILLISECONDS)
//                    .tag("dataPointId", dataPoint.getDataPointId())
////                    .tag("dataPointName", dataPoint.getDataPointName())
////                    .tag("equipmentId", dataPoint.getEquipmentId())
////                    .tag("equipmentName", dataPoint.getEquipmentName())
////                    .tag("uomName", dataPoint.getUomName())
//                    .addField("value", dataPoint.getValue().doubleValue())
//                    .build();
//
//            influxDB.write(dbName, rpName, point); // 使用注入的库名/保留策略
//        } catch (Exception e) {
//            log.error("写入实时数据失败: {}", dataPoint, e);
//        }
//    }
//
//    /**
//     * 批量写入
//     */
//    public void batchWriteHistoryData(List<Point> points) {
//        if (points.isEmpty()) {
//            log.warn("批量写入数据为空，跳过");
//            return;
//        }
//        try {
//            // 用BatchPoints.Builder构建，调用point()添加Point
//            BatchPoints.Builder batchBuilder = BatchPoints
//                    .database(dbName) // 注入的库名
//                    .retentionPolicy(rpName); // 注入的保留策略
//
//            // 遍历List<Point>，调用point()添加
//            for (Point point : points) {
//                batchBuilder.point(point);
//            }
//
//            // 构建BatchPoints并写入
//            BatchPoints batchPoints = batchBuilder.build();
//            influxDB.write(batchPoints);
//            log.info("批量写入{}条历史数据成功", points.size());
//        } catch (Exception e) {
//            log.error("批量写入历史数据失败", e);
//        }
//    }
//
//
//    /**
//     * 构建历史数据 Point
//     */
//    public Point buildHistoryPoint(DataPoint dataPoint) {
//        if (dataPoint.getTimestampFrom() == null || dataPoint.getValue() == null) {
//            return null;
//        }
//
//        try {
//            // 使用写入时间作为InfluxDB主time，业务时间作为字段
//            long timestamp = dataPoint.getTimestampFrom()
//                    .toInstant(ZoneOffset.UTC)
//                    .toEpochMilli();
//
//            return Point.measurement(measurement)
//                    .time(timestamp, TimeUnit.MILLISECONDS)
//                    .tag("dataPointId", dataPoint.getDataPointId())
////                    .tag("dataPointName", dataPoint.getDataPointName())
////                    .tag("equipmentId", dataPoint.getEquipmentId())
////                    .tag("equipmentName", dataPoint.getEquipmentName())
////                    .tag("uomName", dataPoint.getUomName())
//                    //.tag("dataType", "history")
//                    .addField("value", dataPoint.getValue().doubleValue())
////                    .addField("timestampTo", dataPoint.getTimestampTo() != null ?
////                            dataPoint.getTimestampTo().toInstant(ZoneOffset.UTC).toEpochMilli() : 0)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("构建实时Point失败: {}", dataPoint, e);
//            return null;
//        }
//    }
//}