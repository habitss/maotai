package com.keytech.modules.opl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OplWriteTwoRrDto {
    private String oplTitle;
    private String r1;
    private String r2;
    private String a;
    private String c;
    private String i;
    private String time;
}
