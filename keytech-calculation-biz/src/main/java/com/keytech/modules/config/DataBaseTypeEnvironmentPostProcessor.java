package com.keytech.modules.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author jacksparrow
 * @version v1.0.0
 * @date 2024/9/24 21:18
 *  根据 PRIMARY 数据源所使用的数据库，自动设置数据库类型
 */
public class DataBaseTypeEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DATABASE_TYPE_KEY = "mybatis-plus.configuration.database-id";

    private static final String DATASOURCE_DYNAMIC_KEY = "spring.datasource.dynamic";


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 如果获取不到 DbType，则不进行处理
        DbType dbType = getDbType(environment);
        if (dbType == null) {
            return;
        }
        setDataBaseIdType(environment, dbType);
    }


    public void setDataBaseIdType(ConfigurableEnvironment environment, DbType idType) {
        environment.getSystemProperties().put(DATABASE_TYPE_KEY, idType.getDb());
    }



    public static DbType getDbType(ConfigurableEnvironment environment) {
        String primary = environment.getProperty(DATASOURCE_DYNAMIC_KEY + "." + "primary");
        if (StrUtil.isEmpty(primary)) {
            return null;
        }
        String url = environment.getProperty(DATASOURCE_DYNAMIC_KEY + ".datasource." + primary + ".url");
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        return JdbcUtils.getDbType(url);
    }

}
