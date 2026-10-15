package com.keytech.modules.post.check.controller;

import com.keytech.modules.post.check.service.IPostCheckRecordConfigMainService;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigMain;
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
 * @Description: 岗位检查记录配置主表
 * @Author: jeecg-boot
 * @Date:   2025-01-02
 * @Version: V1.0
 */
@Tag(name="岗位检查记录配置主表")
@RestController
@RequestMapping("/post_check/postCheckRecordConfigMain")
@Slf4j
public class PostCheckRecordConfigMainController extends BaseController<IPostCheckRecordConfigMainService, PostCheckRecordConfigMain> {
	@Autowired
	private IPostCheckRecordConfigMainService postCheckRecordPzZbService;
	
	/**
	 * 分页列表查询
	 *
	 * @param postCheckRecordPzZb
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="岗位检查记录配置主表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PostCheckRecordConfigMain>> queryPageList(PostCheckRecordConfigMain postCheckRecordPzZb,
																  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
																  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
																  @RequestParam(name = "orderField",required = false) String orderField,
																  @RequestParam(name = "orderType",required = false) String orderType,
																  @RequestParam(name = "selectFields",required = false) String selectFields,
																  HttpServletRequest req) {
        QueryWrapper<PostCheckRecordConfigMain> queryWrapper = QueryGenerator.initQueryWrapper(postCheckRecordPzZb, req.getParameterMap());
		Page<PostCheckRecordConfigMain> page = new Page<PostCheckRecordConfigMain>(pageNo, pageSize);
		IPage<PostCheckRecordConfigMain> pageList = postCheckRecordPzZbService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param postCheckRecordPzZb
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置主表-添加")
	@Operation(summary="岗位检查记录配置主表-添加")
	@RequiresPermissions("post_check:mes3_post_check_record_config_main:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PostCheckRecordConfigMain postCheckRecordPzZb) {
		postCheckRecordPzZbService.save(postCheckRecordPzZb);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param postCheckRecordPzZb
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置主表-编辑")
	@Operation(summary="岗位检查记录配置主表-编辑")
	@RequiresPermissions("post_check:mes3_post_check_record_config_main:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PostCheckRecordConfigMain postCheckRecordPzZb) {
		postCheckRecordPzZbService.updateById(postCheckRecordPzZb);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置主表-通过id删除")
	@Operation(summary="岗位检查记录配置主表-通过id删除")
	@RequiresPermissions("post_check:mes3_post_check_record_config_main:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		postCheckRecordPzZbService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "岗位检查记录配置主表-批量删除")
	@Operation(summary="岗位检查记录配置主表-批量删除")
	@RequiresPermissions("post_check:mes3_post_check_record_config_main:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.postCheckRecordPzZbService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="岗位检查记录配置主表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PostCheckRecordConfigMain> queryById(@RequestParam(name="id",required=true) String id) {
		PostCheckRecordConfigMain postCheckRecordPzZb = postCheckRecordPzZbService.getById(id);
		if(postCheckRecordPzZb==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(postCheckRecordPzZb);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param postCheckRecordPzZb
    */
    @RequiresPermissions("post_check:mes3_post_check_record_config_main:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PostCheckRecordConfigMain postCheckRecordPzZb) {
        return super.exportXls(request, postCheckRecordPzZb, PostCheckRecordConfigMain.class, "岗位检查记录配置主表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("post_check:mes3_post_check_record_config_main:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PostCheckRecordConfigMain.class);
    }

}
