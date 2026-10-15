package com.keytech.modules.ems.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * InfluxDB 东8区格式数据
 * @author yepeng
 * @since 2025.05.16
 */
@Data
@NoArgsConstructor
@Schema(description="InfluxDB东8区格式数据")
public class FluxZone8Data {
    @Schema(description="时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    @Schema(description="Flux编码")
    private String fluxCode;
    @Schema(description="值")
    private Double value;

    // 构造函数
    public FluxZone8Data(Instant time, String fluxCode, Double value) {
        // 转换为东8区时间
        this.time = time.atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        this.fluxCode = fluxCode;
        this.value = value;
    }

    // 构造函数
    public FluxZone8Data(Instant time, Double value) {
        this(time, null, value);
    }

    @Override
    public String toString() {
        return "FluxZone8Data{" + "time=" + time + ", fluxCode=" + fluxCode +  ", value=" + value + '}';
    }
}