package com.keytech.modules.alarm.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * KAFKA报警消息
 * @author yepeng
 * @since 2026.01.20
 * */
@Data
@TableName("alarm_kafka_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KAFKA报警消息")
public class AlarmKafkaLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**消息ID*/
	@Excel(name = "消息ID", width = 15)
    @Schema(description = "消息ID")
    private String instanceId;
	/**功能位置*/
	@Excel(name = "功能位置", width = 15)
    @Schema(description = "功能位置")
    private String thingId;
	/**原数据*/
	@Excel(name = "原数据", width = 15)
    @Schema(description = "原数据")
    private String dataJson;
	/**消息时间(非UTC时间)*/
	@Excel(name = "消息时间(非UTC时间)", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "消息时间(非UTC时间)")
    private LocalDateTime msgTime;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**Kafka偏移量*/
	@Excel(name = "Kafka偏移量", width = 15)
    @Schema(description = "Kafka偏移量")
    private Long kfoffset;
}
