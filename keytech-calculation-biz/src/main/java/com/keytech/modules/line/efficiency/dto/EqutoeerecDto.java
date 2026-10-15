package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

import java.util.Date;

/**
 * 推送OEE 数据
 * @author aolei
 * @create 2020-06-26 16:07
 */
@Data
public class EqutoeerecDto {
    /**
     * 推送状态，默认为0
     */
    private Integer status;

    /**
     * 产线code
     */
    private String orgId;

    /**
     * 开始时间
     */
    private Date lineBTime;

    /**
     * 结束时间
     */
    private Date lineETime;

    /**
     * 产量
     */
    private Integer bBBottles;

    /**
     * 实际运行时长（小时）
     */
    private String actRunLength;

    /**
     * 线效率
     */
    private String OEE;

    /**
     * 额定能力
     */
    private Double LineCap;

}
