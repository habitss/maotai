package com.keytech.modules.kpi.entity;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: KPI清单
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KPI清单")
public class KpiInfo implements Serializable {


	/**id(自增长)*/
    @Excel(name = "id", width = 15)
	@TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

	/**工厂*/
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
    @Excel(name = "部门", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门")
    private String deptCode;

	/**产线*/
    @Schema(description = "产线")
    private String lineCode;

	/**kpiCode*/
    @Schema(description = "kpiCode")
    private String kpiCode;

	/**kpi名称*/
	@Excel(name = "kpi名称", width = 15)
    @Schema(description = "kpi名称")
    private String kpiName;

	/**父编号(KPI分类)*/
	@Excel(name = "分类", width = 15,dictTable ="mes3_kpi_type",dicText = "kpi_type",dicCode = "id")
    @Schema(description = "父编号(KPI分类)")
    private String perentId;


    /**是否自动采集*/
    @Excel(name = "是否自动采集(自动/手动)", width = 15,replace = { "自动_1", "手动_0" })
    @Schema(description = "是否自动采集")
    private Integer active;

	/**是否进入柏拉图 0否 1是*/
	@Excel(name = "是否进入柏拉图", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否进入柏拉图 0否 1是")
    private Integer plato;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String text;

    /**单位*/
    @Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;

    /**父级(KPI清单上级)*/
    @Excel(name = "父级", width = 15,dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code")
    @Schema(description = "父级(KPI清单上级)")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String parentId;

    /**红区判断条件 和目标值比较 0 比小 1 比大*/
    @Excel(name = "红区判断条件", width = 15 ,replace = { "不计算_-1", "大于_1", "小于_0", "范围_2", "比例_3" })
    @Schema(description = "和目标值比较 0 比小 1 比大")
    private String compareValue;

    /**第三方接口指标key*/
    @Excel(name = "第三方接口指标key", width = 15)
    @Schema(description = "第三方接口指标key")
    private String thirdKey;

    /**第三方接口指标名称*/
    @Excel(name = "第三方接口指标名称", width = 15)
    @Schema(description = "第三方接口指标名称")
    private String thirdName;

    /**保留小数位数*/
    @Excel(name = "保留小数位数", width = 15)
    @Schema(description = "保留小数位数")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer decimalNum;

    /**是否在周/月报中展示*/
    @Excel(name = "是否在周/月报中展示", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否在周/月报中展示")
    private Integer visible;

    /**是否在填写中展示*/
    @Excel(name = "是否录入中显示", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否在填写中展示")
    private Integer visibleWrite;

    /**填写部门(录入权限)*/
    @Excel(name = "填写部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "录入权限")
    private String inputPermission;

    /**是否可用(是否激活0不激活 1激活)*/
    @Excel(name = "是否可用", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否激活0不激活 1激活")
    private Integer activeLook;

    /**报表排序*/
    @Excel(name = "报表排序", width = 15)
    @Schema(description = "报表排序")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double reportSort;

    /**填报排序*/
    @Excel(name = "填报排序", width = 15)
    @Schema(description = "填报排序")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double inputSort;

    /**所属年份*/
    @Excel(name = "年份", width = 15)
    @Schema(description = "年份")
    private String year;


    /**数据填报职责分工 R*/
    @Excel(name = "R", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "R")
    private String provideBy;

    /**数据填报职责分工 A*/
    @Excel(name = "A", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "A")
    private String possessBy;

    /**数据填报职责分工 C*/
    @Excel(name = "C", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "C")
    private String consultedBy;

    /**数据填报职责分工I*/
    @Excel(name = "I", width = 15)
    @Schema(description = "I")
    private String informedBy;


    /**KPI职责分工R*/
//    @Excel(name = "KPI职责分工-R", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "R")
    private String dutyProvideBy;

    /**KPI职责分工A*/
//    @Excel(name = "KPI职责分工-A", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "A")
    private String dutyPossessBy;

    /**KPI职责分工C*/
//    @Excel(name = "KPI职责分工-C", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "C")
    private String dutyConsultedBy;

    /**KPI职责分工I*/
//    @Excel(name = "KPI职责分工-I", width = 15)
    @Schema(description = "I")
    private String dutyInformedBy;

    /**截止时间描述*/
    @Excel(name = "截止时间", width = 15)
    @Schema(description = "截止时间")
    private String deadline;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**0:默认不重点关注  1:重点关注*/
    @Schema(description = "0:默认不重点关注  1:重点关注")
    private Integer emphasisType;

    /**自定义标签*/
    @Excel(name = "标签", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_KPI_INFO_LABEL'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_KPI_INFO_LABEL'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "标签")
    private String label;

	/**周月报权限*/
    @Schema(description = "周月报权限")
    private String reportPermission;

    /**分类名称*/
    @Schema(description = "分类")
    private String typeChildName;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**删除状态(0未删,1已删)*/
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;

    private static final long serialVersionUID = 1L;

    //层级部门名称
    @TableField(exist = false)
    private String deptName;

    //填写部门名称
    @TableField(exist = false)
    private String inputDeptName;

    @TableField(exist = false)
    //@Excel(name = "分类", width = 15, exportConvert = true)
    private String kpiTypeName;

    @TableField(exist = false)
    private List<KpiInfo> children = new ArrayList<>();

    /**KPI树**/
    @TableField(exist = false)
    private Boolean typeBool = false;
    @TableField(exist = false)
    private List<String> tian; //天
    @TableField(exist = false)
    private String month; //年月

    /**KPI清单*/
    @TableField(exist = false)
    private String orgCode;

    /**KPI复制目标年份*/
    @TableField(exist = false)
    private String targetYear;


    /**
     * 导出
     *
     * @return
     */
    public String convertgetKpiTypeName() {
        //转换分类的id 匹配字典
        if(null == perentId ){
            return "";
        }
        String parentIdStr = StrUtil.toString(perentId);
        long parentId = NumberUtil.parseLong(parentIdStr);
//        String[] replace = (String[]) SpringContextUtils.getHttpServletRequest().getAttribute(BizConstant.KPI_TYPE_DATA_DICT);
//        Object o = Tools.replaceValue(replace, parentId, true);
        String[] replace = (String[]) SpringContextUtils.getHttpServletRequest().getAttribute("mes3_kpi_type:id:kpi_type");
        Object o = replaceValue(replace, parentId, true);
        return this.kpiTypeName = StrUtil.toString(o);
    }


    public static Object replaceValue(String[] replace, Object result, boolean multiReplace) {
        if (result == null) {
            return "";
        }
        if (replace == null || replace.length <= 0) {
            return result;
        }
        String temp = String.valueOf(result);
        String backValue = "";
        if (temp.indexOf(",") > 0 && multiReplace) {
            //原值中带有逗号，认为他是多值的
            String[] multiReplaces = temp.split(",");
            for (String str : multiReplaces) {
                backValue = backValue.concat(replaceSingleValue(replace, str) + ",");
            }
            if (backValue.equals("")) {
                backValue = temp;
            } else {
                backValue = backValue.substring(0, backValue.length() - 1);
            }
        } else {
            backValue = replaceSingleValue(replace, temp);
        }
        //update-begin-author:liusq date:20210204 for:字典替换失败提示日志
        if (backValue.equals(temp)) {

        }
        //update-end-author:liusq date:20210204 for:字典替换失败提示日志
        return backValue;
    }

    /**
     * 单值替换 ,若没找到则原值返回
     */
    private static String replaceSingleValue(String[] replace, String temp) {
        String[] tempArr;
        for (int i = 0; i < replace.length; i++) {
            tempArr = replace[i].split("_");
            if (tempArr.length < 2) {
                continue;
            }
            if (temp.equals(tempArr[1])) {
                return tempArr[0];
            }
        }
        return temp;
    }
}
