package com.keytech.modules.alarm.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.service.IAlarmPointService;
import com.keytech.modules.df.mdm.service.ISyncDfMdmDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Arrays;
import java.util.List;


/**
 * 报警点位
 * @author liu
 * @since 2026.01.12
 * */
@Tag(name="报警点位")
@RestController
@RequestMapping("/alarm/alarmPoint")
@Slf4j
@RequiredArgsConstructor
public class AlarmPointController extends BaseController<IAlarmPointService, AlarmPoint> {
    private final ISyncDfMdmDataService syncDfMdmDataService;

	/**
	 * 报警点位分页查询
	 * @author Rocye
	 * @since 2026.02.26
	 */
	@Operation(summary = "报警点位分页查询", description = "报警点位分页查询")
	@AutoDict
	@GetMapping({"/list/page"})
	public Result<IPage<AlarmPoint>> getPageList(AlarmPoint entity, HttpServletRequest req,
												 @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
												 @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
												 @RequestParam(name = "orderField",required = false) String orderField,
												 @RequestParam(name = "orderType",required = false) String orderType,
												 @RequestParam(name = "selectFields",required = false) String selectFields) {
		QueryWrapper<AlarmPoint> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
		//根据当前登录用户部门展示数据
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String orgCode = loginUser.getOrgCode();
		if (StringUtils.isNotEmpty(orgCode)) {
			queryWrapper.likeRight("factory_code", orgCode);
		}
		if (Strings.isNotEmpty(selectFields)) {
			List<String> selectFieldList = Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).toList();
			queryWrapper.select(selectFieldList);
		}
		if (Strings.isNotEmpty(orderType) && Strings.isNotEmpty(orderField)) {
			if ("desc".equals(orderType)) {
				queryWrapper.orderByDesc(StrUtil.toUnderlineCase(orderField));
			}
			if ("asc".equals(orderType)) {
				queryWrapper.orderByAsc(StrUtil.toUnderlineCase(orderField));
			}
		}
		queryWrapper.eq("tenant_id", req.getIntHeader("X-Tenant-Id"));
		Page<AlarmPoint> page = new Page<>(pageNo, pageSize);
		return Result.OK(this.baseService.page(page, queryWrapper));
	}

	/**
	 * 添加
	 */
	@AutoLog(value = "报警点位-添加")
	@Operation(summary="报警点位-添加")
	@RequiresPermissions("alarm:alarm_point:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AlarmPoint alarmPoint) {
		baseService.save(alarmPoint);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "报警点位-编辑")
	@Operation(summary="报警点位-编辑")
	@RequiresPermissions("alarm:alarm_point:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AlarmPoint alarmPoint) {
		baseService.updateById(alarmPoint);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "报警点位-通过id删除")
	@Operation(summary="报警点位-通过id删除")
	@RequiresPermissions("alarm:alarm_point:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("alarm:alarm_point:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AlarmPoint alarmPoint) {
        return super.exportXls(request, alarmPoint, AlarmPoint.class, "报警点位");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("alarm:alarm_point:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AlarmPoint.class);
    }

	/**
	 * 查询下拉选项列表
	 */
	@GetMapping("/list/options")
	public Result<List<AlarmPoint>> listOptions(AlarmPoint alarmPoint, HttpServletRequest req) {
		try {
			QueryWrapper<AlarmPoint> queryWrapper = QueryGenerator.initQueryWrapper(alarmPoint, req.getParameterMap());
			String selectFields = req.getParameter("selectFields");
			if (selectFields == null) {
				selectFields = "id,alarm_pi_code,alarm_name";
			}
			queryWrapper.select(selectFields);
			List<AlarmPoint> options = baseService.list(queryWrapper);
			return Result.ok(options);
		} catch (Exception e) {
			log.error("查询报警点位下拉选项列表失败", e);
			return Result.error("查询报警点位下拉选项列表失败");
		}
	}

	/**
	 * 重新转换报警点位的 factoryCode、deptCode、lineCode 和 deviceId
	 * @since 2026.02.07
	 */
	@AutoLog(value = "报警点位-重新转换报警点位映射")
	@Operation(summary="报警点位-重新转换报警点位映射")
	@GetMapping(value = "/refreshAlarmPointMapping")
	public Result<String> refreshAlarmPointMapping() {
		try {
			syncDfMdmDataService.refreshAlarmPointMapping();
			return Result.ok("重新转换");
		} catch (Exception e) {
			log.error("重新转换报警点位映射时发生错误: {}", e.getMessage(), e);
			return Result.error("重新转换报警点位映射时发生异常");
		}
	}

}
