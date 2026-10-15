package com.keytech.modules.pi.common;

/**
 * @FileName CommonConstants
 * @Description 常量类
 * @Author HQ
 * @date 2025-10-15
 **/
public class CommonConstants {
    /**
     * 未删除标志
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 已删除标志
     */
    public static final Integer DELETED = 1;
    /**
     * true
     */
    public static final Boolean TRUE = true;
    /**
     * false
     */
    public static final Boolean FALSE = false;
    /**
     * 租户
     * tenant_id
     */
    public static final String TENANT_ID = "tenant_id";

    private CommonConstants() {
        // 防止实例化
        throw new AssertionError("不能实例化常量类");
    }
}
