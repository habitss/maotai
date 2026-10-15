package com.keytech.modules.alarm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.alarm.entity.AlarmPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.alarm.entity.AlarmTypes;
import com.keytech.modules.alarm.service.IAlarmTypesService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 报警点位类型
 * @author liu
 * @since 2026.01.26
 * */
@Tag(name="报警点位类型")
@RestController
@RequestMapping("/alarm/alarmTypes")
@Slf4j
public class AlarmTypesController extends BaseController<IAlarmTypesService, AlarmTypes> {

	/**
	 * 分页列表查询
	 * @param alarmTypes
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 */
	//@AutoLog(value = "报警点位类型-分页列表查询")
	@Operation(summary="报警点位类型-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<AlarmTypes>> queryPageList(AlarmTypes alarmTypes,
												   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												   HttpServletRequest req) {
		String hasQuery = req.getParameter("hasQuery");
		if(hasQuery != null && "true".equals(hasQuery)){
			QueryWrapper<AlarmTypes> queryWrapper =  QueryGenerator.initQueryWrapper(alarmTypes, req.getParameterMap());
			List<AlarmTypes> list = baseService.queryTreeListNoPage(queryWrapper);
			IPage<AlarmTypes> pageList = new Page<>(1, 10, list.size());
			pageList.setRecords(list);
			return Result.OK(pageList);
		}else{
			Integer parentId = alarmTypes.getParentId();
			if (parentId == null) {
				parentId = 0;
			}
			alarmTypes.setParentId(null);
			QueryWrapper<AlarmTypes> queryWrapper = QueryGenerator.initQueryWrapper(alarmTypes, req.getParameterMap());
			// 使用 eq 防止模糊查询
			queryWrapper.eq("parent_id", parentId);
			Page<AlarmTypes> page = new Page<AlarmTypes>(pageNo, pageSize);
			IPage<AlarmTypes> pageList = baseService.page(page, queryWrapper);
			return Result.OK(pageList);
		}
	}

	/**
	 * 加载节点的子数据
	 * @param pid
	 */
	@RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") Integer pid) {
		Result<List<SelectTreeModel>> result = new Result<>();
		try {
			List<SelectTreeModel> ls = baseService.queryListByPid(pid);
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}

	/**
	 * 加载一级节点/如果是同步 则所有数据
	 * @param async
	 * @param pid
	 */
	@RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async,
													  @RequestParam(name = "pid") Integer pid) {
		Result<List<SelectTreeModel>> result = new Result<>();
		try {
			List<SelectTreeModel> ls = baseService.queryListByPid(pid);
			if (!async) {
				loadAllChildren(ls);
			}
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}

	/**
	 * 递归求子节点 同步加载用到
	 * @param ls
	 */
	private void loadAllChildren(List<SelectTreeModel> ls) {
		for (SelectTreeModel tsm : ls) {
			List<SelectTreeModel> temp = baseService.queryListByPid(Integer.valueOf(tsm.getKey()));
			if (temp != null && temp.size() > 0) {
				tsm.setChildren(temp);
				loadAllChildren(temp);
			}
		}
	}

	/**
	 * 获取子数据
	 * @param alarmTypes
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "报警点位类型-获取子数据")
	@Operation(summary="报警点位类型-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<AlarmTypes>> queryPageList(AlarmTypes alarmTypes,HttpServletRequest req) {
		QueryWrapper<AlarmTypes> queryWrapper = QueryGenerator.initQueryWrapper(alarmTypes, req.getParameterMap());
		List<AlarmTypes> list = baseService.list(queryWrapper);
		IPage<AlarmTypes> pageList = new Page<>(1, 10, list.size());
		pageList.setRecords(list);
		return Result.OK(pageList);
	}

	/**
	 * 批量查询子节点
	 * @param parentIds 父ID（多个采用半角逗号分割）
	 * @return 返回 IPage
	 * @param parentIds
	 * @return
	 */
	//@AutoLog(value = "报警点位类型-批量获取子数据")
	@Operation(summary="报警点位类型-批量获取子数据")
	@GetMapping("/getChildListBatch")
	public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
		try {
			QueryWrapper<AlarmTypes> queryWrapper = new QueryWrapper<>();
			List<String> parentIdList = Arrays.asList(parentIds.split(","));
			queryWrapper.in("parent_id", parentIdList);
			List<AlarmTypes> list = baseService.list(queryWrapper);
			IPage<AlarmTypes> pageList = new Page<>(1, 10, list.size());
			pageList.setRecords(list);
			return Result.OK(pageList);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error("批量查询子节点失败：" + e.getMessage());
		}
	}

	/**
	 * 添加
	 * @param alarmTypes
	 * @return
	 */
	@AutoLog(value = "报警点位类型-添加")
	@Operation(summary="报警点位类型-添加")
	@RequiresPermissions("alarm:alarm_types:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AlarmTypes alarmTypes) {
		baseService.addAlarmTypes(alarmTypes);
		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 * @param alarmTypes
	 */
	@AutoLog(value = "报警点位类型-编辑")
	@Operation(summary="报警点位类型-编辑")
	@RequiresPermissions("alarm:alarm_types:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AlarmTypes alarmTypes) {
		baseService.updateAlarmTypes(alarmTypes);
		return Result.OK("编辑成功!");
	}

	/**
	 * 通过id删除
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报警点位类型-通过id删除")
	@Operation(summary="报警点位类型-通过id删除")
	@RequiresPermissions("alarm:alarm_types:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) Integer id) {
		baseService.deleteAlarmTypes(id);
		return Result.OK("删除成功!");
	}


	/**
	 * 通过id查询
	 * @param id
	 */
	//@AutoLog(value = "报警点位类型-通过id查询")
	@Operation(summary="报警点位类型-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<AlarmTypes> queryById(@RequestParam(name="id",required=true) Integer id) {
		AlarmTypes alarmTypes = baseService.getById(id);
		if(alarmTypes==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(alarmTypes);
	}

	/**
	 * 导出excel
	 */
	@RequiresPermissions("alarm:alarm_types:exportXls")
	@GetMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, AlarmTypes alarmTypes) {
		return super.exportXls(request, alarmTypes, AlarmTypes.class, "报警点位类型");
	}

	/**
	 * 通过excel导入数据
	 */
	@RequiresPermissions("alarm:alarm_types:importExcel")
	@PostMapping(value = "/importExcel")
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, AlarmTypes.class);
	}

	/**
	 * 查询下拉选项列表
	 */
	@GetMapping("/list/options")
	public Result<List<AlarmTypes>> listOptions(AlarmTypes alarmTypes, HttpServletRequest req) {
		try {
			QueryWrapper<AlarmTypes> queryWrapper = QueryGenerator.initQueryWrapper(alarmTypes, req.getParameterMap());
			String selectFields = req.getParameter("selectFields");
			if (selectFields == null) {
				selectFields = "id,type_name";
			}
			queryWrapper.select(selectFields);

			List<AlarmTypes> options = baseService.list(queryWrapper);
			return Result.ok(options);
		} catch (Exception e) {
			log.error("查询报警点位类型下拉选项列表失败", e);
			return Result.error("查询报警点位类型下拉选项列表失败");
		}
	}

}
