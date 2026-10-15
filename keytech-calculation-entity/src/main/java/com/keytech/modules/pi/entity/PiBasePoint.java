package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 基础点位维护
 * @Author: HQ
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@TableName("mes3_pi_base_point")
@Schema(description="基础点位维护")
public class PiBasePoint implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description="id")
    private String id;

	/**租户id**/
	@Schema(description="租户id")
	private Integer tenantId;

	/**工厂*/
	@Schema(description="工厂")
	@Excel(name = "工厂", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@NotBlank(message = "工厂Code为空")
    private String factoryCode;
	/**部门*/
	@Schema(description="部门")
	@Excel(name = "部门", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@NotBlank(message = "部门Code为空")
    private String deptCode;
	/**产线*/
	@Schema(description="产线")
	@Excel(name = "产线", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@NotBlank(message = "产线Code为空")
	private String lineCode;

	/**设备id*/
	@Schema(description="设备")
	@Excel(name = "设备", width = 15 , dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
	@Dict(dictTable ="mes3_mdm_device where del_flag = 0",dicText = "device_name",dicCode = "id")
    private Integer deviceId;

	/**点位编码*/
	@Excel(name = "点位编码", width = 15)
	@Schema(description="点位编码")
    private String pointCode;

	/**点位类型(步骤/信号点/生产信息)*/
	@Excel(name = "点位类型", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_POINT_TYPE'", dicText = "item_key", dicCode = "item_val")
	@Schema(description="点位类型(步骤/信号点/生产信息)")
	@Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_POINT_TYPE'", dicText = "item_key", dicCode = "item_val")
    private String pointType;
	/**点位中文描述*/
	@Excel(name = "点位中文描述", width = 15)
	@Schema(description="点位中文描述")
    private String pointName;
	/**IP地址*/
	@Excel(name = "IP地址", width = 15)
	@Schema(description="IP地址")
    private String ip;
	/**DB地址*/
	@Excel(name = "DB地址", width = 15)
	@Schema(description="DB地址")
    private String dbAddress;
	/**数据类型*/
	@Excel(name = "数据类型", width = 15)
	@Schema(description="数据类型")
    private String dataType;
	/**汉化方式(步骤汉化/字典表汉化/不汉化)*/
	@Excel(name = "汉化方式", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_SINICIZED'", dicText = "item_key", dicCode = "item_val")
	@Schema(description="汉化方式(步骤汉化/字典表汉化/不汉化)")
	@Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_SINICIZED'", dicText = "item_key", dicCode = "item_val")
    private String cnWay;
	/**字典表汉化code*/
	@Excel(name = "字典表汉化Code", width = 15)
	@Schema(description="字典表汉化Code")
	@Dict(dictTable ="mes3_mdm_dict_group",dicText = "dict_group_name",dicCode = "dict_group_code")
    private String dictCode;

	/**点位功能位置编码*/
	@Excel(name = "点位功能位置编码", width = 15)
	@Schema(description="点位功能位置编码")
	private String tagFunlocCode;

	/**IOT属性编码*/
	@Excel(name = "IOT属性编码", width = 15)
	@Schema(description="IOT属性编码")
	@TableField(updateStrategy = FieldStrategy.ALWAYS)
	private String iotAttrName;

	/**数据属性(0实际值/1标准值)*/
	@Excel(name = "数据属性", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_DATAATTRIBUTE'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_DATAATTRIBUTE'", dicText = "item_key", dicCode = "item_val")
	@Schema(description="数据属性")
	@TableField(updateStrategy = FieldStrategy.ALWAYS)
	private String dataAttribute;

	/**绑定标准值*/
	@Schema(description="绑定标准值")
	@Excel(name = "绑定标准值", width = 15, dictTable ="mes3_pi_base_point where del_flag = 0 and data_attribute = '1'", dicText = "point_name", dicCode = "id")
	@Dict(dictTable ="mes3_pi_base_point where del_flag = 0 and data_attribute = '1'", dicText = "point_name", dicCode = "id")
	@TableField(updateStrategy = FieldStrategy.ALWAYS)
	private String bindStandard;

	/**排序*/
	//@Excel(name = "排序", width = 15)
	@Schema(description="排序")
	private Integer sort;
	/**删除状态(0未删,1已删)*/
	//@Excel(name = "删除状态", width = 15)
	@Schema(description="删除状态(0未删,1已删)")
	@TableLogic
	private Integer delFlag;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description="创建时间")
    private Date createTime;
	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description="修改时间")
    private Date updateTime;
	/**创建人*/
	@Schema(description="创建人")
    private String createBy;
	/**修改人*/
	@Schema(description="修改人")
    private String updateBy;

	/**查询*/
	@TableField(exist = false)
	private String queryDept;

	/**采集周期*/
	@Schema(description="采集周期")
	private Integer collectPeriod;
	/**能源类型*/
	@Schema(description="能源类型")
	private String energyType;
	/**单位*/
	@Schema(description="单位")
	private String unit;

	/**点位最后更新时间*/
	@TableField(exist = false)
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;

	/**点位最新值*/
	@TableField(exist = false)
	private Double latestValue;

	/**订单批次集合  {"orderBatch":"1-2","startDate":"","endDate":""}*/
	@TableField(exist = false)
	private String orderBatchJson;

	/**是否显示最新值 */
	@TableField(exist = false)
	private Boolean isNewData;
	/**标准集合id */
	@TableField(exist = false)
	private String standardIds;
}
