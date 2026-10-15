package com.keytech.modules.maotai.equipment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备操作手册(SOP/SWI 等文档)
 * @author maotai
 * @since 2026-06-15
 */
@Data
@TableName("mes3_maotai_equipment_doc")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "设备操作手册")
public class EquipmentDoc extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**所属设备ID(关联 mes3_maotai_equipment_ledger.id)*/
    @Schema(description = "所属设备ID")
    private String equipmentId;

    /**文档类型(字典 MES3_EQUIP_DOC_TYPE:SOP/SWI/MT/EM)*/
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_EQUIP_DOC_TYPE' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "文档类型")
    private String docType;

    /**文档标题*/
    @Schema(description = "文档标题")
    private String docTitle;

    /**文档编码(如 SOP-YS-4500-001)*/
    @Schema(description = "文档编码")
    private String docCode;

    /**版本(如 V3.2)*/
    @Schema(description = "版本")
    private String version;

    /**编制人*/
    @Schema(description = "编制人")
    private String author;

    /**审批人*/
    @Schema(description = "审批人")
    private String approver;

    /**发布日期*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "发布日期")
    private Date publishDate;

    /**复审日期*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "复审日期")
    private Date reviewDate;

    /**上传文件URL(/sys/common/upload 返回)*/
    @Schema(description = "上传文件URL")
    private String fileUrl;

    /**上传文件名*/
    @Schema(description = "上传文件名")
    private String fileName;

    /**备注*/
    @Schema(description = "备注")
    private String remark;
}
