package com.keytech.modules.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


/**
 * 算法重算
 */
@Data
public class RerunDto {

    /**自定义表表名*/
    private String tableName;

    /**自定义表id*/
    private String tableId;

    /**字段集合*/
    private List<String> fieldList;

    /**范围开始时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date timeStart;

    /**范围及时时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date timeEnd;

}
