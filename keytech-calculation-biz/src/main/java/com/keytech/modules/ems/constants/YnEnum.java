package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是或否(0-否 1-是)
 * @author yepeng
 * @since 2025.05.24
 */
@Getter
@AllArgsConstructor
public enum YnEnum {

    NO(0, "否"),
    YES(1, "是"),
    ;

    /**
     * 取值
     */
    private final int val;
    /**
     * 名称
     */
    private final String name;

}