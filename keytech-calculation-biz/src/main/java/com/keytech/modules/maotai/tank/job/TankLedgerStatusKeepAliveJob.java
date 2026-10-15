package com.keytech.modules.maotai.tank.job;

import com.keytech.modules.maotai.tank.service.ITankRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备台账罐体状态保持 job（兜底，与驾驶舱实时回写共存）。
 * <p>
 * 设计（2026-09-11 定稿）：罐体状态的唯一源头是 InfluxDB 点位实时判定
 * （TankRealtimeServiceImpl：点位 LAST + 阈值/保养判定），设备台账 run_status
 * 必须始终等于该判定结果。驾驶舱页面被访问时由请求路径实时回写（10 秒轮询）；
 * 无人访问驾驶舱期间由本 job 兜底——每分钟代跑一遍全部 4 车间的判定+回写
 * （复用 listByWorkshop 全链路），粒度与模拟器每分钟写入一致。
 * <p>
 * 台账 3-维修为人工状态，回写逻辑内置跳过，本 job 无需处理。
 *
 * @author cockpit
 * @since 2026-09-11
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TankLedgerStatusKeepAliveJob {

    private final ITankRealtimeService tankRealtimeService;

    /** 每分钟第 10 秒执行,错开模拟器整分写入,读到的一定是本分钟新值 */
    @Scheduled(cron = "10 * * * * ?")
    public void refresh() {
        try {
            for (int ws = 0; ws < 4; ws++) {
                tankRealtimeService.listByWorkshop(ws);
            }
        } catch (Exception e) {
            log.error("[台账状态保持] 兜底刷新失败", e);
        }
    }
}
