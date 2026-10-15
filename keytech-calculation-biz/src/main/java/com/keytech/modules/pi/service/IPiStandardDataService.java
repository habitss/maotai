package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.PiGreenDataDto;
import com.keytech.modules.pi.dto.RerunDto;
import com.keytech.modules.pi.entity.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: PI数据红黄区
 * @Author: aolei
 * @Date:   2024-12-16
 * @Version: V1.0
 */
public interface IPiStandardDataService extends BaseService<PiStandardData> {

    /**
     * 开始红黄区标准计算算法，定时任务调用后，每次计算5分钟内有数据更新的数据
     */
    void startStandardCalculate();

    /**
     * 重算红黄区
     * @param rerunDto
     */
    void reSunStandardCalculate(RerunDto rerunDto);

    /**
     * 计算红黄区
     * @param data PI值
     * @param piStandardMaintenanceList 标准集合
     * @param customizeTableFieldByTableName 字段配置信息
     * @return field：field4，color：red，manage_id，maintenance_id格式的json字符串
     */
    String getRedYellowJson(Map<String,Object> data, List<PiStandardMaintenance> piStandardMaintenanceList,List<PiCustomizeTableField> customizeTableFieldByTableName);

    /**
     * 剔除PI数据，将pi显示成灰色
     * @param tableCode 表名称
     * @param piCodes PI集合
     * @param dataId 数据id
     */
    void setPiColorToGrey(String tableCode,List<String> piCodes,String dataId);

    /**
     * PI绿区率查询
     * @param tableName 表中文名称
     * @param tableId 表ID
     * @param tableCode 表英文名称
     * @param queryField 查询字段名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<PiGreenDataDto> piGreenData(String tableName, String tableId, String tableCode, String queryField, String startTime, String endTime);

    /**
     * 首页重点关注pi查询
     * @param factoryCode 工厂编码
     * @param deptCode 部门编码
     * @return
     */
    List<PiGreenDataDto> getPayCloseAttentionPi(String factoryCode, String deptCode, String isFocus, String startTime, String endTime);

    /**
     * 获取一致性指数（主表明细）
     * @param zbList 指数zbid集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public List<ConsistencyConfigZb> getConsistencyIndexBySum(List<ConsistencyConfigZb> zbList, String startTime, String endTime);

    /**
     * 获取一致性指数（明细表明细）
     * @param mxList 明细表ID集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public List<ConsistencyConfigMx> getConsistencyIndexByDetails(List<ConsistencyConfigMx> mxList,String startTime, String endTime);


    /**
     * PI周月累统计查询
     * @param deptCode 部门编码
     * @param lineCode 产线编码
     * @param year 年份
     * @param months 月份
     * @param piName PI名称
     * @return 统计结果
     */
    Map<String, Object> getPiMonWeekSummary(String deptCode, String lineCode, String year, String months, String piName);

    /**
     * PI绿区率查询-根据部门查询
     * @param deptCode 部门编码
     * @param lineCode 产线编码
     * @param piName PI名称
     * @param tableIds 表id（PI报表ID）
     * @param queryField 查询字段
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 结果
     */
    List<PiGreenDataDto> piGreenDataByDept(String deptCode, String lineCode, String piName, String tableIds, String queryField, String startTime, String endTime);

}
