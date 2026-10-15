package com.keytech.modules.line.efficiency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @FileName ManualRaDto
 * @Description 上位机系统手动操作记录
 * @Author HQ
 * @date 2025-11-13
 **/
@Data
@AllArgsConstructor
public class ManualFtDto {

    /**
     * 序号
     */
    private Integer serialNumber;

    /**
     * 工序
     */
    private String process;

    /**
     * 设备名
     */
    private String equipmentName;

    /**
     * 设备编号
     */
    private String equipmentCode;

    /**
     * 对象修改时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date modificationTime;

    /**
     * 修改属性
     */
    private String modifiedProperty;

    /**
     * 修改前状态/值
     */
    private String previousValue;

    /**
     * 修改后状态/值
     */
    private String currentValue;

    /**
     * 备注
     */
    private String remark;

    /**
     * 记录id
     */
    private String recordId;
    /**
     * 删除标识
     */
    private Integer delFlag;

}
