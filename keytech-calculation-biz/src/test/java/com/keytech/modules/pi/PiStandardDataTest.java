package com.keytech.modules.pi;

import com.keytech.modules.pi.dto.RerunDto;
import com.keytech.modules.pi.entity.PiStep;
import com.keytech.modules.pi.service.IPiStandardDataService;
import com.keytech.modules.pi.service.IPiStepService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class PiStandardDataTest {

    @Resource
    private IPiStandardDataService piStandardDataService;

    //--story=1049990 --user=敖磊 02-10-PI日报-红黄区重算配置 https://www.tapd.cn/63554897/s/1334003
    @Test
    public void startStandardCalculate(){
        piStandardDataService.startStandardCalculate();
    }

    //--story=1049961 --user=敖磊 02-08-PI日报-红黄区判断逻辑及重算逻辑 https://www.tapd.cn/63554897/s/1334005
    @Test
    public void reSunStandardCalculate(){
        RerunDto rerunDto = new RerunDto();
        piStandardDataService.reSunStandardCalculate(rerunDto);
    }

}
