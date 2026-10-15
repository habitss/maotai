package com.keytech.modules.df.mdm.controller;

import com.keytech.modules.df.mdm.entity.DfTableFieldInfo;
import com.keytech.modules.df.mdm.service.IDfTableFieldInfoService;
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
import java.util.List;
import java.util.Map;

/**
 * @Description: DF数据库表字段信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
@Tag(name="DF数据库表字段信息")
@RestController
@RequestMapping("/df/mdm/dfTableFieldInfo")
@Slf4j
public class DfTableFieldInfoController extends BaseController<IDfTableFieldInfoService, DfTableFieldInfo> {
	@Autowired
	private IDfTableFieldInfoService dfTableFieldInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dfTableFieldInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="DF数据库表字段信息-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<DfTableFieldInfo>> queryPageList(DfTableFieldInfo dfTableFieldInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<DfTableFieldInfo> queryWrapper = QueryGenerator.initQueryWrapper(dfTableFieldInfo, req.getParameterMap());
		queryWrapper.orderBy(true,true, "df_field_name", "df_field_name");
		Page<DfTableFieldInfo> page = new Page<DfTableFieldInfo>(pageNo, pageSize);
		IPage<DfTableFieldInfo> pageList = dfTableFieldInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 /**
	  * 同步字段
	  * @param dfTableFieldInfo
	  * @return
	  */
	 @AutoLog(value = "DF数据库表字段信息-同步字段")
	 @Operation(summary="DF数据库表字段信息-同步字段")
	 @RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:edit")
	 @PostMapping(value = "/syncDfTableField")
	 public Result<String> syncDfTableField(@RequestBody DfTableFieldInfo dfTableFieldInfo) {
		 List<Map<String, Object>> dfFieldList = dfTableFieldInfoService.queryDfFieldList(dfTableFieldInfo.getDfTableName());
		 dfTableFieldInfoService.syncDfTableField(dfTableFieldInfo, dfFieldList);
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }
	
	/**
	 *   添加
	 *
	 * @param dfTableFieldInfo
	 * @return
	 */
	@AutoLog(value = "DF数据库表字段信息-添加")
	@Operation(summary="DF数据库表字段信息-添加")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfTableFieldInfo dfTableFieldInfo) {
		dfTableFieldInfoService.save(dfTableFieldInfo);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dfTableFieldInfo
	 * @return
	 */
	@AutoLog(value = "DF数据库表字段信息-编辑")
	@Operation(summary="DF数据库表字段信息-编辑")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfTableFieldInfo dfTableFieldInfo) {
		dfTableFieldInfoService.updateById(dfTableFieldInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DF数据库表字段信息-通过id删除")
	@Operation(summary="DF数据库表字段信息-通过id删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		dfTableFieldInfoService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DF数据库表字段信息-批量删除")
	@Operation(summary="DF数据库表字段信息-批量删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dfTableFieldInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="DF数据库表字段信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<DfTableFieldInfo> queryById(@RequestParam(name="id",required=true) String id) {
		DfTableFieldInfo dfTableFieldInfo = dfTableFieldInfoService.getById(id);
		if(dfTableFieldInfo==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(dfTableFieldInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param dfTableFieldInfo
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfTableFieldInfo dfTableFieldInfo) {
        return super.exportXls(request, dfTableFieldInfo, DfTableFieldInfo.class, "DF数据库表字段信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_table_field_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfTableFieldInfo.class);
    }

}
