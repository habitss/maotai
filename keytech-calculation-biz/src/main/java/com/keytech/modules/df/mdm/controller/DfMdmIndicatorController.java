package com.keytech.modules.df.mdm.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.df.mdm.entity.DfMdmDeptMapping;
import com.keytech.modules.df.mdm.entity.DfMdmIndicator;
import com.keytech.modules.df.mdm.job.SyncDfMdmDataJob;
import com.keytech.modules.df.mdm.service.IDfMdmDeptMappingService;
import com.keytech.modules.df.mdm.service.IDfMdmIndicatorService;
import com.keytech.modules.df.mdm.service.ISyncDfMdmDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MDM-指标主数据表
 * @author yepeng
 * @since 2025.12.08
 * */
@Tag(name="MDM-指标主数据表")
@RestController
@RequestMapping("/df/mdm/dfMdmIndicator")
@Slf4j
public class DfMdmIndicatorController extends BaseController<IDfMdmIndicatorService, DfMdmIndicator> {
	@Resource
	private IDfMdmDeptMappingService dfMdmDeptMappingService;

	 /**
	  * 添加
	  */
	 @AutoLog(value = "MDM-指标主数据表-字典查询")
	 @Operation(summary="MDM-指标主数据表-字典查询")
	 @GetMapping(value = "/queryIndicatorDic")
	 public Result<List<DfMdmIndicator>> queryIndicatorDic(DfMdmIndicator dfMdmIndicator) {
		 QueryWrapper<DfMdmIndicator> wrapper = new QueryWrapper<>();
		 wrapper.select("inst_use_code", "CONCAT(COALESCE(CONCAT_WS('/', fac_desc, depar_desc, process_desc, line_desc)), ' (', funloc_desc,')') AS funloc_desc");
		 wrapper.eq("enable_flag", "Y");
		 wrapper.eq("ind_stand_code", dfMdmIndicator.getIndStandCode());
		 wrapper.orderByAsc("fac_code", "depar_code", "process_code", "line_code", "funloc_code");
		 if(StrUtil.isNotBlank(dfMdmIndicator.getFactoryCode())){
			 //组织架构映射
			 List<DfMdmDeptMapping> deptMappingList = dfMdmDeptMappingService.lambdaQuery()
					 .in(DfMdmDeptMapping::getSysDeptCode, Arrays.asList(dfMdmIndicator.getFactoryCode().split( ",")))
					 .list();
			 List<String> mdmFullCodeList = deptMappingList.stream().map(DfMdmDeptMapping::getMdmFullCode).toList();
			 wrapper.in("fac_code", mdmFullCodeList);
		 }
		 return Result.OK(baseService.list(wrapper));
	 }

	/**
	 * 添加
	 */
	@AutoLog(value = "MDM-指标主数据表-添加")
	@Operation(summary="MDM-指标主数据表-添加")
	@RequiresPermissions("mes3_mdm_df_indicator:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfMdmIndicator dfMdmIndicator) {
		baseService.save(dfMdmIndicator);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "MDM-指标主数据表-编辑")
	@Operation(summary="MDM-指标主数据表-编辑")
	@RequiresPermissions("mes3_mdm_df_indicator:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfMdmIndicator dfMdmIndicator) {
		baseService.updateById(dfMdmIndicator);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "MDM-指标主数据表-通过id删除")
	@Operation(summary="MDM-指标主数据表-通过id删除")
	@RequiresPermissions("mes3_mdm_df_indicator:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("mes3_mdm_df_indicator:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfMdmIndicator dfMdmIndicator) {
        return super.exportXls(request, dfMdmIndicator, DfMdmIndicator.class, "MDM-指标主数据表");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("mes3_mdm_df_indicator:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfMdmIndicator.class);
    }
}
