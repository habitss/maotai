package com.keytech.modules.maotai.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.maotai.workorder.entity.TransferRoute;
import com.keytech.modules.maotai.workorder.service.TransferRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 转酒路径配置管理
 * <p>
 * 工单管理>转酒路径配置:按车间+工艺+源罐/目标罐/卸车撬维护路径,
 * 工单创建的转酒路径下拉候选即来自本表(未配置则无候选,无代码兜底)。
 *
 * @author workorder
 * @since 2026-09-15
 */
@Tag(name = "转酒路径配置")
@RestController
@RequestMapping("/maotai/transferRoute")
@Slf4j
@RequiredArgsConstructor
public class TransferRouteController {

    private final TransferRouteService transferRouteService;

    @Operation(summary = "路径配置分页查询", description = "筛选:车间/工艺/源罐/目标罐/撬/启用")
    @GetMapping("/list/page")
    public Result<IPage<TransferRoute>> getPageList(@RequestParam(required = false) String workshopCode,
                                                    @RequestParam(required = false) String processType,
                                                    @RequestParam(required = false) String sourceTank,
                                                    @RequestParam(required = false) String targetTank,
                                                    @RequestParam(required = false) String skidCode,
                                                    @RequestParam(required = false) Integer enabled,
                                                    @RequestParam(required = false) String column,
                                                    @RequestParam(required = false) String order,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        QueryWrapper<TransferRoute> qw = new QueryWrapper<TransferRoute>().eq("del_flag", 0);
        qw.eq(workshopCode != null && !workshopCode.isEmpty(), "workshop_code", workshopCode);
        qw.eq(processType != null && !processType.isEmpty(), "process_type", processType);
        qw.like(sourceTank != null && !sourceTank.isEmpty(), "source_tank", sourceTank);
        qw.like(targetTank != null && !targetTank.isEmpty(), "target_tank", targetTank);
        qw.eq(skidCode != null && !skidCode.isEmpty(), "skid_code", skidCode);
        qw.eq(enabled != null, "enabled", enabled);
        if ("routeCode".equals(column)) {
            qw.orderBy(true, !"desc".equalsIgnoreCase(order), "route_code");
        } else {
            qw.orderByAsc("workshop_code").orderByAsc("process_type").orderByAsc("sort_no").orderByAsc("route_code");
        }
        return Result.OK(transferRouteService.page(new Page<>(pageNo, pageSize), qw));
    }

    @Operation(summary = "新增路径配置")
    @PostMapping("/add")
    public Result<TransferRoute> add(@RequestBody TransferRoute route) {
        return Result.OK(transferRouteService.saveOrUpdateRoute(route, currentUserName()));
    }

    @Operation(summary = "编辑路径配置")
    @PutMapping("/edit")
    public Result<TransferRoute> edit(@RequestBody TransferRoute route) {
        return Result.OK(transferRouteService.saveOrUpdateRoute(route, currentUserName()));
    }

    @Operation(summary = "删除路径配置", description = "逻辑删除")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam String id) {
        return Result.OK(transferRouteService.removeById(id));
    }

    /** 当前登录用户 realname(无登录上下文回退 system) */
    private String currentUserName() {
        try {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            return user != null && user.getRealname() != null ? user.getRealname() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}
