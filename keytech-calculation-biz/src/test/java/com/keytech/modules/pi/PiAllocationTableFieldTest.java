package com.keytech.modules.pi;

import com.keytech.modules.pi.dto.TransferDataDto;
import com.keytech.modules.pi.entity.PiAllocationTable;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.service.IPiAllocationTableFieldService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class PiAllocationTableFieldTest {

    @Resource
    private IPiAllocationTableFieldService iPiAllocationTableFieldService;


    //--story=1049782 --user=贺强 02-02-PI日报-自定义日报-固定列、查询条件、列宽等配置 https://www.tapd.cn/63554897/s/1329120
    @Test
    public void getAllocationFieldTest(){
        PiAllocationTable piAllocationTable = new PiAllocationTable();
        piAllocationTable.setId("1");
        List<TransferDataDto> list = iPiAllocationTableFieldService.getAllocationField(piAllocationTable);
        for (TransferDataDto transferDataDto : list) {
            System.out.println(transferDataDto.toString());
        }
    }
}
