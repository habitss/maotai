package com.keytech.modules.opl.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.List;

@Data
public class UserStatisticsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realname;


    /**
     * 部门code(当前选择登录部门)
     */
    /**
     * 负责部门
     */
    @Excel(name = "部门", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String orgCode;

    /**
     * 部门名称
     */
    private transient String orgCodeTxt;


    /**
     * 工号，唯一键
     */
    @Excel(name = "工号", width = 15)
    private String workNo;


    /**
     * 负责部门
     */
    @Excel(name = "负责部门", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String departIds;


    /**
     * 工厂code
     */
    @TableField(exist = false)
    private String factoryCode;
    @TableField(exist = false)
    /**
     * 工厂名称
     */
    private String factoryName;
    /**
     * 字典岗位ID
     */
    @Dict(dicCode = "inspector_post")
    private Integer postId;
    /**
     * 部门id
     */
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private transient String orgCodeDeptId;


    //Opl数
    private Integer oplNumber;
    //已完成
    private Integer oplSuccess;
    //计划
    private Integer oplPlan;
    //延迟
    private Integer oplDelay;
    //完成率
    private String oplRate;

    //计划数/诊断梳
    private Integer planNumber;
    //已完成
    private Integer planSuccess;
    //计划
    private Integer plan;
    //延迟
    private Integer planDelay;
    //完成率
    private String planRate;

    //oplid集合
    private List<String> oplIds;


}