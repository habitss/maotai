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
 * 国际化语言定义
 * @author yepeng
 * @since 2025.12.16
 * */
@Data
@TableName("mes3_mdm_i18n_lang")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="国际化语言定义")
public class MdmI18nLang implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键ID*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**语言编码*/
	@Excel(name = "语言编码", width = 15)
    @Schema(description = "语言编码")
    private String langCode;
	/**语言名称*/
	@Excel(name = "语言名称", width = 15)
    @Schema(description = "语言名称")
    private String langName;
	/**是否可用*/
	@Excel(name = "是否可用", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_YN'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_YN'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "是否可用")
    private Integer enabled;
	/**版本号*/
	@Excel(name = "版本号", width = 15)
    @Schema(description = "版本号")
    private Long version;
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
