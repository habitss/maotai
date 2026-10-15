package com.keytech.modules.meeting.dto;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName FactoryCodeEnum
 * @Description TODO
 * @createTime 2025年03月24日 13:24:00
 */
@Getter
@AllArgsConstructor
public enum FactoryCodeEnum {

    //DF("A10A10A10", "包头雪鹿","http:///192.168.1.6:3100/"),
    CF("A10A02A02", "燕京赤峰","http://123.178.244.78:8000/"),
    XL("A10A03A03", "包头雪鹿","http://110.16.13.6:18040/"),
    ZJ("A10A04A04", "燕京中京","http://218.202.102.66:8000/"),
    XD("A10A05A05", "浙江仙都","http://61.153.72.60:8443/"),
    JC("A10A06A06", "燕京金川","http://124.67.81.74:8000/"),
    HS("A10A07A07", "燕京呼和浩特","http://1.183.24.54:18040/"),
    BJ("A10A08A08", "燕京北京","http://114.255.93.176:81/"),
    BJ_YF("A10A09A09", "燕京北京一分","http://111.204.103.131:81/"),
    JX("A10A10A10", "燕京江西","http://218.65.37.250:91/"),
    LS("A10A11A11", "燕京丽水","http://200.200.201.148:81/"),
    XY("A10A12A12", "燕京襄阳","http://192.168.12.241:81/"),
    SY("A10A13A13", "燕京十堰","http://10.146.24.24:81/");


    private final String code;
    private final String factoryName;
    private final String netWorkAddress;

    public static FactoryCodeEnum getEnumByCode(String code) {
        FactoryCodeEnum codeEnum = EnumUtil.getBy(FactoryCodeEnum.class, bean -> bean.getCode().equals(code));
        if (codeEnum == null) {
            codeEnum = FactoryCodeEnum.JX;
        }
        return codeEnum;
    }
}
