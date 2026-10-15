package com.keytech.modules.pi;

import com.keytech.modules.pi.entity.PiStep;
import com.keytech.modules.pi.service.IPiStepService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class PiStepTest {

    @Resource
    private IPiStepService piStepService;

    //--story=1049748 --user=贺强 01-02-PI算法模型-代码汉化模块管理（步骤管理、代码管理） https://www.tapd.cn/63554897/s/1327902
    @Test
    public void editPiStepTest(){
        PiStep piStep = new PiStep();
        piStep.setId("1");
        piStep.setStepName("测试").setStepNum("测试").setDeviceId(13).setSort(1.0);
        piStepService.updateById(piStep);
    }

}
