package com.keytech.modules.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hq
 * @date 2025/05/12/
 * 系统使用统计(各功能部门 数量)
 */
@Data
@NoArgsConstructor // 无参构造
@AllArgsConstructor // 全参构造
public class UseStatisticsDto {

    //部门
    private String deptCode;

    //数量
    private Integer num;
}
