package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.dto.PiDfAlgorithmDto;
import com.keytech.modules.pi.entity.PiDfAlgorithmBaseinfo;
import com.keytech.modules.pi.service.IPiDfAlgorithmBaseinfoService;
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

 /**
 * @Description: DF算法-基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-20
 * @Version: V1.0
 */
@Tag(name="DF算法-基础信息")
@RestController
@RequestMapping("/pi/piDfAlgorithmBaseinfo")
@Slf4j
public class PiDfAlgorithmBaseinfoController extends BaseController<IPiDfAlgorithmBaseinfoService, PiDfAlgorithmBaseinfo> {
	@Autowired
	private IPiDfAlgorithmBaseinfoService piDfAlgorithmBaseinfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piDfAlgorithmBaseinfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="DF算法-基础信息-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiDfAlgorithmBaseinfo>> queryPageList(PiDfAlgorithmBaseinfo piDfAlgorithmBaseinfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiDfAlgorithmBaseinfo> queryWrapper = QueryGenerator.initQueryWrapper(piDfAlgorithmBaseinfo, req.getParameterMap());
		Page<PiDfAlgorithmBaseinfo> page = new Page<PiDfAlgorithmBaseinfo>(pageNo, pageSize);
		IPage<PiDfAlgorithmBaseinfo> pageList = piDfAlgorithmBaseinfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piDfAlgorithmDto
	 * @return
	 */
	@AutoLog(value = "DF算法-基础信息-添加")
	@Operation(summary="DF算法-基础信息-添加")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiDfAlgorithmDto piDfAlgorithmDto) {
		piDfAlgorithmBaseinfoService.saveOrUpdateData(piDfAlgorithmDto);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piDfAlgorithmDto
	 * @return
	 */
	@AutoLog(value = "DF算法-基础信息-编辑")
	@Operation(summary="DF算法-基础信息-编辑")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiDfAlgorithmDto piDfAlgorithmDto) {
		piDfAlgorithmBaseinfoService.saveOrUpdateData(piDfAlgorithmDto);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DF算法-基础信息-通过id删除")
	@Operation(summary="DF算法-基础信息-通过id删除")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		piDfAlgorithmBaseinfoService.removeData(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DF算法-基础信息-批量删除")
	@Operation(summary="DF算法-基础信息-批量删除")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.piDfAlgorithmBaseinfoService.removeData(ids);
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="DF算法-基础信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiDfAlgorithmBaseinfo> queryById(@RequestParam(name="id",required=true) String id) {
		PiDfAlgorithmBaseinfo piDfAlgorithmBaseinfo = piDfAlgorithmBaseinfoService.getById(id);
		if(piDfAlgorithmBaseinfo==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piDfAlgorithmBaseinfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piDfAlgorithmBaseinfo
    */
    @RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiDfAlgorithmBaseinfo piDfAlgorithmBaseinfo) {
        return super.exportXls(request, piDfAlgorithmBaseinfo, PiDfAlgorithmBaseinfo.class, "DF算法-基础信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiDfAlgorithmBaseinfo.class);
	}

	@PostMapping(value = "/syncDfDataJob")
	public Result<String> syncDfDataJob(@RequestBody String paramJson) {
		try {
			piDfAlgorithmBaseinfoService.syncDfDataJob();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
		return Result.OK("添加成功！");
	}
}
