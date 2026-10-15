package com.keytech.modules.pi;

import cn.hutool.core.lang.Pair;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.IPiComputationModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PI计算模型测试用例
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PiComputationModelTest {

    @Resource
    private IPiComputationModelService piComputationModelService;

    /**
     * 测试模型核心方法
     * --story=1049777 --user=敖磊 01–13-PI算法模型-计算结果模型-计算型 https://www.tapd.cn/63554897/s/1327183
     */
    @Test
    public void calculateTest(){
        PiCalculateSignal calculateSignal = new PiCalculateSignal();
        piComputationModelService.calculate(calculateSignal);
    }

    /**
     * 重算
     */
    @Test
    public void rePiCalculateSignal(){
        String tableName = "";
        List<String> listPI = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now();
        piComputationModelService.rePiCalculateSignal( tableName, listPI, startTime, endTime);
    }


    /**
     * PI算法模型-计算结果模型-结果型
     * --story=1049776 --user=敖磊 01-12-PI算法模型-计算结果模型-结果型 https://www.tapd.cn/63554897/s/1327185
     */
    @Test
    public void arithmeticByPiTest(){
        piComputationModelService.arithmeticByPi("1","mes3_database_001","field_0001",LocalDateTime.now(),1);
    }


    /**
     * PI算法模型-计算结果模型-时间型
     * --story=1049775 --user=敖磊 01-11-PI算法模型-计算结果模型-时间型 https://www.tapd.cn/63554897/s/1327199
     */
    @Test
    public void arithmeticByBasicTest(){
        PiCalculateSignal calculateSignal = new PiCalculateSignal();
        piComputationModelService.arithmeticByBasic(calculateSignal);
    }

    /**
     * PI算法模型-自定义数据表-字段维护
     * --story=1049768 --user=敖磊 01-05-PI算法模型-自定义数据表-字段维护 https://www.tapd.cn/63554897/s/1327202
     */
    @Test
    public void retrievalSignalByMomentTest(){
        List<PiRetrievalSignal> retrievalSignalList = new ArrayList<>();
        PiCalculateSignal calculateSignal = new PiCalculateSignal();
        piComputationModelService.retrievalSignalByMoment(calculateSignal);
    }

    /**
     * 触发条件实现方法
     * --story=1049893 --user=敖磊 01-22-PI算法模型-定时任务服务对接 https://www.tapd.cn/63554897/s/1328396
     */
    @Test
    public void piCalculateSignal(){
        String parameterObject = "360";
        piComputationModelService.piCalculateSignal(parameterObject);
    }

    /**
     * 计算实现方法
     * --story=1049938 --user=敖磊 01-23-PI算法模型-联调测试 https://www.tapd.cn/63554897/s/1329819
     */
    @Test
    public void startCalculateSignal(){
        List<PiCalculateSignal> piCalculateSignalList = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now();
        piComputationModelService.startCalculateSignal(piCalculateSignalList,startTime,endTime,1);
    }


    /**
     * 根据范围取数信号获取符合要求的时间范围
     * --story=1049939 --user=敖磊 01-24-PI算法模型-监控型算法需求讨论及集成 https://www.tapd.cn/63554897/s/1331394
     */
    @Test
    public void getTimeListByRetrievalStep(){
        PiCalculateSignal calculateSignal = new PiCalculateSignal();
        piComputationModelService.getTimeListByRetrievalStep(calculateSignal);
    }

    /**
     * 根据范围取数信号获取符合要求的时间范围
     * --story=1049939 --user=敖磊 01-24-PI算法模型-监控型算法需求讨论及集成 https://www.tapd.cn/63554897/s/1331394
     */
    @Test
    public String influxDbCommFunByMonitoring(){
        List<PiComputationMonitorModel> listMonitorModel = new ArrayList<>();
        Integer frequency = 60;
        List<Pair<LocalDateTime,LocalDateTime>> listTimePair = new ArrayList<>();
        PiCalculateSignal calculateSignal = new PiCalculateSignal();
        return piComputationModelService.influxDbCommFunByMonitoring(calculateSignal, frequency, listTimePair);
    }


}
