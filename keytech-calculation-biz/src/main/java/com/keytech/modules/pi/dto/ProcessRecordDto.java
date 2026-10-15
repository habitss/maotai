package com.keytech.modules.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @FileName ProcessRecordDto
 * @Description PI过程记录
 * @Author HQ
 * @date 2026-03-02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRecordDto {
    //PI
    private String field;

    //最后更新时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    //计算信号ID
    private String calculateSignalId;

    //算法类型 (0基础算法 1adapter算法  2RA算法)
    private String type = "0";
}
