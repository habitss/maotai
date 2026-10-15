package com.keytech.modules.alarm.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 报警点位类型
 * @author liu
 * @since 2026.01.26
 * */
@Data
@TableName("alarm_types")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="报警点位类型")
public class AlarmTypes implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**类型名称*/
	@Excel(name = "类型名称", width = 15)
    @Schema(description = "类型名称")
    private String typeName;
	/**父类型ID*/
    @Excel(name = "父类型", width = 15, dictTable = "alarm_types", dicText = "type_name", dicCode = "id")
    @Dict(dictTable = "alarm_types", dicText = "type_name", dicCode = "id")
    @Schema(description = "父类型ID")
    private Integer parentId;
	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
	/**删除标记*/
	@Excel(name = "删除标记", width = 15)
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;

    /**是否有子节点*/
    @Excel(name = "是否有子节点", width = 15, dicCode = "yn")
    @Dict(dicCode = "yn")
    @Schema(description = "是否有子节点")
    private String hasChild;

    //大类
    @TableField(exist = false)
    private String categoryName;
}
