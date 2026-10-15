package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.CopyStandardDto;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiStandardMaintenance;
import com.keytech.modules.pi.entity.PiStandardManage;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiStandardMaintenanceService;
import com.keytech.modules.pi.service.IPiStandardManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Description: pi标准维护
 * @Author: sjw
 * @Date: 2024/12/11 15:24
 */
@Tag(name="pi标准维护")
@RestController
@RequestMapping("/pi/standardMaintenance")
@Slf4j
public class PiStandardMaintenanceController extends BaseController<IPiStandardMaintenanceService, PiStandardMaintenance> {

    @Autowired
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;

    @Autowired
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;

    @Autowired
    private IPiStandardManageService piStandardManageService;

    /**
     * 分页列表查询
     * @param piStandardMaintenance
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "pi自定义报表字段-分页列表查询")
    @Operation(summary = "pi自定义报表字段-分页列表查询", description = "pi自定义报表字段-分页列表查询")
    @GetMapping(value = "/queryPageList")
    public Result<IPage<PiStandardMaintenance>> queryPageList(PiStandardMaintenance piStandardMaintenance,
                                                              @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
                                                              @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
                                                              @RequestParam(name = "orderField",required = false) String orderField,
                                                              @RequestParam(name = "orderType",required = false) String orderType,
                                                              @RequestParam(name = "selectFields",required = false) String selectFields,
                                                              HttpServletRequest req) {
        Page<PiStandardMaintenance> list = baseService.list(piStandardMaintenance, pageNo, pageSize, orderField, orderType, null, req);
        return Result.OK(list);
    }

    /**
     * 添加
     * @param piStandardMaintenance
     * @return
     */
    @AutoLog(value = "PI标准维护-添加")
    @Operation(summary = "PI标准维护-添加", description = "PI标准维护-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody PiStandardMaintenance piStandardMaintenance) {
        baseService.saveOrUpdate(piStandardMaintenance);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     * @param piStandardMaintenance
     * @return
     */
    @AutoLog(value = "PI标准维护-编辑")
    @Operation(summary = "PI标准维护-编辑", description = "PI标准维护-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody PiStandardMaintenance piStandardMaintenance) {
        baseService.updateById(piStandardMaintenance);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     * @param id
     * @return
     */
    @AutoLog(value = "PI标准维护-通过id删除")
    @Operation(summary = "PI标准维护-通过id删除", description = "PI标准维护-通过id删除")
    @DeleteMapping(value = "/deleteData")
    public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
        baseService.removeById(id);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }


    /**
     * 导出excel
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiStandardMaintenance piStandardMaintenance) {
        piStandardMaintenance.setDeptCode(null);
        return exportXls(request, piStandardMaintenance, PiStandardMaintenance.class, "PI标准维护");
    }

    /**
     * 通过excel导入数据
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "PI标准维护-导入")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return importExcelGenCode(request, response, PiStandardMaintenance.class);
    }

    private Result<?> importExcelGenCode(HttpServletRequest request, HttpServletResponse response,
                                         Class<PiStandardMaintenance> piStandardMaintenanceClass) throws IOException {
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                //获取上传文件中的数据
                List<PiStandardMaintenance> list = ExcelImportUtil.importExcel(file.getInputStream(), piStandardMaintenanceClass, params);

                String manageId = list.get(0).getManageId();
                String tableName = list.get(0).getTableName();

                //获取标准管理
                LambdaQueryWrapper<PiStandardManage> standardQueryWrapper = new LambdaQueryWrapper<>();
                standardQueryWrapper.eq(PiStandardManage::getId, manageId);
                standardQueryWrapper.eq(PiStandardManage::getTableName, tableName);
                PiStandardManage piStandardManage = piStandardManageService.getOne(standardQueryWrapper);
                if(null == piStandardManage){
                    errorLines++;
                    errorMessage.add("标准与表名不匹配。");
                }else{
                    //获取对应的表字段数据
                    LambdaQueryWrapper<PiCustomizeTableField> fieldQueryWrapper = new LambdaQueryWrapper<>();
                    fieldQueryWrapper.eq(PiCustomizeTableField::getTableName, tableName);
                    List<PiCustomizeTableField> fieldList = piCustomizeTableFieldService.list(fieldQueryWrapper);

                    //获取该标准已经维护过的pi字段
                    LambdaQueryWrapper<PiStandardMaintenance> maintenanceQueryWrapper = new LambdaQueryWrapper<>();
                    maintenanceQueryWrapper.eq(PiStandardMaintenance::getTableName, tableName);
                    maintenanceQueryWrapper.eq(PiStandardMaintenance::getManageId, manageId);
                    List<PiStandardMaintenance> maintenanceList = baseService.list(maintenanceQueryWrapper);

                    //需要存储的数据
                    List<PiStandardMaintenance> saveList = new ArrayList<>();
                    //数据比较
                    for(int i=0; i<list.size(); i++){
                        PiStandardMaintenance piStandardMaintenance = list.get(i);
                        piStandardMaintenance.setTableName(tableName);
                        piStandardMaintenance.setManageId(manageId);
                        //校验指标类型
                        if(StrUtil.isNotBlank(piStandardMaintenance.getIndicatorType())
                                && !"1".equals(piStandardMaintenance.getIndicatorType())
                                && !"2".equals(piStandardMaintenance.getIndicatorType())){
                            errorLines++;
                            int lineNumber = i + 1;
                            errorMessage.add("第 " + lineNumber + "行， 指标类型：【" + piStandardMaintenance.getIndicatorType() + "】不存在。");
                            continue;
                        }

                        //校验字段是否存在
                        boolean isField = false;
                        for(PiCustomizeTableField customizeTableField : fieldList){
                            if(customizeTableField.getFieldName().equals(piStandardMaintenance.getFieldName())){
                                if(null != customizeTableField.getSort()){
                                    piStandardMaintenance.setSort(Double.valueOf(customizeTableField.getSort()));
                                }
                                piStandardMaintenance.setFieldText(customizeTableField.getName());
                                isField = true;
                                break;
                            }
                        }
                        if(!isField){
                            errorLines++;
                            int lineNumber = i + 1;
                            errorMessage.add("第 " + lineNumber + "行， 表字段：【" + piStandardMaintenance.getFieldText() + "】不存在报表中。");
                            continue;
                        }

                        //校验PI维护数据
                        boolean isMaintenance = false;
                        for(PiStandardMaintenance maintenance : maintenanceList){
                            //判断ID与已录入PI数据不同，但是字段相同
                            if(!maintenance.getId().equals(piStandardMaintenance.getId())
                                    && maintenance.getFieldName().equals(piStandardMaintenance.getFieldName())){
                                isMaintenance = true;
                                errorLines++;
                                int lineNumber = i + 1;
                                errorMessage.add("第 " + lineNumber + "行， 表字段：【" + piStandardMaintenance.getFieldText() + "】已在PI数据中录入过。");
                                break;
                            }
                        }
                        if(isMaintenance){
                            continue;
                        }

                        //校验导入数据是否有重复
                        boolean isRepeat = false;
                        for(PiStandardMaintenance saveObj : saveList){
                            //判断ID与已录入PI数据不同，但是字段相同
                            if(saveObj.getFieldName().equals(piStandardMaintenance.getFieldName())){
                                isRepeat = true;
                                errorLines++;
                                int lineNumber = i + 1;
                                errorMessage.add("第 " + lineNumber + "行， 字段名在导入表格中重复。");
                                break;
                            }
                        }
                        if(isRepeat){
                            continue;
                        }
                        successLines++;
                        saveList.add(piStandardMaintenance);
                    }
                    long start = System.currentTimeMillis();
                    baseService.saveOrUpdateBatch(saveList);
                    log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }


    /**
     * 获取标准值
     * @return
     */
    @AutoLog(value = "获取标准值")
    @Operation(summary = "获取标准值", description = "获取标准值")
    @GetMapping(value = "/queryStandard")
    public Result<?> queryStandard(@RequestParam(name="tableName") String tableName, @RequestParam(name="fieldName") String fieldName, @RequestParam(required = false) String dataId) {
        List<PiStandardMaintenance> list = new ArrayList<>();
        if(StrUtil.isNotBlank(tableName) && StrUtil.isNotBlank(fieldName)){
            PiStandardMaintenance piStandardMaintenance = new PiStandardMaintenance();
            piStandardMaintenance.setTableName(tableName);
            piStandardMaintenance.setFieldName(fieldName);
            piStandardMaintenance.setId(dataId); //数据id
            list = iPiStandardMaintenanceService.queryStandard(piStandardMaintenance);
        }
        return Result.OK(list);
    }

    /***
     * 批量更新/新增
     */
    @AutoLog(value = "标准批新增/更新")
    @Operation(summary = "标准批新增/更新", description = "标准批新增/更新")
    @RequestMapping(value = "/batchEditVal", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> batchEditVal(@RequestBody List<PiStandardMaintenance> maintenanceList) {

        Map<String,String> mapR= baseService.batchEditVal(maintenanceList);
        if("0".equals(mapR.get("code"))){
            return Result.OK("保存成功!");
        }else if("3".equals(mapR.get("code"))){
            return Result.error("无可修改的数据!");
        }else{
            return Result.error("保存失败!");
        }
    }

    /**
     *  批量删除
     *
     * @return
     */
    @AutoLog(value = "PI标准-批量删除")
    @Operation(summary = "PI标准-批量删除", description = "pi报表-批量删除")
    @DeleteMapping(value = "/batchDelete")
    public Result<String> batchDelete(@RequestParam(name="data",required=true) String data) {
        boolean isOk = baseService.deleteDataBatch(data);
        if(isOk){
            return Result.OK("删除成功!");
        }else{
            return Result.error("删除失败!");
        }

    }

    /**
     * 复制标准
     */
    @AutoLog(value = "复制标准")
    @Operation(summary = "复制标准", description = "复制标准")
    @PostMapping(value = "/copyStandard")
    public Result<String> copyStandard(@RequestBody CopyStandardDto copyStandardDto) {
        boolean isOk = baseService.copyStandard(copyStandardDto);
        if(isOk){
            return Result.OK(PiCommonConstants.OPERATION_SUCCESS);
        }else{
            return Result.error(PiCommonConstants.OPERATION_FAIL);
        }
    }

}
