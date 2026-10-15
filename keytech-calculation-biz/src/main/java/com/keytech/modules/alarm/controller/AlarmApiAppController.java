package com.keytech.modules.alarm.controller;

import com.keytech.base.controller.BaseController;
import com.keytech.modules.alarm.entity.AlarmApiApp;
import com.keytech.modules.alarm.service.IAlarmApiAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.Map;

/**
 * 报警应用管理控制器
 * @author yepeng
 * @since 2026.02.08
 */
@Tag(name = "报警应用管理")
@RestController
@RequestMapping("/alarm/apiApp")
@Slf4j
@RequiredArgsConstructor
public class AlarmApiAppController extends BaseController<IAlarmApiAppService, AlarmApiApp> {

    /**
     * 添加
     */
    @AutoLog(value = "报警接口应用表-添加")
    @Operation(summary="报警接口应用表-添加")
    @RequiresPermissions("alarm:alarm_api_app:add")
    @PostMapping(value = "/add")
    public Result<Map<String, String>> add(@RequestBody AlarmApiApp alarmApiApp) {
        try {
            Map<String, String> result = baseService.createAppWithSecret(alarmApiApp);
            return Result.OK("添加成功，请妥善保存appSecret！", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 编辑
     */
    @AutoLog(value = "报警接口应用表-编辑")
    @Operation(summary="报警接口应用表-编辑")
    @RequiresPermissions("alarm:alarm_api_app:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody AlarmApiApp alarmApiApp) {
        baseService.updateById(alarmApiApp);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "报警接口应用表-通过id删除")
    @Operation(summary="报警接口应用表-通过id删除")
    @RequiresPermissions("alarm:alarm_api_app:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id") String id) {
        baseService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 导出excel
     */
    @RequiresPermissions("alarm:alarm_api_app:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AlarmApiApp alarmApiApp) {
        return super.exportXls(request, alarmApiApp, AlarmApiApp.class, "报警接口应用表");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("alarm:alarm_api_app:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AlarmApiApp.class);
    }

    /**
     * 重置应用密钥
     * @param appId 应用标识
     * @return 新的应用密钥
     */
    @GetMapping("/resetSecret/{appId}")
    public Result<String> resetSecret(@PathVariable String appId) {
        try {
            String newSecret = baseService.resetAppSecret(appId);
            return Result.OK("密钥重置成功，请妥善保存新密钥", newSecret);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清理过期的旧密钥
     * @param appId 应用标识
     * @return 是否清理成功
     */
    @GetMapping("/cleanOldSecret/{appId}")
    public Result<Boolean> cleanOldSecret(@PathVariable String appId) {
        try {
            boolean result = baseService.cleanExpiredOldSecret(appId);
            if (result) {
                return Result.OK("清理成功", true);
            } else {
                return Result.OK("无需清理或清理失败", false);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
