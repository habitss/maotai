import { defHttp } from '/@/utils/http/axios';
import { LoginParams, LoginResultModel, GetUserInfoModel } from './model/userModel';

import { ErrorMessageMode } from '/#/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { setAuthCache } from '/@/utils/auth';
import { TOKEN_KEY } from '/@/enums/cacheEnum';
import { router } from '/@/router';
import { PageEnum } from '/@/enums/pageEnum';
import { ExceptionEnum } from "@/enums/exceptionEnum";

const { createErrorModal } = useMessage();
enum Api {
  Login = '/mes3_sys/login',
  phoneLogin = '/sys/phoneLogin',
  Logout = '/sys/logout',
  GetUserInfo = '/sys/user/getUserInfo',
  // 获取系统权限
  // 1、查询用户拥有的按钮/表单访问权限
  // 2、所有权限
  // 3、系统安全模式
  GetPermCode = '/sys/permission/getPermCode',
  //新加的获取图形验证码的接口
  getInputCode = '/sys/randomImage',
  //获取短信验证码的接口
  getCaptcha = '/sys/sms',
  //注册接口
  registerApi = '/sys/user/register',
  //校验用户接口
  checkOnlyUser = '/sys/user/checkOnlyUser',
  //SSO登录校验
  validateCasLogin = '/sys/cas/client/validateLogin',
  //校验手机号
  phoneVerify = '/sys/user/phoneVerification',
  //修改密码
  passwordChange = '/sys/user/passwordChange',
  //第三方登录
  thirdLogin = '/sys/thirdLogin/getLoginUser',
  //第三方登录
  getThirdCaptcha = '/sys/thirdSms',
  //获取二维码信息
  getLoginQrcode = '/sys/getLoginQrcode',
  //监控二维码扫描状态
  getQrcodeToken = '/sys/getQrcodeToken',
  //获取登录页面的相关背景和文案
  getLoginBgAndTitle = '/sys/thirdLogin/keytech/loginPageInfo',
  //保存登录页面title和图片设置
  saveLoginBgAndTitle = '/sysLoginPageConfig',
  //获取登录页面的配置的列表
  getLoginPageConfigList = '/sysLoginPageConfig/list',
  //通过ID获取登录页面的配置的
  getLoginPageConfigById = '/sysLoginPageConfig',
  // 获取用户上岗/离岗状态信息
  getPostStatus = '/workorder/postRecord/findPostRecordByParam',
  // 上岗
  toPost = '/workorder/postRecord/post',
  // 下岗
  toOutPost = '/workorder/postRecord/leavePost',
  // 岗位列表
  getPostOptions = '/mes3_mdm/post/iQueryPageList',
  // 班组列表
  getGroupOptions = '/mes3_mdm/shiftGroup/queryPageList',
  // 接班
  toRelayPost = '/workorder/postShiftChange/requestShiftChange',
  // 修改在线记录时间
  updateOnlineRecordTime = '/sys/log/updateOnlineRecordTime',
}

/**
 * @description: user login api
 */
export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.Login,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: user phoneLogin api
 */
export function phoneLoginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.phoneLogin,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: getUserInfo
 */
export function getUserInfo() {
  return defHttp.get<GetUserInfoModel>({ url: Api.GetUserInfo }, {}).catch((e) => {
    // update-begin--author:zyf---date:20220425---for:【VUEN-76】捕获接口超时异常,跳转到登录界面
    if (e && (e.message.includes('timeout') || e.message.includes('401'))) {
      //接口不通时跳转到登录界面
      const userStore = useUserStoreWithOut();
      userStore.setToken('');
      setAuthCache(TOKEN_KEY, null);

      // update-begin-author:sunjianlei date:20230306 for: 修复登录成功后，没有正确重定向的问题
      router.push({
        path: PageEnum.BASE_LOGIN,
        query: {
          // 传入当前的路由，登录成功后跳转到当前路由
          redirect: router.currentRoute.value.fullPath,
        }
      });
      // update-end-author:sunjianlei date:20230306 for: 修复登录成功后，没有正确重定向的问题

    }
    // update-end--author:zyf---date:20220425---for:【VUEN-76】捕获接口超时异常,跳转到登录界面
  });
}

/**
 * @description: getPostStatus
 * 获取用户上岗/下岗状态
 */
export function getPostStatus(params) {
  // return defHttp.get({ url: Api.getPostStatus, params });
}

/**
 * @description: onPost
 * 进行上岗
 */
export function onPost(params) {
  return defHttp.post({ url: Api.toPost, params });
}

/**
 * @description: outPost
 * 进行离岗
 */
export function outPost(params) {
  return defHttp.get({ url: Api.toOutPost, params });
}

/**
 * @description: relayPost
 * 接班
 */
