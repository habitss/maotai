package com.keytech.modules.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Strings;
import com.keytech.base.controller.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.MicrosoftSsoConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 用户邮箱登录
 * @Author: jeecg-boot
 * @Date:   2025-05-16
 * @Version: V1.0
 */
@Tag(name="用户唯一部门")
@RestController
@RequestMapping("/mes3Email")
@Slf4j
public class EmailLoginController {
    @Autowired
    private ISysUserService sysUserService;
    @Resource
    private BaseCommonService baseCommonService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ISysTenantService sysTenantService;
    @Autowired
    private ISysDictService sysDictService;
    @Autowired
    private MicrosoftSsoConfig microsoftSsoConfig;
    @Autowired
    private ISysThirdAppConfigService appConfigService;
    @Value("${justauth.type.WECHAT_ENTERPRISE.tenant-id}")
    private String configTenantId;

    @RequestMapping(value = "/emailLogin", method = RequestMethod.POST)
    public Result<JSONObject> login(@RequestBody Map<String,Object> emailMap){
        Result<JSONObject> result = new Result<JSONObject>();
        //1. 校验用户是否有效
        //update-begin-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail,emailMap.get("email").toString());
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        //update-end-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
        result = sysUserService.checkUserIsEffective(sysUser);
        if(!result.isSuccess()) {
            return result;
        }
        //用户登录信息
        userInfo(sysUser, result);
        //update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
//		redisUtil.del(realKey);
        //update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        baseCommonService.addLog("用户名: " + sysUser.getUsername() + ",登录成功！", CommonConstant.LOG_TYPE_1, null,loginUser);
        //update-end--Author:wangshuai  Date:20200714  for：登录日志没有记录人员
        return result;
    }
    /**
     * 企业微信/钉钉 OAuth2登录
     *
     * @param source
     * @param state
     * @return
     */
    @GetMapping("/oauth2/{source}/login")
    public String oauth2LoginCallback(@PathVariable("source") String source, @RequestParam("state") String state, HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(value = "tenantId",required = false,defaultValue = "0") String tenantId) throws Exception {
        String url;
        //应用id为空，说明没有配置lowAppId
        if(oConvertUtils.isEmpty(tenantId)){
            return "租户编码未配置";
        }
        log.info("configTenantId==============={}",configTenantId);

        if (CommonConstant.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
            tenantId = Strings.isNullOrEmpty(configTenantId)?"0":configTenantId;

            //换成第三方app配置表
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.QYWX.getType());
            if(null == config){
                return "还未配置企业微信应用，请配置企业微信应用";
            }
            StringBuilder builder = new StringBuilder();
            // 构造企业微信OAuth2登录授权地址
            builder.append("https://open.weixin.qq.com/connect/oauth2/authorize");
            // 企业的CorpID
            builder.append("?appid=").append(config.getClientId());
            log.info("oauth2LoginCallback state==============={}",state);

            // 授权后重定向的回调链接地址，请使用urlencode对链接进行处理
            //String redirectUri = CommonUtils.getBaseUrl(request)  + "/sys/thirdLogin/oauth2/wechat_enterprise/callback?tenantId="+tenantId;;
            String redirectUri = state  + "/keytech-boot/sys/thirdLogin/oauth2/wechat_enterprise/callback?tenantId="+tenantId;;

            builder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
            // 返回类型，此时固定为：code
            builder.append("&response_type=code");
            // 应用授权作用域。
            // snsapi_base：静默授权，可获取成员的的基础信息（UserId与DeviceId）；
            // builder.append("&scope=snsapi_base");
            builder.append("&scope=snsapi_privateinfo");
            // 重定向后会带上state参数，长度不可超过128个字节
            builder.append("&state=").append(state);
            builder.append("&agentid=").append(config.getAgentId());

            // 终端使用此参数判断是否需要带上身份信息
            builder.append("#wechat_redirect");
            url = builder.toString();
        } else if (CommonConstant.DINGTALK.equalsIgnoreCase(source)) {
            //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]新建企业微信和钉钉配置表，通过租户模式隔离------------
            //换成第三方app配置表
            SysThirdAppConfig appConfig = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.DD.getType());
            if(null == appConfig){
                return "还未配置钉钉应用，请配置钉钉应用";
            }
            //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]新建企业微信和钉钉配置表，通过租户模式隔离------------
            StringBuilder builder = new StringBuilder();
            // 构造钉钉OAuth2登录授权地址
            builder.append("https://login.dingtalk.com/oauth2/auth");
            // 授权通过/拒绝后回调地址。
            // 注意 需要与注册应用时登记的域名保持一致。
            String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/oauth2/dingtalk/callback?tenantId="+tenantId;
            builder.append("?redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
            // 固定值为code。
            // 授权通过后返回authCode。
            builder.append("&response_type=code");
            // 步骤一中创建的应用详情中获取。
            // 企业内部应用：client_id为应用的AppKey。
            builder.append("&client_id=").append(appConfig.getClientId());
            // 授权范围，授权页面显示的授权信息以应用注册时配置的为准。
            // openid：授权后可获得用户userid
            builder.append("&scope=openid");
            // 跟随authCode原样返回。
            builder.append("&state=").append(state);
            //update-begin---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 钉钉无法登录------------
            builder.append("&prompt=").append("consent");
            //update-end---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 钉钉无法登录--------------
            url = builder.toString();
        }else if (CommonConstant.MICROSOFT.equalsIgnoreCase(source)) {
            //百威SSO登录
            StringBuilder builder = new StringBuilder();
//          https://login.microsoftonline.com/common/oauth2/v2.0/authorize?response_type=code
//			&client_id=0a9ea3d6-4479-4f67-95c6-fb98875fdc80
//			&redirect_uri=http://localhost:8089/tsm-auth/thirdLogin/microsoft/callback
//			&state=17b2d9476045c9bd5efde272976ce7dd
//			&response_mode=query
//			&scope=profile%20email%20openid%20offline_access
            builder.append("https://login.microsoftonline.com/").append(microsoftSsoConfig.getTenant()).append("/oauth2/v2.0/authorize?");
            builder.append("response_type=code");
            builder.append("&client_id=").append(microsoftSsoConfig.getClientId());
            builder.append("&redirect_uri=").append(microsoftSsoConfig.getRedirectUri());
            builder.append("&state=").append(state);
            builder.append("&_t=").append(System.currentTimeMillis());
            builder.append("&scope=user.read");
            url = builder.toString();
        } else {
            return "不支持的source";
        }
        log.info("oauth2 login url:" + url);
        return url;
    }
    /**
     * 用户信息
     *
     * @param sysUser
     * @param result
     * @return
     */
    private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result) {
        String syspassword = sysUser.getPassword();
        String username = sysUser.getUsername();
        // 获取用户部门信息
        JSONObject obj = new JSONObject();
        List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
        obj.put("departs", departs);
        if (departs == null || departs.size() == 0) {
            obj.put("multi_depart", 0);
        } else if (departs.size() == 1) {
            sysUserService.updateUserDepart(username, departs.get(0).getOrgCode(),sysUser.getLoginTenantId());
            obj.put("multi_depart", 1);
        } else {
            //查询当前是否有登录部门
            // update-begin--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
            SysUser sysUserById = sysUserService.getById(sysUser.getId());
            if(oConvertUtils.isEmpty(sysUserById.getOrgCode())){
                sysUserService.updateUserDepart(username, departs.get(0).getOrgCode(),sysUser.getLoginTenantId());
            }
            // update-end--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
            obj.put("multi_depart", 2);
        }
        // update-begin--Author:sunjianlei Date:20210802 for：获取用户租户信息
        String tenantIds = sysUser.getRelTenantIds();
        if (oConvertUtils.isNotEmpty(tenantIds)) {
            List<Integer> tenantIdList = Arrays.stream(tenantIds.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());;
            // 该方法仅查询有效的租户，如果返回0个就说明所有的租户均无效。
            List<SysTenant> tenantList = sysTenantService.queryEffectiveTenant(tenantIdList);
            if (tenantList.size() == 0) {
                result.error500("与该用户关联的租户均已被冻结，无法登录！");
                return result;
            } else {
                obj.put("tenantList", tenantList);
            }
        }
        // update-end--Author:sunjianlei Date:20210802 for：获取用户租户信息
        // 生成token
        String token = JwtUtil.sign(username, syspassword);
        // 设置token缓存有效时间
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
        obj.put("token", token);
        obj.put("userInfo", sysUser);
        obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
        result.setResult(obj);
        result.success("登录成功");
        return result;
    }
}
