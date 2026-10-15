package com.keytech.modules.kafka.df.config;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DfSiteEnum {
    WUH("CN54","WUH"), //武汉
    PUT("CN01","PUT"), //莆田
    FOS("CN55","FOS"), //佛山
    HRB("CN09","HRB"), //哈尔滨
    NAC("CN28","NAC"), //南昌
    JSB("CN12","JSB"), //金士百
    XIX("CN96","XIX"), //新乡
    ZIY("CN93","ZIY"), //资阳
    ZHZ("CN27","ZHZ"), //漳州
    WEZ("CN10","WEZ"), //温州
    SUQ("CN79","SUQ"), //宿迁
    NAN("CN30","NAN"), //南宁
    BAO("CN32","BAO"), //保定
    MDJ("CN24","MDJ"), //牡丹江
    TAS("CN34","TAS"), //唐山
    SHL("CN15","SHL"), //石梁
    NAT("CNA8","NAT"), //南通
    YIK("CN48","YIK"), //营口
    JMS("CNN7","JMS"), //佳木斯
    JIZ("CN46","JIZ"), //锦州
    JIS("CN29","JIS"), //吉水
    WUG("CN80","WUG"), //武冈
    YAJ("CN44","YAJ"), //延吉
    KUM("CN11","KUM"), //昆明
    NIB("CN18","NIB"), //宁波
    XIY("CN41","XIY"), //信阳
    ;

    /**
     * 编码
     */
    private final String code;
    /**
     * 名字
     */
    private final String name;

    public static DfSiteEnum getByCode(String code) {
        if(null == code){
            return null;
        }
        return ArrayUtil.firstMatch(o -> o.getCode().equals(code), values());
    }
    public static String getNameByCode(String code) {
        DfSiteEnum df = getByCode(code);
        if(null == df){
            return "";
        }
        return df.getName();
    }
}
