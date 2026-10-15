package com.keytech.modules.line.efficiency.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecord;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;

 /**
 * @Description: 线效率-线效状态记录表
 * @Author: jeecg-boot
 * @Date:   2025-06-04
 * @Version: V1.0
 */
@Tag(name="线效率-线效状态记录表")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencyStateRecord")
@Slf4j
public class LineEfficiencyStateRecordController extends BaseController<ILineEfficiencyStateRecordService, LineEfficiencyStateRecord> {
	@Autowired
	private ILineEfficiencyStateRecordService lineEfficiencyStateRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencyStateRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="线效率-线效状态记录表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<LineEfficiencyStateRecord>> queryPageList(LineEfficiencyStateRecord lineEfficiencyStateRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		//产线名称设置模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyStateRecord.getLineName())){
			lineEfficiencyStateRecord.setLineName("*"+lineEfficiencyStateRecord.getLineName()+"*");
		}

		//产线名称设置模糊查询
		if(StringUtils.isNoneBlank(lineEfficiencyStateRecord.getSbName())){
			lineEfficiencyStateRecord.setSbName("*"+lineEfficiencyStateRecord.getSbName()+"*");
		}
        QueryWrapper<LineEfficiencyStateRecord> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencyStateRecord, req.getParameterMap());
		Page<LineEfficiencyStateRecord> page = new Page<LineEfficiencyStateRecord>(pageNo, pageSize);
		IPage<LineEfficiencyStateRecord> pageList = lineEfficiencyStateRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param lineEfficiencyStateRecord
	 * @return
	 */
	@AutoLog(value = "线效率-线效状态记录表-添加")
	@Operation(summary="线效率-线效状态记录表-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencyStateRecord lineEfficiencyStateRecord) {
		lineEfficiencyStateRecordService.save(lineEfficiencyStateRecord);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencyStateRecord
	 * @return
	 */
	@AutoLog(value = "线效率-线效状态记录表-编辑")
	@Operation(summary="线效率-线效状态记录表-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencyStateRecord lineEfficiencyStateRecord) {
		lineEfficiencyStateRecordService.updateById(lineEfficiencyStateRecord);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "线效率-线效状态记录表-通过id删除")
	@Operation(summary="线效率-线效状态记录表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencyStateRecordService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "线效率-线效状态记录表-批量删除")
	@Operation(summary="线效率-线效状态记录表-批量删除")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencyStateRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="线效率-线效状态记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencyStateRecord> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencyStateRecord lineEfficiencyStateRecord = lineEfficiencyStateRecordService.getById(id);
		if(lineEfficiencyStateRecord==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencyStateRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param lineEfficiencyStateRecord
    */
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_state_record_sku:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, LineEfficiencyStateRecord lineEfficiencyStateRecord) {
        //排除id条件
        lineEfficiencyStateRecord.setId(null);
        // 与queryPageList保持一致：产线名称、设备名称设置模糊查询
        if(StringUtils.isNoneBlank(lineEfficiencyStateRecord.getLineName())){
            lineEfficiencyStateRecord.setLineName("*"+lineEfficiencyStateRecord.getLineName()+"*");
        }
        if(StringUtils.isNoneBlank(lineEfficiencyStateRecord.getSbName())){
            lineEfficiencyStateRecord.setSbName("*"+lineEfficiencyStateRecord.getSbName()+"*");
        }
        return super.exportXls(request, lineEfficiencyStateRecord, LineEfficiencyStateRecord.class, "送总部OPI拆分");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, LineEfficiencyStateRecord.class);
    }

}
