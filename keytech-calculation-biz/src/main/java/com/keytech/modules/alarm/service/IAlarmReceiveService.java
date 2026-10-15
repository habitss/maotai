package com.keytech.modules.alarm.service;

import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.dto.AlarmReceiveResult;
import com.keytech.modules.alarm.entity.AlarmPoint;
import java.util.List;

/**
 * 报警接收服务
 * @author yepeng
 * @since 2026.01.20
 */
public interface IAlarmReceiveService {
    /**
     * 批量接收报警数据
     * @param reqList 报警数据接收请求参数列表
     */
    void batchReceive(List<AlarmReceiveRequest> reqList);

    /**
     * 接收报警数据
     * @param req 报警数据接收请求参数
     * @param point 报警点位
     * @return 报警接收结果
     */
    AlarmReceiveResult receive(AlarmReceiveRequest req, AlarmPoint point);

}
