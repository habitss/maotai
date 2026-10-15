package com.keytech.modules.period.controller;

import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.modules.period.service.IPeriodVerificationConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.io.IOException;
import java.util.Arrays;

 /**
 * @Description: 期间核查填报项配置
 * @Author: qipengbo
 * @Date:   2025-03-10
 * @Version: V1.0
 */
@Tag(name="期间核查填报项配置")
@RestController
@RequestMapping("/period/periodVerificationConfig")
@Slf4j
public class PeriodVerificationConfigController extends BaseController<IPeriodVerificationConfigService, PeriodVerificationConfig> {
	@Autowired
	private IPeriodVerificationConfigService periodVerificationConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param periodVerificationConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="期间核查填报项配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PeriodVerificationConfig>> queryPageList(PeriodVerificationConfig periodVerificationConfig,
																 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
																 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
																 @RequestParam(name = "orderField",required = false) String orderField,
																 @RequestParam(name = "orderType",required = false) String orderType,
																 @RequestParam(name = "selectFields",required = false) String selectFields,
																 HttpServletRequest req) {
		String[] device = req.getParameterMap().get("dataId");
		if(device!= null && device.length > 0){
			periodVerificationConfig.setApparatusId(device[0]);
		}
        QueryWrapper<PeriodVerificationConfig> queryWrapper = QueryGenerator.initQueryWrapper(periodVerificationConfig, req.getParameterMap());
		queryWrapper.orderByAsc("sort");
		Page<PeriodVerificationConfig> page = new Page<>(pageNo, pageSize);
		IPage<PeriodVerificationConfig> pageList = periodVerificationConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param periodVerificationConfig
	 * @return
	 */
	@AutoLog(value = "期间核查填报项配置-添加")
	@Operation(summary="期间核查填报项配置-添加")
//	@RequiresPermissions("period:period_verification_cofig:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PeriodVerificationConfig periodVerificationConfig) {
		periodVerificationConfigService.save(periodVerificationConfig);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param periodVerificationConfig
	 * @return
	 */
	@AutoLog(value = "期间核查填报项配置-编辑")
	@Operation(summary="期间核查填报项配置-编辑")
//	@RequiresPermissions("period:period_verification_cofig:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PeriodVerificationConfig periodVerificationConfig) {
		periodVerificationConfigService.updateById(periodVerificationConfig);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "期间核查填报项配置-通过id删除")
	@Operation(summary="期间核查填报项配置-通过id删除")
//	@RequiresPermissions("period:period_verification_cofig:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		periodVerificationConfigService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "期间核查填报项配置-批量删除")
	@Operation(summary="期间核查填报项配置-批量删除")
//	@RequiresPermissions("period:period_verification_cofig:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.periodVerificationConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="期间核查填报项配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PeriodVerificationConfig> queryById(@RequestParam(name="id",required=true) String id) {
		PeriodVerificationConfig periodVerificationConfig = periodVerificationConfigService.getById(id);
		if(periodVerificationConfig ==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(periodVerificationConfig);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param periodVerificationConfig
    */
//    @RequiresPermissions("period:period_verification_cofig:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PeriodVerificationConfig periodVerificationConfig) {
        return super.exportXls(request, periodVerificationConfig, PeriodVerificationConfig.class, "期间核查填报项配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("period:period_verification_cofig:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            return periodVerificationConfigService.importExcelBy(request, response, PeriodVerificationConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
