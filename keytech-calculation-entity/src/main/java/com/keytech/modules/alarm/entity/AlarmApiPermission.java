package com.keytech.modules.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serial;
import java.io.Serializable;

/**
 * 报警接口权限表
 * @author yepeng
 * @since 2026.01.29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("alarm_api_app_permission")
public class AlarmApiPermission implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 应用标识
     */
    @Excel(name = "应用标识", width = 15)
    @Schema(description = "应用标识")
    private String appId;

    /**
     * 接口编码
     */
    @Excel(name = "接口编码", width = 15)
    @Schema(description = "接口编码")
    private String apiCode;
}
