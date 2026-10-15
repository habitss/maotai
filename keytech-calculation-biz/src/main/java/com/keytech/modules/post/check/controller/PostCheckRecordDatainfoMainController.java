package com.keytech.modules.post.check.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigProject;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigStandard;
import com.keytech.modules.post.check.entity.PostCheckRecordDatainfoMain;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigMainService;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigProjectService;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigStandardService;
import com.keytech.modules.post.check.service.IPostCheckRecordDatainfoMainService;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 岗位记录填报主表
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
@Tag(name="岗位记录填报主表")
@RestController
@RequestMapping("/post/check/postCheckRecordDatainfoMain")
@Slf4j
public class PostCheckRecordDatainfoMainController extends BaseController<IPostCheckRecordDatainfoMainService, PostCheckRecordDatainfoMain> {
	@Autowired
	private IPostCheckRecordDatainfoMainService postCheckRecordDatainfoMainService;

	@Autowired
	private IPostCheckRecordConfigProjectService postCheckRecordConfigProjectService;

	@Autowired
	private IPostCheckRecordConfigStandardService postCheckRecordConfigStandardService;
	
	/**
	 * 分页列表查询
	 *
	 * @param postCheckRecordDatainfoMain
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="岗位记录填报主表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PostCheckRecordDatainfoMain>> queryPageList(PostCheckRecordDatainfoMain postCheckRecordDatainfoMain,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PostCheckRecordDatainfoMain> queryWrapper = QueryGenerator.initQueryWrapper(postCheckRecordDatainfoMain, req.getParameterMap());
		queryWrapper.orderByDesc("filling_date");
		queryWrapper.orderByDesc("create_time");
		Page<PostCheckRecordDatainfoMain> page = new Page<PostCheckRecordDatainfoMain>(pageNo, pageSize);
		IPage<PostCheckRecordDatainfoMain> pageList = postCheckRecordDatainfoMainService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 * @param postCheckRecordDatainfoMain
	 * @return
	 */
	@AutoLog(value = "岗位记录填报主表-添加")
	@Operation(summary="岗位记录填报主表-添加")
	@PostMapping(value = "/saveDataInfo")
	public Result<String> saveOrUpdate(@RequestBody PostCheckRecordDatainfoMain postCheckRecordDatainfoMain) {
		postCheckRecordDatainfoMainService.saveDataInfo(postCheckRecordDatainfoMain);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *   添加
	 *
	 * @param postCheckRecordDatainfoMain
	 * @return
	 */
	@AutoLog(value = "岗位记录填报主表-添加")
	@Operation(summary="岗位记录填报主表-添加")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_main:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PostCheckRecordDatainfoMain postCheckRecordDatainfoMain) {
		postCheckRecordDatainfoMainService.save(postCheckRecordDatainfoMain);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param postCheckRecordDatainfoMain
	 * @return
	 */
	@AutoLog(value = "岗位记录填报主表-编辑")
	@Operation(summary="岗位记录填报主表-编辑")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_main:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PostCheckRecordDatainfoMain postCheckRecordDatainfoMain) {
		postCheckRecordDatainfoMainService.updateById(postCheckRecordDatainfoMain);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "岗位记录填报主表-通过id删除")
	@Operation(summary="岗位记录填报主表-通过id删除")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_main:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		postCheckRecordDatainfoMainService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "岗位记录填报主表-批量删除")
	@Operation(summary="岗位记录填报主表-批量删除")
	@RequiresPermissions("post.check:mes3_post_check_record_datainfo_main:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.postCheckRecordDatainfoMainService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 获取数据及配置
	 * @param configId 配置主表id
	 * @return
	 */
	@Operation(summary="岗位记录填报主表-获取数据及配置")
	@GetMapping(value = "/queryConfig")
	public Result<PostCheckRecordDatainfoMain> queryDataAndConfig(@RequestParam(name="configId") String configId) {
		PostCheckRecordDatainfoMain postCheckRecordDatainfoMain = new PostCheckRecordDatainfoMain();
		//获取检查项配置并转换为json
		List<PostCheckRecordConfigProject> configProjectList = postCheckRecordConfigProjectService.list(
				new LambdaQueryWrapper<PostCheckRecordConfigProject>().eq(PostCheckRecordConfigProject::getZbId, configId)
						.orderByAsc(PostCheckRecordConfigProject::getStep)
		);
		postCheckRecordDatainfoMain.setConfigProject(JSONUtil.toJsonStr(configProjectList));
		//获取检查标准配置并转换为json
		List<String> idList = configProjectList.stream()
				.map(PostCheckRecordConfigProject::getId)
				.collect(Collectors.toList());
		List<PostCheckRecordConfigStandard> standardList = postCheckRecordConfigStandardService.list(
				new LambdaQueryWrapper<PostCheckRecordConfigStandard>().in(PostCheckRecordConfigStandard::getJcxmId, idList)
						.orderByAsc(PostCheckRecordConfigStandard::getSort)
		);
		postCheckRecordDatainfoMain.setConfigStandard(JSONUtil.toJsonStr(standardList));
		return Result.OK(postCheckRecordDatainfoMain);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param postCheckRecordDatainfoMain
    */
    @RequiresPermissions("post.check:mes3_post_check_record_datainfo_main:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PostCheckRecordDatainfoMain postCheckRecordDatainfoMain) {
        return super.exportXls(request, postCheckRecordDatainfoMain, PostCheckRecordDatainfoMain.class, "岗位记录填报主表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("post.check:mes3_post_check_record_datainfo_main:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PostCheckRecordDatainfoMain.class);
    }

}
