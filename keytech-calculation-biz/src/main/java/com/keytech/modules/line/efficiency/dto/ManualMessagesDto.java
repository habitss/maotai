package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

import java.util.Date;

/**
 * @FileName ManualMessagesDto
 * @Description 上位机系统手动操作记录(其他工厂)
 * @Author HQ
 * @date 2025-11-18
 **/
@Data
public class ManualMessagesDto {

    /**ID*/
    private String dsId;
    /**修改属性 class_name-msg_text*/
    private String className;
    private String msgText;
    /**对象修改时间*/
    private Date msgTime;
    /**设备编号*/
//    private String istanceName;
    private String instanceName;
    /**设备名*/
    private String unitName;
    /**工序*/
    private String reccatName;


}
