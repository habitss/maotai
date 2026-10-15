package com.keytech.modules.pi.service;


import cn.hutool.core.lang.Pair;
import com.keytech.base.service.BaseService;

import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.entity.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * pi-计算模型
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-15 09:34:22
 */
public interface IPiComputationModelService extends BaseService<PiComputationModel> {

    /**
     * 根据计算信号id集合查询所有的取值信号集合
     * @param calculateIds
     * @return
     */
    List<PiComputationModel> queryListBycalculateIds(List<String> calculateIds);

    /**
     * 触发条件实现方法
     * @param parameterObject 定时任务传入的参数
     */
    void piCalculateSignal(String parameterObject);

    /**
     * 配置定时重算（每次重算一周内的数据）
     * @param parameterObject 定时任务传入的参数
     */
    void automaticRePiCalculateSignal(String parameterObject);

    /**
     * 重算
     * @param tableName 表名称
     * @param listPI PI集合
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     */
    void rePiCalculateSignal(String tableName,List<String> listPI, LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 开始计算
     * @param piCalculateSignalList 计算信号集合
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     * @param reCalculateBj 重算标记 1定时计算，2手动重算，3定时重算
     */
    void startCalculateSignal(List<PiCalculateSignal> piCalculateSignalList,LocalDateTime startTime,LocalDateTime endTime,int reCalculateBj);

    /**
     * 进入计算核心逻辑
     * @param calculateSignal 触发条件对象
     */
    void calculate(PiCalculateSignal calculateSignal);

    /**
     * 模型计算（PI日报间的计算）
     * @param dataId
     * @param piTable
     * @param piCode
     * @param time
     */
    void arithmeticByPi(String dataId,String piTable,String piCode,LocalDateTime time,int calculateBj);

    /**
     * 模型计算（基础点位间的计算）
     * @param calculateSignal 触发信号模型
     * @return
     */
    String arithmeticByBasic(PiCalculateSignal calculateSignal);

    /**
     * 获取唯一约束的值
     * @param calculateSignal 触发模型
     * @return
     */
    List<TableConditionDto> getConditionList(PiCalculateSignal calculateSignal);


    /**
     * 瞬时取数的取数信号判断
     * @param calculateSignal 触发条件对象
     * @return
     */
    boolean retrievalSignalByMoment(PiCalculateSignal calculateSignal);

    /**
     * 根据范围取数信号获取符合要求的时间范围
     * @param calculateSignal 触发信号
     * @return
     */
    List<Pair<LocalDateTime,LocalDateTime>> getTimeListByRetrievalStep(PiCalculateSignal calculateSignal);


    /**
     * 计算（最大值、最小值、平均值、累计值、第一个值、最后一个值等）-监控型算法
     * @param calculateSignal 触发信号
     * @param frequency 频率
     * @param listTimePair 时间集合
     * @return
     */
    String influxDbCommFunByMonitoring(PiCalculateSignal calculateSignal,Integer frequency, List<Pair<LocalDateTime,LocalDateTime>> listTimePair);

    /**
     * 定时按照PI数据表配置的天数，进行数据清理，将多余的PI数据清理掉
     */
    void dataCleansing();

    /**
     * 根据模型配置的计算公式，提取出里面对应的pi或者基础点位
     * @param formulaCode 计算公式
     * @return
     */
    List<String> getListPiCodeByFormulaCode(String formulaCode);

    /**
     * 计算（获取瞬时数据）
     * @param tagCodes 基础点位集合
     * @param time 时间
     * @param type 类型，up向上、down向下、now现在
     * @return
     */
    List<MeasurementData> influxDbMoment(List<String> tagCodes, LocalDateTime time, String type);

    /**
     * 传入开始时间，结束时间和点位列表，查询列表时间范围内，influxDB中的数据，返回一个MeasurementData集合
     * @param startTime
     * @param endTime
     * @param calculateSignalTagCodeList
     * @param frequency
     * @return
     */
    List<MeasurementData> getMeasurementDataList(LocalDateTime startTime,LocalDateTime endTime,List<String> calculateSignalTagCodeList,Integer frequency);

    /**
     * 手动修改PI，触发PI间算法，进行自动计算
     * @param dataIds 修改的数据行id
     * @param piTable 修改的数据表
     */
    void savePICalculateSignal(List<String> dataIds, String piTable,List<String> piCodes);

    /**
     * 取两个时间集合中所有的交集
     * @param startIntervals 开始集合
     * @param endIntervals 需要比对的集合
     * @return
     */
     List<Pair<LocalDateTime,LocalDateTime>> getIntersectionFromTimeList(List<Pair<LocalDateTime, LocalDateTime>> startIntervals,List<Pair<LocalDateTime, LocalDateTime>> endIntervals);

    /**
     * 合并重叠的时间区间
     * @param timePairs 时间区间列表
     * @return 合并后的时间区间列表
     */
     List<Pair<LocalDateTime, LocalDateTime>> mergeOverlappingTimePairs(List<Pair<LocalDateTime, LocalDateTime>> timePairs);


    /**
     * 对数据进行格式化转换
     * @param retData
     * @return
     */
     String changeDataFormat(String retData);
}
