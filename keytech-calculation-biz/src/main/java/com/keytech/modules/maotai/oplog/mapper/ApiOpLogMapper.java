package com.keytech.modules.maotai.oplog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.oplog.entity.ApiOpLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * 接口操作日志(总表) Mapper。
 * cleanup 用物理 DELETE(entity 有 @TableLogic,走 removeById 会变 UPDATE,清理必须物理删)。
 * claimForRetry 是条件更新抢占(影响行数=1 才算占到,防 job 重叠双发)。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Mapper
public interface ApiOpLogMapper extends BaseMapper<ApiOpLog> {

    /** 物理删除超期日志(不含待重试行),LIMIT 分批防大事务长锁 */
    @Delete("DELETE FROM mes3_maotai_api_op_log WHERE create_time < #{cutoff} AND (retry_status IS NULL OR retry_status <> 0) LIMIT 500")
    int cleanupBefore(@Param("cutoff") Date cutoff);

    /** 抢占待重试行:仅 retry_status=0 时生效,同时计入重试次数、顺延下次时间 */
    @Update("UPDATE mes3_maotai_api_op_log SET retry_count = retry_count + 1, next_retry_time = #{nextRetryTime}, update_time = NOW() WHERE id = #{id} AND retry_status = 0")
    int claimForRetry(@Param("id") String id, @Param("nextRetryTime") Date nextRetryTime);
}
