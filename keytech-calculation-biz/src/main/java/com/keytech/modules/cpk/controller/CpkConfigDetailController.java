package com.keytech.modules.cpk.controller;

import com.keytech.modules.cpk.entity.CpkConfigDetail;
import com.keytech.modules.cpk.service.ICpkConfigDetailService;
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
 * @Description: CPK配置详情
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Tag(name="CPK配置详情")
@RestController
@RequestMapping("/cpk/cpkConfigDetail")
@Slf4j
public class CpkConfigDetailController extends BaseController<ICpkConfigDetailService, CpkConfigDetail> {
	@Autowired
	private ICpkConfigDetailService cpkConfigDetailService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cpkConfigDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="CPK配置详情-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<CpkConfigDetail>> queryPageList(CpkConfigDetail cpkConfigDetail,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<CpkConfigDetail> queryWrapper = QueryGenerator.initQueryWrapper(cpkConfigDetail, req.getParameterMap());
		Page<CpkConfigDetail> page = new Page<CpkConfigDetail>(pageNo, pageSize);
		IPage<CpkConfigDetail> pageList = cpkConfigDetailService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cpkConfigDetail
	 * @return
	 */
	@AutoLog(value = "CPK配置详情-添加")
	@Operation(summary="CPK配置详情-添加")
	@RequiresPermissions("cpk:mes3_cpk_config_detail:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CpkConfigDetail cpkConfigDetail) {
		cpkConfigDetailService.save(cpkConfigDetail);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param cpkConfigDetail
	 * @return
	 */
	@AutoLog(value = "CPK配置详情-编辑")
	@Operation(summary="CPK配置详情-编辑")
	@RequiresPermissions("cpk:mes3_cpk_config_detail:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CpkConfigDetail cpkConfigDetail) {
		cpkConfigDetailService.updateById(cpkConfigDetail);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "CPK配置详情-通过id删除")
	@Operation(summary="CPK配置详情-通过id删除")
	@RequiresPermissions("cpk:mes3_cpk_config_detail:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cpkConfigDetailService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "CPK配置详情-批量删除")
	@Operation(summary="CPK配置详情-批量删除")
	@RequiresPermissions("cpk:mes3_cpk_config_detail:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cpkConfigDetailService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="CPK配置详情-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CpkConfigDetail> queryById(@RequestParam(name="id",required=true) String id) {
		CpkConfigDetail cpkConfigDetail = cpkConfigDetailService.getById(id);
		if(cpkConfigDetail==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(cpkConfigDetail);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cpkConfigDetail
    */
    @RequiresPermissions("cpk:mes3_cpk_config_detail:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CpkConfigDetail cpkConfigDetail) {
        return super.exportXls(request, cpkConfigDetail, CpkConfigDetail.class, "CPK配置详情");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cpk:mes3_cpk_config_detail:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CpkConfigDetail.class);
    }

}
