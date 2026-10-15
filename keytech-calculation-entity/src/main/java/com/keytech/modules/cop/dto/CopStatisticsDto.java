package com.keytech.modules.cop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @ClassName: CopStatisticsDto
 * @Description: TODO
 * @Author: Jun
 * @Date: 2025/2/24 15:55
 **/
@Data
public class CopStatisticsDto {
    /**
     * 部门
     */
    private String deptCode;
    /**
     * 产线
     */
    private String lineCode;
    /**
     * 年
     */
    private int year;
    /**
     * 月
     */
    private String months;

    private int month;
    /**
     * 0 设备维度
     * 1 清理人维度
     * 2 检查人维度
     */
    private int type;

    /**
     * 频次
     */
    private String frequencys;

    @Excel(name = "名称(*)",width = 15)
    private String name;

    //        '1': 'green', //已完成
    //        '2': 'red', //延迟
    //        '3': 'red', //延迟完成
    //        '4': 'bc', //待审核
    //        '5': 'red', //未执行
    //        '0': 'red', //不合格
    //        '-1': 'yc', //计划
    //        '6': 'grey' //因生产取消
    @Excel(name = "已完成",width = 15)
    private Integer complete;
    @Excel(name = "延迟",width = 15)
    private Integer delay;
    @Excel(name = "延迟完成",width = 15)
    private Integer delayComplete;
    @Excel(name = "待审核",width = 15)
    private Integer check;
    @Excel(name = "未执行",width = 15)
    private Integer noExecute;
    @Excel(name = "不合格",width = 15)
    private Integer unqualified;
    @Excel(name = "计划",width = 15)
    private Integer plan;
    @Excel(name = "因生产取消",width = 15)
    private Integer noExecutePlan;

    //合计
    @Excel(name = "理论完成数",width = 15)
    private int total;
    //理论完成数
    @Excel(name = "理论完成数",width = 15)
    private int completeCount;
    //完成率
    @Excel(name = "完成率",width = 15)
    private String completeRate;
}
