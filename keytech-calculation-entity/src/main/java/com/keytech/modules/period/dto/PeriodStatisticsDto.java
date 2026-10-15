package com.keytech.modules.period.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: PeriodStatisticsDto
 * @Description: TODO
 * @Author: qipengbo
 * @Date: 2025/3/13 15:55
 **/
@Data
public class PeriodStatisticsDto implements Serializable {
    /**
     * 部门
     */
    @Excel(name = "部门(*)", width = 15, needMerge = true,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String deptCode;
    /**
     * 产线
     */
    @Excel(name = "产线(*)", width = 15, needMerge = true,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String lineCode;
    /**
     * 年
     */
    @NotNull
    private Integer year;
    /**
     * 月
     */
    @NotNull
    private String months;
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


    //理论完成数
    @Excel(name = "理论完成数",width = 15)
    private int completeCount;

    //合计
    @Excel(name = "合计",width = 15)
    private int total;

    //完成率
    @Excel(name = "完成率",width = 15)
    private String completeRate;
}
