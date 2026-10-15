package com.keytech.modules.plan.dto;

import com.keytech.modules.mdm3.entity.MdmItemSelection;
import com.keytech.modules.plan.entity.PlanOrderResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/6/19 13:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlanOrderResultDto extends PlanOrderResult {
    //条目检查项
    private String itemName;
    //条目检查标准
    private String itemStandard;
    //条目检查内容
    private String detail;
    //条目类型
    private String resultTypeCode;
    //检查地点
    private String itemLocation;
    //该条目是否需要上传图片
    private Integer isUploadImg;

    /**该条目是否需要检查时间*/
    private Integer isCheckTime;

    /**该条目是否需要确认人*/
    private Integer isConfirmUser;

    /**该条目是否需要对结果进行合格判断*/
    private Integer configIsQualified;

    /**该条目是否需要备注*/
    private Integer isRemark;

    /**该条目是否必填*/
    private Integer isRequire;

    /**该条目是否需要对结果进行合格判断*/
    private Integer isNa;

    //推送频率备注
    private String configRemark;
    //下拉条目的选项
    private List<MdmItemSelection> selectionList;
}
