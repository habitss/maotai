package com.keytech.modules.common;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.keytech.modules.config.DataBaseTypeEnvironmentPostProcessor;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jacksparrow
 * @version v1.0.0
 * @date 2025/7/22 14:10
 */

public class DataBaseTypeUtil {
    public static DbType getDbType() {
        ConfigurableEnvironment environment = SpringContextUtils.getBean(ConfigurableEnvironment.class);
        return DataBaseTypeEnvironmentPostProcessor.getDbType(environment);
    }

    public static String getDbName() {
        ConfigurableEnvironment environment = SpringContextUtils.getBean(ConfigurableEnvironment.class);
        String primary = environment.getProperty("spring.datasource.dynamic.primary");
        if (StrUtil.isEmpty(primary)) {
            primary = "master";
        }
        String url = environment.getProperty("spring.datasource.dynamic.datasource." + primary + ".url");
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        Pattern pattern = Pattern.compile("jdbc:[a-z]+://[^/]+/(\\w+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
