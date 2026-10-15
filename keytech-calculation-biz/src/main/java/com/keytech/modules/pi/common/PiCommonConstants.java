package com.keytech.modules.pi.common;

/**
 * @FileName CommonConstants
 * @Description PI常量类
 * @Author HQ
 * @date 2025-10-15
 **/
public class PiCommonConstants {

    /**
     * 0 值
     */
    public static final String INSTANT = "0";

    /**
     * PI日报间算法的PI不能设置成可编辑状态
     */
    public static final String PI_CANNOT_EDIT = "PI日报间算法的PI不能设置成可编辑状态";
    /**
     * 配置了PI间算法，无法修改数据，如需修改，请修改计算因子PI
     */
    public static final String CANNOT_EDIT = "配置了PI间算法，无法修改数据，如需修改，请修改计算因子PI";

    /**
     * 编辑成功!
     */
    public static final String EDIT_SUCCESS = "编辑成功!";

    /**
     * table_id
     */
    public static final String TABLE_ID = "table_id";

    /**
     * field_id
     */
    public static final String FIELD_ID = "field_id";

    /**
     * 开启/显示状态/是
     */
    public static final String OPEN = "1";
    /**
     * 关闭/不显示状态/否
     */
    public static final String CLOSE = "0";



    /**
     * 1 pi日报
     */
    public static final Integer PI_DAILY = 1;

    /**
     * 操作成功
     */
    public static final String OPERATION_SUCCESS = "操作成功";

    /**
     * 操作失败
     */
    public static final String OPERATION_FAIL = "操作失败";

    private PiCommonConstants() {
        // 防止实例化
        throw new AssertionError("不能实例化常量类");
    }
    /**
     * 0
     */
    public static final Integer ZERO = 0;

    /**
     * 1
     */
    public static final Integer ONE = 1;

    /**
     * 2
     */
    public static final Integer TWO = 2;

    /**
     * 1 字符
     */
    public static final String ONE_STR = "1";

    /**
     * 2 字符
     */
    public static final String TWO_STR = "2";

    /**
     * 排序方式 DESC
     */
    public static final String SORT_DESC = "desc";
    /**
     * 排序方式 ASC
     */
    public static final String SORT_ASC = "asc";

    /**
     * id
     */
    public static final String ID = "id";

    /**
     * 过程记录JSON
     */
    public static final String TRIGGER_JSON = "trigger_json";
}
