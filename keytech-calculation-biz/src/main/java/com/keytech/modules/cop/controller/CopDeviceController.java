package com.keytech.modules.cop.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.service.ICopDeviceService;
import com.keytech.modules.cop.service.ICopPointService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * cop设备管理 前端控制器
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 02:08:08
 */
@Tag(name="COP设备管理")
@Slf4j
@RestController
@RequestMapping("/cop/copDevice")
public class CopDeviceController extends BaseController<ICopDeviceService, CopDevice> {
    @Autowired
    private ICopDeviceService iCopDeviceService;

    @Autowired
    private ICopPointService iCopPointService;

    @AutoLog(value = "COP设备管理-分页列表查询")
    @Operation(summary = "COP设备管理-分页列表查询", description = "COP设备管理-分页列表查询")
    @GetMapping("/queryPageList")
    public Result<IPage<CopDevice>> queryPageList(CopDevice copDevice,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  @RequestParam(name = "orderField", required = false) String orderField,
                                                  @RequestParam(name = "orderType", required = false) String orderType,
                                                  @RequestParam(name = "selectFields", required = false) String selectFields,
                                                  HttpServletRequest req) {

        if (StrUtil.isNotBlank(copDevice.getCopDeviceName())) {
            copDevice.setCopDeviceName("*" + copDevice.getCopDeviceName() + "*");
        }
        QueryWrapper<CopDevice> queryWrapper = QueryGenerator.initQueryWrapper(copDevice, req.getParameterMap());
        queryWrapper.orderByAsc("sort");
        queryWrapper.orderByDesc("id");
        Page<CopDevice> page = new Page<>(pageNo, pageSize);
        IPage<CopDevice> pageList = iCopDeviceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
    @AutoLog(value = "COP设备管理-添加")
    @Operation(summary = "COP设备管理-添加", description = "COP设备管理-添加")
    @PostMapping(value = "/add")
    public Result<CopDevice> add(@RequestBody CopDevice copDevice) {
        try {
            long count =iCopDeviceService.count(new LambdaQueryWrapper<CopDevice>().eq(CopDevice::getDeptCode, copDevice.getDeptCode()).eq(CopDevice::getLineCode, copDevice.getLineCode()).eq(CopDevice::getCopDeviceName, copDevice.getCopDeviceName()));
            if(count < 1) {
                iCopDeviceService.save(copDevice);
            } else {
                return Result.error("新增失败，请检查该产线，工段下是否已经存在 "+copDevice.getCopDeviceName());
            }
        }catch (DuplicateKeyException e){
            return Result.error("新增失败，请检查该产线，工段下是否已经存在 "+copDevice.getCopDeviceName());
        }
        return Result.OK(copDevice);
    }

    @AutoLog(value = "COP设备管理-编辑")
    @Operation(summary = "COP设备管理-编辑", description = "COP设备管理-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<CopDevice> edit(@RequestBody CopDevice copDevice) {
        try {
            iCopDeviceService.updateById(copDevice);
            if(copDevice.getActive() == 0){
                List<CopPoint> list = iCopPointService.list(new LambdaQueryWrapper<CopPoint>().eq(CopPoint::getCopDeviceId, copDevice.getId()));
                if(CollectionUtils.isNotEmpty(list)){
                    list.forEach(point -> {
                        point.setActive(0);
                        point.setCopDeviceName(copDevice.getCopDeviceName());
                    });
                    iCopPointService.updateBatchById(list);
                }
            } else {
                List<CopPoint> list = iCopPointService.list(new LambdaQueryWrapper<CopPoint>().eq(CopPoint::getCopDeviceId, copDevice.getId()));
                if(CollectionUtils.isNotEmpty(list)){
                    list.forEach(point -> {
                        point.setActive(1);
                        point.setCopDeviceName(copDevice.getCopDeviceName());
                    });
                    iCopPointService.updateBatchById(list);
                }
            }
        }catch (DuplicateKeyException e){
            return Result.error("编辑失败，请检查该产线，工段下是否已经存在 "+copDevice.getCopDeviceName());
        }
        return Result.OK(copDevice);
    }

    @AutoLog(value = "COP设备管理-通过id删除")
    @Operation(summary = "COP设备管理-通过id删除", description = "COP设备管理-通过id删除")
    @DeleteMapping("/deleteData")
    public Result<String> deleteData(@RequestParam String id)  {
        iCopDeviceService.deleteData(id);

        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "COP设备管理-批量删除")
    @Operation(summary="COP设备管理-批量删除")
    @DeleteMapping(value = "/deletionBatch")
    public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
        this.iCopDeviceService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 导出excel
     *
     * @param request
     * @param copDevice
     */
//    @RequiresPermissions("meeting:mes3_meeting_template:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CopDevice copDevice) {
        return super.exportXls(request, copDevice, CopDevice.class, "cop设备配置");
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
        return iCopDeviceService.importExcelBy(request, response, CopDevice.class);
    }
}
