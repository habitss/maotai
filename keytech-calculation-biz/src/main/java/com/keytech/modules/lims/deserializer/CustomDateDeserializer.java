package com.keytech.modules.lims.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Jackson 自定义日期反序列化器（兼容两种格式）
 * @since 1.0.0
 */
@Slf4j
public class CustomDateDeserializer extends StdDeserializer<Date> {
    private static final String[] FORMATS = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};

    /**
     * 无参构造（必须保留，供 Jackson 反序列化器工厂使用）
     */
    public CustomDateDeserializer() {
        this(null);
    }

    /**
     * 带类型参数的构造（支持泛型反序列化，通常无需手动调用）
     *
     * @param vc 反序列化的目标类型（如 Date.class）
     */
    public CustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText().trim();
        if (dateStr.isEmpty()) return null;

        for (String format : FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                Date date = sdf.parse(dateStr);
                if (format.equals("yyyy-MM-dd")) { // 纯日期格式补全时间
                    Calendar calendar = Calendar.getInstance(); // 使用默认时区
                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);   // 小时
                    calendar.set(Calendar.MINUTE, 0);      // 分钟
                    calendar.set(Calendar.SECOND, 0);      // 秒
                    calendar.set(Calendar.MILLISECOND, 0); // 毫秒
                    date = calendar.getTime(); // 生成新的Date对象
                }
                return date;
            } catch (ParseException e) { log.error("时间格式异常"+e); }
        }
        throw new IOException("不支持的格式: " + dateStr + "，支持: " + String.join(", ", FORMATS));
    }
}