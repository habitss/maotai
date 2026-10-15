package com.keytech.modules.post.record.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import com.keytech.modules.post.take.entity.TakeUpPost;
import com.keytech.modules.post.record.service.ITakeUpPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 上下岗记录
 * @Author: jeecg-boot
 * @Date:   2025-06-16
 * @Version: V1.0
 */
@Tag(name="上下岗记录")
@RestController
@RequestMapping("/post/take/takeUpPost")
@Slf4j
public class TakeUpPostController extends BaseController<ITakeUpPostService, TakeUpPost> {
	@Autowired
	private ITakeUpPostService postRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param takeUpPost
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="上下岗记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<TakeUpPost>> queryPageList(TakeUpPost takeUpPost,
												   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												   @RequestParam(name = "orderField",required = false) String orderField,
												   @RequestParam(name = "orderType",required = false) String orderType,
												   @RequestParam(name = "selectFields",required = false) String selectFields,
												   HttpServletRequest req) {
        QueryWrapper<TakeUpPost> queryWrapper = QueryGenerator.initQueryWrapper(takeUpPost, req.getParameterMap());
		Page<TakeUpPost> page = new Page<TakeUpPost>(pageNo, pageSize);
		IPage<TakeUpPost> pageList = postRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	 /**
	  * 获取全部数据
	  * @param takeUpPost
	  * @param req
	  * @return
	  */
	 @Operation(summary="上下岗记录-获取全部数据")
	 @GetMapping(value = "/listAll")
	 @AutoDict
	 public Result<List<TakeUpPost>> listAll(TakeUpPost takeUpPost, HttpServletRequest req) {
		 //去除时间字段，临时处理，后续App重新发版后去掉
		 takeUpPost.setPostStartTime(null);
		 QueryWrapper<TakeUpPost> queryWrapper = QueryGenerator.initQueryWrapper(takeUpPost, req.getParameterMap());
		 if(StrUtil.isNotBlank(takeUpPost.getDistinctField())){
			 //sql注入检查
			 SqlInjectionUtil.filterContent(takeUpPost.getDistinctField());
			 queryWrapper.select("DISTINCT  "+takeUpPost.getDistinctField());
		 }
		 return Result.OK(postRecordService.list(queryWrapper));
	 }

	/**
	 * 上岗操作
	 * @param takeUpPost
	 * @return
	 */
	@AutoLog(value = "上岗操作-新增")
	@Operation(summary="上岗操作-新增")
	@RequestMapping(value = "/startPost", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> startPost(@RequestBody TakeUpPost takeUpPost, HttpServletRequest request) {
		return postRecordService.startPost(takeUpPost, request);
	}

	/**
	 * 批量上岗操作
	 * @param takeUpPostList
	 * @return
	 */
	@AutoLog(value = "批量上岗操作-新增")
	@Operation(summary="批量上岗操作-新增")
	@RequestMapping(value = "/startPostBatch", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> startPostBatch(@RequestBody List<TakeUpPost> takeUpPostList, HttpServletRequest request) {
		for(TakeUpPost takeUpPost : takeUpPostList){
			postRecordService.startPost(takeUpPost, request);
		}
		return Result.OK("上岗成功!");
	}

	 /**
	  * 离岗操作
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "离岗操作-编辑")
	 @Operation(summary="离岗操作-编辑")
	 @RequestMapping(value = "/endPost", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> endPost(@RequestBody String id) {
		 if(StrUtil.isBlank(id)){
			 return Result.error("必传值为空!");
		 }
		 postRecordService.endPostBatch(id);
		 return Result.OK("离岗成功!");
	 }

	 /**
	  * 批量离岗
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "批量离岗-编辑")
	 @Operation(summary="批量离岗-编辑")
	 @RequestMapping(value = "/endPostBatch", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> endPostBatch(@RequestParam(name = "ids",required = false) String ids) {
		 if(StrUtil.isBlank(ids)){
			 return Result.error("必传值为空!");
		 }
		 postRecordService.endPostBatch(ids);
		 return Result.OK("离岗成功!");
	 }

	/**
	 * 定时离岗
	 * @param paramJson 定时任务参数
	 */
	@AutoLog(value = "定时离岗")
	@Operation(summary="定时离岗")
	@PostMapping(value = "/autoEndPost")
	public Result<String> autoEndPost(@RequestBody String paramJson) {
		postRecordService.endPostByTime();
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
}
