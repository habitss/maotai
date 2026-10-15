package com.keytech.modules.lims.dto;

import lombok.Data;

/**
 * lims返回
 */
@Data
public class LimsResultDto {

    String code;
    String msg;

    public static LimsResultDto error(String code, String msg) {
        LimsResultDto r = new LimsResultDto();
        r.setMsg(msg);
        r.setCode(code);
        return r;
    }


    public static LimsResultDto success(String code) {
        LimsResultDto r = new LimsResultDto();
        r.setCode(code);
        return r;
    }

    public static LimsResultDto success(String code, String msg) {
        LimsResultDto r = new LimsResultDto();
        r.setMsg(msg);
        r.setCode(code);
        return r;
    }
}
