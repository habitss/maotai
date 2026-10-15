package com.keytech.modules.i18n.controller;

import com.keytech.base.controller.BaseController;
import com.keytech.modules.i18n.dto.I18nEntryPageResult;
import com.keytech.modules.i18n.dto.I18nEntryRowDto;
import com.keytech.modules.i18n.service.I18nEntryPageService;
import com.keytech.modules.i18n.service.I18nRedisSyncService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.i18n.entity.MdmI18nEntryLang;
import com.keytech.modules.i18n.service.IMdmI18nEntryLangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

/**
 * 国际化词条翻译
 * @author yepeng
 * @since 2025.12.16
 * */
@Tag(name="国际化词条翻译")
@RestController
@RequestMapping("/i18n/mdmI18nEntryLang")
@Slf4j
@RequiredArgsConstructor
public class MdmI18nEntryLangController extends BaseController<IMdmI18nEntryLangService, MdmI18nEntryLang> {
	 private final I18nEntryPageService i18nEntryPageService;
	 private final I18nRedisSyncService i18nRedisSyncService;

	/**
	 * 词条维度分页查询（横向多语言）
	 * @param entryDesc 词条描述
	 * @param pageNo 页码
	 * @param pageSize 每页条数
	 * @return 分页结果
	 */
	 @GetMapping("/pageWithLang")
	 public Result<I18nEntryPageResult> getEntryWithLangPage(
			 @RequestParam(required = false) String entryDesc,
			 @RequestParam(defaultValue = "1") Integer pageNo,
			 @RequestParam(defaultValue = "20") Integer pageSize) {
		 return Result.OK(i18nEntryPageService.pageEntryWithLang(entryDesc, pageNo, pageSize));
	 }

	 /**
	  * 保存单行（一个词条的所有语言）
	  * @param row 单行数据
	  * @param request http request
	  * @return 保存结果
	  */
	 @PostMapping("/saveRow")
	 public Result<String> saveRow(@RequestBody I18nEntryRowDto row, HttpServletRequest request) {
		 String tenantId = request.getHeader("X-Tenant-Id");
		 if (StringUtils.isNotBlank(tenantId)) {
			 row.setTenetId(Integer.parseInt(tenantId));
		 }
		 i18nEntryPageService.saveRow(row);
		 return Result.OK();
	 }

	/**
	 * 词条维度分页查询（横向多语言）
	 * @return 分页结果
	 */
	@GetMapping("/version")
	public Result<Map<String, Long>> getLangVersion() {
		return Result.OK(i18nRedisSyncService.getLangVersions());
	}

	/**
	 * 导出ZIP语言包
	 * @param lang 语言代码
	 * @param response http response
	 */
	@GetMapping("/zip")
	public void downloadLangZip(@RequestParam String lang, HttpServletResponse response) throws IOException {
		byte[] zipBytes = i18nRedisSyncService.buildRedisLangZip(lang);
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=" + lang + ".zip");
		response.setContentLength(zipBytes.length);
		response.getOutputStream().write(zipBytes);
		response.flushBuffer();
	}

	/**
	 * 添加
	 */
	@AutoLog(value = "国际化词条翻译-添加")
	@Operation(summary="国际化词条翻译-添加")
	@RequiresPermissions("i18n:mes3_mdm_i18n_entry_lang:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MdmI18nEntryLang mdmI18nEntryLang) {
		baseService.save(mdmI18nEntryLang);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "国际化词条翻译-编辑")
	@Operation(summary="国际化词条翻译-编辑")
	@RequiresPermissions("i18n:mes3_mdm_i18n_entry_lang:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MdmI18nEntryLang mdmI18nEntryLang) {
		baseService.updateById(mdmI18nEntryLang);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "国际化词条翻译-通过id删除")
	@Operation(summary="国际化词条翻译-通过id删除")
	@RequiresPermissions("i18n:mes3_mdm_i18n_entry_lang:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("i18n:mes3_mdm_i18n_entry_lang:exportXls")
    @GetMapping(value = "/exportXls")
    public void exportXls(HttpServletResponse response, String entryDesc) throws IOException {
		this.i18nEntryPageService.exportExcel(response, entryDesc, "国际化词条翻译");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("i18n:mes3_mdm_i18n_entry_lang:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
		return this.i18nEntryPageService.importExcel(file);
    }

}
