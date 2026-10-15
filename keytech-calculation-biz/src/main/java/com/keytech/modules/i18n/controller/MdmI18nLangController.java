package com.keytech.modules.i18n.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.i18n.entity.MdmI18nLang;
import com.keytech.modules.i18n.service.IMdmI18nLangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;


 /**
 * 国际化语言定义
 * @author yepeng
 * @since 2025.12.16
 * */
@Tag(name="国际化语言定义")
@RestController
@RequestMapping("/i18n/mdmI18nLang")
@Slf4j
public class MdmI18nLangController extends BaseController<IMdmI18nLangService, MdmI18nLang> {

	/**
	 * 添加
	 */
	@AutoLog(value = "国际化语言定义-添加")
	@Operation(summary="国际化语言定义-添加")
	@RequiresPermissions("i18n:mes3_mdm_i18n_lang:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MdmI18nLang mdmI18nLang) {
		baseService.save(mdmI18nLang);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "国际化语言定义-编辑")
	@Operation(summary="国际化语言定义-编辑")
	@RequiresPermissions("i18n:mes3_mdm_i18n_lang:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MdmI18nLang mdmI18nLang) {
		baseService.updateById(mdmI18nLang);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "国际化语言定义-通过id删除")
	@Operation(summary="国际化语言定义-通过id删除")
	@RequiresPermissions("i18n:mes3_mdm_i18n_lang:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("i18n:mes3_mdm_i18n_lang:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MdmI18nLang mdmI18nLang) {
        return super.exportXls(request, mdmI18nLang, MdmI18nLang.class, "国际化语言定义");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("i18n:mes3_mdm_i18n_lang:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MdmI18nLang.class);
    }

}
