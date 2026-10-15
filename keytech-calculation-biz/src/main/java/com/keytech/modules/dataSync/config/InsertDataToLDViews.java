package com.keytech.modules.dataSync.config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsertDataToLDViews {

    // 数据库连接信息
    private static final String URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=MES";
    private static final String USER = "sa";
    private static final String PASSWORD = "wx147258";

    // 设备状态常量
    private static final int STATUS_STOP = 1;
    private static final int STATUS_START = 2;
    private static final int STATUS_READY = 4;
    private static final int STATUS_NO_MATERIAL = 8;
    private static final int STATUS_BLOCKED = 16;
    private static final int STATUS_RUNNING = 128;
    private static final int STATUS_EQUIPMENT_FAULT = 1024;
    private static final int STATUS_EMERGENCY_STOP = 4096;
    private static final int STATUS_STANDBY = 16384;

    // 操作模式常量
    private static final int MODE_CLOSED = 1;
    private static final int MODE_MANUAL = 2;
    private static final int MODE_SEMI_AUTO = 4;
    private static final int MODE_AUTO = 8;

    public static void main(String[] args) {
        System.out.println("开始插入数据...");
        insertCurrentData();
        insertHistoryData();
        System.out.println("数据插入完成！");
    }

    /**
     * 向即时值视图插入多条数据
     */
    private static void insertCurrentData() {
        String sql = "INSERT INTO [dbo].[V_DATAPOINT_LDS_YANJING_CURRENT] " +
                "(Data_Point_ID, Data_Point_Name, Equipment_ID, Equipment_Name, UOM_Name, Value, TIMESTAMP_FROM) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // 准备多组实时数据
        List<CurrentDataPoint> currentDataList = new ArrayList<>();

        // 添加实时数据
        currentDataList.add(new CurrentDataPoint(1001, "发酵罐1温度", 101, "发酵罐1", "°C", 24.8f));
        currentDataList.add(new CurrentDataPoint(1002, "发酵罐1压力", 101, "发酵罐1", "bar", 1.2f));
        currentDataList.add(new CurrentDataPoint(1003, "发酵罐1液位", 101, "发酵罐1", "%", 85.5f));
        currentDataList.add(new CurrentDataPoint(1004, "发酵罐1PH值", 101, "发酵罐1", "pH", 5.8f));
        currentDataList.add(new CurrentDataPoint(1005, "发酵罐1状态", 101, "发酵罐1", "状态码", STATUS_RUNNING));

        currentDataList.add(new CurrentDataPoint(1006, "发酵罐2温度", 102, "发酵罐2", "°C", 25.2f));
        currentDataList.add(new CurrentDataPoint(1007, "发酵罐2压力", 102, "发酵罐2", "bar", 1.1f));
        currentDataList.add(new CurrentDataPoint(1008, "发酵罐2状态", 102, "发酵罐2", "状态码", STATUS_STANDBY));

        currentDataList.add(new CurrentDataPoint(1009, "灌装机1速度", 201, "灌装机1", "瓶/分钟", 120.0f));
        currentDataList.add(new CurrentDataPoint(1010, "灌装机1状态", 201, "灌装机1", "状态码", STATUS_RUNNING));
        currentDataList.add(new CurrentDataPoint(1011, "灌装机1操作模式", 201, "灌装机1", "模式码", MODE_AUTO));
        currentDataList.add(new CurrentDataPoint(1012, "灌装机1产量", 201, "灌装机1", "瓶", 12500.0f));

        currentDataList.add(new CurrentDataPoint(1013, "灌装机2速度", 202, "灌装机2", "瓶/分钟", 0.0f));
        currentDataList.add(new CurrentDataPoint(1014, "灌装机2状态", 202, "灌装机2", "状态码", STATUS_EQUIPMENT_FAULT));
        currentDataList.add(new CurrentDataPoint(1015, "灌装机2操作模式", 202, "灌装机2", "模式码", MODE_CLOSED));

        currentDataList.add(new CurrentDataPoint(1016, "贴标机1速度", 301, "贴标机1", "瓶/分钟", 115.0f));
        currentDataList.add(new CurrentDataPoint(1017, "贴标机1状态", 301, "贴标机1", "状态码", STATUS_RUNNING));
        currentDataList.add(new CurrentDataPoint(1018, "贴标机1故障代码", 301, "贴标机1", "代码", 0.0f));

        currentDataList.add(new CurrentDataPoint(1019, "包装机1速度", 401, "包装机1", "箱/小时", 45.0f));
        currentDataList.add(new CurrentDataPoint(1020, "包装机1状态", 401, "包装机1", "状态码", STATUS_BLOCKED));
        currentDataList.add(new CurrentDataPoint(1021, "包装机1操作模式", 401, "包装机1", "模式码", MODE_SEMI_AUTO));

        currentDataList.add(new CurrentDataPoint(1022, "空压机1压力", 501, "空压机1", "MPa", 0.75f));
        currentDataList.add(new CurrentDataPoint(1023, "空压机1温度", 501, "空压机1", "°C", 35.2f));
        currentDataList.add(new CurrentDataPoint(1024, "空压机1状态", 501, "空压机1", "状态码", STATUS_RUNNING));

        currentDataList.add(new CurrentDataPoint(1025, "水处理PH值", 601, "水处理设备", "pH", 7.2f));
        currentDataList.add(new CurrentDataPoint(1026, "水处理电导率", 601, "水处理设备", "μS/cm", 15.8f));
        currentDataList.add(new CurrentDataPoint(1027, "水处理状态", 601, "水处理设备", "状态码", STATUS_READY));

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int totalInserted = 0;
            long currentTime = System.currentTimeMillis();

            for (CurrentDataPoint data : currentDataList) {
                pstmt.setInt(1, data.dataPointId);
                pstmt.setString(2, data.dataPointName);
                pstmt.setInt(3, data.equipmentId);
                pstmt.setString(4, data.equipmentName);
                pstmt.setString(5, data.uomName);
                pstmt.setFloat(6, data.value);
                pstmt.setTimestamp(7, new Timestamp(currentTime)); // 使用相同的时间戳

                int rowsAffected = pstmt.executeUpdate();
                totalInserted += rowsAffected;
                System.out.println("插入实时数据点: " + data.dataPointName + " (设备: " + data.equipmentName + ")");
            }

            System.out.println("即时值视图插入完成，共插入 " + totalInserted + " 条数据");

        } catch (SQLException e) {
            System.err.println("插入即时值数据时出错:");
            e.printStackTrace();
        }
    }

    /**
     * 向历史值视图插入多条数据
     */
    private static void insertHistoryData() {
        String sql = "INSERT INTO [dbo].[V_DATAPOINT_LDS_YANJING_HISTORY] " +
                "(Data_Point_ID, Data_Point_Name, Equipment_ID, Equipment_Name, UOM_Name, Value, TIMESTAMP_FROM, TIMESTAMP_TO) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // 准备多组历史数据
        List<HistoryDataPoint> historyDataList = new ArrayList<>();

        long currentTime = System.currentTimeMillis();
        long hourMs = 3600 * 1000L; // 1小时的毫秒数

        // 添加历史数据 - 发酵罐1温度历史记录（每小时一个点）
        for (int i = 0; i < 24; i++) {
            long timestampFrom = currentTime - (24 - i) * hourMs;
            long timestampTo = timestampFrom + hourMs;
            float temperature = 24.0f + i * 0.1f + (float)Math.random() * 0.2f; // 模拟温度变化

            historyDataList.add(new HistoryDataPoint(
                    1001, "发酵罐1温度_历史", 101, "发酵罐1", "°C",
                    temperature, timestampFrom, timestampTo
            ));
        }

        // 添加历史数据 - 灌装机1速度历史记录
        for (int i = 0; i < 12; i++) {
            long timestampFrom = currentTime - (12 - i) * 2 * hourMs; // 每2小时一个点
            long timestampTo = timestampFrom + 2 * hourMs;
            float speed = 110.0f + i * 2.0f + (float)Math.random() * 3.0f; // 模拟速度变化

            historyDataList.add(new HistoryDataPoint(
                    1009, "灌装机1速度_历史", 201, "灌装机1", "瓶/分钟",
                    speed, timestampFrom, timestampTo
            ));
        }

        // 添加历史数据 - 灌装机1产量累积
        float cumulativeOutput = 12000.0f;
        for (int i = 0; i < 8; i++) {
            long timestampFrom = currentTime - (8 - i) * 3 * hourMs; // 每3小时一个点
            long timestampTo = timestampFrom + 3 * hourMs;
            cumulativeOutput += 150.0f + (float)Math.random() * 100.0f; // 每个时段增加产量

            historyDataList.add(new HistoryDataPoint(
                    1012, "灌装机1产量_历史", 201, "灌装机1", "瓶",
                    cumulativeOutput, timestampFrom, timestampTo
            ));
        }

        // 添加历史数据 - 发酵罐1压力
        for (int i = 0; i < 6; i++) {
            long timestampFrom = currentTime - (6 - i) * 4 * hourMs; // 每4小时一个点
            long timestampTo = timestampFrom + 4 * hourMs;
            float pressure = 1.0f + i * 0.05f + (float)Math.random() * 0.1f; // 模拟压力变化

            historyDataList.add(new HistoryDataPoint(
                    1002, "发酵罐1压力_历史", 101, "发酵罐1", "bar",
                    pressure, timestampFrom, timestampTo
            ));
        }

        // 添加历史数据 - 空压机1压力
        for (int i = 0; i < 10; i++) {
            long timestampFrom = currentTime - (10 - i) * hourMs; // 每小时一个点
            long timestampTo = timestampFrom + hourMs;
            float pressure = 0.7f + i * 0.01f + (float)Math.random() * 0.05f; // 模拟压力变化

            historyDataList.add(new HistoryDataPoint(
                    1022, "空压机1压力_历史", 501, "空压机1", "MPa",
                    pressure, timestampFrom, timestampTo
            ));
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int totalInserted = 0;

            for (HistoryDataPoint data : historyDataList) {
                pstmt.setInt(1, data.dataPointId);
                pstmt.setString(2, data.dataPointName);
                pstmt.setInt(3, data.equipmentId);
                pstmt.setString(4, data.equipmentName);
                pstmt.setString(5, data.uomName);
                pstmt.setFloat(6, data.value);
                pstmt.setTimestamp(7, new Timestamp(data.timestampFrom));
                pstmt.setTimestamp(8, new Timestamp(data.timestampTo));

                int rowsAffected = pstmt.executeUpdate();
                totalInserted += rowsAffected;

                if (totalInserted % 10 == 0) {
                    System.out.println("已插入 " + totalInserted + " 条历史数据...");
                }
            }

            System.out.println("历史值视图插入完成，共插入 " + totalInserted + " 条数据");

        } catch (SQLException e) {
            System.err.println("插入历史值数据时出错:");
            e.printStackTrace();
        }
    }

    /**
     * 实时数据点实体类
     */
    static class CurrentDataPoint {
        int dataPointId;
        String dataPointName;
        int equipmentId;
        String equipmentName;
        String uomName;
        float value;

        public CurrentDataPoint(int dataPointId, String dataPointName, int equipmentId,
                                String equipmentName, String uomName, float value) {
            this.dataPointId = dataPointId;
            this.dataPointName = dataPointName;
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.uomName = uomName;
            this.value = value;
        }
    }

    /**
     * 历史数据点实体类
     */
    static class HistoryDataPoint {
        int dataPointId;
        String dataPointName;
        int equipmentId;
        String equipmentName;
        String uomName;
        float value;
        long timestampFrom;
        long timestampTo;

        public HistoryDataPoint(int dataPointId, String dataPointName, int equipmentId,
                                String equipmentName, String uomName, float value,
                                long timestampFrom, long timestampTo) {
            this.dataPointId = dataPointId;
            this.dataPointName = dataPointName;
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.uomName = uomName;
            this.value = value;
            this.timestampFrom = timestampFrom;
            this.timestampTo = timestampTo;
        }
    }
}