package com.keytech.modules.fivewhy.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/3/3 17:52
 */
@Tag(name="5Why分析Test")
@RestController
@RequestMapping("/fiveTest")
@Slf4j
public class FiveTestController extends BaseController<IFiveWhyAnalyseService, FiveWhyAnalyse> {

    @Autowired
    private IFiveWhyAnalyseService fiveWhyAnalyseService;

    @Autowired
    private IFiveWhyDetailService fiveWhyDetailService;

    @AutoLog(value = "5Why分析-test")
    @Operation(summary="5Why分析-test")
    @GetMapping(value = "/test")
    public Result<String> getTest(@RequestParam(name="id",required=true) String id) {
        List<FiveWhyAnalyse> list = fiveWhyAnalyseService.list();
        List<FiveWhyDetail> details = new ArrayList<>();
        for(FiveWhyAnalyse f : list){
            f.getMeasuresJson();
            List<FiveWhyDetail> dList = JSONUtil.toList(f.getMeasuresJson(), FiveWhyDetail.class);


            for(FiveWhyDetail d : dList){
                d.setRecordId(f.getId());
                if(StrUtil.isNotBlank(d.getFiveWhyId()) && d.getFiveWhyId().indexOf("temp_")>=0){
                    d.setFiveWhyId(IdUtil.getSnowflakeNextIdStr());
                }
                details.add(d);
            }
        }

        fiveWhyDetailService.saveBatch(details);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }
}
