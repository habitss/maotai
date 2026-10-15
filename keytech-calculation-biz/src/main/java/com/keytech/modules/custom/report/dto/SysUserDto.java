package com.keytech.modules.custom.report.dto;

import lombok.Data;

/**
 * 用户
 */
@Data
public class SysUserDto {
    private String id;
    /**
     * 登录名
     */
    private String username;

    /**
     * 姓名
     */
    private String realname;
}
