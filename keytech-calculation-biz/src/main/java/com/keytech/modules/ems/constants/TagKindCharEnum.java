package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 点位大类字母标识(1-COLLECT 2-MANUAL 3-THIRDPARTY 4-ORSDATA)
 * @author yepeng
 * @since 2025.05.13
 */
@Getter
@AllArgsConstructor
public enum TagKindCharEnum {

    COLLECT("C", "采集点位"),
    MANUAL("M", "手输点位"),
    THIRDPARTY("T", "第三方点位"),
    ORSDATA("O", "中间点位"),
    ;

    /**
     * 取值
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;

    /**
     * 通过值获取对象
     * @param code
     */
    public static TagKindCharEnum fromCode(String code) {
        for (TagKindCharEnum tagKind : TagKindCharEnum.values()) {
            if (tagKind.getCode() == code) {
                return tagKind;
            }
        }
        return null;
    }

}