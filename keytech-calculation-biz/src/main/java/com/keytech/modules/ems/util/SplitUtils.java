package com.keytech.modules.ems.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 切分批次工具类
 * @author yepeng
 * @since 2025.04.30
 */
public class SplitUtils {
    private SplitUtils() {
        throw new IllegalStateException("Utility class");
    }

    // 按指定大小分组
    public static  <T> List<List<T>> splitList(List<T> list, int batchSize) {
        List<List<T>> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0; i < size; i += batchSize) {
            result.add(list.subList(i, Math.min(size, i + batchSize)));
        }
        return result;
    }

}
