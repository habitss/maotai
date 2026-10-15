package com.keytech.modules.i18n.controller;

import com.keytech.base.controller.BaseController;
import com.keytech.modules.i18n.service.impl.I18nHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.i18n.entity.MdmI18nEntry;
import com.keytech.modules.i18n.service.IMdmI18nEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;


 /**
 * 国际化词条
 * @author yepeng
 * @since 2025.12.16
 * */
@Tag(name="国际化词条")
@RestController
@RequestMapping("/i18n/mdmI18nEntry")
@Slf4j
public class MdmI18nEntryController extends BaseController<IMdmI18nEntryService, MdmI18nEntry> {

	/**
	 * 添加
	 */
	@AutoLog(value = "国际化词条-添加")
	@Operation(summary="国际化词条-添加")
	@RequiresPermissions("i18n:mes3_mdm_i18n_entry:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MdmI18nEntry mdmI18nEntry) {
		baseService.save(mdmI18nEntry);
		return Result.OK(I18nHelper.get("common.addSuccess"));
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "国际化词条-编辑")
	@Operation(summary="国际化词条-编辑")
	@RequiresPermissions("i18n:mes3_mdm_i18n_entry:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MdmI18nEntry mdmI18nEntry) {
		baseService.updateById(mdmI18nEntry);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "国际化词条-通过id删除")
	@Operation(summary="国际化词条-通过id删除")
	@RequiresPermissions("i18n:mes3_mdm_i18n_entry:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("i18n:mes3_mdm_i18n_entry:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MdmI18nEntry mdmI18nEntry) {
        return super.exportXls(request, mdmI18nEntry, MdmI18nEntry.class, "国际化词条");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("i18n:mes3_mdm_i18n_entry:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MdmI18nEntry.class);
    }

}
