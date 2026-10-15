package com.keytech.modules.pi;


import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.service.IPiBasePointService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class PiBasePointTest {

    @Resource
    private IPiBasePointService piBasePointService;


    //--story=1049766 --user=贺强 01-03-PI算法模型-基础点位信息维护 https://www.tapd.cn/63554897/s/1327901
    @Test
    public void getAddPiBasePointTest(){
        PiBasePoint piBasePoint = new PiBasePoint();
        piBasePoint.setPointCode("bz001").setPointName("测试步骤").setPointType("1").setSort(1);
        piBasePointService.save(piBasePoint);
    }

}
