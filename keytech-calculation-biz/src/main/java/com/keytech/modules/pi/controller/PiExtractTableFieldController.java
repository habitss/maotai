package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.modules.pi.service.IPiExtractTableFieldService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.keytech.base.controller.BaseController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: PI-数据抽取配置字段表
 * @Author: HQ
 * @Date:   2025-08-26
 * @Version: V1.0
 */
@Tag(name="PI-数据抽取配置字段表")
@RestController
@RequestMapping("/pi/piExtractTableField")
@Slf4j
public class PiExtractTableFieldController extends BaseController<IPiExtractTableFieldService, PiExtractTableField> {
	@Autowired
	private IPiExtractTableFieldService piExtractTableFieldService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piExtractTableField
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI-数据抽取配置字段表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiExtractTableField>> queryPageList(PiExtractTableField piExtractTableField,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiExtractTableField> queryWrapper = QueryGenerator.initQueryWrapper(piExtractTableField, req.getParameterMap());
		Page<PiExtractTableField> page = new Page<PiExtractTableField>(pageNo, pageSize);
		IPage<PiExtractTableField> pageList = piExtractTableFieldService.page(page, queryWrapper);

		if(pageList != null && pageList.getTotal() == 0 && StrUtil.isNotBlank(piExtractTableField.getSourceTable())){
			//获取字段
//			List<ColumnInfoVO> fieldList = piExtractTableFieldService.getTableField(piExtractTableField.getSourceTable());
//			List<PiExtractTableField> list = new ArrayList<>();
//			for (ColumnInfoVO columnInfoVO : fieldList) {
//				PiExtractTableField piExtractTableFieldTmp = new PiExtractTableField();
//				piExtractTableFieldTmp.setSourceField(columnInfoVO.getColumnName());
//				piExtractTableFieldTmp.setSourceFieldName(columnInfoVO.getColumnComment());
//				list.add(piExtractTableFieldTmp);
//			}

			List<PiExtractTableField> list = new ArrayList<>();
			PiExtractTableField piExtractTableFieldTmp1 = new PiExtractTableField();
			piExtractTableFieldTmp1.setSourceField("f_01");
			piExtractTableFieldTmp1.setSourceFieldName("字段1");
			list.add(piExtractTableFieldTmp1);
			//字段2
			PiExtractTableField piExtractTableFieldTmp2 = new PiExtractTableField();
			piExtractTableFieldTmp2.setSourceField("f_02");
			piExtractTableFieldTmp2.setSourceFieldName("字段2");
			list.add(piExtractTableFieldTmp2);
			//字段3
			PiExtractTableField piExtractTableFieldTmp3 = new PiExtractTableField();
			piExtractTableFieldTmp3.setSourceField("f_03");
			piExtractTableFieldTmp3.setSourceFieldName("字段3");
			list.add(piExtractTableFieldTmp3);
			//字段4
			PiExtractTableField piExtractTableFieldTmp4 = new PiExtractTableField();
			piExtractTableFieldTmp4.setSourceField("f_04");
			piExtractTableFieldTmp4.setSourceFieldName("字段4");
			list.add(piExtractTableFieldTmp4);

			pageList.setRecords(list);
		}
		return Result.OK(pageList);
	}

	/**
	 * 获取目标表字段
	 * @return
	 */
	@Operation(summary="PI-数据抽取配置主表-获取目标表字段")
	@GetMapping(value = "/getTargetTableField")
	public Result<?> getTargetTableField(PiExtractTableField piExtractTableField) {
		return Result.OK(piExtractTableFieldService.getTargetTableField(piExtractTableField));
	}


	/**
	 *   添加
	 *
	 * @param piExtractTableField
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置字段表-添加")
	@Operation(summary="PI-数据抽取配置字段表-添加")
	@RequiresPermissions("pi:mes3_pi_extract_table_field:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiExtractTableField piExtractTableField) {
		piExtractTableFieldService.save(piExtractTableField);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piExtractTableField
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置字段表-编辑")
	@Operation(summary="PI-数据抽取配置字段表-编辑")
	@RequiresPermissions("pi:mes3_pi_extract_table_field:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiExtractTableField piExtractTableField) {
		piExtractTableFieldService.updateById(piExtractTableField);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置字段表-通过id删除")
	@Operation(summary="PI-数据抽取配置字段表-通过id删除")
	@RequiresPermissions("pi:mes3_pi_extract_table_field:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piExtractTableFieldService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置字段表-批量删除")
	@Operation(summary="PI-数据抽取配置字段表-批量删除")
	@RequiresPermissions("pi:mes3_pi_extract_table_field:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piExtractTableFieldService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI-数据抽取配置字段表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiExtractTableField> queryById(@RequestParam(name="id",required=true) String id) {
		PiExtractTableField piExtractTableField = piExtractTableFieldService.getById(id);
		if(piExtractTableField==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piExtractTableField);
	}


}
