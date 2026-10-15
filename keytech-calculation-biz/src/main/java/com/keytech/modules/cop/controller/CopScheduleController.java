package com.keytech.modules.cop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.cop.dto.CopPlanRank;
import com.keytech.modules.cop.dto.CopSchedulerVo;
import com.keytech.modules.cop.dto.CopStatisticsDto;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.dto.PeriodStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import com.keytech.base.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * cop计划排期 前端控制器
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
@Tag(name="COP计划排期")
@Slf4j
@RestController
@RequestMapping("/cop/copSchedule")
public class CopScheduleController extends BaseController<ICopScheduleService, CopSchedule> {
    @Autowired
    private ICopScheduleService iCopScheduleService;

    @Autowired
    private ICopPointService iCopPointService;

    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

    @AutoLog(value = "COP计划排期-分页列表查询")
    @Operation(summary = "COP计划排期-分页列表查询", description = "COP计划排期-分页列表查询")
    @GetMapping("/queryPageList")
    public Result<IPage<CopSchedule>> queryPageList(CopSchedule copScheduleRequestVO,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, @RequestParam(name = "orderField", required = false) String orderField,
                                                    @RequestParam(name = "orderType", required = false) String orderType,
                                                    @RequestParam(name = "selectFields", required = false) String selectFields,
                                                    HttpServletRequest req){
        List<String> monthList;
        if(null != copScheduleRequestVO && StringUtils.isNotEmpty(copScheduleRequestVO.getMonths())) {
            monthList = new ArrayList<>();
            if(copScheduleRequestVO.getMonths().contains(",")) {
                //测试发现服务器上使用Arrays.asList会出现数组越界错误，本地无法复现，暂时使用String[]进行分割
                String[] str = copScheduleRequestVO.getMonths().split(",");
                for(String s : str) {
                    monthList.add(s);
                }
                //monthList = Arrays.asList(ArrayUtils.toStringArray());
            } else {
                monthList.add(copScheduleRequestVO.getMonths());
            }
        } else {
            monthList = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12");
        }
        return Result.OK(iCopScheduleService.initCopScheduleList(copScheduleRequestVO, monthList,pageNo,pageSize));
    }


    @AutoLog(value = "COP计划排期-排期")
    @Operation(summary = "COP计划排期-排期", description = "COP计划排期-排期")
    @PostMapping(value = "/saveOrUpdate")
    public Result<CopSchedule> saveOrUpdate(@RequestBody CopSchedulerVo copSchedule) {
        CopSchedule schedule;
        try {
            schedule =iCopScheduleService.saveOrUpdateCopScheduler(copSchedule);
        }catch (DuplicateKeyException e){
            return Result.error("新增失败，请检查该设备清洗点是否已有此步骤");
        }
        return Result.OK(schedule);
    }

    @AutoLog(value = "COP计划排期-取消排期")
    @Operation(summary = "COP计划排期-取消排期", description = "COP计划排期-取消排期")
    @PostMapping(value = "/cancel")
    public Result<CopSchedule> cancel(@RequestBody CopSchedulerVo copSchedule) {
        CopSchedule schedule;
        try {
            schedule =iCopScheduleService.cancelCopScheduler(copSchedule);
        }catch (DuplicateKeyException e){
            return Result.error("新增失败，请检查该设备清洗点是否已有此步骤");
        }
        return Result.OK(schedule);
    }

    @AutoLog(value = "COP计划排期-排期配置更新")
    @Operation(summary = "COP计划排期-排期配置更新", description = "COP计划排期-排期配置更新")
    @PostMapping(value = "/updateCop")
    public Result<String> updateCop(@RequestBody List<CopSchedulerVo> copSchedule) {
        try {
            iCopScheduleService.updateCop(copSchedule);
        }catch (Exception e){
            return Result.error("保存失败，请检查该设备清洗点是否已有此步骤");
        }
        return Result.OK("保存成功");
    }

    /**
     *  修改排期状态
     *
     * @param scheduleSubmit
     * @return
     */
    @AutoLog(value = "COP计划排期-修改排期状态")
    @Operation(summary="COP计划排期-修改排期状态")
    @RequestMapping(value = "/process", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> processEdit(@RequestBody CopSchedulerVo scheduleSubmit) {
        iCopScheduleService.processCopSchedule(scheduleSubmit.getId(),scheduleSubmit.getNum(),String.valueOf(scheduleSubmit.getResult()));
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "COP计划排期-通过id删除")
    @Operation(summary = "COP计划排期-通过id删除", description = "COP计划排期-通过id删除")
    @DeleteMapping("/deleteData")
    public Result<Void> deleteData(@RequestParam String id) {
        CopSchedule copSchedule = iCopScheduleService.getById(id);
        if(null != copSchedule) {
            iCopPointService.deleteData(copSchedule.getCopPointId());
            iCopScheduleService.removeById(id);
        } else {
            iCopPointService.removeById(id);
        }

        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    /**
     * 导出excel
     *
     * @param copStatisticsDto
     */
    //@RequiresPermissions("cop:mes3_cop_schedule:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(CopStatisticsDto copStatisticsDto) throws Exception {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<CopStatisticsDto> resultList = iCopScheduleService.copStatistics(copStatisticsDto);
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject("fileName", "cop统计表");
        mv.addObject("entity", CopStatisticsDto.class);
        ExportParams exportParams = new ExportParams("cop统计" + "报表", "导出人:" + sysUser.getRealname(), "cop排期统计表");
        exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
        mv.addObject("params", exportParams);
        mv.addObject("data", resultList);
        return mv;
    }


    @AutoLog(value = "COP计划排期-统计表")
    @Operation(summary = "COP计划排期-统计表", description = "COP计划排期-统计表")
    @PostMapping("/copStatistics")
    public Result<List<CopStatisticsDto>> copStatistics(@RequestBody CopStatisticsDto copStatisticsDto){
        List<CopStatisticsDto> list;
        try {
            list = iCopScheduleService.copStatistics(copStatisticsDto);
        }catch (Exception e){
            log.error("统计表报错",e);
            return Result.error("查询出错");
        }
        return Result.OK(list);
    }

    @AutoLog(value = "COP计划排期-完成率排名")
    @Operation(summary = "COP计划排期-完成率排名", description = "COP计划排期-完成率排名")
    @PostMapping("/copPlanRank")
    public Result<List<CopPlanRank>> copPlanRank(@RequestBody WSActionStatisticsDto copStatisticsDto){
        List<CopPlanRank> list;
        try {
            list = iCopScheduleService.copPlanRank(copStatisticsDto);
        }catch (Exception e){
            log.error("统计表报错",e);
            return Result.error("查询出错");
        }
        return Result.OK(list);
    }

    @AutoLog(value = "COP计划排期-车间首页统计表")
    @Operation(summary = "COP计划排期-车间首页统计表", description = "COP计划排期-车间首页统计表")
    @PostMapping("/copLineStatistics")
    public Result<List<CopPlanRank>> copLineStatistics(@RequestBody WSActionStatisticsDto copStatisticsDto){
        List<CopPlanRank> list;
        try {
            list = iCopScheduleService.copLineStatistics(copStatisticsDto);
        }catch (Exception e){
            log.error("统计表报错",e);
            return Result.error("查询出错");
        }
        return Result.OK(list);
    }
}
