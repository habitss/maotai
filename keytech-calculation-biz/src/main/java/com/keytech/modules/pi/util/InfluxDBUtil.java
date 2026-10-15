package com.keytech.modules.pi.util;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.keytech.modules.pi.common.InfluxDBProperties;
import com.keytech.modules.pi.common.MeasurementData;
import okhttp3.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @FileName InfluxDBUtil
 * @Description
 * @Author HQ
 * @date 2025-08-19
 **/
public class InfluxDBUtil {

    private final InfluxDBProperties properties;
    private final OkHttpClient client;
    private InfluxDBClient influxDBClient;
    private final String tag;

    public InfluxDBUtil(InfluxDBProperties properties, String tag) {
        this.properties = properties;
        this.tag = tag;
        try {
            this.client = OkHttpClientFactory.createIgnoreSslClient();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 初始化InfluxDB
     */
    public void init() {
        // 判断版本
        if ("2.x".equalsIgnoreCase(properties.getVersion())) {
            // 2.x 版本
            influxDBClient = InfluxDBClientFactory.create(
                    properties.getUrl(),
                    properties.getToken().toCharArray(),
                    properties.getOrg(),
                    properties.getBucket()
            );
            influxDBClient.setLogLevel(LogLevel.BASIC);
        } else {
            //1.8 版本
            System.out.println("InfluxDB 1.8 模式，使用 BasicAuth 调用 /api/v2/query");
        }
    }

    /**
     *  根据不通版本查询数据
     */
    public List<MeasurementData> query(String fluxQuery) throws IOException {
        if ("2.x".equalsIgnoreCase(properties.getVersion())) {
            //2.x版本
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> list = queryApi.query(fluxQuery);
            List<FluxRecord> plisher = new ArrayList<>();
            if(null !=list && list.size()>0){
                for(FluxTable table:list){
                    plisher.addAll(table.getRecords());
                }
            }
            return convertToMeasurementDataList(plisher);
        } else {
            //1.8版本
            return queryWithHttp(fluxQuery);
        }
    }

    /**
     * 1.8版本 查询数据
     * @param fluxQuery
     * @return
     * @throws IOException
     */
    private List<MeasurementData> queryWithHttp(String fluxQuery) throws IOException {
        //设置用户名 密码
        String credentials = properties.getUsername() + ":" + properties.getPassword();
        String basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String bodyJson = "{\"query\": \"" + fluxQuery.replace("\"", "\\\"") + "\"}";
        RequestBody body = RequestBody.create(bodyJson, JSON);
        //请求influxDB接口
        Request request = new Request.Builder()
                .url(properties.getUrl() + "api/v2/query?org=-")
                .addHeader("Authorization", basicAuth)
                .addHeader("Accept", "application/csv")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Query failed: " + response);
        }
        String csv = response.body().string();
        return parseToDataPoints(csv);
    }

    /**
     * 关闭
     */
    public void close() {
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }

    /**
     * 数据转换 csv 转 List<MeasurementData>
     * @param csvData
     * @return
     */
    public List<MeasurementData> parseToDataPoints(String csvData) {
        List<MeasurementData> result = new ArrayList<>();

        // 手动指定 header（与 InfluxDB 返回的一致）
        String[] headers = {
                "blank","result", "table", "_start", "_stop", "_time",
                "_value", "_field", "_measurement", "property", "thingid"
        };

        try (CSVParser parser = CSVParser.parse(
                new StringReader(csvData),
                CSVFormat.DEFAULT.withHeader(headers).withSkipHeaderRecord()
        )) {
            for (CSVRecord record : parser) {
                try {
                    String timeStr = record.get("_time");
                    String valueStr = record.get("_value");
                    String thingid = record.get("thingid");
                    String property = record.get("property");

                    if (timeStr == null || valueStr == null || valueStr.isEmpty()) {
                        continue; // 跳过空值
                    }

                    MeasurementData point = new MeasurementData(
                            Instant.parse(timeStr),
                            thingid + "-" + property,
                            Double.parseDouble(valueStr)
                    );
                    result.add(point);
                } catch (Exception innerEx) {
                    System.err.println("Skipped invalid record: " + innerEx.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV", e);
        }
        return result;
    }

    /**
     * 将FluxRecord列表转换为MeasurementData列表
     *
     * @param fluxRecords FluxRecord对象列表
     * @return MeasurementData对象列表
     */
    public List<MeasurementData> convertToMeasurementDataList(List<FluxRecord> fluxRecords) {
        //转换
        List<MeasurementData> measurementDataList = fluxRecords.stream()
                .map(record -> new MeasurementData(
                        record.getTime(), //时间
                        record.getValueByKey(tag).toString(), //tag
                        Double.parseDouble(record.getValueByKey("_value").toString()) //值
                ))
                .collect(Collectors.toList());
        return  measurementDataList;
    }
}
