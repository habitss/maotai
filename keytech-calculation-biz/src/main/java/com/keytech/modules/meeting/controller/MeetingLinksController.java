package com.keytech.modules.meeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.meeting.entity.MeetingLinks;
import com.keytech.modules.meeting.service.IMeetingLinksService;
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
 * @Description: mes3_meeting_links
 * @Author: jeecg-boot
 * @Date:   2025-05-29
 * @Version: V1.0
 */
@Tag(name="mes3_meeting_links")
@RestController
@RequestMapping("/meeting/meetingLinks")
@Slf4j
public class MeetingLinksController extends BaseController<IMeetingLinksService, MeetingLinks> {
	@Autowired
	private IMeetingLinksService meetingLinksService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingLinks
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="mes3_meeting_links-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingLinks>> queryPageList(MeetingLinks meetingLinks,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<MeetingLinks> queryWrapper = QueryGenerator.initQueryWrapper(meetingLinks, req.getParameterMap());
		Page<MeetingLinks> page = new Page<MeetingLinks>(pageNo, pageSize);
		IPage<MeetingLinks> pageList = meetingLinksService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param meetingLinks
	 * @return
	 */
	@AutoLog(value = "mes3_meeting_links-添加")
	@Operation(summary="mes3_meeting_links-添加")
	@RequiresPermissions("meeting:mes3_meeting_links:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingLinks meetingLinks) {
		meetingLinksService.save(meetingLinks);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingLinks
	 * @return
	 */
	@AutoLog(value = "mes3_meeting_links-编辑")
	@Operation(summary="mes3_meeting_links-编辑")
	@RequiresPermissions("meeting:mes3_meeting_links:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingLinks meetingLinks) {
		meetingLinksService.updateById(meetingLinks);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "mes3_meeting_links-通过id删除")
	@Operation(summary="mes3_meeting_links-通过id删除")
	@RequiresPermissions("meeting:mes3_meeting_links:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		meetingLinksService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "mes3_meeting_links-批量删除")
	@Operation(summary="mes3_meeting_links-批量删除")
	@RequiresPermissions("meeting:mes3_meeting_links:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.meetingLinksService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="mes3_meeting_links-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingLinks> queryById(@RequestParam(name="id") String id) {
		MeetingLinks meetingLinks = meetingLinksService.getById(id);
		if(meetingLinks==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingLinks);
	}

	 /**
	  * 通过template查询
	  *
	  * @param links
	  * @return
	  */
	 @Operation(summary="mes3_meeting_links-通过template查询")
	 @GetMapping(value = "/queryByTemplate")
	 public Result<List<MeetingLinks>> queryByTemplate(MeetingLinks links) {
		 List<MeetingLinks> meetingLinks = meetingLinksService.list(new LambdaQueryWrapper<MeetingLinks>()
				 .eq(MeetingLinks::getTemplateId, links.getTemplateId()));
		 if(meetingLinks==null) {
			 return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		 }
		 return Result.OK(meetingLinks);
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param meetingLinks
    */
    @RequiresPermissions("meeting:mes3_meeting_links:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingLinks meetingLinks) {
        return super.exportXls(request, meetingLinks, MeetingLinks.class, "mes3_meeting_links");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("meeting:mes3_meeting_links:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MeetingLinks.class);
    }

}
