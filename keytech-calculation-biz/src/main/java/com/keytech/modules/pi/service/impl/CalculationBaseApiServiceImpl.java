package com.keytech.modules.pi.service.impl;

import com.keytech.modules.calculation.CalculationBaseApi;
import com.keytech.modules.pi.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 对外API服务
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-26 09:34:22
 */
@Service
public class CalculationBaseApiServiceImpl implements CalculationBaseApi {

    @Resource
    IPiComputationModelService computationModelService;

    /**
     * 触发条件实现方法
     * @param parameterObject 定时任务传入的参数
     */
    @Override
    public void piCalculateSignal(String parameterObject){
        computationModelService.piCalculateSignal(parameterObject);
    }

    /**
     * 重算
     * @param tableName 计算表
     * @param listPI 计算的PI
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     */
//    @Override
//    public void rePiCalculateSignal(String tableName,List<String> listPI, LocalDateTime startTime, LocalDateTime endTime){
//
//        //进入重算程序，重算是将开始时间和结束时间按照5分钟进行分割，然后查询前6分钟的
//        LocalDateTime endTimeTemp = startTime;
//
//        //默认最多重算10天，分割成2880个5分钟
//        for(int i = 0;i<2880; i++){
//            if(i != 0){
//                endTimeTemp = endTimeTemp.plusSeconds(300);
//            }
//            LocalDateTime startTimeTemp = endTimeTemp.minusSeconds(360);
//            if(endTimeTemp.isBefore(endTime)){
//                computationModelService.startCalculateSignal(tableName,listPI,startTimeTemp,endTimeTemp,true);
//            }else{
//                break;
//            }
//        }
//
//    }

}
