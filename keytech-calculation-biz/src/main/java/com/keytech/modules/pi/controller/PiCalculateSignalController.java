package com.keytech.modules.pi.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.entity.MdmShiftType;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.entity.PiCalculateSignal;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.service.IPiBasePointService;
import com.keytech.modules.pi.service.IPiCalculateSignalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tag(name="计算信号")
@RestController
@RequestMapping("/pi/piCalculateSignal")
@Slf4j
public class PiCalculateSignalController extends BaseController<IPiCalculateSignalService, PiCalculateSignal> {

    @Autowired
    private IPiCalculateSignalService iPiCalculateSignalService;

    /**
     * 分页列表查询
     *
     * @param piCalculateSignal
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "计算信号-分页列表查询")
    @Operation(summary = "计算信号-分页列表查询", description = "计算信号-分页列表查询")
    @GetMapping(value = "/listData")
    public Result<IPage<PiCalculateSignal>> queryPageList(PiCalculateSignal piCalculateSignal,
                                                              @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
                                                              @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
                                                              @RequestParam(name = "orderField",required = false) String orderField,
                                                              @RequestParam(name = "orderType",required = false) String orderType,
                                                              @RequestParam(name = "selectFields",required = false) String selectFields,
                                                              HttpServletRequest req) {
        QueryWrapper<PiCalculateSignal> queryWrapper = QueryGenerator.initQueryWrapper(piCalculateSignal, req.getParameterMap());
        queryWrapper.orderByDesc("is_enabled").orderByAsc("create_time").orderByAsc("id");
        Page<PiCalculateSignal> page = new Page<PiCalculateSignal>(pageNo, pageSize);
        IPage<PiCalculateSignal> pageList = iPiCalculateSignalService.page(page, queryWrapper);
        //添加额外信息(取数信号/计算模型)
        pageList = iPiCalculateSignalService.addInformation(pageList);
        return Result.OK(pageList);
    }


    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "基础点位维护-通过id删除")
    @Operation(summary = "基础点位维护-通过id删除", description = "基础点位维护-通过id删除")
    @DeleteMapping(value = "/deleteData")
    public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
        //iPiCalculateSignalService.removeById(id);
        List<String> asList = new ArrayList<>();
        asList.add(id);
        iPiCalculateSignalService.deleteDataBatch(asList);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "基础点位维护-批量删除")
    @Operation(summary = "基础点位维护-批量删除", description = "基础点位维护-批量删除")
    @DeleteMapping(value = "/deleteDataBatch")
    public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
        //iPiCalculateSignalService.removeByIds(Arrays.asList(ids.split(",")));
        //批量删除,同时删除管理表
        iPiCalculateSignalService.deleteDataBatch(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }



    /**
     * 获取算法数据
     */
    @Operation(summary = "算法查询", description = "算法查询")
    @GetMapping(value = "/getAlgorithmData")
    public Result<AlgorithmDataDto> getAlgorithmData(PiCalculateSignal piCalculateSignal) {
        AlgorithmDataDto algorithmDataDto = iPiCalculateSignalService.getAlgorithmData(piCalculateSignal);
        return Result.OK(algorithmDataDto);
    }

    /**
     * 算法启用/禁用 updateEnable
     */
    @Operation(summary="算法启用/禁用 updateEnable")
    @GetMapping(value = "/updateEnable")
    public Result<?> updateEnable(@RequestParam(name="ids",required=true) String ids, @RequestParam(name="isEnabled",required=true) Integer isEnabled) {
        List<String> list = Arrays.asList(ids.split(","));
        //通过id集合获取数据
        List<PiCalculateSignal> listField = baseService.listByIds(list);
        for (PiCalculateSignal piCalculateSignal : listField) {
            piCalculateSignal.setIsEnabled(isEnabled);
        }
        baseService.updateBatchById(listField);
        return Result.OK("批量操作成功");
    }

}
