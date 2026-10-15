package com.keytech.modules.pi;

import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class PiCustomizeTableFieldTest {

    @Resource
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;


    //--story=1049768 --user=贺强 01-05-PI算法模型-自定义数据表-字段维护 https://www.tapd.cn/63554897/s/1327904
    @Test
    public void getTableDataTest(){
        PiCustomizeTableField piCustomizeTableField = new PiCustomizeTableField();
        piCustomizeTableField.setTableId("1").setType("1").setFieldLength(50);
        piCustomizeTableFieldService.saveField(piCustomizeTableField);
    }


    //--story=1049779 --user=贺强 01-14-PI算法模型-结果写入模型 https://www.tapd.cn/63554897/s/1327908
    @Test
    public void addPiCustomizeTableFieldTest(){
        PiCustomizeTableField piCustomizeTableField = new PiCustomizeTableField();
        piCustomizeTableField.setTableId("1").setType("1").setFieldLength(50).setFieldName("filed_001");
        piCustomizeTableFieldService.save(piCustomizeTableField);
    }


    //--story=1049887 --user=贺强 01-19-PI算法模型-计算函数前端效果实现及后端存储 https://www.tapd.cn/63554897/s/1327909
    @Test
    public void editPiCustomizeTableFieldTest(){
        PiCustomizeTableField piCustomizeTableField = new PiCustomizeTableField();
        piCustomizeTableField.setId("1").setTableId("1").setType("1").setFieldLength(50).setFieldName("filed_001");
        piCustomizeTableFieldService.updateById(piCustomizeTableField);
    }

}
