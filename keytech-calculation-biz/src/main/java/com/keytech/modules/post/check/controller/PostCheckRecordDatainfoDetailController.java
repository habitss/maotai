package com.keytech.modules.post.check.controller;

import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoDetail;
import com.keytech.modules.post.check.service.IPostCheckRecordDatainfoDetailService;
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
 * @Description: 岗位记录填报明细表
 * @Author: jeecg-boot
 * @Date:   2025-02-11
 * @Version: V1.0
 */
@Tag(name="岗位记录填报明细表")
@RestController
@RequestMapping("/post/check/postCheckRecordDatainfoDetail")
@Slf4j
public class PostCheckRecordDatainfoDetailController extends BaseController<IPostCheckRecordDatainfoDetailService, PostCheckRecordDatainfoDetail> {
	@Autowired
	private IPostCheckRecordDatainfoDetailService postCheckRecordDatainfoDetailService;
	
	/**
	 * 分页列表查询
	 *
	 * @param postCheckRecordDatainfoDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="岗位记录填报明细表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PostCheckRecordDatainfoDetail>> queryPageList(PostCheckRecordDatainfoDetail postCheckRecordDatainfoDetail,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PostCheckRecordDatainfoDetail> queryWrapper = QueryGenerator.initQueryWrapper(postCheckRecordDatainfoDetail, req.getParameterMap());
		Page<PostCheckRecordDatainfoDetail> page = new Page<PostCheckRecordDatainfoDetail>(pageNo, pageSize);
		IPage<PostCheckRecordDatainfoDetail> pageList = postCheckRecordDatainfoDetailService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param postCheckRecordDatainfoDetail
	 * @return
	 */
	@AutoLog(value = "岗位记录填报明细表-添加")
	@Operation(summary="岗位记录填报明细表-添加")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_detail:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PostCheckRecordDatainfoDetail postCheckRecordDatainfoDetail) {
		postCheckRecordDatainfoDetailService.save(postCheckRecordDatainfoDetail);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param postCheckRecordDatainfoDetail
	 * @return
	 */
	@AutoLog(value = "岗位记录填报明细表-编辑")
	@Operation(summary="岗位记录填报明细表-编辑")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_detail:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PostCheckRecordDatainfoDetail postCheckRecordDatainfoDetail) {
		postCheckRecordDatainfoDetailService.updateById(postCheckRecordDatainfoDetail);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "岗位记录填报明细表-通过id删除")
	@Operation(summary="岗位记录填报明细表-通过id删除")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_detail:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		postCheckRecordDatainfoDetailService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "岗位记录填报明细表-批量删除")
	@Operation(summary="岗位记录填报明细表-批量删除")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_detail:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.postCheckRecordDatainfoDetailService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="岗位记录填报明细表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PostCheckRecordDatainfoDetail> queryById(@RequestParam(name="id",required=true) String id) {
		PostCheckRecordDatainfoDetail postCheckRecordDatainfoDetail = postCheckRecordDatainfoDetailService.getById(id);
		if(postCheckRecordDatainfoDetail==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(postCheckRecordDatainfoDetail);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param postCheckRecordDatainfoDetail
    */
    @RequiresPermissions("post.check:mes3_post_check_record_datainfo_detail:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PostCheckRecordDatainfoDetail postCheckRecordDatainfoDetail) {
        return super.exportXls(request, postCheckRecordDatainfoDetail, PostCheckRecordDatainfoDetail.class, "岗位记录填报明细表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("post.check:mes3_post_check_record_datainfo_detail:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PostCheckRecordDatainfoDetail.class);
    }

}
