package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.dto.PiRepeatCopyDto;
import com.keytech.modules.pi.entity.PiRepeatExceptionType;
import com.keytech.modules.pi.service.IPiRepeatExceptionTypeService;
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
 * @Description: pi重复异常类型配置
 * @Author: jeecg-boot
 * @Date:   2025-12-03
 * @Version: V1.0
 */
@Tag(name="pi重复异常类型配置")
@RestController
@RequestMapping("/pi/piRepeatExceptionType")
@Slf4j
public class PiRepeatExceptionTypeController extends BaseController<IPiRepeatExceptionTypeService, PiRepeatExceptionType> {
	@Autowired
	private IPiRepeatExceptionTypeService piRepeatExceptionTypeService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piRepeatExceptionType
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="pi重复异常类型配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiRepeatExceptionType>> queryPageList(PiRepeatExceptionType piRepeatExceptionType,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiRepeatExceptionType> queryWrapper = QueryGenerator.initQueryWrapper(piRepeatExceptionType, req.getParameterMap());
		queryWrapper.orderByDesc("id");
		Page<PiRepeatExceptionType> page = new Page<PiRepeatExceptionType>(pageNo, pageSize);
		IPage<PiRepeatExceptionType> pageList = piRepeatExceptionTypeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * 获取所有数据
	 * @param piRepeatExceptionType 查询参数
	 * @return
	 */
	@Operation(summary="pi重复异常类型配置-获取所有数据")
	@GetMapping(value = "/listAll")
	public Result<List<PiRepeatExceptionType>> listAll(PiRepeatExceptionType piRepeatExceptionType,HttpServletRequest req) {
		if(StrUtil.isBlank(piRepeatExceptionType.getPiTableCode())
				||StrUtil.isBlank(piRepeatExceptionType.getPiFieldCode())){
			return null;
		}
		QueryWrapper<PiRepeatExceptionType> queryWrapper = QueryGenerator.initQueryWrapper(piRepeatExceptionType, req.getParameterMap());
		queryWrapper.orderByAsc("sort");
		queryWrapper.orderByDesc("id");
		return Result.OK(piRepeatExceptionTypeService.list(queryWrapper));
	}
	
	/**
	 *   添加
	 *
	 * @param piRepeatExceptionType
	 * @return
	 */
	@AutoLog(value = "pi重复异常类型配置-添加")
	@Operation(summary="pi重复异常类型配置-添加")
	@RequiresPermissions("pi:mes3_pi_repeat_exception_type:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiRepeatExceptionType piRepeatExceptionType) {
		piRepeatExceptionTypeService.saveOrUpdateData(piRepeatExceptionType);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piRepeatExceptionType
	 * @return
	 */
	@AutoLog(value = "pi重复异常类型配置-编辑")
	@Operation(summary="pi重复异常类型配置-编辑")
	@RequiresPermissions("pi:mes3_pi_repeat_exception_type:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiRepeatExceptionType piRepeatExceptionType) {
		piRepeatExceptionTypeService.saveOrUpdateData(piRepeatExceptionType);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pi重复异常类型配置-通过id删除")
	@Operation(summary="pi重复异常类型配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_repeat_exception_type:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piRepeatExceptionTypeService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "pi重复异常类型配置-批量删除")
	@Operation(summary="pi重复异常类型配置-批量删除")
	@RequiresPermissions("pi:mes3_pi_repeat_exception_type:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piRepeatExceptionTypeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="pi重复异常类型配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiRepeatExceptionType> queryById(@RequestParam(name="id",required=true) String id) {
		PiRepeatExceptionType piRepeatExceptionType = piRepeatExceptionTypeService.getById(id);
		if(piRepeatExceptionType==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piRepeatExceptionType);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piRepeatExceptionType
    */
    @RequiresPermissions("pi:mes3_pi_repeat_exception_type:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiRepeatExceptionType piRepeatExceptionType) {
        return super.exportXls(request, piRepeatExceptionType, PiRepeatExceptionType.class, "pi重复异常类型配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_repeat_exception_type:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiRepeatExceptionType.class);
    }

	 /**
	  * pi重复异常类型配置复制
	  * @param dto 复制的配置数据
	  * @return 结果
	  */
	@AutoLog(value = "pi重复异常类型配置复制-添加")
	@Operation(summary="pi重复异常类型配置复制-添加")
	@RequiresPermissions("pi:mes3_pi_repeat_exception_type:add")
	@PostMapping(value = "/addCopy")
	public Result<String> addCopy(@RequestBody PiRepeatCopyDto dto) {
		piRepeatExceptionTypeService.addCopy(dto);
	 	return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

}
