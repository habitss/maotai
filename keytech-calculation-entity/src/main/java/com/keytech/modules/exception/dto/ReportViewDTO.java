package com.keytech.modules.exception.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName ReportViewDTO
 * @Description TODO
 * @createTime 2025年03月27日 22:58:00
 */
@Data
public class ReportViewDTO {

    //1 产线 2.异常分类
    private String type;

    private long jan;

    private long feb;

    private long mar;

    private long apr;

    private long may;

    private long jun;

    private long jul;

    private long aug;

    private long sep;

    private long oct;

    private long nov;

    private long dec;

    private long total;

    private int count;
}
