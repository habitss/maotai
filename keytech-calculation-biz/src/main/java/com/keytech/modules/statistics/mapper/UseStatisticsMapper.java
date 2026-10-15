package com.keytech.modules.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.statistics.dto.UseStatisticsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description:
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Mapper
public interface UseStatisticsMapper extends BaseMapper<KpiInfo> {


    /**
     * 行动计划
     * @param checkDateBegin
     * @param checkDateEnd
     */
    @Select(" SELECT " +
        "    LEFT(b.org_code, 12) AS deptCode," +
        "    COUNT(*) AS num " +
        " FROM " +
        "    mes3_action_plan_track a " +
        "    INNER JOIN sys_depart b ON a.put_dept_id = b.id " +
        " WHERE " +
        "    a.raise_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} and b.org_code is not null and a.del_flag=0 " +
        " GROUP BY " +
        "    LEFT(b.org_code, 12) ")
    List<UseStatisticsDto> move(@Param("checkDateBegin") String checkDateBegin,
                                @Param("checkDateEnd") String checkDateEnd);


    /**
     * 分层检查
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" SELECT " +
            " LEFT(b.org_code, 12) AS deptCode, " +
            " COUNT(*) AS num " +
            " FROM " +
            " mes3_manage_check_list a " +
            " INNER JOIN sys_depart b ON a.check_user_dept = b.id " +
            " WHERE " +
            " a.check_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} and b.org_code is not null and a.del_flag=0 " +
            " GROUP BY " +
            " LEFT(b.org_code, 12) ")
    List<UseStatisticsDto> layering(@Param("checkDateBegin") String checkDateBegin,
                                    @Param("checkDateEnd") String checkDateEnd);


    /**
     * 有效会议-工厂
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select a.factory_code AS deptCode,count(*) AS num  from "+
            " (select meeting_id ,factory_code from mes3_meeting_check where factory_code is not null and factory_code != '' and del_flag=0 "+
            " and check_time BETWEEN #{checkDateBegin} AND #{checkDateEnd} "+
            " group by meeting_id ,factory_code) a "+
            " group by factory_code ")
    List<UseStatisticsDto> factoryMeeting(@Param("checkDateBegin") String checkDateBegin,
                                          @Param("checkDateEnd") String checkDateEnd);

    /**
     * 有效会议-部门
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select dept_code AS deptCode,count(*) AS num from " +
            " (select meeting_id ,dept_code from mes3_meeting_check where dept_code is not null and dept_code != '' and del_flag=0 " +
            " and length(dept_code) = 12 and del_flag=0 " +
            " and check_time BETWEEN #{checkDateBegin} AND #{checkDateEnd} " +
            " group by meeting_id ,dept_code) a " +
            " group by dept_code ")
    List<UseStatisticsDto> deptMeeting(@Param("checkDateBegin") String checkDateBegin,
                                   @Param("checkDateEnd") String checkDateEnd);

    /**
     * 有效会议-车间
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select LEFT(workshop,12) AS deptCode,count(*) AS num from " +
            " (select meeting_id ,workshop from mes3_meeting_check where workshop is not null and workshop != '' and del_flag=0 " +
            " and check_time BETWEEN #{checkDateBegin} AND #{checkDateEnd} " +
            " group by meeting_id ,workshop) a " +
            " group by  LEFT(workshop,12) ")
    List<UseStatisticsDto> workshopMeeting(@Param("checkDateBegin") String checkDateBegin,
                                   @Param("checkDateEnd") String checkDateEnd);


    /**
     * 异常报告
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select dept_code ,count(*) AS num from mes3_exception_report where dept_code is not null and del_flag=0 " +
            " and exception_time BETWEEN #{checkDateBegin} AND #{checkDateEnd} " +
            " group by dept_code ")
    List<UseStatisticsDto> abnormal(@Param("checkDateBegin") String checkDateBegin,
                                    @Param("checkDateEnd") String checkDateEnd);

    /**
     * 期间核查
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select dept_code ,count(*) AS num from mes3_period_verification_submit where dept_code is not null and del_flag=0 " +
            " and check_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} " +
            " group by dept_code ")
    List<UseStatisticsDto> period(@Param("checkDateBegin") String checkDateBegin,
                                  @Param("checkDateEnd") String checkDateEnd);

    /**
     * COP报表
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select dept_code ,count(*) AS num from mes3_cop_submit where dept_code is not null and del_flag=0 " +
            " and clear_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} " +
            " group by dept_code ")
    List<UseStatisticsDto> cop(@Param("checkDateBegin") String checkDateBegin,
                               @Param("checkDateEnd") String checkDateEnd);

    /**
     * 岗位记录(通用)
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select a.dept_code,count(*) AS num from  mes3_post_check_record_datainfo_main a INNER JOIN mes3_post_check_record_config_main b on " +
            " a.config_main_id = b.id and b.report_type=1 " +
            " where a.dept_code is not null and a.filling_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} and a.del_flag=0 " +
            " group by a.dept_code " )
    List<UseStatisticsDto> post(@Param("checkDateBegin") String checkDateBegin,
                                @Param("checkDateEnd") String checkDateEnd);

    /**
     * 岗位检查清单
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select a.dept_code,count(*) AS num from  mes3_post_check_record_datainfo_main a INNER JOIN mes3_post_check_record_config_main b on " +
            " a.config_main_id = b.id and b.report_type=2 " +
            " where a.dept_code is not null and a.filling_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} and a.del_flag=0 " +
            " group by a.dept_code " )
    List<UseStatisticsDto> postInspect(@Param("checkDateBegin") String checkDateBegin,
                                       @Param("checkDateEnd") String checkDateEnd);

    /**
     * 管理者检查清单
     * @param checkDateBegin
     * @param checkDateEnd
     * @return
     */
    @Select(" select dept_code ,count(*) AS num from mes3_manager_checklist_edit_main where dept_code is not null and del_flag=0 " +
            " and record_date BETWEEN #{checkDateBegin} AND #{checkDateEnd} " +
            " group by dept_code " )
    List<UseStatisticsDto> managerInspect(@Param("checkDateBegin") String checkDateBegin,
                                          @Param("checkDateEnd") String checkDateEnd);

    /**
     * 获取KPI实际值
     * @param year
     * @return
     */
    @Select(" select a.id,b.dept_code,a.kpi_code,a.value,a.year,b.perent_id from mes3_kpi_report_actual_day_value a INNER JOIN " +
            " mes3_kpi_info b on a.kpi_code = b.kpi_code " +
            " where a.year = #{year} and a.del_flag=0")
    List<KpiReportActualDayValue> kpiActual(@Param("year") String year);
}
