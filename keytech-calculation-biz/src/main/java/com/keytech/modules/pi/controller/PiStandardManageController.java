package com.keytech.modules.pi.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.entity.PiStandardMaintenance;
import com.keytech.modules.pi.entity.PiStandardManage;
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
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: PI标准管理
 * @Author: sjw
 * @Date: 2024/12/11 15:25
 */
@Tag(name="PI标准管理")
@RestController
@RequestMapping("/pi/standardManager")
@Slf4j
public class PiStandardManageController extends BaseController<IPiStandardManageService, PiStandardManage> {

    @Autowired
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;

    /**
     * 分页列表查询
     * @param piStandardManage
     * @param pageNo
     * @param pageSize
     * @param orderField
     * @param orderType
     * @param selectFields
     * @param req
     * @return
     */
    @AutoLog(value = "PI标准管理-分页列表查询")
    @Operation(summary = "PI标准管理-分页列表查询", description = "PI标准管理-分页列表查询")
    @GetMapping(value = "/queryPageList")
    public Result<IPage<PiStandardManage>> queryPageList(PiStandardManage piStandardManage,
                                                         @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
                                                         @RequestParam(name = "orderField",required = false) String orderField,
                                                         @RequestParam(name = "orderType",required = false) String orderType,
                                                         @RequestParam(name = "selectFields",required = false) String selectFields,
                                                         HttpServletRequest req) {
        QueryWrapper<PiStandardManage> queryWrapper = QueryGenerator.initQueryWrapper(piStandardManage, req.getParameterMap());
        if(StrUtil.isNotBlank(piStandardManage.getFieldName())){
            queryWrapper.exists(
                    " select 1 from mes3_pi_standard_maintenance a where mes3_pi_standard_manage.id = a.manage_id " +
                            " and a.field_name = '" + piStandardManage.getFieldName()+"' and a.del_flag = 0 "
            );
        }
        Page<PiStandardManage> page = new Page<>(pageNo, pageSize);
        IPage<PiStandardManage> list = baseService.page(page, queryWrapper);
        return Result.OK(list);
    }

    /**
     * 添加
     * @param piStandardManage
     * @return
     */
    @AutoLog(value = "PI标准管理-添加")
    @Operation(summary = "PI标准管理-添加", description = "PI标准管理-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody PiStandardManage piStandardManage) {
        baseService.save(piStandardManage);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     * @param piStandardManage
     * @return
     */
    @AutoLog(value = "PI标准管理-编辑")
    @Operation(summary = "PI标准管理-编辑", description = "PI标准管理-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody PiStandardManage piStandardManage) {
        //根据id修改指定数据
        baseService.updateById(piStandardManage);

        //根据customizeId修改重算时间字段
        LambdaUpdateWrapper<PiStandardManage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PiStandardManage::getCustomizeId, piStandardManage.getCustomizeId());
        updateWrapper.set(PiStandardManage::getCalcTimeField, piStandardManage.getCalcTimeField());
        baseService.update(updateWrapper);

        //同步更新子表manageName
        if (StrUtil.isNotBlank(piStandardManage.getStandardName()) && StrUtil.isNotBlank(piStandardManage.getId())) {
            LambdaUpdateWrapper<PiStandardMaintenance> maintenanceUpdateWrapper = new LambdaUpdateWrapper<>();
            maintenanceUpdateWrapper.eq(PiStandardMaintenance::getManageId, piStandardManage.getId());
            maintenanceUpdateWrapper.set(PiStandardMaintenance::getManageName, piStandardManage.getStandardName());
            iPiStandardMaintenanceService.update(maintenanceUpdateWrapper);
        }
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     * @param id
     * @return
     */
    @AutoLog(value = "PI标准管理-通过id删除")
    @Operation(summary = "PI标准管理-通过id删除", description = "PI标准管理-通过id删除")
    @DeleteMapping(value = "/deleteData")
    public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
        baseService.removeById(id);
        iPiStandardMaintenanceService.remove(new LambdaQueryWrapper<PiStandardMaintenance>()
                .eq(PiStandardMaintenance::getManageId, id)
        );
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }

