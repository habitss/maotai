package com.keytech.modules.pi.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.keytech.modules.pi.entity.PiDfTableFieldMapping;
import com.keytech.modules.pi.service.IPiDfTableFieldMappingService;
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

/**
 * @Description: PI字段与DF字段映射
 * @Author: jeecg-boot
 * @Date:   2026-01-15
 * @Version: V1.0
 */
@Tag(name="PI字段与DF字段映射")
@RestController
@RequestMapping("/pi/piDfTableFieldMapping")
@Slf4j
public class PiDfTableFieldMappingController extends BaseController<IPiDfTableFieldMappingService, PiDfTableFieldMapping> {
	@Autowired
	private IPiDfTableFieldMappingService piDfTableFieldMappingService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piDfTableFieldMapping
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI字段与DF字段映射-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiDfTableFieldMapping>> queryPageList(PiDfTableFieldMapping piDfTableFieldMapping,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiDfTableFieldMapping> queryWrapper = QueryGenerator.initQueryWrapper(piDfTableFieldMapping, req.getParameterMap());
		Page<PiDfTableFieldMapping> page = new Page<PiDfTableFieldMapping>(pageNo, pageSize);
		IPage<PiDfTableFieldMapping> pageList = piDfTableFieldMappingService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	 /**
	  * PI字段与DF字段映射-已映射字段查询
	  * @param piDfTableFieldMapping 查询对象
	  * @param req 参数
	  * @return 结果
	  */
	 @Operation(summary="PI字段与DF字段映射-已映射字段查询")
	 @GetMapping(value = "/queryMappingData")
	 public Result<PiDfTableFieldMapping> queryMappingData(PiDfTableFieldMapping piDfTableFieldMapping,
															   HttpServletRequest req) {
		 QueryWrapper<PiDfTableFieldMapping> queryWrapper = QueryGenerator.initQueryWrapper(piDfTableFieldMapping, req.getParameterMap());
		 List<PiDfTableFieldMapping> list = piDfTableFieldMappingService.list(queryWrapper);
		 return Result.OK(CollectionUtil.isNotEmpty(list) ? list.get(0) : null);
	 }
	
	/**
	 *   添加
	 *
	 * @param piDfTableFieldMapping
	 * @return
	 */
	@AutoLog(value = "PI字段与DF字段映射-添加")
	@Operation(summary="PI字段与DF字段映射-添加")
	@RequiresPermissions("pi:mes3_pi_df_table_field_mapping:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiDfTableFieldMapping piDfTableFieldMapping) {
		piDfTableFieldMappingService.save(piDfTableFieldMapping);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piDfTableFieldMapping
	 * @return
	 */
	@AutoLog(value = "PI字段与DF字段映射-编辑")
	@Operation(summary="PI字段与DF字段映射-编辑")
	@RequiresPermissions("pi:mes3_pi_df_table_field_mapping:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiDfTableFieldMapping piDfTableFieldMapping) {
		piDfTableFieldMappingService.updateById(piDfTableFieldMapping);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI字段与DF字段映射-通过id删除")
	@Operation(summary="PI字段与DF字段映射-通过id删除")
	@RequiresPermissions("pi:mes3_pi_df_table_field_mapping:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piDfTableFieldMappingService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI字段与DF字段映射-批量删除")
	@Operation(summary="PI字段与DF字段映射-批量删除")
	@RequiresPermissions("pi:mes3_pi_df_table_field_mapping:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piDfTableFieldMappingService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI字段与DF字段映射-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiDfTableFieldMapping> queryById(@RequestParam(name="id",required=true) String id) {
		PiDfTableFieldMapping piDfTableFieldMapping = piDfTableFieldMappingService.getById(id);
		if(piDfTableFieldMapping==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piDfTableFieldMapping);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piDfTableFieldMapping
    */
    @RequiresPermissions("pi:mes3_pi_df_table_field_mapping:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiDfTableFieldMapping piDfTableFieldMapping) {
        return super.exportXls(request, piDfTableFieldMapping, PiDfTableFieldMapping.class, "PI字段与DF字段映射");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_df_table_field_mapping:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiDfTableFieldMapping.class);
    }

}
