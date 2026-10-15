package com.keytech.modules.alarm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 报警数据接收统一接口请求参数
 * 用于第三方平台调用报警接入接口
 * @author yepeng
 * @since 2026.01.20
 */
@Data
@Schema(description = "报警数据接收请求参数")
public class AlarmReceiveRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 来源系统标识
     * 用于标识报警来源的系统，如：EMS、LIMS、MES、SCADA等
     * 需在系统管理平台预先注册获取
     */
    @Schema(
        description = "来源系统标识",
        example = "EMS",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "来源系统标识(sourceSystem)不能为空")
    @Size(max = 64, message = "来源系统标识(sourceSystem)长度不能超过64")
    private String sourceSystem;

    /**
     * 请求唯一标识
     * 用于接口幂等性控制，避免重复处理同一报警
     * 建议格式：{系统标识}-{yyyyMMdd}-{HHmmss}-{4位序列号}
     */
    @Schema(
        description = "请求唯一标识，用于幂等性控制",
        example = "EMS-20250305-181522-0001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "请求唯一标识(requestId)不能为空")
    @Size(max = 100, message = "请求唯一标识(requestId)长度不能超过100")
    private String requestId;

    /**
     * 报警点位唯一编码
     * 报警系统中的唯一标识，需预先在报警点位管理中配置
     */
    @Schema(
        description = "报警点位唯一编码",
        example = "APAC-CN93223001-0801_AggreAlr24-16",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "报警点位编码(alarmPiCode)不能为空")
    @Size(max = 128, message = "报警点位编码(alarmPiCode)长度不能超过128")
    private String alarmPiCode;

    /**
     * 报警发生时间
     * 格式：yyyy-MM-dd HH:mm:ss
     * 时区：东八区（北京时间）
     */
    @Schema(
        description = "报警发生时间，格式：yyyy-MM-dd HH:mm:ss",
        example = "2025-03-05 18:15:22",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "报警时间(alarmTime)不能为空")
    @Size(max = 32, message = "报警时间(alarmTime)长度不能超过32")
    private String alarmTime;
    
    /**
     * 报警等级
     * 1-低 2-中 3-高
     * 不传则使用报警点位配置的默认等级
     */
    @Schema(
        description = "报警等级：1-低 2-中 3-高",
        example = "2",
        defaultValue = "使用点位默认等级"
    )
    private Integer alarmLevel;
    
    /**
     * 报警描述
     * 可详细描述报警情况，便于快速定位问题
     */
    @Schema(
        description = "报警描述信息",
        example = "大米火焰检测信号异常，火焰检测器检测到火焰信号持续低于阈值"
    )
    @Size(max = 500, message = "报警描述(alarmDesc)长度不能超过500")
    private String alarmDesc;
    
    /**
     * 在岗人员
     * 报警发生时负责该区域/设备的人员
     */
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z·\\s]{0,50}$",
             message = "在岗人员姓名只能包含中英文、空格和间隔号")
    @Schema(
        description = "报警发生时在岗人员",
        example = "张三"
    )
    private String onDutyPerson;
    
    /**
     * 扩展信息
     * JSON格式，用于传递额外的业务参数
     *  upperLimit: 上限值
     *  lowerLimit: 下限值
     *  piValue: 当前报警值
     */
    @Schema(
        description = "扩展信息，JSON格式",
        example = "{\"upperLimit\":\"230.0\",\"lowerLimit\":\"220.0\",\"piValue\":\"235.6\"}"
    )
    @Size(max = 1000, message = "扩展信息(extInfo)长度不能超过1000")
    private String extInfo;
    
    /**
     * 时间戳
     * 请求发起时间戳，单位：毫秒
     * 用于防止重放攻击，服务器会验证时间戳是否在有效期内（±5分钟）
     */
    @Schema(
        description = "请求时间戳，单位：毫秒",
        example = "1741162522000"
    )
    private Long timestamp;
}