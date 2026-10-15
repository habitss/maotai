package com.keytech.modules.exception.dto;

import lombok.Data;

import java.util.List;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName ExceptionReportVO
 * @Description TODO
 * @createTime 2025年03月28日 11:11:00
 */
@Data
public class ExceptionReportVO {

    private String deptCode;

    private String lineCode;

    private String trigger;

    private String times;

    private List<String> timeRange;

    private int type;
}
