package com.keytech.modules.i18n.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 国际化词条
 * @author yepeng
 * @since 2025.12.16
 * */
@Data
@TableName("mes3_mdm_i18n_entry")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="国际化词条")
public class MdmI18nEntry implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键ID*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**词条标识*/
	@Excel(name = "词条标识", width = 15)
    @Schema(description = "词条标识")
    private String entryKey;
	/**所属模块*/
	@Excel(name = "所属模块", width = 15, dictTable = "mes3_mdm_i18n_module", dicText = "module_name", dicCode = "module_code")
	@Dict(dictTable = "mes3_mdm_i18n_module", dicText = "module_name", dicCode = "module_code")
    @Schema(description = "所属模块")
    private String module;
	/**词条描述*/
	@Excel(name = "词条描述", width = 15)
    @Schema(description = "词条描述")
    private String description;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @Schema(description = "排序号")
    private Integer sort;
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
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;
}
