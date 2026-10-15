package com.keytech.modules.alarm.mapper;


import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.vo.AlarmCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报警事实
 * @author liu
 * @since 2026.01.12
 * */
@Mapper
public interface AlarmEventMapper extends BaseMapper<AlarmEvent> {

    //按报警类型统计数量
    List<AlarmCountVo> selectCountByAlarmCategory(@Param("factoryCode") String factoryCode,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    //按设备统计数量
    List<AlarmCountVo> selectCountByDevice(@Param("factoryCode") String factoryCode,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("orderBy") String orderBy,
                                           @Param("limit") Integer limit);

    /**
     * 查询重复报警数量
     * @param sourceSystem 来源系统
     * @param requestId 接口请求ID
     * @return 重复报警数量
     * */
    @Select("""
        SELECT count(1) FROM alarm_event
        WHERE source_system = #{sourceSystem} AND request_id = #{requestId} AND del_flag = 0
    """)
    Integer countDuplicate(@Param("sourceSystem") String sourceSystem, @Param("requestId") String requestId);

    /**
     * 按 org_code 批量查部门名(给中集报警列表翻译 factory/dept/line 用)。
     * <p><b>@InterceptorIgnore(tenantLine="true") 关掉多租户拦截</b>——jeecg 3.7.1 多租户拦截器会给
     * sys_depart 查询拼 tenant_id 条件,但 maotai 单租户场景下上下文租户值是 "1006,1006"(逗号串),
     * 直接拼成 {@code tenant_id = 1006,1006} 非法 SQL。部门表是跨租户共享的字典表,本就该忽略租户隔离。
     * 不关拦截器会抛 SQLSyntaxErrorException(见 ScadaAlarmQueryController.fillDeptNames)。
     * @param codes org_code 集合(factory/dept/line 合集)
     * @return List&lt;Map&gt; 每项 {org_code, depart_name};只取这两列
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("<script>" +
            "SELECT org_code, depart_name FROM sys_depart WHERE del_flag = 0 AND org_code IN " +
            "<foreach collection='codes' item='c' open='(' separator=',' close=')'>#{c}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectDeptNameByOrgCodes(@Param("codes") List<String> codes);
}
