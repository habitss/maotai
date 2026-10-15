package com.keytech.modules.pi.dto;

import com.keytech.modules.pi.entity.PiRepeatExceptionType;
import lombok.Data;
import java.util.List;

/**
 * 复制重复异常配置DTO
 */
@Data
public class PiRepeatCopyDto {
    /**pi报表Id*/
    private String piTableId;
    /**pi报表实际表名*/
    private String piTableCode;
    /**pi字段id*/
    private String piFieldId;
    /**pi字段实际字段名*/
    private String piFieldCode;
    /**复制的配置*/
    List<PiRepeatExceptionType> copyList;
}
