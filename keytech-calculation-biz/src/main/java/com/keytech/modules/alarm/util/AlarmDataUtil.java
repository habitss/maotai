package com.keytech.modules.alarm.util;


import java.util.ArrayList;
import java.util.List;

/**
 * DF报警值解析工具类
 * @author liu
 * @since 2025.01.13
 */
public class AlarmDataUtil {

    /**
     * 解析DF报警值
     * DF提供报警值换算成二进制数,对其进行从左边到右的补码至32位
     * 从右往左（←）数,值为1的位，表示该位产生了报警
     * 返回产生了报警的位列表
     */
    public static List<Integer> parseAlarmValue(Integer num) {
        List<Integer> positions = new ArrayList<>();
        if (num == null) {
            return positions;
        }
        // 将Integer转换为32位二进制字符串，前面补零
        String binaryStr = String.format("%32s", Integer.toBinaryString(num)).replace(' ', '0');
        // 从右往左遍历（即从最低位到最高位），统计值为1的位的位置（从1开始）
        for (int i = binaryStr.length() - 1; i >= 0; i--) {
            char c = binaryStr.charAt(i);
            if (c == '1') {
                // 位置从1开始，所以是 (32 - i)
                positions.add(32 - i);
            }
        }
        return positions;
    }


}
