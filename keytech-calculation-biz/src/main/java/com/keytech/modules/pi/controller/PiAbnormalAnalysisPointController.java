package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.util.DataValidUtil;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPoint;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

 /**
 * @Description: 异常点位配置
 * @Author: jeecg-boot
 * @Date:   2025-03-04
 * @Version: V1.0
 */
@Tag(name="异常点位配置")
@RestController
@RequestMapping("/pi/piAbnormalAnalysisPoint")
@Slf4j
public class PiAbnormalAnalysisPointController extends BaseController<IPiAbnormalAnalysisPointService, PiAbnormalAnalysisPoint>{
	@Autowired
	private IPiAbnormalAnalysisPointService piAbnormalAnalysisPointService;

	/**
	 * 分页列表查询
	 *
	 * @param piAbnormalAnalysisPoint
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "异常点位配置-分页列表查询")
	@Operation(summary="异常点位配置-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<PiAbnormalAnalysisPoint>> queryPageList(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint,
									@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									@RequestParam(name = "orderField",required = false) String orderField,
									@RequestParam(name = "orderType",required = false) String orderType,
									@RequestParam(name = "selectFields",required = false) String selectFields,
								   	HttpServletRequest req) {
		String hasQuery = req.getParameter("hasQuery");
        if(hasQuery != null && "true".equals(hasQuery)){
            QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper =  QueryGenerator.initQueryWrapper(piAbnormalAnalysisPoint, req.getParameterMap());
			queryWrapper.orderByAsc(Arrays.asList("table_code","field_code","serial_number","id"));
			List<PiAbnormalAnalysisPoint> list = piAbnormalAnalysisPointService.queryTreeListNoPage(queryWrapper);
            IPage<PiAbnormalAnalysisPoint> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        }else{
            String parentId = piAbnormalAnalysisPoint.getParentId();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            piAbnormalAnalysisPoint.setParentId(null);
            QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = QueryGenerator.initQueryWrapper(piAbnormalAnalysisPoint, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("parent_id", parentId);
			queryWrapper.orderByAsc(Arrays.asList("table_code","field_code","serial_number","id"));
            Page<PiAbnormalAnalysisPoint> page = new Page<PiAbnormalAnalysisPoint>(pageNo, pageSize);
            IPage<PiAbnormalAnalysisPoint> pageList = piAbnormalAnalysisPointService.page(page, queryWrapper);
            return Result.OK(pageList);
        }
	}

	 /**
	  * 【vue3专用】加载节点的子数据
	  *
	  * @param pid
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") String pid) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = piAbnormalAnalysisPointService.queryListByPid(pid);
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
	  * 【vue3专用】加载一级节点/如果是同步 则所有数据
	  *
	  * @param async
	  * @param pcode
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "pcode") String pcode) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = piAbnormalAnalysisPointService.queryListByCode(pcode);
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
	  * 【vue3专用】递归求子节点 同步加载用到
	  *
	  * @param ls
	  */
	 private void loadAllChildren(List<SelectTreeModel> ls) {
		 for (SelectTreeModel tsm : ls) {
			 List<SelectTreeModel> temp = piAbnormalAnalysisPointService.queryListByPid(tsm.getKey());
			 if (temp != null && temp.size() > 0) {
				 tsm.setChildren(temp);
				 loadAllChildren(temp);
			 }
		 }
	 }

	 /**
      * 获取子数据
      * @param piAbnormalAnalysisPoint
      * @param req
      * @return
      */
	//@AutoLog(value = "异常点位配置-获取子数据")
	@Operation(summary="异常点位配置-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<PiAbnormalAnalysisPoint>> queryPageList(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint,HttpServletRequest req) {
		QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = QueryGenerator.initQueryWrapper(piAbnormalAnalysisPoint, req.getParameterMap());
		List<PiAbnormalAnalysisPoint> list = piAbnormalAnalysisPointService.list(queryWrapper);
		IPage<PiAbnormalAnalysisPoint> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
		return Result.OK(pageList);
	}

    /**
      * 批量查询子节点
      * @param parentIds 父ID（多个采用半角逗号分割）
      * @return 返回 IPage
      * @param parentIds
      * @return
      */
	//@AutoLog(value = "异常点位配置-批量获取子数据")
    @Operation(summary="异常点位配置-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("parent_id", parentIdList);
			queryWrapper.orderByAsc("serial_number");
            List<PiAbnormalAnalysisPoint> list = piAbnormalAnalysisPointService.list(queryWrapper);
            IPage<PiAbnormalAnalysisPoint> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }
	
	/**
	 *   添加
	 *
	 * @param piAbnormalAnalysisPoint
	 * @return
	 */
	@AutoLog(value = "异常点位配置-添加")
	@Operation(summary="异常点位配置-添加")
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAbnormalAnalysisPoint piAbnormalAnalysisPoint) {
		validExists(piAbnormalAnalysisPoint);
		piAbnormalAnalysisPointService.addPiAbnormalAnalysisPoint(piAbnormalAnalysisPoint);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piAbnormalAnalysisPoint
	 * @return
	 */
	@AutoLog(value = "异常点位配置-编辑")
	@Operation(summary="异常点位配置-编辑")
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAbnormalAnalysisPoint piAbnormalAnalysisPoint) {
		validExists(piAbnormalAnalysisPoint);
		piAbnormalAnalysisPointService.updatePiAbnormalAnalysisPoint(piAbnormalAnalysisPoint);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "异常点位配置-通过id删除")
	@Operation(summary="异常点位配置-通过id删除")
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piAbnormalAnalysisPointService.deletePiAbnormalAnalysisPoint(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "异常点位配置-批量删除")
	@Operation(summary="异常点位配置-批量删除")
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point:deleteBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piAbnormalAnalysisPointService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "异常点位配置-通过id查询")
	@Operation(summary="异常点位配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiAbnormalAnalysisPoint> queryById(@RequestParam(name="id",required=true) String id) {
		PiAbnormalAnalysisPoint piAbnormalAnalysisPoint = piAbnormalAnalysisPointService.getById(id);
		if(piAbnormalAnalysisPoint==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piAbnormalAnalysisPoint);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piAbnormalAnalysisPoint
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiAbnormalAnalysisPoint piAbnormalAnalysisPoint) {
		return super.exportXls(request, piAbnormalAnalysisPoint, PiAbnormalAnalysisPoint.class, "异常点位配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_point:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return this.piAbnormalAnalysisPointService.importExcelBy(request, response, PiAbnormalAnalysisPoint.class);
    }

	private void validExists(PiAbnormalAnalysisPoint piAbnormalAnalysisPoint){
		if(DataValidUtil.getDataExist(new String[]{"dept_code", "line_code", "serial_number"}, piAbnormalAnalysisPoint, "and")){
			throw new JeecgBootException("编号已存在");
		}
	}
}
