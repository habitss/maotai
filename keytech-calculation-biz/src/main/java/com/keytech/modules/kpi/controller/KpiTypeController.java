package com.keytech.modules.kpi.controller;

import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.service.IKpiTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: KPI分类
 * @Author: HQ
 * @Date:   2025-02-06
 * @Version: V1.0
 */
@Tag(name="KPI分类")
@RestController
@RequestMapping("/kpi/kpiType")
@Slf4j
public class KpiTypeController extends BaseController<IKpiTypeService, KpiType> {
	@Autowired
	private IKpiTypeService kpiTypeService;

	 /**
	  * 分类列表查询
	  *
	  * @param
	  * @return
	  */
	 @Operation(summary="KPI分类-列表查询")
	 @GetMapping(value = "/typeDataList")
	 public Result<?> typeDataList(@RequestParam(required = false) String id,@RequestParam(required = false) String year) {
		 return Result.OK(kpiTypeService.typeDataList(id,year));
	 }


	/**
	 *   添加
	 *
	 * @param kpiType
	 * @return
	 */
	@AutoLog(value = "KPI分类-添加")
	@Operation(summary="KPI分类-添加")
	@RequiresPermissions("kpi:mes3_kpi_type:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody KpiType kpiType) {
		kpiTypeService.save(kpiType);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiType
	 * @return
	 */
	@AutoLog(value = "KPI分类-编辑")
	@Operation(summary="KPI分类-编辑")
	@RequiresPermissions("kpi:mes3_kpi_type:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiType kpiType) {
		kpiTypeService.updateById(kpiType);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "KPI分类-通过id删除")
	@Operation(summary="KPI分类-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_type:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiTypeService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "KPI分类-批量删除")
	@Operation(summary="KPI分类-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_type:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiTypeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="KPI分类-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiType> queryById(@RequestParam(name="id",required=true) String id) {
		KpiType kpiType = kpiTypeService.getById(id);
		if(kpiType==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiType);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiType
    */
    @RequiresPermissions("kpi:mes3_kpi_type:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiType kpiType) {
        return super.exportXls(request, kpiType, KpiType.class, "mes3_kpi_type");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_type:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, KpiType.class);
    }


	/**
	 * 加载一级节点/如果是同步 则所有数据
	 *
	 * @param async
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "parentId") String parentId) {
		Result<List<SelectTreeModel>> result = new Result<>();
		try {
			List<SelectTreeModel> ls =kpiTypeService.queryListByPid(parentId);
			if (!async) {
				loadAllChildren(ls);
			}
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}

	/**
	 * 递归求子节点 同步加载用到
	 *
	 * @param ls
	 */
	private void loadAllChildren(List<SelectTreeModel> ls) {
		for (SelectTreeModel tsm : ls) {
			List<SelectTreeModel> temp = kpiTypeService.queryListByPid(tsm.getKey());
			if (temp != null && temp.size() > 0) {
				tsm.setChildren(temp);
				loadAllChildren(temp);
			}
		}
	}

}
