package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiDfAlgorithmUnique;
import com.keytech.modules.pi.service.IPiDfAlgorithmUniqueService;
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
 * @Description: DF算法-唯一约束
 * @Author: jeecg-boot
 * @Date:   2026-01-20
 * @Version: V1.0
 */
@Tag(name="DF算法-唯一约束")
@RestController
@RequestMapping("/pi/piDfAlgorithmUnique")
@Slf4j
public class PiDfAlgorithmUniqueController extends BaseController<IPiDfAlgorithmUniqueService, PiDfAlgorithmUnique> {
	@Autowired
	private IPiDfAlgorithmUniqueService piDfAlgorithmUniqueService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piDfAlgorithmUnique
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="DF算法-唯一约束-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiDfAlgorithmUnique>> queryPageList(PiDfAlgorithmUnique piDfAlgorithmUnique,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiDfAlgorithmUnique> queryWrapper = QueryGenerator.initQueryWrapper(piDfAlgorithmUnique, req.getParameterMap());
		Page<PiDfAlgorithmUnique> page = new Page<PiDfAlgorithmUnique>(pageNo, pageSize);
		IPage<PiDfAlgorithmUnique> pageList = piDfAlgorithmUniqueService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piDfAlgorithmUnique
	 * @return
	 */
	@AutoLog(value = "DF算法-唯一约束-添加")
	@Operation(summary="DF算法-唯一约束-添加")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiDfAlgorithmUnique piDfAlgorithmUnique) {
		piDfAlgorithmUniqueService.save(piDfAlgorithmUnique);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piDfAlgorithmUnique
	 * @return
	 */
	@AutoLog(value = "DF算法-唯一约束-编辑")
	@Operation(summary="DF算法-唯一约束-编辑")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiDfAlgorithmUnique piDfAlgorithmUnique) {
		piDfAlgorithmUniqueService.updateById(piDfAlgorithmUnique);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DF算法-唯一约束-通过id删除")
	@Operation(summary="DF算法-唯一约束-通过id删除")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_baseinfo:add")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piDfAlgorithmUniqueService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DF算法-唯一约束-批量删除")
	@Operation(summary="DF算法-唯一约束-批量删除")
	@RequiresPermissions("pi:mes3_pi_df_algorithm_unique:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piDfAlgorithmUniqueService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="DF算法-唯一约束-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiDfAlgorithmUnique> queryById(@RequestParam(name="id",required=true) String id) {
		PiDfAlgorithmUnique piDfAlgorithmUnique = piDfAlgorithmUniqueService.getById(id);
		if(piDfAlgorithmUnique==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piDfAlgorithmUnique);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piDfAlgorithmUnique
    */
    @RequiresPermissions("pi:mes3_pi_df_algorithm_unique:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiDfAlgorithmUnique piDfAlgorithmUnique) {
        return super.exportXls(request, piDfAlgorithmUnique, PiDfAlgorithmUnique.class, "DF算法-唯一约束");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_df_algorithm_unique:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiDfAlgorithmUnique.class);
    }

}
