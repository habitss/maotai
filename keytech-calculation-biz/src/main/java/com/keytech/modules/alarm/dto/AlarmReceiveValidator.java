package com.keytech.modules.alarm.dto;

import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 报警接收请求参数验证器
 * @author yepeng
 * @since 2026.01.21
 */
public class AlarmReceiveValidator {
    //语法验证
    private static final Pattern SOURCE_SYSTEM_PATTERN = Pattern.compile("^\\w+$");
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");
    private static final Pattern PI_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_.]+$");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    public static Map<String, String> validate(AlarmReceiveRequest req) {
        Map<String, String> result = new HashMap<>();

        // sourceSystem
        if (StringUtils.isBlank(req.getSourceSystem())) {
            result.put("sourceSystem", "来源系统标识不能为空");
        } else {
            if (req.getSourceSystem().length() < 2 || req.getSourceSystem().length() > 10) {
                result.put("sourceSystem", "来源系统标识长度应为2-10个字符");
            } else if (!SOURCE_SYSTEM_PATTERN.matcher(req.getSourceSystem()).matches()) {
                result.put("sourceSystem", "来源系统标识只能包含大小写字母、数字和下划线");
            }
        }

        // requestId
        if (StringUtils.isBlank(req.getRequestId())) {
            result.put("requestId", "请求ID不能为空");
        } else if (!REQUEST_ID_PATTERN.matcher(req.getRequestId()).matches()) {
            result.put("requestId", "请求ID格式不合法");
        }

        // alarmPiCode
        if (StringUtils.isBlank(req.getAlarmPiCode())) {
            result.put("alarmPiCode", "报警PI编码不能为空");
        } else if (req.getAlarmPiCode().length() > 128) {
            result.put("alarmPiCode", "报警PI编码最大长度为128个字符");
        } else if (!PI_CODE_PATTERN.matcher(req.getAlarmPiCode()).matches()) {
            result.put("alarmPiCode", "报警PI编码格式不合法");
        }

        // alarmTime
        if (StringUtils.isBlank(req.getAlarmTime())) {
            result.put("alarmTime", "报警时间不能为空");
        } else if (!DATETIME_PATTERN.matcher(req.getAlarmTime()).matches()) {
            result.put("alarmTime", "报警时间格式错误，应为：yyyy-MM-dd HH:mm:ss");
        }

        // alarmLevel
        if (req.getAlarmLevel() != null) {
            if (req.getAlarmLevel() < 1 || req.getAlarmLevel() > 3) {
                result.put("alarmLevel", "报警等级取值范围为1-3");
            }
        }

        // alarmDesc
        if (req.getAlarmDesc() != null && req.getAlarmDesc().length() > 500) {
            result.put("alarmDesc", "报警描述最大长度为500个字符");
        }

        return result;
    }

}
