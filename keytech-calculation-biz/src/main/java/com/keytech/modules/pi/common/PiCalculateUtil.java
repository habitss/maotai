package com.keytech.modules.pi.common;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableQueryDto;
import com.keytech.modules.pi.entity.PiCalculateSignal;
import com.keytech.modules.pi.entity.PiComputationModel;
import com.keytech.modules.pi.entity.PiRetrievalSignal;
import com.keytech.modules.pi.entity.PiRetrievalStep;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PI计算公共方法
 * @author 敖磊
 * @since 2024/11/15
 */
public class PiCalculateUtil {







    public static void main(String[] args) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 创建一个日期时间格式化器，用于自定义输出格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

        // 格式化当前的日期和时间
        String formattedDateTime = currentDateTime.format(formatter);

        // 打印当前的年月日和小时
        System.out.println("当前的年月日和小时是: " + formattedDateTime);
    }




}
