package com.keytech.modules.pi.common;
import java.time.Instant;
import java.util.Date;

/**
 * @Author f'l
 * @date 2024/11/7 23:12
 */
public class MeasurementData {
    private Instant time;
    private String cnlNum;
    private Double value;

    // 构造函数
    public MeasurementData(Instant time, String cnlNum, Double value) {
        this.time = time;
        this.cnlNum = cnlNum;
        this.value = value;
    }

    // Getter和Setter
    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getCnlNum() {
        return cnlNum;
    }

    public void setCnlNum(String cnlNum) {
        this.cnlNum = cnlNum;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MeasurementData{" +
                "time=" + time +
                ", cnlNum='" + cnlNum + '\'' +
                ", value=" + value +
                '}';
    }
}