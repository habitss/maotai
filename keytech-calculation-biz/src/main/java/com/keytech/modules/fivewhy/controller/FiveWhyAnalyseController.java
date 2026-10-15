package com.keytech.modules.fivewhy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.fivewhy.dto.FiveWhyAnalyseDto;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * @Description: 5Why分析
 * @Author: jeecg-boot
 * @Date:   2025-01-20
 * @Version: V1.0
 */
@Tag(name="5Why分析")
@RestController
@RequestMapping("/five/why/fiveWhyAnalyse")
@Slf4j
public class FiveWhyAnalyseController extends BaseController<IFiveWhyAnalyseService, FiveWhyAnalyse> {
	@Autowired
	private IFiveWhyAnalyseService fiveWhyAnalyseService;
	
	/**
	 * 分页列表查询
	 *
	 * @param fiveWhyAnalyse
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
    @Operation(summary="5Why分析-分页列表查询")
    @GetMapping(value = "/queryPageList")
    public Result<IPage<FiveWhyAnalyse>> queryPageList(FiveWhyAnalyse fiveWhyAnalyse,
                                                       @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                       @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                       @RequestParam(name = "orderField",required = false) String orderField,
                                                       @RequestParam(name = "orderType",required = false) String orderType,
                                                       @RequestParam(name = "selectFields",required = false) String selectFields,
                                                       HttpServletRequest req) {
        QueryWrapper<FiveWhyAnalyse> queryWrapper = new QueryWrapper<>();

        // 处理时间范围查询
        String createTimeBegin = req.getParameter("createTime_begin");
        String createTimeEnd = req.getParameter("createTime_end");
        if (createTimeBegin != null && !createTimeBegin.isEmpty()) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (createTimeEnd != null && !createTimeEnd.isEmpty()) {
            queryWrapper.le("create_time", createTimeEnd);
        }

        // 处理表名查询
        if (fiveWhyAnalyse.getTableCode() != null && !fiveWhyAnalyse.getTableCode().isEmpty()) {
            queryWrapper.eq("table_code", fiveWhyAnalyse.getTableCode());
        }

        // 处理字段名查询
        if (fiveWhyAnalyse.getFieldCode() != null && !fiveWhyAnalyse.getFieldCode().isEmpty()) {
            queryWrapper.eq("field_code", fiveWhyAnalyse.getFieldCode());
        }

        // 处理异常点的模糊查询
        String problemValue = null;
        if (fiveWhyAnalyse.getProblem() != null && !fiveWhyAnalyse.getProblem().isEmpty()) {
            problemValue = fiveWhyAnalyse.getProblem();
        } else if (req.getParameter("problem") != null && !req.getParameter("problem").isEmpty()) {
            problemValue = req.getParameter("problem");
        }

        if (problemValue != null && !problemValue.isEmpty()) {
            queryWrapper.like("problem", problemValue);
        }

        Page<FiveWhyAnalyse> page = new Page<FiveWhyAnalyse>(pageNo, pageSize);
        IPage<FiveWhyAnalyse> pageList = fiveWhyAnalyseService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

	 /**
	  * 查询
	  * @param fiveWhyAnalyse
	  * @param req
	  * @return
	  */
	 @Operation(summary="5Why分析-查询")
	 @GetMapping(value = "/queryData")
	 public Result<FiveWhyAnalyseDto> queryData(FiveWhyAnalyse fiveWhyAnalyse, HttpServletRequest req) {
		 return Result.OK(fiveWhyAnalyseService.queryData(fiveWhyAnalyse, req));
	 }
	