export function relayPost(params) {
  return defHttp.post({ url: Api.toRelayPost, params });
}

/**
 * @description: outPost
 * 获取可上岗列表
 */
export function postOptions(params) {
  return defHttp.get({ url: Api.getPostOptions, params });
}
/**
 * @description: outPost
 * 获取班组列表
 */
export function groupOptions(params) {
  return defHttp.get({ url: Api.getGroupOptions, params });
}

export function getPermCode() {
  return defHttp.get({ url: Api.GetPermCode });
}

export function doLogout() {
  return defHttp.get({ url: Api.Logout });
}

export function getCodeInfo(currdatetime) {
  let url = Api.getInputCode + `/${currdatetime}`;
  return defHttp.get({ url: url });
}
/**
 * @description: 获取短信验证码
 */
export function getCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        //update-begin---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】同一个IP，1分钟超过5次短信，则提示需要验证码---
        if(res.code != ExceptionEnum.PHONE_SMS_FAIL_CODE){
          createErrorModal({ title: '错误提示', content: res.message || '未知问题' });
          reject();
        }
        reject(res);
        //update-end---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】同一个IP，1分钟超过5次短信，则提示需要验证码---
      }
    }).catch((res)=>{
      createErrorModal({ title: '错误提示', content: res.message || '未知问题' });
      reject();
    });
  });
}

/**
 * @description: 注册接口
 */
export function register(params) {
  return defHttp.post({ url: Api.registerApi, params }, { isReturnNativeResponse: true });
}

/**
 *校验用户是否存在
 * @param params
 */
export const checkOnlyUser = (params) => defHttp.get({ url: Api.checkOnlyUser, params }, { isTransformResponse: false });
/**
 *校验手机号码
 * @param params
 */
export const phoneVerify = (params) => defHttp.post({ url: Api.phoneVerify, params }, { isTransformResponse: false });
/**
 *密码修改
 * @param params
 */
export const passwordChange = (params) => defHttp.get({ url: Api.passwordChange, params }, { isTransformResponse: false });
/**
 * @description: 第三方登录
 */
export function thirdLogin(params, mode: ErrorMessageMode = 'modal') {
  //==========begin 第三方登录/auth2登录需要传递租户id===========
  let tenantId = "0";
  if(!params.tenantId){
    tenantId = params.tenantId;
  }
  //==========end 第三方登录/auth2登录需要传递租户id===========
  return defHttp.get<LoginResultModel>(
    {
      url: `${Api.thirdLogin}/${params.token}/${params.thirdType}/${tenantId}`,
    },
    {
      errorMessageMode: mode,
    }
  );
}
/**
 * @description: 获取第三方短信验证码
 */
export function setThirdCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getThirdCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        createErrorModal({ title: '错误提示', content: res.message || '未知问题' });
        reject();
      }
    });
  });
}

/**
 * 获取登录二维码信息
 */
export function getLoginQrcode() {
  let url = Api.getLoginQrcode;
  return defHttp.get({ url: url });
}

/**
 * 监控扫码状态
 */
export function getQrcodeToken(params) {
  let url = Api.getQrcodeToken;
  return defHttp.get({ url: url, params });
}

/**
 * SSO登录校验
 */
export async function validateCasLogin(params) {
  let url = Api.validateCasLogin;
  return defHttp.get({ url: url, params });
}

/**
 * 获取登录页面的背景和相关文案说明
 */
export function getLoginBgImageSrc(params) {
  let url = Api.getLoginBgAndTitle;
  return defHttp.get({ url: url, params });
}

/**
 * 保存登录背景和标题设置
 */
export function saveLoginBgImageSrc(params) {
  let url = Api.saveLoginBgAndTitle;
  return defHttp.post({ url: url, params }, {});
}

/**
 * 更新登录背景和标题设置
 */
export function saveEditLoginBgImageSrc(params) {
  let url = Api.saveLoginBgAndTitle;
  return defHttp.put({ url: url, params }, {});
}


/**
 * 获取登录页面的背景和相关文案说明
 */
export function getLoginPageConfig(params) {
  let url = Api.getLoginPageConfigList;
  return defHttp.get({ url: url, params });
}

/**
 * 获取登录页面的背景和相关文案通过Id
 */
export function getLoginPageConfigById(params) {
  let url = Api.getLoginPageConfigById;
  return defHttp.get({ url: url + `/${params.id}/` });
}

/**
 * 删除登录页面的背景和相关文案通过Id
 */
export function deleteLoginPageConfigById(params) {
  let url = Api.getLoginPageConfigById;
  return defHttp.delete({ url: url + `/${params.id}/` });
}

/**
 * 修改在线记录时间
 */
export function updateOnlineRecordTime(params) {
  let url = Api.updateOnlineRecordTime;
  return defHttp.put({ url: url, params }, {isTransformResponse: false});
}