package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

/**
 * 外排水推送对象
 * @author aolei
 * @create 2020-05-05 16:07
 */
@Data
public class EhsSendDto {
    /**
     * 数据中⼼表ID
     */
    private String companyId;

    /**
     * 创建时间：同监测时间
     * 数据中⼼表创建时间
     */
    private String createTime;

    /**
     * 监测时间
     * YYYY-MM-DD HH:MM:SS
     */
    private String monitorDateTime;

    /**
     * 采集值 ，值对应单位 ，涉及到换算 ，详见枚举2：采集值单位换算表
     */
    private String monitorValue;

    /**
     * ⼯⼚code：
     * 需与PS中⼀致 ，见枚 举3：⼯⼚code
     */
    private String orgCode;

    /**
     * 点位CODE ，详见枚 举1：点位信息
     */
    private String paramCode;

    /**
     * 点位名称
     */
    private String paramName;

    /**
     * 处理消息
     */
    private String processMsg;

    /**
     * 处理状态
     */
    private String processStatus;

    /**
     * 采集时间
     */
    private String scadaInsertTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 更新时间：同监测时间
     * 数据中⼼表更新时间
     */
    private String updateTime;
}