	/**
	 *   添加
	 *
	 * @param fiveWhyAnalyseDto
	 * @return
	 */
	@AutoLog(value = "5Why分析-添加")
	@Operation(summary="5Why分析-添加")
	@PostMapping(value = "/add")
	public Result<String> saveOrUpdateFiveWhy(@RequestBody FiveWhyAnalyseDto fiveWhyAnalyseDto) {
		fiveWhyAnalyseService.saveOrUpdateFiveWhy(fiveWhyAnalyseDto);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "5Why分析-通过id删除")
	@Operation(summary="5Why分析-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		fiveWhyAnalyseService.deleteFiveWhy(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}


    /**
     * 导出excel
     *
     * @param request
     * @param fiveWhyAnalyse
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FiveWhyAnalyse fiveWhyAnalyse) {
        return super.exportXls(request, fiveWhyAnalyse, FiveWhyAnalyse.class, "5Why分析");
    }

    /**
    * 导出excel
    *
    * @param request
    * @param fiveWhyAnalyse
    */
    @RequestMapping(value = "/exportXls_5W")
    public void exportXls_5W(HttpServletRequest request, FiveWhyAnalyse fiveWhyAnalyse) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        // 创建查询条件
        QueryWrapper<FiveWhyAnalyse> queryWrapper = new QueryWrapper<>();

        // 处理时间范围查询
        String createTimeBegin = request.getParameter("createTime_begin");
        String createTimeEnd = request.getParameter("createTime_end");
        if (createTimeBegin != null && !createTimeBegin.isEmpty()) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (createTimeEnd != null && !createTimeEnd.isEmpty()) {
            queryWrapper.le("create_time", createTimeEnd);
        }

        // 处理表名查询
        if (fiveWhyAnalyse.getTableCode() != null && !fiveWhyAnalyse.getTableCode().isEmpty()) {
            queryWrapper.eq("table_code", fiveWhyAnalyse.getTableCode());
        }

        // 处理字段名查询
        if (fiveWhyAnalyse.getFieldCode() != null && !fiveWhyAnalyse.getFieldCode().isEmpty()) {
            queryWrapper.eq("field_code", fiveWhyAnalyse.getFieldCode());
        }

        // 处理异常点的模糊查询
        String problemValue = null;
        if (fiveWhyAnalyse.getProblem() != null && !fiveWhyAnalyse.getProblem().isEmpty()) {
            problemValue = fiveWhyAnalyse.getProblem();
        } else if (request.getParameter("problem") != null && !request.getParameter("problem").isEmpty()) {
            problemValue = request.getParameter("problem");
        }

        if (problemValue != null && !problemValue.isEmpty()) {
            queryWrapper.like("problem", problemValue);
        }

        // 查询数据
        List<FiveWhyAnalyse> list = fiveWhyAnalyseService.list(queryWrapper);

        try {
            // 输出Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=5WhyAnalysis.xlsx");

            // 使用字节数组输出流作为中间缓冲区
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 创建工作簿并写入到字节数组输出流
            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建工作表
                Sheet sheet = workbook.createSheet("5Why分析数据");

                // 处理每条数据
                int rowIndex = 0;

                for (FiveWhyAnalyse data : list) {
                    // 1. 导出5W分析部分
                    // 解析reasonJson
                    List<Map> whyList = new ArrayList<>();
                    if (data.getReasonJson() != null && !data.getReasonJson().isEmpty()) {
                        try {
                            whyList = JSONUtil.toList(data.getReasonJson(), Map.class);
                        } catch (Exception e) {
                            log.error("解析reasonJson失败", e);
                        }
                    }

                    // 创建5W分析标题行
                    Row whyTitleRow = sheet.createRow(rowIndex);
                    whyTitleRow.createCell(0).setCellValue("问题");
                    whyTitleRow.createCell(1).setCellValue("问题描述");

                    // 动态生成5W列
                    int colIndex = 2;
                    for (int i = 0; i < whyList.size(); i++) {
                        whyTitleRow.createCell(colIndex++).setCellValue((i + 1) + ".为什么");
                        whyTitleRow.createCell(colIndex++).setCellValue((i + 1) + ".原因");
                    }
                    rowIndex++;

                    // 创建5W分析数据行
                    Row whyDataRow = sheet.createRow(rowIndex);
                    whyDataRow.createCell(0).setCellValue(data.getProblem());
                    whyDataRow.createCell(1).setCellValue(data.getProblemDesc());

                    // 填充5W数据
                    colIndex = 2;
                    for (Map why : whyList) {
                        whyDataRow.createCell(colIndex++).setCellValue(why.get("why") != null ? why.get("why").toString() : "");
                        whyDataRow.createCell(colIndex++).setCellValue(why.get("reason") != null ? why.get("reason").toString() : "");
                    }
                    rowIndex++;

                    // 2. 导出措施部分
                    // 解析measuresJson
                    List<Map> measureList = new ArrayList<>();
                    if (data.getMeasuresJson() != null && !data.getMeasuresJson().isEmpty()) {
                        try {
                            measureList = JSONUtil.toList(data.getMeasuresJson(), Map.class);
                        } catch (Exception e) {
                            log.error("解析measuresJson失败", e);
                        }
                    }

                    // 创建措施标题行
                    Row measureTitleRow = sheet.createRow(rowIndex);
                    measureTitleRow.createCell(0).setCellValue("问题");
                    measureTitleRow.createCell(1).setCellValue("原因");
                    measureTitleRow.createCell(2).setCellValue("问题解决");
                    measureTitleRow.createCell(3).setCellValue("提出人");
                    measureTitleRow.createCell(4).setCellValue("提出部门");
                    measureTitleRow.createCell(5).setCellValue("责任人");
                    measureTitleRow.createCell(6).setCellValue("责任部门");
                    rowIndex++;

                    // 填充措施数据
                    for (Map measure : measureList) {
                        Row measureDataRow = sheet.createRow(rowIndex);
                        measureDataRow.createCell(0).setCellValue(measure.get("problem") != null ? measure.get("problem").toString() : "");
                        measureDataRow.createCell(1).setCellValue(measure.get("reason") != null ? measure.get("reason").toString() : "");
                        measureDataRow.createCell(2).setCellValue(measure.get("solveProblem") != null ? measure.get("solveProblem").toString() : "");
                        measureDataRow.createCell(3).setCellValue(measure.get("putUserId") != null ? measure.get("putUserId").toString() : "");
                        measureDataRow.createCell(4).setCellValue(measure.get("putDeptId") != null ? measure.get("putDeptId").toString() : "");
                        measureDataRow.createCell(5).setCellValue(measure.get("dutyUserId") != null ? measure.get("dutyUserId").toString() : "");
                        measureDataRow.createCell(6).setCellValue(measure.get("dutyDeptId") != null ? measure.get("dutyDeptId").toString() : "");
                        rowIndex++;
                    }

                    // 添加空行分隔
                    rowIndex++;
                }

                // 自动调整列宽
                for (int i = 0; i < 10; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入到字节数组输出流
                workbook.write(baos);
            }

            // 获取响应输出流并写入数据
            OutputStream outputStream = response.getOutputStream();
            baos.writeTo(outputStream);
            outputStream.flush();

        } catch (Exception e) {
            log.error("导出Excel失败", e);
        }
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
        return super.importExcel(request, response, FiveWhyAnalyse.class);
    }

}
