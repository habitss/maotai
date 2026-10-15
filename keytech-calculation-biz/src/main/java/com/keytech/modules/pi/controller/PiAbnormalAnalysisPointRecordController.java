package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiAbnormalAnalysisPointRecord;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointRecordService;
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

import java.util.Arrays;

 /**
 * @Description: PI异常分析-异常点位记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-06
 * @Version: V1.0
 */
@Tag(name="PI异常分析-异常点位记录表")
@RestController
@RequestMapping("/pi/piAbnormalAnalysisPointRecord")
@Slf4j
public class PiAbnormalAnalysisPointRecordController extends BaseController<IPiAbnormalAnalysisPointRecordService, PiAbnormalAnalysisPointRecord> {
	@Autowired
	private IPiAbnormalAnalysisPointRecordService piAbnormalAnalysisPointRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piAbnormalAnalysisPointRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI异常分析-异常点位记录表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiAbnormalAnalysisPointRecord>> queryPageList(PiAbnormalAnalysisPointRecord piAbnormalAnalysisPointRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiAbnormalAnalysisPointRecord> queryWrapper = QueryGenerator.initQueryWrapper(piAbnormalAnalysisPointRecord, req.getParameterMap());
		Page<PiAbnormalAnalysisPointRecord> page = new Page<PiAbnormalAnalysisPointRecord>(pageNo, pageSize);
		IPage<PiAbnormalAnalysisPointRecord> pageList = piAbnormalAnalysisPointRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piAbnormalAnalysisPointRecord
	 * @return
	 */
	@AutoLog(value = "PI异常分析-异常点位记录表-添加")
	@Operation(summary="PI异常分析-异常点位记录表-添加")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_point_record:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAbnormalAnalysisPointRecord piAbnormalAnalysisPointRecord) {
		piAbnormalAnalysisPointRecordService.save(piAbnormalAnalysisPointRecord);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piAbnormalAnalysisPointRecord
	 * @return
	 */
	@AutoLog(value = "PI异常分析-异常点位记录表-编辑")
	@Operation(summary="PI异常分析-异常点位记录表-编辑")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_point_record:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAbnormalAnalysisPointRecord piAbnormalAnalysisPointRecord) {
		piAbnormalAnalysisPointRecordService.updateById(piAbnormalAnalysisPointRecord);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI异常分析-异常点位记录表-通过id删除")
	@Operation(summary="PI异常分析-异常点位记录表-通过id删除")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_point_record:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piAbnormalAnalysisPointRecordService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI异常分析-异常点位记录表-批量删除")
	@Operation(summary="PI异常分析-异常点位记录表-批量删除")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_point_record:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piAbnormalAnalysisPointRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI异常分析-异常点位记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiAbnormalAnalysisPointRecord> queryById(@RequestParam(name="id",required=true) String id) {
		PiAbnormalAnalysisPointRecord piAbnormalAnalysisPointRecord = piAbnormalAnalysisPointRecordService.getById(id);
		if(piAbnormalAnalysisPointRecord==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piAbnormalAnalysisPointRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piAbnormalAnalysisPointRecord
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point_record:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiAbnormalAnalysisPointRecord piAbnormalAnalysisPointRecord) {
        return super.exportXls(request, piAbnormalAnalysisPointRecord, PiAbnormalAnalysisPointRecord.class, "PI异常分析-异常点位记录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point_record:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiAbnormalAnalysisPointRecord.class);
    }

}
