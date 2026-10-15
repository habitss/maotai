package com.keytech.modules.meeting.controller;

import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.meeting.dto.MeetingRecordVO;
import com.keytech.modules.meeting.entity.MeetingRecord;
import com.keytech.modules.meeting.service.IMeetingRecordService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 会议审核
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="会议审核")
@RestController
@RequestMapping("/meeting/meetingRecord")
@Slf4j
public class MeetingRecordController extends BaseController<IMeetingRecordService, MeetingRecord> {
	@Autowired
	private IMeetingRecordService meetingRecordService;

	@Autowired
	private IActionPlanTrackService actionPlanTrackService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="会议审核-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingRecord>> queryPageList(MeetingRecord meetingRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="50") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<MeetingRecord> queryWrapper = QueryGenerator.initQueryWrapper(meetingRecord, req.getParameterMap());
		Page<MeetingRecord> page = new Page<MeetingRecord>(pageNo, pageSize);
		IPage<MeetingRecord> pageList = meetingRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 /**
	  * 列表查询
	  *
	  * @param meetingRecord
	  * @return
	  */
	 @Operation(summary="会议审核-列表查询")
	 @GetMapping(value = "/queryList")
	 public Result<List<MeetingRecord>> queryList(MeetingRecord meetingRecord) {
		 return Result.OK(meetingRecordService.queryListMerge(meetingRecord));
	 }
	
	/**
	 *   添加
	 *
	 * @param recordVO
	 * @return
	 */
	@AutoLog(value = "会议审核-添加")
	@Operation(summary="会议审核-添加")
	//@RequiresPermissions("meeting:mes3_meeting_record:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingRecordVO recordVO) {

		List<MeetingRecord> result = new ArrayList<>();
		result.addAll(recordVO.getContent());
		result.addAll(recordVO.getEffect());
		result.forEach(record -> {
			record.setRecorder(recordVO.getRecorder());
			record.setLeader(recordVO.getLeader());
			MeetingRecord meetingRecord = meetingRecordService.getById(record.getId());
			if(null != meetingRecord) {
				meetingRecordService.updateById(record);
			} else {
				record.setId(null);
				meetingRecordService.save(record);
			}
		});
		actionPlanTrackService.saveOrUpdateAction(recordVO.getMeetingId(), recordVO.getActionPlanTrackList());
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingRecord
	 * @return
	 */
	@AutoLog(value = "会议审核-编辑")
	@Operation(summary="会议审核-编辑")
	@RequiresPermissions("meeting:mes3_meeting_record:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingRecord meetingRecord) {
		meetingRecordService.updateById(meetingRecord);

		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "会议审核-通过id删除")
	@Operation(summary="会议审核-通过id删除")
	@RequiresPermissions("meeting:mes3_meeting_record:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		meetingRecordService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "会议审核-批量删除")
	@Operation(summary="会议审核-批量删除")
	@RequiresPermissions("meeting:mes3_meeting_record:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.meetingRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="会议审核-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingRecord> queryById(@RequestParam(name="id",required=true) String id) {
		MeetingRecord meetingRecord = meetingRecordService.getById(id);
		if(meetingRecord==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param meetingRecord
    */
    @RequiresPermissions("meeting:mes3_meeting_record:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingRecord meetingRecord) {
        return super.exportXls(request, meetingRecord, MeetingRecord.class, "会议审核");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("meeting:mes3_meeting_record:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MeetingRecord.class);
    }

}
