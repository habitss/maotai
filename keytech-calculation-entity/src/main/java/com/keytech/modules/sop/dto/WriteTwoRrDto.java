package com.keytech.modules.sop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: xzz
 * @date: 2024/7/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteTwoRrDto {
    private String sopTitle;
    private String r1;
    private String r2;
    private String a;
    private String c;
    private String i;
    private String time;
}
