package com.keytech.modules.i18n.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 国际化词条翻译
 * @author yepeng
 * @since 2025.12.16
 * */
@Data
@TableName("mes3_mdm_i18n_entry_lang")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="国际化词条翻译")
public class MdmI18nEntryLang implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键ID*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**词条ID*/
	@Excel(name = "词条描述", width = 15, dictTable = "mes3_mdm_i18n_entry", dicText = "description", dicCode = "id")
	@Dict(dictTable = "mes3_mdm_i18n_entry", dicText = "description", dicCode = "id")
    @Schema(description = "词条ID")
    private Integer entryId;
	/**语言编码*/
	@Excel(name = "语言编码", width = 15, dictTable = "mes3_mdm_i18n_lang", dicText = "lang_name", dicCode = "lang_code")
	@Dict(dictTable = "mes3_mdm_i18n_lang", dicText = "lang_name", dicCode = "lang_code")
    @Schema(description = "语言编码")
    private String langCode;
	/**内容*/
	@Excel(name = "内容", width = 15)
    @Schema(description = "内容")
    private String content;
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

    //词条Key
    @TableField(exist = false)
    private String entryKey;
}
