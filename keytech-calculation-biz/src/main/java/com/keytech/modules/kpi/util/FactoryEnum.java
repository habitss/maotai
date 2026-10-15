package com.keytech.modules.kpi.util;

public enum FactoryEnum {
    TEST_FACTORY("包头雪鹿1", "0"),//测试
    BT_FACTORY("包头工厂", "5"),//包头
    JC_FACTORY("金川工厂", "6"),//金川
    ZJ_FACTORY("中京工厂", "8"),//中京
    CF_FACTORY("赤峰工厂", "9"),//赤峰
    XD_FACTORY("仙都工厂", "19"),//仙都
    ZMD_FACTORY("驻马店工厂", "22"),//驻马店
    BJ_FACTORY("北京工厂", "2"),//北京
    HS_FACTORY("呼和浩特工厂", "3"),//呼市
    JA_FACTORY("江西工厂", "16"),//吉安
    YF_FACTORY("北京一分工厂", "21");//一分

    private final String code;
    private final String primary;

    FactoryEnum(String code, String primary) {
        this.code = code;
        this.primary = primary;
    }

    public String getCode() {
        return code;
    }

    public String getPrimary() {
        return primary;
    }

    public static String getPrimaryByCode(String code) {
        for (FactoryEnum factory : FactoryEnum.values()) {
            if (factory.getCode().equals(code)) {
                return factory.getPrimary();
            }
        }
        throw new IllegalArgumentException("Unsupported factory code: " + code);
    }
}