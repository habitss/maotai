package com.keytech.modules.cop.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cop.entity.CopConfig;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.service.ICopConfigService;
import com.keytech.modules.cop.service.ICopDeviceService;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.exception.entity.ExceptionReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import com.keytech.base.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * cop清洗点 前端控制器
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 04:42:21
 */
@Tag(name="COP清洗点")
@Slf4j
@RestController
@RequestMapping("/cop/copPoint")
public class CopPointController extends BaseController<ICopPointService, CopPoint> {
    @Autowired
    private ICopPointService iCopPointService;

    @Autowired
    private ICopConfigService iCopConfigService;

    @Autowired
    private ICopDeviceService iCopDeviceService;

    @Autowired
    private ICopScheduleService iCopScheduleService;

    @AutoLog(value = "COP清洗点-分页列表查询")
    @Operation(summary = "COP清洗点-分页列表查询", description = "COP清洗点-分页列表查询")
    @GetMapping("/queryPageList")
    public Result<IPage<CopPoint>> queryPageList(CopPoint copPoint,
                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                             @RequestParam(name = "orderField", required = false) String orderField,
                                             @RequestParam(name = "orderType", required = false) String orderType,
                                             @RequestParam(name = "selectFields", required = false) String selectFields,
                                             HttpServletRequest req) {

        if (StrUtil.isNotBlank(copPoint.getPointName())) {
            copPoint.setPointName("*" + copPoint.getPointName() + "*");
        }
        String[] device = req.getParameterMap().get("dataId");
        if(device!= null && device.length > 0){
            copPoint.setCopDeviceId(device[0]);
        }
        QueryWrapper<CopPoint> queryWrapper = QueryGenerator.initQueryWrapper(copPoint, req.getParameterMap());
        queryWrapper.orderByAsc("sort");
        Page<CopPoint> page = new Page<>(pageNo, pageSize);
        IPage<CopPoint> pageList = iCopPointService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @Operation(summary="COP清洗点-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<CopPoint> queryById(@RequestParam(name="id") String id) {
        CopPoint copPoint = iCopPointService.getById(id);
        if(copPoint==null) {
            return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
        }
        return Result.OK(copPoint);
    }

    @AutoLog(value = "COP清洗点-添加")
    @Operation(summary = "COP清洗点-添加", description = "COP清洗点-添加")
    @PostMapping(value = "/add")
    public Result<CopPoint> add(@RequestBody CopPoint copPoint) {
        return Result.OK(iCopPointService.savePoint(copPoint));
    }

    @AutoLog(value = "COP清洗点-编辑")
    @Operation(summary = "COP清洗点-编辑", description = "COP清洗点-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<CopPoint> edit(@RequestBody CopPoint copPoint) {
        try {
            iCopPointService.updateById(copPoint);
            //更新点位配置的同时，更新排期中的频率
            List<CopSchedule> scheduleList = iCopScheduleService.list(new LambdaQueryWrapper<CopSchedule>().eq(CopSchedule::getCopPointId, copPoint.getId()));
            if(CollectionUtils.isNotEmpty(scheduleList)) {
                for (CopSchedule schedule : scheduleList) {
                    schedule.setFrequency(copPoint.getFrequency());
                    iCopScheduleService.updateById(schedule);
                }
            }
        }catch (DuplicateKeyException e){
            return Result.error("编辑失败，请检查该设备清洗点是否已有此步骤");
        }
        return Result.OK(copPoint);
    }

    @AutoLog(value = "COP清洗点-通过id删除")
    @Operation(summary = "COP清洗点-通过id删除", description = "COP清洗点-通过id删除")
    @DeleteMapping("/deleteData")
    public Result<Void> deleteData(@RequestParam String id) {
        iCopPointService.deleteData(id);

        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "COP点位管理-批量删除")
    @Operation(summary="COP点位管理-批量删除")
    @DeleteMapping(value = "/deletionBatch")
    public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
        List<String> configIds = iCopConfigService.list(new LambdaQueryWrapper<CopConfig>().in(CopConfig::getCopPointId, Arrays.asList(ids.split(",")))).stream().map(CopConfig::getId).toList();
        if(CollectionUtils.isNotEmpty(configIds)) {
            iCopConfigService.removeBatchByIds(configIds);
        }
        this.iCopPointService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 导出excel
     *
     * @param request
     * @param copPoint
     */
//    @RequiresPermissions("meeting:mes3_meeting_template:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CopPoint copPoint) {
        return super.exportXls(request, copPoint, CopPoint.class, "COP清洗点配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
//    @RequiresPermissions("meeting:mes3_meeting_template:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response)  throws IOException {
        return iCopPointService.importExcelBy(request, response, CopPoint.class);
    }
}
