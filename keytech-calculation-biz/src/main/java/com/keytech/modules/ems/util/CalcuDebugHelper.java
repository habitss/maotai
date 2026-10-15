package com.keytech.modules.ems.util;

import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.vo.CalcuDebugResult;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 采集计算调试模式工具类
 * @author yepeng
 * @since 2025.06.15
 */
public class CalcuDebugHelper {

    /**
     * 查询采集计算调试模式设置
     * @param redisTemplate redisTemplate
     */
    public static CalcuDebugResult getCalcuDebugResult(RedisTemplate<String, String> redisTemplate) {
        CalcuDebugResult result = new CalcuDebugResult();
        boolean isDebug = "1".equals(redisTemplate.opsForValue().get(EmsConstantsX.J_CALCULATE_DEBUG));
        result.setDebug(isDebug);

        if (isDebug) {
            String debugCtags = redisTemplate.opsForValue().get(EmsConstantsX.J_CALC_DEBUG_C_TAGS);
            String debugOtags = redisTemplate.opsForValue().get(EmsConstantsX.J_CALC_DEBUG_O_TAGS);

            List<String> debugCtagIdList = (debugCtags == null || debugCtags.isBlank())
                    ? Collections.emptyList()
                    : Arrays.stream(debugCtags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();

            List<String> debugOtagIdList = (debugOtags == null || debugOtags.isBlank())
                    ? Collections.emptyList()
                    : Arrays.stream(debugOtags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();

            result.setCTagIds(debugCtagIdList);
            result.setOTagIds(debugOtagIdList);
        } else {
            result.setCTagIds(Collections.emptyList());
            result.setOTagIds(Collections.emptyList());
        }

        return result;
    }

}
