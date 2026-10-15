package com.keytech.modules.serviceAgreement.controller;

import com.keytech.modules.serviceAgreement.entity.ServiceAgreementException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementInput;
import com.keytech.modules.serviceAgreement.service.IServiceAgreementExceptionService;
import com.keytech.modules.serviceAgreement.service.IServiceAgreementInputService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @Description: 服务协议异常记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Tag(name="服务协议异常记录表")
@RestController
@RequestMapping("/serviceAgreement/serviceAgreementException")
@Slf4j
public class ServiceAgreementExceptionController extends BaseController<IServiceAgreementExceptionService, ServiceAgreementException> {
	@Autowired
	private IServiceAgreementExceptionService serviceAgreementExceptionService;
	 @Autowired
	private IServiceAgreementInputService serviceAgreementInputService;
	
	/**
	 * 分页列表查询
	 *
	 * @param serviceAgreementException
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="服务协议异常记录表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ServiceAgreementException>> queryPageList(ServiceAgreementException serviceAgreementException,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		if(StringUtils.isEmpty(serviceAgreementException.getProvideDept()) && StringUtils.isEmpty(serviceAgreementException.getReceiveDept())) {
			if (StringUtils.isNotBlank(serviceAgreementException.getDeptCode()) && serviceAgreementException.getDeptCode().length() > 9) {
				if (serviceAgreementException.getType() == 1) {
					serviceAgreementException.setProvideDept(serviceAgreementException.getDeptCode());
					serviceAgreementException.setDeptCode(null);
				} else if (serviceAgreementException.getType() == 2) {
					serviceAgreementException.setReceiveDept(serviceAgreementException.getDeptCode());
					serviceAgreementException.setDeptCode(null);
				}
			} else {
				serviceAgreementException.setDeptCode(null);
			}
		} else {
			serviceAgreementException.setDeptCode(null);
		}
		//如果部门为工厂，查询全部
		if(StringUtils.isNotBlank(serviceAgreementException.getProvideDept()) && serviceAgreementException.getProvideDept().length() <= 9){
			serviceAgreementException.setProvideDept(null);
		}
		if(StringUtils.isNotBlank(serviceAgreementException.getReceiveDept()) && serviceAgreementException.getReceiveDept().length() <= 9){
			serviceAgreementException.setReceiveDept(null);
		}
        QueryWrapper<ServiceAgreementException> queryWrapper = QueryGenerator.initQueryWrapper(serviceAgreementException, req.getParameterMap());
		Page<ServiceAgreementException> page = new Page<>(pageNo, pageSize);
		IPage<ServiceAgreementException> pageList = serviceAgreementExceptionService.queryPageList(page, queryWrapper,req);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param serviceAgreementException
	 * @return
	 */
	@AutoLog(value = "服务协议异常记录表-添加")
	@Operation(summary="服务协议异常记录表-添加")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ServiceAgreementException serviceAgreementException) {
		if(StringUtils.isNotEmpty(serviceAgreementException.getServiceAgreementId())) {
			ServiceAgreementInput input = serviceAgreementInputService.getById(serviceAgreementException.getServiceAgreementId());
			serviceAgreementException.setProduct(input.getProduct());
			serviceAgreementException.setReceiveDept(input.getReceiveDept());
			serviceAgreementException.setProvideDept(input.getProvideDept());
			serviceAgreementException.setActivity(input.getActivity());
		}
		serviceAgreementExceptionService.save(serviceAgreementException);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param serviceAgreementException
	 * @return
	 */
	@AutoLog(value = "服务协议异常记录表-编辑")
	@Operation(summary="服务协议异常记录表-编辑")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ServiceAgreementException serviceAgreementException) {
		serviceAgreementExceptionService.updateById(serviceAgreementException);
		return Result.OK("编辑成功!");
	}

	 /**
	  *   审核
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "服务协议异常记录表-审核")
	 @Operation(summary="服务协议异常记录表-审核")
	 @RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:delete")
	 @DeleteMapping(value = "/check")
	 public Result<String> check(@RequestParam(name="id") String id) {
		 ServiceAgreementException exception = serviceAgreementExceptionService.getById(id);
		 if(null != exception) {
			 exception.setExamineStatus(1);
			 serviceAgreementExceptionService.updateById(exception);
		 }

		 return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	 }

	/**
	 * 更新确认状态
	 *
	 * @param serviceAgreementException
	 * @return
	 */
	@AutoLog(value = "服务协议异常记录表-更新确认状态")
	@Operation(summary="服务协议异常记录表-更新确认状态")
	@PostMapping(value = "/updateConfirmStatus")
	public Result<String> updateConfirmStatus(@RequestBody ServiceAgreementException serviceAgreementException) {
		serviceAgreementExceptionService.updateById(serviceAgreementException);
		return Result.OK("更新成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "服务协议异常记录表-通过id删除")
	@Operation(summary="服务协议异常记录表-通过id删除")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		serviceAgreementExceptionService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "服务协议异常记录表-批量删除")
	@Operation(summary="服务协议异常记录表-批量删除")
	@RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.serviceAgreementExceptionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="服务协议异常记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ServiceAgreementException> queryById(@RequestParam(name="id",required=true) String id) {
		ServiceAgreementException serviceAgreementException = serviceAgreementExceptionService.getById(id);
		if(serviceAgreementException==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(serviceAgreementException);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param serviceAgreementException
    */
    @RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ServiceAgreementException serviceAgreementException) {
		if(StringUtils.isEmpty(serviceAgreementException.getProvideDept()) && StringUtils.isEmpty(serviceAgreementException.getReceiveDept())) {
			if (StringUtils.isNotBlank(serviceAgreementException.getDeptCode()) && serviceAgreementException.getDeptCode().length() > 9) {
				if (serviceAgreementException.getType() == 1) {
					serviceAgreementException.setProvideDept(serviceAgreementException.getDeptCode());
					serviceAgreementException.setDeptCode(null);
				} else if (serviceAgreementException.getType() == 2) {
					serviceAgreementException.setReceiveDept(serviceAgreementException.getDeptCode());
					serviceAgreementException.setDeptCode(null);
				}
			} else {
				serviceAgreementException.setDeptCode(null);
			}
		} else {
			serviceAgreementException.setDeptCode(null);
		}
		return super.exportXls(request, serviceAgreementException, ServiceAgreementException.class, "服务协议异常记录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("serviceAgreement:mes3_service_agreement_exception:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ServiceAgreementException.class);
    }

}
