package com.keytech.modules.project.management.dto;

import com.keytech.modules.project.management.entity.ProjectManagement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @Description: 项目个人激励统计dto
 * @Author: sjw
 * @Date: 2025/4/21 15:11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectManagementSummaryDto extends ProjectManagement {

    /**
     * 项目等级
     */
    private String projectLevel_dictText;

    /**
     * 项目类型
     */
    private String projectType_dictText;

    /**
     * 项目来源
     */
    private String projectSource_dictText;

    /**
     * 主控部门名称
     */
    private String sysOrgId_dictText;

    /**
     * 人员姓名
     */
    private String leaderUserId;

    /**
     * 人员姓名
     */
    private String leaderUserId_dictText;

    /**
     * 项目贡献度
     */
    private Double contribution;

    /**
     * 01 月
     */
    private Double month1;

    /**
     * 02 月
     */
    private Double month2;

    /**
     * 03 月
     */
    private Double month3;

    /**
     * 04 月
     */
    private Double month4;

    /**
     * 05 月
     */
    private Double month5;

    /**
     * 06 月
     */
    private Double month6;

    /**
     * 07 月
     */
    private Double month7;

    /**
     * 08 月
     */
    private Double month8;

    /**
     * 09 月
     */
    private Double month9;

    /**
     * 10 月
     */
    private Double month10;

    /**
     * 11 月
     */
    private Double month11;

    /**
     * 12 月
     */
    private Double month12;

    /**
     * 汇总
     */
    private Double summarys;

    /**
     * 汇总
     *
     * @return
     */
    public Double getSummarys() {
        double result = this.month1 +
                this.month2 +
                this.month3 +
                this.month4 +
                this.month5 +
                this.month6 +
                this.month7 +
                this.month8 +
                this.month9 +
                this.month10 +
                this.month11 +
                this.month12
        ;
        this.summarys = new BigDecimal(result).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return this.summarys;
    }
}
