package com.keytech.modules.cop.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cop.entity.CopConfig;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.service.ICopConfigService;
import com.keytech.modules.cop.service.ICopDeviceService;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.period.entity.PeriodVerificationConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import com.keytech.base.controller.BaseController;

import java.util.List;

/**
 * <p>
 * cop清洗项目配置 前端控制器
 * </p>
 *
 * @author lijun
 * @since 2025-02-19 02:03:56
 */
@Tag(name="COP清洗项目配置")
@Slf4j
@RestController
@RequestMapping("/cop/copConfig")
public class CopConfigController extends BaseController<ICopConfigService, CopConfig> {
    @Autowired
    private ICopConfigService iCopConfigService;

    @Autowired
    private ICopDeviceService iCopDeviceService;

    @Autowired
    private ICopPointService iCopPointService;

    @AutoLog(value = "COP清洗项目配置-分页列表查询")
    @Operation(summary = "COP清洗项目配置-分页列表查询", description = "COP清洗项目配置-分页列表查询")
    @GetMapping("/queryPageList")
    public Result<IPage<CopConfig>> queryPageList(CopConfig copConfig,
                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                             @RequestParam(name = "orderField", required = false) String orderField,
                                             @RequestParam(name = "orderType", required = false) String orderType,
                                             @RequestParam(name = "selectFields", required = false) String selectFields,
                                             HttpServletRequest req) {
        //模糊查询设置
        if (StrUtil.isNotBlank(copConfig.getFieldName())) {
            copConfig.setFieldName("*" + copConfig.getFieldName() + "*");
        }
        //获取设备id
        String[] device = req.getParameterMap().get("dataId");
        if(device!= null && device.length > 0){
            //获取设备信息
            CopDevice copDevice = iCopDeviceService.getById(device[0]);
            if(null != copDevice){
                //获取清洗点集合
                List<CopPoint> pointList = iCopPointService.list(new LambdaQueryWrapper<CopPoint>()
                        .eq(CopPoint::getCopDeviceId, copDevice.getId()));
                //清洗点非空获取第一条点位数据id用于查询配置
                if(!pointList.isEmpty()){
                    copConfig.setCopPointId(pointList.get(0).getId());
                } else {
                    //没有配置清洗点直接返回空
                    return null;
                }
            } else {
                copConfig.setCopPointId(device[0]);
            }
        }
        QueryWrapper<CopConfig> queryWrapper = QueryGenerator.initQueryWrapper(copConfig, req.getParameterMap());
        queryWrapper.orderByAsc("sort");
        Page<CopConfig> page = new Page<>(pageNo, pageSize);
        IPage<CopConfig> pageList = iCopConfigService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
    @AutoLog(value = "COP清洗项目配置-添加")
    @Operation(summary = "COP清洗项目配置-添加", description = "COP清洗项目配置-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CopConfig copConfig) {
        iCopConfigService.saveOrUpdateData(copConfig, true);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    @AutoLog(value = "COP清洗项目配置-编辑")
    @Operation(summary = "COP清洗项目配置-编辑", description = "COP清洗项目配置-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody CopConfig copConfig) {
        iCopConfigService.saveOrUpdateData(copConfig, false);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    @AutoLog(value = "COP清洗项目配置-通过id删除")
    @Operation(summary = "COP清洗项目配置-通过id删除", description = "COP清洗项目配置-通过id删除")
    @DeleteMapping("/deleteData")
    public Result<String> deleteData(@RequestBody CopConfig copConfig) {
        iCopConfigService.deleteData(copConfig);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }
}
