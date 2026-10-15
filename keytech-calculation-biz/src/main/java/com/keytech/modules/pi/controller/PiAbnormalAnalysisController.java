package com.keytech.modules.pi.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.dto.PiAbnormalAnalysisDto;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisEvaluation;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPointRecord;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisEvaluationService;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointRecordService;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: PI异常分析
 * @Author: 敖磊
 * @Date:   2025-02-08
 * @Version: V1.0
 */
@Tag(name="PI异常分析")
@RestController
@RequestMapping("/pi/piAbnormalAnalysis")
@Slf4j
public class PiAbnormalAnalysisController extends BaseController<IPiAbnormalAnalysisService, PiAbnormalAnalysis> {
	@Resource
	private IPiAbnormalAnalysisService piAbnormalAnalysisService;
	@Resource
	private IPiAbnormalAnalysisPointRecordService pointRecordService;
	@Resource
	private IPiAbnormalAnalysisEvaluationService evaluationService;

	/**
	 * 自定义报表服务
	 */
	@Resource
	private IPiCustomizeTableFieldService piCustomizeTableFieldService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piAbnormalAnalysis
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="PI异常分析-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiAbnormalAnalysis>> queryPageList(PiAbnormalAnalysis piAbnormalAnalysis,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiAbnormalAnalysis> queryWrapper = QueryGenerator.initQueryWrapper(piAbnormalAnalysis, req.getParameterMap());
		Page<PiAbnormalAnalysis> page = new Page<PiAbnormalAnalysis>(pageNo, pageSize);
		IPage<PiAbnormalAnalysis> pageList = piAbnormalAnalysisService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piAbnormalAnalysisDto
	 * @return
	 */
	@AutoLog(value = "PI异常分析-添加")
	@Operation(summary="PI异常分析-添加")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAbnormalAnalysisDto piAbnormalAnalysisDto) {
		//设置id
		piAbnormalAnalysisDto.setId(IdUtil.getSnowflakeNextIdStr());
		piAbnormalAnalysisService.save(piAbnormalAnalysisDto);

		//保存评价信息
		if(CollUtil.isNotEmpty(piAbnormalAnalysisDto.getEvaluationList())){
			for(PiAbnormalAnalysisEvaluation item: piAbnormalAnalysisDto.getEvaluationList()){
				item.setRecordId(piAbnormalAnalysisDto.getId());
			}
			evaluationService.saveBatch(piAbnormalAnalysisDto.getEvaluationList());
		}
		//保存异常点位记录信息
		if(CollUtil.isNotEmpty(piAbnormalAnalysisDto.getPointRecordList())){
			for(PiAbnormalAnalysisPointRecord item: piAbnormalAnalysisDto.getPointRecordList()){
				item.setId(null);
				item.setRecordId(piAbnormalAnalysisDto.getId());
			}
			pointRecordService.saveBatch(piAbnormalAnalysisDto.getPointRecordList());
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piAbnormalAnalysis
	 * @return
	 */
	@AutoLog(value = "PI异常分析-编辑")
	@Operation(summary="PI异常分析-编辑")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAbnormalAnalysis piAbnormalAnalysis) {
		piAbnormalAnalysisService.updateById(piAbnormalAnalysis);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI异常分析-通过id删除")
	@Operation(summary="PI异常分析-通过id删除")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piAbnormalAnalysisService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI异常分析-批量删除")
	@Operation(summary="PI异常分析-批量删除")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piAbnormalAnalysisService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI异常分析-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiAbnormalAnalysis> queryById(@RequestParam(name="id",required=true) String id) {
		PiAbnormalAnalysis piAbnormalAnalysis = piAbnormalAnalysisService.getById(id);
		if(piAbnormalAnalysis==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piAbnormalAnalysis);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piAbnormalAnalysis
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiAbnormalAnalysis piAbnormalAnalysis) {
        return super.exportXls(request, piAbnormalAnalysis, PiAbnormalAnalysis.class, "PI异常分析");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiAbnormalAnalysis.class);
    }


	 /**
	  * 查询单个PI的异常分析记录
	  * @param piAbnormalAnalysis
	  * @return
	  */
	 @AutoLog(value = "查询单个PI的异常分析记录")
	 @Operation(summary="查询单个PI的异常分析记录")
	 @PostMapping(value = "/queryAbnormalAnalysis")
	 public Result<PiAbnormalAnalysisDto> queryAbnormalAnalysis(@RequestBody PiAbnormalAnalysis piAbnormalAnalysis)  {
		 PiAbnormalAnalysisDto dto = new PiAbnormalAnalysisDto();
		 //查询PI是否有异常分析记录
		 piAbnormalAnalysis = piAbnormalAnalysisService.queryAbnormalAnalysis(piAbnormalAnalysis);
		 BeanUtil.copyProperties(piAbnormalAnalysis, dto);
		 dto.setEvaluationList(evaluationService.list(new QueryWrapper<PiAbnormalAnalysisEvaluation>().eq("record_id",dto.getId())));
		 dto.setPointRecordList(pointRecordService.list(new QueryWrapper<PiAbnormalAnalysisPointRecord>().eq("record_id",dto.getId())));
		 return Result.OK(dto);
	 }

	/**
	 * 查询单个PI的过程分析记录
	 * @param tableCode 基础数据表code
	 * @param dataId 数据id
	 * @param fieldCode pi code
	 * @return
	 */
	 @AutoLog(value = "查询单个PI的过程分析记录")
	 @Operation(summary="查询单个PI的过程分析记录")
	 @GetMapping(value = "/queryProcessAnalysis")
	 public Result<Map<String,List<MeasurementData>>> queryProcessAnalysis(
			 @RequestParam(name="tableCode",required=true) String tableCode,
			 @RequestParam(name="dataId",required=true) String dataId,
			 @RequestParam(name="fieldCode",required=true) String fieldCode,
			 @RequestParam(name="dataPage",required=true) Integer dataPage,
			 @RequestParam(name="dataOffSet",required=true) Integer dataOffSet
	 ) {
		 //查询PI是否有异常分析记录
		 Map<String,List<MeasurementData>> listPointDataAll = piAbnormalAnalysisService.getBasePointDataList(tableCode,dataId,fieldCode,dataPage,dataOffSet);
		 return Result.OK(listPointDataAll);
	 }

	/**
	 * 查询PI数据生成时间
	 * @param tableCode 基础数据表code
	 * @param dataId 数据id
	 * @param fieldCode pi code
	 * @return
	 */
	@AutoLog(value = "查询PI数据生成时间")
	@Operation(summary="查询PI数据生成时间")
	@GetMapping(value = "/queryTriggerTime")
	public Result<Map> queryTriggerTime(
			@RequestParam(name="tableCode",required=true) String tableCode,
			@RequestParam(name="dataId",required=true) String dataId,
			@RequestParam(name="fieldCode",required=true) String fieldCode
	) {
		//查询PI是否有异常分析记录
		Map triggerMap = piAbnormalAnalysisService.getTriggerTimeForPi(dataId,tableCode,fieldCode);
		return Result.OK(triggerMap);
	}

	/**
	 * 查询PI标准
	 * @param tableCode 基础数据表code
	 * @param dataId 数据id
	 * @param fieldCode pi code
	 * @param dataValue pi数据实际值
	 * @return
	 */
	@AutoLog(value = "查询PI标准")
	@Operation(summary="查询PI标准")
	@GetMapping(value = "/queryPiStandardData")
	public Result<Map> queryPiStandardData(
			@RequestParam(name="tableCode",required=true) String tableCode,
			@RequestParam(name="dataId",required=true) String dataId,
			@RequestParam(name="fieldCode",required=true) String fieldCode,
			@RequestParam(name="dataValue",required=true) String dataValue
	) {
		//查询PI是否有异常分析记录
		Map standardDataMap = piAbnormalAnalysisService.getPiStandardData(tableCode,dataId,fieldCode,dataValue);
		return Result.OK(standardDataMap);
	}

	/**
	 * PI过程记录-查询字段信息（用于查看计算模型）
	 * @param tableCode
	 * @param fieldCode
	 * @return
	 */
	@AutoLog(value = "PI过程记录-查询字段信息（用于查看计算模型）")
	@Operation(summary="PI过程记录-查询字段信息（用于查看计算模型）")
	@GetMapping(value = "/getCustomizeTableField")
	public Result<PiCustomizeTableField> getCustomizeTableField(
			@RequestParam(name="tableCode",required=true) String tableCode,
			@RequestParam(name="fieldCode",required=true) String fieldCode
	){
		PiCustomizeTableField customizeTableField = new PiCustomizeTableField();
		List<PiCustomizeTableField> listData = piCustomizeTableFieldService.lambdaQuery()
				.eq(PiCustomizeTableField::getTableName,tableCode)
				.eq(PiCustomizeTableField::getFieldName,fieldCode)
				.list();
		if(null != listData && listData.size() > 0){
			customizeTableField = listData.get(0);
		}
		return Result.OK(customizeTableField);
	}


}
