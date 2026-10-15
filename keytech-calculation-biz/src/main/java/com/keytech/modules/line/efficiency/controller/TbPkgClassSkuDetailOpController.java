package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.TbPkgClassSkuDetailOp;
import com.keytech.modules.line.efficiency.service.ITbPkgClassSkuDetailOpService;
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
 * @Description: OPI拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-03
 * @Version: V1.0
 */
@Tag(name="OPI拆分")
@RestController
@RequestMapping("/line/tbPkgClassSkuDetailOp")
@Slf4j
public class TbPkgClassSkuDetailOpController extends BaseController<ITbPkgClassSkuDetailOpService, TbPkgClassSkuDetailOp> {
	@Autowired
	private ITbPkgClassSkuDetailOpService tbPkgClassSkuDetailOpService;
	
	/**
	 * 分页列表查询
	 *
	 * @param tbPkgClassSkuDetailOp
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="OPI拆分-分页列表查询")
	@GetMapping(value = "/queryPageData")
	public Result<IPage<TbPkgClassSkuDetailOp>> queryPageList(TbPkgClassSkuDetailOp tbPkgClassSkuDetailOp,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<TbPkgClassSkuDetailOp> queryWrapper = QueryGenerator.initQueryWrapper(tbPkgClassSkuDetailOp, req.getParameterMap());
		Page<TbPkgClassSkuDetailOp> page = new Page<TbPkgClassSkuDetailOp>(pageNo, pageSize);
		IPage<TbPkgClassSkuDetailOp> pageList = tbPkgClassSkuDetailOpService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param tbPkgClassSkuDetailOp
	 * @return
	 */
	@AutoLog(value = "OPI拆分-添加")
	@Operation(summary="OPI拆分-添加")
	@RequiresPermissions("line:tb_pkg_class_sku_detail_op:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TbPkgClassSkuDetailOp tbPkgClassSkuDetailOp) {
		tbPkgClassSkuDetailOpService.save(tbPkgClassSkuDetailOp);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param tbPkgClassSkuDetailOp
	 * @return
	 */
	@AutoLog(value = "OPI拆分-编辑")
	@Operation(summary="OPI拆分-编辑")
	@RequiresPermissions("line:tb_pkg_class_sku_detail_op:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody TbPkgClassSkuDetailOp tbPkgClassSkuDetailOp) {
		tbPkgClassSkuDetailOpService.updateById(tbPkgClassSkuDetailOp);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "OPI拆分-通过id删除")
	@Operation(summary="OPI拆分-通过id删除")
	@RequiresPermissions("line:tb_pkg_class_sku_detail_op:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		tbPkgClassSkuDetailOpService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "OPI拆分-批量删除")
	@Operation(summary="OPI拆分-批量删除")
	@RequiresPermissions("line:tb_pkg_class_sku_detail_op:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.tbPkgClassSkuDetailOpService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="OPI拆分-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TbPkgClassSkuDetailOp> queryById(@RequestParam(name="id",required=true) String id) {
		TbPkgClassSkuDetailOp tbPkgClassSkuDetailOp = tbPkgClassSkuDetailOpService.getById(id);
		if(tbPkgClassSkuDetailOp==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(tbPkgClassSkuDetailOp);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param tbPkgClassSkuDetailOp
    */
    @RequiresPermissions("line:tb_pkg_class_sku_detail_op:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, TbPkgClassSkuDetailOp tbPkgClassSkuDetailOp) {
        return super.exportXls(request, tbPkgClassSkuDetailOp, TbPkgClassSkuDetailOp.class, "OPI拆分");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("line:tb_pkg_class_sku_detail_op:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, TbPkgClassSkuDetailOp.class);
    }

	 /**
	  *  拆分
	  *
	  * @param tbPkgClassSkuDetailOpList
	  * @return
	  */
	 @AutoLog(value = "OPI拆分-拆分")
	 @Operation(summary="OPI拆分-拆分")
	 @PostMapping(value = "/dataSplit")
	 public Result<String> dataSplit(@RequestBody List<TbPkgClassSkuDetailOp> tbPkgClassSkuDetailOpList) {
		 tbPkgClassSkuDetailOpService.dataSplit(tbPkgClassSkuDetailOpList);
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }
}
