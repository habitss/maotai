package com.keytech.modules.ems.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.ems.service.ISystemService;
import com.keytech.modules.ems.vo.TagOption;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.ems.entity.AggregationStatus;
import com.keytech.modules.ems.service.IAggregationStatusService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聚合计算状态表
 * @author yepeng
 * @since 2025.05.12
 * */
@Tag(name="聚合计算状态表")
@RestController
@RequestMapping("/ems/aggregationStatus")
@Slf4j
@RequiredArgsConstructor
public class AggregationStatusController extends BaseController<IAggregationStatusService, AggregationStatus> {
	private final ISystemService systemService;

	/**
	 * 分页列表查询
	 * @param aggregationStatus
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="聚合计算状态-分页列表查询")
	@GetMapping(value = "/listPage")
	public Result<IPage<AggregationStatus>> listPage(AggregationStatus aggregationStatus,
												  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												  HttpServletRequest req) {
		// 自定义查询规则
		Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
		// 自定义多选的查询规则为：LIKE_WITH_OR
		customeRuleMap.put("tagKind", QueryRuleEnum.LIKE_WITH_OR);
		QueryWrapper<AggregationStatus> queryWrapper = QueryGenerator.initQueryWrapper(aggregationStatus, req.getParameterMap(), customeRuleMap);
		Page<AggregationStatus> page = new Page<>(pageNo, pageSize);
		IPage<AggregationStatus> pageList = baseService.page(page, queryWrapper);
		List<TagOption> tagOptions = pageList.getRecords().stream().map(item -> new TagOption(item.getTagId(), null, item.getTagKind())).distinct().toList();
		Map<String, String> tagOptionMap = this.systemService.getTagOptionNameList(tagOptions).stream().collect(Collectors.toMap(t->t.getTagKind()+"|"+t.getTagId(), TagOption::getTagName));
		for (AggregationStatus item : pageList.getRecords()) {
			item.setTagName(tagOptionMap.get(item.getTagKind()+"|"+item.getTagId()));
		}
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param aggregationStatus
	 * @return
	 */
	@AutoLog(value = "聚合计算状态表-添加")
	@Operation(summary="聚合计算状态表-添加")
	@RequiresPermissions("ems:ems_aggregation_status:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AggregationStatus aggregationStatus) {
		baseService.save(aggregationStatus);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param aggregationStatus
	 * @return
	 */
	@AutoLog(value = "聚合计算状态表-编辑")
	@Operation(summary="聚合计算状态表-编辑")
	@RequiresPermissions("ems:ems_aggregation_status:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AggregationStatus aggregationStatus) {
		baseService.updateById(aggregationStatus);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "聚合计算状态表-通过id删除")
	@Operation(summary="聚合计算状态表-通过id删除")
	@RequiresPermissions("ems:ems_aggregation_status:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
    * 导出excel
    *
    * @param request
    * @param aggregationStatus
    */
    @RequiresPermissions("ems:ems_aggregation_status:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AggregationStatus aggregationStatus) {
        return super.exportXls(request, aggregationStatus, AggregationStatus.class, "聚合计算状态表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("ems:ems_aggregation_status:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AggregationStatus.class);
    }

}
