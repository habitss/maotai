package com.keytech.modules.kpi.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author HQ
 * @version v1.0.0
 * @date 2023/3/11 18:01
 */
public class DataPageUtils {

    public static <T> Page<T> dataStreamPage(List<T> list, long pageNo, long pageSize, Predicate<? super T> condition) {
        if (CollUtil.isEmpty(list) || pageNo <= 0) {
            return new Page<>();
        }
        List<T> pageList = list.stream().filter(bean -> Objects.isNull(condition) || condition.test(bean))
                .skip((pageNo - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        return getPage(list, pageNo, pageSize, condition, pageList);
    }
    public static <T> Page<T> dataStreamSorted(List<T> list, long pageNo, long pageSize, Predicate<? super T> condition, Comparator<T> sortComparator ) {
        List<T> pageList = list.stream().sorted(sortComparator)
                .filter(bean -> Objects.isNull(condition) || condition.test(bean))
                .skip((pageNo - 1) * pageSize).limit(pageSize).collect(Collectors.toList());


        return getPage(list, pageNo, pageSize, condition, pageList);
    }

    private static <T> Page<T> getPage(List<T> list, long pageNo, long pageSize, Predicate<? super T> condition, List<T> pageList) {
        pageSize = pageSize == 0 ? 10 : pageSize;
        Page<T> page = new Page<>(pageNo, pageSize);
        page.setRecords(pageList);
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        //total
        long count = list.stream().filter(bean -> Objects.isNull(condition) || condition.test(bean)).count();
        page.setTotal(count);
        page.setPages(pageList.size() / pageSize + (pageList.size() % pageSize == 0 ? 0 : 1));
        return page;
    }
}
