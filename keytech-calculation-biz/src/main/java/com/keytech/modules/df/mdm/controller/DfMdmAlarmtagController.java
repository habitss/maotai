package com.keytech.modules.df.mdm.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.df.mdm.entity.DfMdmAlarmtag;
import com.keytech.modules.df.mdm.service.IDfMdmAlarmtagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

 /**
 * MDM-报警点位主数据表
 * @author yepeng
 * @since 2025.12.24
 * */
@Tag(name="MDM-报警点位主数据表")
@RestController
@RequestMapping("/df/mdm/dfMdmAlarmtag")
@Slf4j
public class DfMdmAlarmtagController extends BaseController<IDfMdmAlarmtagService, DfMdmAlarmtag> {

	/**
	 * 添加
	 */
	@AutoLog(value = "MDM-报警点位主数据表-添加")
	@Operation(summary="MDM-报警点位主数据表-添加")
	@RequiresPermissions("df.mdm:mes3_mdm_df_alarmtag:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfMdmAlarmtag dfMdmAlarmtag) {
		baseService.save(dfMdmAlarmtag);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "MDM-报警点位主数据表-编辑")
	@Operation(summary="MDM-报警点位主数据表-编辑")
	@RequiresPermissions("df.mdm:mes3_mdm_df_alarmtag:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfMdmAlarmtag dfMdmAlarmtag) {
		baseService.updateById(dfMdmAlarmtag);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "MDM-报警点位主数据表-通过id删除")
	@Operation(summary="MDM-报警点位主数据表-通过id删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_alarmtag:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("df.mdm:mes3_mdm_df_alarmtag:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfMdmAlarmtag dfMdmAlarmtag) {
        return super.exportXls(request, dfMdmAlarmtag, DfMdmAlarmtag.class, "MDM-报警点位主数据表");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("df.mdm:mes3_mdm_df_alarmtag:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfMdmAlarmtag.class);
    }

}