    /**
     * 获取最大排序值
     * @param customizeId
     * @return
     */
    @AutoLog(value = "PI标准管理-获取最大排序")
    @Operation(summary = "PI标准管理-获取最大排序", description = "PI标准管理-获取最大排序")
    @GetMapping(value = "/getMaxSort")
    public Result<Double> getMaxSort(@RequestParam(name = "customizeId") String customizeId) {
        return Result.OK(baseService.getMaxSort(customizeId));
    }


    /**
     * 导出excel
     *
     * @param request  HTTP请求对象
     * @param piStandardManage PI标准管理实体对象，用于传递查询条件
     * @return ModelAndView 返回Excel文件视图
     */
    @AutoLog(value = "PI标准管理-导出excel")
    @Operation(summary = "PI标准管理-导出excel", description = "PI标准管理-导出excel")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiStandardManage piStandardManage) {
        return exportXls(request, piStandardManage, PiStandardManage.class, "PI标准管理");
    }

    /**
     * 通过excel导入数据
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @return 导入结果
     */
    @Operation(summary = "通过excel导入数据")
    @PostMapping(value = "/importExcel")
    @Transactional
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return importExcelGenCode(request, response, PiStandardManage.class);
    }

    /**
     * 导入数据处理
     * @param request
     * @param response
     * @param piStandardManageClass
     * @return
     * @throws IOException
     */
    private Result<?> importExcelGenCode(HttpServletRequest request, HttpServletResponse response,
                                         Class<PiStandardManage> piStandardManageClass) throws IOException {
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
                List<PiStandardManage> list = ExcelImportUtil.importExcel(file.getInputStream(), piStandardManageClass, params);

                //关联表名
                String tableName = list.get(0).getTableName();
                //重算字段
                String calcTimeField = list.get(0).getCalcTimeField();

                //获取标准是否存在
                LambdaQueryWrapper<PiStandardManage> standardQueryWrapper = new LambdaQueryWrapper<>();
                standardQueryWrapper.eq(PiStandardManage::getTableName, tableName);
                List<PiStandardManage> piStandardManage = baseService.list(standardQueryWrapper);
                if(CollectionUtil.isNotEmpty(piStandardManage)){
                    //如果标准已存在，统一重算字段
                    calcTimeField = piStandardManage.get(0).getCalcTimeField();
                }

                if(StrUtil.isBlank(calcTimeField)){
                    errorLines++;
                    errorMessage.add("第一行数据重算时间字段不能为空!");
                } else if (StrUtil.isBlank(tableName)){
                    errorLines++;
                    errorMessage.add("第一行数据关联的数据库表名字段不能为空!");
                } else if (StrUtil.isBlank(list.get(0).getCustomizeId())){
                    errorLines++;
                    errorMessage.add("第一行数据PI基础数据表Id字段不能为空!");
                } else {
                    for (PiStandardManage p : list){
                        p.setCalcTimeField(calcTimeField);
                        p.setTableName(tableName);
                        p.setConditionJson(null);
                        p.setConditionText(null);
                    }
                    successLines = list.size();
                    baseService.saveOrUpdateBatch(list);
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
     * 复制PI标准
     * @param id
     * @return
     */
    @AutoLog(value = "PI标准管理-复制标准")
    @Operation(summary = "PI标准管理-复制标准", description = "PI标准管理-复制标准")
    @GetMapping(value = "/standardCopy")
    public Result<Double> standardCopy(@RequestParam(name="id",required=true) String id) {
        Boolean isOk = baseService.standardCopy(id);
        if(isOk){
            return Result.OK("复制成功");
        }else{
            return Result.error("复制失败");
        }

    }
}
