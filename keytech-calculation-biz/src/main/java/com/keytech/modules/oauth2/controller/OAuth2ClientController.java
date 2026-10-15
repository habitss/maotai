package com.keytech.modules.oauth2.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.oauth2.entity.OAuth2Client;
import com.keytech.modules.oauth2.service.IOAuth2ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 第三方OAuth2应用管理控制器（后台运营使用，标准Shiro鉴权，非开放接口）。
 * @author claude
 * @since 2026.08.19
 */
@Tag(name = "第三方OAuth2应用管理")
@RestController
@RequestMapping("/oauth2/client")
@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientController extends BaseController<IOAuth2ClientService, OAuth2Client> {

    /**
     * 分页列表查询。
     * 覆盖 BaseController 继承来的 /queryPageList：原版不按 X-Tenant-Id 过滤租户，
     * 会导致任意登录用户看到全部租户的第三方应用列表（参考 EquipmentLedgerController
     * 同类问题的修法）。不额外加 @RequiresPermissions("oauth2:mes3_oauth2_client:list")——
     * 该权限码要挂在子菜单本身，菜单管理界面的表单不支持给非按钮类型的菜单填写授权标识，
     * 配置成本高；查看列表本身风险有限，写操作仍然由 add/edit/delete 各自的按钮权限把关。
     */
    @Override
    @GetMapping("/queryPageList")
    public Result<IPage<OAuth2Client>> queryPageList(OAuth2Client entity, Integer pageNo, Integer pageSize,
                                                       String orderField, String orderType, String selectFields,
                                                       HttpServletRequest req) {
        QueryWrapper<OAuth2Client> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
        // 租户过滤
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            queryWrapper.eq("tenant_id", tenantId);
        }
        // 指定字段查询
        if (StringUtils.isNotEmpty(selectFields)) {
            List<String> selectFieldList = Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).toList();
            queryWrapper.select(selectFieldList);
        }
        // 排序
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(orderType)) {
            if ("desc".equalsIgnoreCase(orderType)) {
                queryWrapper.orderByDesc(StrUtil.toUnderlineCase(orderField));
            } else if ("asc".equalsIgnoreCase(orderType)) {
                queryWrapper.orderByAsc(StrUtil.toUnderlineCase(orderField));
            }
        }
        Page<OAuth2Client> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        return Result.OK(baseService.page(page, queryWrapper));
    }

    @AutoLog(value = "第三方OAuth2应用-添加")
    @Operation(summary = "第三方OAuth2应用-添加")
    @RequiresPermissions("oauth2:mes3_oauth2_client:add")
    @PostMapping(value = "/add")
    public Result<Map<String, String>> add(@RequestBody OAuth2Client client, HttpServletRequest req) {
        try {
            // 租户ID不需要前端传入，按项目惯例从请求头回填（参考 EquipmentLedgerController.add）
            if (client.getTenantId() == null) {
                int tenantId = req.getIntHeader("X-Tenant-Id");
                if (tenantId > 0) {
                    client.setTenantId(tenantId);
                }
            }
            Map<String, String> result = baseService.createClientWithSecret(client);
            return Result.OK("添加成功，请妥善保存client_secret！", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @AutoLog(value = "第三方OAuth2应用-编辑")
    @Operation(summary = "第三方OAuth2应用-编辑")
    @RequiresPermissions("oauth2:mes3_oauth2_client:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody OAuth2Client client) {
        // 编辑接口不允许直接改clientSecretHash，密钥变更一律走 resetSecret
        client.setClientSecretHash(null);
        client.setClientSecretOldHash(null);
        baseService.updateById(client);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "第三方OAuth2应用-通过id删除")
    @Operation(summary = "第三方OAuth2应用-通过id删除")
    @RequiresPermissions("oauth2:mes3_oauth2_client:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        baseService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "重置应用密钥")
    @RequiresPermissions("oauth2:mes3_oauth2_client:edit")
    @GetMapping("/resetSecret/{clientId}")
    public Result<String> resetSecret(@PathVariable String clientId) {
        try {
            String newSecret = baseService.resetClientSecret(clientId);
            return Result.OK("密钥重置成功，请妥善保存新密钥", newSecret);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // 以下方法覆盖 BaseController 继承来的通用写接口。
    // 这些接口默认只受 Shiro“已登录”校验，不带 @RequiresPermissions，
    // 且会绕过 add/edit 中对 clientSecretHash 的生成与保护逻辑（能让任意已登录用户
    // 直接读写密钥哈希字段）。此处全部禁用，强制统一走 add/edit/delete/resetSecret。
    // ---------------------------------------------------------------------

    @Override
    @PostMapping
    public Result<Void> save(@RequestBody OAuth2Client entity) {
        return Result.error("请使用 /oauth2/client/add 接口创建应用（会自动生成并哈希 client_secret）");
    }

    @Override
    @PutMapping
    public Result<Void> updateById(@RequestBody OAuth2Client entity) {
        return Result.error("请使用 /oauth2/client/edit 接口编辑应用（禁止直接篡改密钥字段）");
    }

    @Override
    @PostMapping("/saveBatch")
    public Result<Void> saveBatch(@RequestBody List<OAuth2Client> entityList) {
        return Result.error("不支持批量创建，请使用 /oauth2/client/add 逐个创建应用");
    }

    @Override
    @DeleteMapping("/delBatch")
    public Result<Void> delBatch(@RequestBody List<Long> ids) {
        return Result.error("请使用 /oauth2/client/delete 接口删除应用");
    }
}
