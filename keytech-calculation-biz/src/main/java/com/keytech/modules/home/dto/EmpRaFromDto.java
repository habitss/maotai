package com.keytech.modules.home.dto;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.util.List;

/**
 * @author she
 * @date 2023/04/14/10:52
 */
@Data
public class EmpRaFromDto{

    //工厂
    private String factory;
    private String userId;  //人id
    private List<Integer> state;  //状态
    private String stateTime;  //开始时间
    private String eddTime;    //结束时间
    private Integer surplusDay =7; //剩余天数
    //是否显示ra单类型 put 提出单 dut 责任单 verify 确认单 execute 执行单
    private List<String> showType = CollUtil.toList("put","dut","verify","execute");
}
