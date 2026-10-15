package com.keytech.modules.df.mdm.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.df.mdm.entity.DfMdmRecipe;
import com.keytech.modules.df.mdm.service.IDfMdmRecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;


 /**
 * MDM-配方主数据表
 * @author yepeng
 * @since 2025.12.08
 * */
@Tag(name="MDM-配方主数据表")
@RestController
@RequestMapping("/df/mdm/dfMdmRecipe")
@Slf4j
public class DfMdmRecipeController extends BaseController<IDfMdmRecipeService, DfMdmRecipe> {

	/**
	 * 添加
	 */
	@AutoLog(value = "MDM-配方主数据表-添加")
	@Operation(summary="MDM-配方主数据表-添加")
	@RequiresPermissions("mes3_mdm_df_recipe:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfMdmRecipe dfMdmRecipe) {
		baseService.save(dfMdmRecipe);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "MDM-配方主数据表-编辑")
	@Operation(summary="MDM-配方主数据表-编辑")
	@RequiresPermissions("mes3_mdm_df_recipe:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfMdmRecipe dfMdmRecipe) {
		baseService.updateById(dfMdmRecipe);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "MDM-配方主数据表-通过id删除")
	@Operation(summary="MDM-配方主数据表-通过id删除")
	@RequiresPermissions("mes3_mdm_df_recipe:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("mes3_mdm_df_recipe:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfMdmRecipe dfMdmRecipe) {
        return super.exportXls(request, dfMdmRecipe, DfMdmRecipe.class, "MDM-配方主数据表");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("mes3_mdm_df_recipe:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfMdmRecipe.class);
    }

}
