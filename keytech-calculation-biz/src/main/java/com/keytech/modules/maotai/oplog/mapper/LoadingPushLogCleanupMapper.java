package com.keytech.modules.maotai.oplog.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * loading_push_log 物理清理专用 Mapper。
 * LoadingPushLog entity 有 @TableLogic,走 BaseMapper 的 delete 会变 UPDATE,这里必须物理 DELETE。
 * 必须放在 mapper 包(项目 MapperScan 扫 com.keytech.modules.**.mapper*,放 job 包注册不上——
 * 2026-08-14 部署时踩过:bean 找不到导致启动失败)。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Mapper
public interface LoadingPushLogCleanupMapper {

    /** 物理删除超期推送流水,LIMIT 分批 */
    @Delete("DELETE FROM mes3_maotai_loading_push_log WHERE create_time < #{cutoff} LIMIT 500")
    int cleanupPushLogBefore(@Param("cutoff") Date cutoff);
}
