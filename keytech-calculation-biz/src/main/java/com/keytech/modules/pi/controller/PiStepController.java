package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.service.IPiBasePointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import com.keytech.modules.pi.service.IPiStepService;
import com.keytech.modules.pi.entity.PiStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 步骤管理
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Tag(name="步骤管理")
@RestController
@RequestMapping("/pi/piStep")
public class PiStepController extends BaseController<IPiStepService,PiStep> {


  @Autowired
  private IPiStepService piStepService;

     /**
      * 分页列表查询
      * @param piStep
      * @param pageNo
      * @param pageSize
      * @param orderField
      * @param orderType
      * @param selectFields
      * @param req
      * @return
      */
   @AutoLog(value = "步骤维护-分页列表查询")
   @Operation(summary = "步骤维护-分页列表查询", description = "步骤维护-分页列表查询")
   @GetMapping(value = "/listData")
   public Result<IPage<PiStep>> queryPageList(PiStep piStep,
                                                   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
                                                   @RequestParam(name = "orderField",required = false) String orderField,
                                                   @RequestParam(name = "orderType",required = false) String orderType,
                                                   @RequestParam(name = "selectFields",required = false) String selectFields,
                                                   HttpServletRequest req) {
    if(StrUtil.isNotBlank(piStep.getStepNum())){piStep.setStepNum("*"+piStep.getStepNum()+"*");}
    if(StrUtil.isNotBlank(piStep.getStepName())){piStep.setStepName("*"+piStep.getStepName()+"*");}
    if(StrUtil.isNotBlank(piStep.getQueryDept())){piStep.setDeptCode(piStep.getQueryDept());}
    Page<PiStep> list = baseService.list(piStep, pageNo, pageSize, orderField, orderType, null, req);
    return Result.OK(list);
   }

    /**
     *   添加
     *
     * @param piStep
     * @return
     */
    @AutoLog(value = "步骤维护-添加")
    @Operation(summary = "步骤维护-添加", description = "步骤维护-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody PiStep piStep) {
        baseService.save(piStep);
        return Result.OK("添加成功！");
    }

  /**
   *  编辑
   *
   * @param piStep
   * @return
   */
  @AutoLog(value = "步骤维护-编辑")
  @Operation(summary = "步骤维护-编辑", description = "步骤维护-编辑")
  @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
  public Result<String> edit(@RequestBody PiStep piStep) {
   piStepService.updateById(piStep);
   return Result.OK("编辑成功!");
  }

  /**
   *   通过id删除
   *
   * @param id
   * @return
   */
  @AutoLog(value = "步骤维护-通过id删除")
  @Operation(summary = "步骤维护-通过id删除", description = "步骤维护-通过id删除")
  @DeleteMapping(value = "/deleteData")
  public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
   piStepService.removeById(id);
   return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
  }

  /**
   *  批量删除
   *
   * @param ids
   * @return
   */
  @AutoLog(value = "步骤维护-批量删除")
  @Operation(summary = "步骤维护-批量删除", description = "步骤维护-批量删除")
  @DeleteMapping(value = "/deleteDataBatch")
  public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
   piStepService.removeByIds(Arrays.asList(ids.split(",")));
   return Result.OK("批量删除成功!");
  }


  /**
   * 点位获取步骤
   * pointCode 点位编码
   */
  @Operation(summary = "点位获取步骤", description = "点位获取步骤")
  @GetMapping(value = "/getPointStep")
  public Result<?> getPointStep(@RequestParam(name="pointCode",required=true) String pointCode) {
    List<PiStep> list = piStepService.getPointStep(pointCode);
    return Result.OK(list);
  }

}
