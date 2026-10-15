package com.keytech.modules.fivewhy.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 5W详情表
 * @Author: jeecg-boot
 * @Date:   2025-02-27
 * @Version: V1.0
 */
@Data
@TableName("mes3_five_why_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="5W详情表")
public class FiveWhyDetail implements Serializable {


	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deptCode;

	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

    /**5w主键*/
    @Excel(name = "5w主键", width = 15, format = "yyyy-MM-dd")
    @Schema(description = "5w主键")
    private String recordId;

	/**类型1：PI，2：KPI，3：差距分析*/
	@Excel(name = "类型1：PI，2：KPI，3：差距分析", width = 15)
    @Schema(description = "类型1：PI，2：KPI，3：差距分析")
    private String type;

	/**问题*/
	@Excel(name = "问题", width = 15)
    @Schema(description = "问题")
    private String problem;

	/**原因*/
	@Excel(name = "原因", width = 15)
    @Schema(description = "原因")
    private String reason;

	/**问题解决*/
	@Excel(name = "问题解决", width = 15)
    @Schema(description = "问题解决")
    private String solveProblem;

	/**提出人id*/
	@Excel(name = "提出人id", width = 15)
    @Schema(description = "提出人id")
    private String putUserId;

	/**提出人姓名*/
	@Excel(name = "提出人姓名", width = 15)
    @Schema(description = "提出人姓名")
    private String putUserName;

	/**提出人单位编码*/
	@Excel(name = "提出人单位编码", width = 15)
    @Schema(description = "提出人单位编码")
    private String putDeptId;

	/**责任人id*/
	@Excel(name = "责任人id", width = 15)
    @Schema(description = "责任人id")
    private String dutyUserId;

	/**责任人姓名*/
	@Excel(name = "责任人姓名", width = 15)
    @Schema(description = "责任人姓名")
    private String dutyUserName;

	/**责任人单位编码*/
	@Excel(name = "责任人单位编码", width = 15)
    @Schema(description = "责任人单位编码")
    private String dutyDeptId;

	/**执行人id*/
	@Excel(name = "执行人id", width = 15)
    @Schema(description = "执行人id")
    private String executeUserId;

	/**执行人姓名*/
	@Excel(name = "执行人姓名", width = 15)
    @Schema(description = "执行人姓名")
    private String executeUserName;

	/**执行人单位编码*/
	@Excel(name = "执行人单位编码", width = 15)
    @Schema(description = "执行人单位编码")
    private String executeDeptId;

	/**确认人id*/
	@Excel(name = "确认人id", width = 15)
    @Schema(description = "确认人id")
    private String verifyUserId;

	/**确认人姓名*/
	@Excel(name = "确认人姓名", width = 15)
    @Schema(description = "确认人姓名")
    private String verifyUserName;

	/**确认人单位编码*/
	@Excel(name = "确认人单位编码", width = 15)
    @Schema(description = "确认人单位编码")
    private String verifyDeptId;

	/**计划完成时间*/
	@Excel(name = "计划完成时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划完成时间")
    private Date planFinishDate;

	/**实际完成时间*/
	@Excel(name = "实际完成时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "实际完成时间")
    private Date practicalFinishDate;

	/**行动级别*/
	@Excel(name = "行动级别", width = 15)
    @Schema(description = "行动级别")
    private String actionLevel;

	/**问题分类*/
	@Excel(name = "问题分类", width = 15)
    @Schema(description = "问题分类")
    private String problemTypeId;

	/**行动措施：1、预防；2、纠正*/
	@Excel(name = "行动措施：1、预防；2、纠正", width = 15)
    @Schema(description = "行动措施：1、预防；2、纠正")
    private String actionMeasures;

	/**完成情况  0正在进行 1已完成  2延迟 3取消 4延迟完成*/
	@Excel(name = "完成情况  0正在进行 1已完成  2延迟 3取消 4延迟完成", width = 15)
    @Schema(description = "完成情况  0正在进行 1已完成  2延迟 3取消 4延迟完成")
    private String finishState;

	/**完成情况描述*/
	@Excel(name = "完成情况描述", width = 15)
    @Schema(description = "完成情况描述")
    private String verifyDescription;

	/**完成情况确认*/
	@Excel(name = "完成情况确认", width = 15)
    @Schema(description = "完成情况确认")
    private String finishVerifyState;

    /**是否上传行动计划*/
    @Excel(name = "是否上传行动计划1：是，0：否", width = 15)
    @Schema(description = "是否上传行动计划1：是，0：否")
    private Boolean actionType;

    /**是否有效*/
    @Excel(name = "是否有效1：是，0：否", width = 15)
    @Schema(description = "是否有效1：是，0：否")
    private Boolean isEnable;

    /**对应5w中reasonjson中的对应id*/
    @Excel(name = "对应5w中reasonjson中的对应id", width = 15, format = "yyyy-MM-dd")
    @Schema(description = "对应5w中reasonjson中的对应id")
    private String fiveWhyId;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注说明*/
	@Excel(name = "备注说明", width = 15)
    @Schema(description = "备注说明")
    private String remark;

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**创建人部门*/
	@Excel(name = "创建人部门", width = 15)
    @Schema(description = "创建人部门")
    private String createDept;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**修改人部门*/
	@Excel(name = "修改人部门", width = 15)
    @Schema(description = "修改人部门")
    private String updateDept;

    private static final long serialVersionUID = 1L;
}
