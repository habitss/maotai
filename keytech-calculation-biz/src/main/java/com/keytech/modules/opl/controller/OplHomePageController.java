package com.keytech.modules.opl.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.opl.entity.OplHomePage;
import com.keytech.modules.opl.service.IOplHomePageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @Description: Opl清单
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Tag(name="Opl清单")
@RestController
@RequestMapping("/opl/oplHomePage")
@Slf4j
public class OplHomePageController extends BaseController<IOplHomePageService, OplHomePage> {
	@Autowired
	private IOplHomePageService oplHomePageService;
	
	/**
	 * 分页列表查询
	 *
	 * @param oplHomePage
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Opl 清单-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<OplHomePage>> queryPageList(OplHomePage oplHomePage,
													@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
													@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
													@RequestParam(name = "orderField",required = false) String orderField,
													@RequestParam(name = "orderType",required = false) String orderType,
													@RequestParam(name = "selectFields",required = false) String selectFields,
													HttpServletRequest req) {

		Page<OplHomePage> page = new Page<>(pageNo, pageSize);
		IPage<OplHomePage> pageList = oplHomePageService.getQueryPageData(page, oplHomePage,req);
		return Result.OK(pageList);
	}
	
	/**
	 *  添加
	 *
	 * @param oplHomePageList
	 * @return
	 */
	@AutoLog(value = "Opl 清单-添加")
	@Operation(summary="Opl 清单-添加")
	@RequiresPermissions("opl:mes3_opl_home_page:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody List<OplHomePage> oplHomePageList) {
		oplHomePageService.batchAddOrUpdate(oplHomePageList);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param oplHomePage
	 * @return
	 */
	@AutoLog(value = "Opl 清单-编辑")
	@Operation(summary="Opl 清单-编辑")
	@RequiresPermissions("opl:mes3_opl_home_page:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody OplHomePage oplHomePage) {
		oplHomePageService.updateById(oplHomePage);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Opl 清单-通过id删除")
	@Operation(summary="Opl 清单-通过id删除")
	@RequiresPermissions("opl:mes3_opl_home_page:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		oplHomePageService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Opl 清单-批量删除")
	@Operation(summary="Opl 清单-批量删除")
//	@RequiresPermissions("opl:mes3_opl_home_page:deleteBatch")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="ids",required=true) String ids) {
		boolean isOk = oplHomePageService.deleteData(ids);
		Result<String> result = Result.OK("批量删除成功!");
		if(!isOk){
			result = Result.error("批量删除失败!");
		}
		return result;
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Opl 清单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<OplHomePage> queryById(@RequestParam(name="id",required=true) String id) {
		OplHomePage oplHomePage = oplHomePageService.getById(id);
		if(oplHomePage==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(oplHomePage);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param oplHomePage
    */
    @RequiresPermissions("opl:mes3_opl_home_page:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, OplHomePage oplHomePage) {
		if(StrUtil.isNotBlank(oplHomePage.getDeptQuery())){
			oplHomePage.setDeptCode(oplHomePage.getDeptQuery());
		}else if(StrUtil.isNotBlank(oplHomePage.getDeptCode())){
			oplHomePage.setDeptCode(oplHomePage.getDeptCode()+"*");
		}
        return super.exportXls(request, oplHomePage, OplHomePage.class, "Opl 清单");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("opl:mes3_opl_home_page:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return oplHomePageService.importExcelGenCode(request, response, OplHomePage.class);
    }


	/**
	 * 诊断统计
	 */
	@Operation(summary="诊断统计")
	@GetMapping(value = "/getOplDept")
	public Result<?> getOplDept(@RequestParam(name="year",required=true) String year,
								@RequestParam(name="depts", required = false) String depts,
								@RequestParam(name="dept", required = false) String dept) {
		return Result.OK(oplHomePageService.getOplDept(year,depts,dept));
	}

	/**
	 * Opl管理回顾统计
	 */
	@Operation(summary="Opl管理回顾统计")
	@GetMapping(value = "/getOplManage")
	public Result<?> getOplManage(@RequestParam(name="year",required=true) String year,
								  @RequestParam(name="month",required=true) String month,
								  @RequestParam(name="depts",required = false) String depts,
								  @RequestParam(name="dept",required = false) String dept) {
		return Result.OK(oplHomePageService.getOplManage(year,month,depts,dept));
	}


	/**
	 *  编写职责-编辑
	 *
	 * @param input
	 * @return
	 */
	@AutoLog(value = "编写职责-添加")
	@Operation(summary="编写职责-添加")
	@PostMapping(value = "/updateByWriteTwoRr")
	public Result<String> updateByWriteTwoRr(@RequestBody JSONObject input) {
		oplHomePageService.updateByWriteTwoRr(input);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
}
