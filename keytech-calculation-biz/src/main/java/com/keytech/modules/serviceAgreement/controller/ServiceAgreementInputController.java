package com.keytech.modules.serviceAgreement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.serviceAgreement.dto.ServiceAgreementExportVO;
import com.keytech.modules.serviceAgreement.dto.ServiceMonthVO;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementInput;
import com.keytech.modules.serviceAgreement.service.IServiceAgreementInputService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 服务协议登记表
 * @Author: aolei
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Tag(name="服务协议登记表")
@RestController
@RequestMapping("/serviceAgreement/serviceAgreementInput")
@Slf4j
public class ServiceAgreementInputController extends BaseController<IServiceAgreementInputService, ServiceAgreementInput> {
	@Autowired
	private IServiceAgreementInputService serviceAgreementInputService;

	@Resource
	private JeecgBaseConfig jeecgBaseConfig;
	
	/**w
	 * 分页列表查询
	 *
	 * @param serviceAgreementInput
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="服务协议登记表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ServiceAgreementInput>> queryPageList(ServiceAgreementInput serviceAgreementInput,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ServiceAgreementInput> queryWrapper = QueryGenerator.initQueryWrapper(serviceAgreementInput, req.getParameterMap());
		queryWrapper.orderByDesc("id");
		Page<ServiceAgreementInput> page = new Page<ServiceAgreementInput>(pageNo, pageSize);
		IPage<ServiceAgreementInput> pageList = serviceAgreementInputService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param serviceAgreementInput
	 * @return
	 */
	@AutoLog(value = "服务协议登记表-添加")
	@Operation(summary="服务协议登记表-添加")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_input:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ServiceAgreementInput serviceAgreementInput) {
		serviceAgreementInputService.save(serviceAgreementInput);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param serviceAgreementInput
	 * @return
	 */
	@AutoLog(value = "服务协议登记表-编辑")
	@Operation(summary="服务协议登记表-编辑")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_input:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ServiceAgreementInput serviceAgreementInput) {
		serviceAgreementInputService.updateById(serviceAgreementInput);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "服务协议登记表-通过id删除")
	@Operation(summary="服务协议登记表-通过id删除")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_input:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		serviceAgreementInputService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "服务协议登记表-批量删除")
	@Operation(summary="服务协议登记表-批量删除")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_input:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.serviceAgreementInputService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="服务协议登记表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ServiceAgreementInput> queryById(@RequestParam(name="id",required=true) String id) {
		ServiceAgreementInput serviceAgreementInput = serviceAgreementInputService.getById(id);
		if(serviceAgreementInput==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(serviceAgreementInput);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param serviceAgreementInput
    */
    @RequiresPermissions("serviceAgreement:mes3_service_agreement_input:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ServiceAgreementInput serviceAgreementInput) {
		return super.exportXls(request, serviceAgreementInput, ServiceAgreementInput.class, "服务协议登记表");
	}

	/**
	 * 导出excel
	 *
	 * @param serviceAgreementInput
	 */
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_input:exportXls")
	@RequestMapping(value = "/exportTotalXls")
	public ModelAndView exportTotalXls(ServiceAgreementInput serviceAgreementInput) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		List<ServiceAgreementInput> list = getList(serviceAgreementInput);
		List<ServiceAgreementInput> result = serviceAgreementInputService.trackingList(list, serviceAgreementInput.getYear());
		List<ServiceAgreementExportVO> voList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(result)) {
			Map<String,List<ServiceAgreementInput>> map = result.stream().collect(Collectors.groupingBy(ServiceAgreementInput::getId));
			map.forEach((k, v) -> {
				ServiceAgreementExportVO vo = new ServiceAgreementExportVO();
				List<ServiceMonthVO> monthVOList = new ArrayList<>();
				v.forEach(item -> {
					BeanUtils.copyProperties(item, vo);
					ServiceMonthVO monthVO = new ServiceMonthVO();
					BeanUtils.copyProperties(item, monthVO);
					monthVOList.add(monthVO);
				});
				vo.setServiceMonthList(monthVOList);
				voList.add(vo);
			});
		}
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		mv.addObject("fileName", "服务协议跟踪统计表");
		mv.addObject("entity", ServiceAgreementExportVO.class);
		ExportParams exportParams = new ExportParams("服务协议跟踪统计" + "报表", "导出人:" + sysUser.getRealname(), "服务协议登记表");
		exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
		mv.addObject("params", exportParams);
		mv.addObject("data", voList);
		return mv;
	}

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("serviceAgreement:mes3_service_agreement_input:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            return serviceAgreementInputService.importExcelBy(request, response, ServiceAgreementInput.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	 @AutoLog(value = "服务协议登记表-分页列表查询")
	 @Operation(summary="服务协议登记表-分页列表查询")
	 @GetMapping(value = "/trackingList")
	 public Result<?> trackingList(ServiceAgreementInput mdmServiceAgreementInput,
								   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
								   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
								   HttpServletRequest req) {
		 List<ServiceAgreementInput> list = getList(mdmServiceAgreementInput);
		 return Result.OK(serviceAgreementInputService.trackingList(list, mdmServiceAgreementInput.getYear()));
	 }

	 private List<ServiceAgreementInput> getList(ServiceAgreementInput mdmServiceAgreementInput){
		 LambdaQueryWrapper<ServiceAgreementInput> queryWrapper = new LambdaQueryWrapper<>();
		 if(StringUtils.isEmpty(mdmServiceAgreementInput.getProvideDept()) && StringUtils.isEmpty(mdmServiceAgreementInput.getReceiveDept())) {
			 if (StringUtils.isNotBlank(mdmServiceAgreementInput.getDeptCode()) && mdmServiceAgreementInput.getDeptCode().length() > 9) {
				 mdmServiceAgreementInput.setProvideDept(mdmServiceAgreementInput.getDeptCode());
				 mdmServiceAgreementInput.setReceiveDept(mdmServiceAgreementInput.getDeptCode());
				 mdmServiceAgreementInput.setDeptCode(null);
				 queryWrapper.eq(ServiceAgreementInput::getProvideDept,mdmServiceAgreementInput.getProvideDept());
				 queryWrapper.or().eq(ServiceAgreementInput::getReceiveDept,mdmServiceAgreementInput.getReceiveDept());
			 } else {
				 mdmServiceAgreementInput.setDeptCode(null);
				 queryWrapper.eq(StringUtils.isNotEmpty(mdmServiceAgreementInput.getProvideDept()),ServiceAgreementInput::getProvideDept,mdmServiceAgreementInput.getDeptCode());
				 queryWrapper.eq(StringUtils.isNotEmpty(mdmServiceAgreementInput.getReceiveDept()),ServiceAgreementInput::getReceiveDept,mdmServiceAgreementInput.getDeptCode());
			 }
		 } else {
			 mdmServiceAgreementInput.setDeptCode(null);
			 queryWrapper.eq(StringUtils.isNotEmpty(mdmServiceAgreementInput.getProvideDept()),ServiceAgreementInput::getProvideDept,mdmServiceAgreementInput.getProvideDept());
			 queryWrapper.eq(StringUtils.isNotEmpty(mdmServiceAgreementInput.getReceiveDept()),ServiceAgreementInput::getReceiveDept,mdmServiceAgreementInput.getReceiveDept());
		 }
		 queryWrapper.like(StringUtils.isNotEmpty(mdmServiceAgreementInput.getProduct()),ServiceAgreementInput::getProduct, "%" + mdmServiceAgreementInput.getProduct() + "%");
		 queryWrapper.like(StringUtils.isNotEmpty(mdmServiceAgreementInput.getActivity()),ServiceAgreementInput::getActivity, "%" + mdmServiceAgreementInput.getActivity() + "%");
		 return  serviceAgreementInputService.list(queryWrapper);
	 }


}
