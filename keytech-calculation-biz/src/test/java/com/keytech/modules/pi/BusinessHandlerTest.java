package com.keytech.modules.pi;


import com.keytech.modules.pi.common.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author f'l
 * @date 2024/11/25 14:51
 */

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j

public class BusinessHandlerTest {
    @Resource
    private InfluxDBClientHelper influxDBClientHelper;

    @Resource
    private BusinessHandler businessHandler;


    @Test
    public void query() {
        QueryCondition queryCondition = new QueryCondition();
        LocalDateTime e=LocalDateTime.now();
        queryCondition.setStart(e.minusDays(1));
        queryCondition.setEnd(e);
        queryCondition.setQueryType(QueryType.FIRST);
        List<QueryTagCondition> tagIds = new ArrayList<>();
        QueryTagCondition queryTagCondition = new QueryTagCondition();
        queryTagCondition.setTagId("APAC-CNN7223001-0101-AGA_CU-INV");
        tagIds.add(queryTagCondition);
        queryCondition.setQueryTagConditionList(tagIds);
        influxDBClientHelper.query(queryCondition);
    }


    @Test
    public void upOrDownNearestValue() {
        QueryCondition queryCondition = new QueryCondition();
        LocalDateTime e=LocalDateTime.now();
        queryCondition.setStart(e.minusDays(7));
        queryCondition.setEnd(e);
        List<QueryTagCondition> list = new ArrayList<>();
        QueryTagCondition queryTagCondition = new QueryTagCondition();
        queryTagCondition.setTagId("APAC-CNN7223001-0101-AGA_CU-INV");
        QueryTagCondition queryTagCondition2 = new QueryTagCondition();
        queryTagCondition2.setTagId("APAC-CNN7223001-0901-SCT_HM_SW_SG-INV");
        QueryTagCondition queryTagCondition3 = new QueryTagCondition();
        queryTagCondition3.setTagId("APAC-CNN7223001-0901-RE_ID-INV");

//        queryTagCondition.setValue(0f);
        list.add(queryTagCondition);
        list.add(queryTagCondition2);
        list.add(queryTagCondition3);
        queryCondition.setQueryTagConditionList(list);
//        businessHandler.upOrDownNearestValue(queryCondition);
//        businessHandler.getMeasurementRowByTime(queryCondition);

        List<QueryTagCondition> list4 = new ArrayList<>();
        QueryTagCondition queryTagCondition4 = new QueryTagCondition();
        queryTagCondition4.setTagId("APAC-CNN7223001-0101-MID_LOP_STM_FL-INV");
        queryTagCondition4.setValue(8.68055499999993f);
        list4.add(queryTagCondition4);
        queryCondition.setQueryTagConditionList(list4);
        businessHandler.getStartAndEndTimeForContinuousTag(queryCondition);
    }


    @Test
    public void getMeasurementRowByTime() {
        QueryCondition queryCondition = new QueryCondition();
        LocalDateTime e=LocalDateTime.now();
        queryCondition.setStart(e.minusDays(7));
        queryCondition.setEnd(e);
        List<QueryTagCondition> list = new ArrayList<>();
        QueryTagCondition queryTagCondition = new QueryTagCondition();
        queryTagCondition.setTagId("APAC-CNN7223001-0101-AGA_CU-INV");
        QueryTagCondition queryTagCondition2 = new QueryTagCondition();
        queryTagCondition2.setTagId("APAC-CNN7223001-0901-SCT_HM_SW_SG-INV");
        QueryTagCondition queryTagCondition3 = new QueryTagCondition();
        queryTagCondition3.setTagId("APAC-CNN7223001-0901-RE_ID-INV");

//        queryTagCondition.setValue(0f);
        list.add(queryTagCondition);
        list.add(queryTagCondition2);
        list.add(queryTagCondition3);
        queryCondition.setQueryTagConditionList(list);
//        businessHandler.upOrDownNearestValue(queryCondition);
//        businessHandler.getMeasurementRowByTime(queryCondition);

        List<QueryTagCondition> list4 = new ArrayList<>();
        QueryTagCondition queryTagCondition4 = new QueryTagCondition();
        queryTagCondition4.setTagId("APAC-CNN7223001-0101-MID_LOP_STM_FL-INV");
        queryTagCondition4.setValue(8.68055499999993f);
        list4.add(queryTagCondition4);
        queryCondition.setQueryTagConditionList(list4);
        businessHandler.getStartAndEndTimeForContinuousTag(queryCondition);
    }

    @Test
    public void getStartAndEndTimeForContinuousTag() {
        QueryCondition queryCondition = new QueryCondition();
        LocalDateTime e=LocalDateTime.now();
        queryCondition.setStart(e.minusDays(7));
        queryCondition.setEnd(e);
        List<QueryTagCondition> list = new ArrayList<>();
        QueryTagCondition queryTagCondition = new QueryTagCondition();
        queryTagCondition.setTagId("APAC-CNN7223001-0101-AGA_CU-INV");
        QueryTagCondition queryTagCondition2 = new QueryTagCondition();
        queryTagCondition2.setTagId("APAC-CNN7223001-0901-SCT_HM_SW_SG-INV");
        QueryTagCondition queryTagCondition3 = new QueryTagCondition();
        queryTagCondition3.setTagId("APAC-CNN7223001-0901-RE_ID-INV");

//        queryTagCondition.setValue(0f);
        list.add(queryTagCondition);
        list.add(queryTagCondition2);
        list.add(queryTagCondition3);
        queryCondition.setQueryTagConditionList(list);
//        businessHandler.upOrDownNearestValue(queryCondition);
//        businessHandler.getMeasurementRowByTime(queryCondition);

        List<QueryTagCondition> list4 = new ArrayList<>();
        QueryTagCondition queryTagCondition4 = new QueryTagCondition();
        queryTagCondition4.setTagId("APAC-CNN7223001-0101-MID_LOP_STM_FL-INV");
        queryTagCondition4.setValue(8.68055499999993f);
        list4.add(queryTagCondition4);
        queryCondition.setQueryTagConditionList(list4);
        businessHandler.getStartAndEndTimeForContinuousTag(queryCondition);
    }
}
