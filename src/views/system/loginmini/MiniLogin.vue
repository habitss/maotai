<template>

  <div class='dashWrap'>
    <div class='dashedInfo'>
      <h1>Hi<span class='dot'>，</span>你好！</h1>
      <h2>欢迎进入茅台B园区数字化平台</h2>
    </div>

    <div class='login-content'>

      <div class='login-main'>
        <div class='login-logo'>
          <img src='@/assets/images/login/logo.png' class='login-logo-img' />
        </div>
        <div class='login-title'>{{ settingBgAndTitle.subTitle }}</div>
        <div class='login-subtitle'>{{ curTitle||title }}</div>

        <a-form-model class='user-layout-login'>

          <div class="login-account">
            <a-form ref="form" :model="formData" :rules="validatorRules" :labelCol="{span: 0}" :wrapperCol="{span: 24}" @keyup.enter.native="loginHandleClick">
              <a-form-model-item required prop="username">
                <a-input v-model:value="formData.username" size="large" placeholder="请输入帐户名">
                  <!-- <img src="@/assets/imgs/user_icon.svg" alt="用户" class="login-icon" slot="prefix"> -->
                  <template #prefix>
                    <img src="@/assets/svg/user_icon.svg" alt="用户" class="login-icon">
                  </template>
                </a-input>
              </a-form-model-item>
              <a-form-model-item required prop="password">
                <a-input
                  v-model:value="formData.password"
                  size="large"
                  type="password"
                  autocomplete="false"
                  placeholder="请输入密码"
                  style="margin-top: 37px;"
                >
                <template #prefix>
                  <img src="@/assets/svg/pwd_icon.svg" alt="用户" class="login-icon">
                </template>
                  <!-- <img src="@/assets/imgs/pwd_icon.svg" class="login-icon" alt="密码" slot="prefix"> -->
                  <!-- <a-icon slot="prefix" type="lock" :style="{ color: '#fff' }" /> -->
                </a-input>
              </a-form-model-item>
            </a-form>
          </div>

          <div class='login-tools'>
            <a-checkbox v-model:checked='rememberPassword' class='remember-check'>记住密码</a-checkbox>
            <a class='forgot-link' @click='handleForgotPassword'>忘记密码?</a>
          </div>

          <a-form-item style='margin: 0;padding: 24px 0 16px 0;'>
          <!--登陆-->
          <a-button size='large' type='primary' htmlType='submit' class='login-button' :loading='loginLoading'
          @click="loginHandleClick">{{ t('language.loginButton') }}</a-button>
        </a-form-item>

        <a-form-item style='margin: 0;padding: 0 0 40px 0;'>
          <a-button size='large' class='sso-button' @click='handleSsoLogin'>SSO 登录</a-button>
        </a-form-item>
      </a-form-model>




      </div>
    </div>
  </div>

</template>
<script lang="ts" setup name="login-mini">
import { getCaptcha, getCodeInfo, getLoginBgImageSrc } from '/@/api/sys/user';
import { computed, onMounted, reactive, ref, toRaw, unref } from 'vue';
import website from '@/config/website';
// import codeImg from '/@/assets/images/checkcode.png';
// import { Rule } from '/@/components/Form';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import { SmsEnum } from '/@/views/sys/login/useLogin';
// import ThirdModal from '/@/views/sys/login/ThirdModal.vue';
// import MiniForgotpad from './MiniForgotpad.vue';
// import MiniRegister from './MiniRegister.vue';
// import MiniCodelogin from './MiniCodelogin.vue';
// import logoImg from '/@/assets/loginmini/icon/jeecg_logo.png';
// import adTextImg from '/@/assets/loginmini/icon/icon-success.png';
// import { AppLocalePicker, AppDarkModeToggle } from '/@/components/Application';
import { defHttp } from '/@/utils/http/axios';
import { useLocaleStore } from '/@/store/modules/locale';
import { useDesign } from '/@/hooks/web/useDesign';
import { useAppInject } from '/@/hooks/web/useAppInject';
import {
  // GithubFilled,
  // WechatFilled,
  // DingtalkCircleFilled,
  createFromIconfontCN,
} from '@ant-design/icons-vue';
import { setStorageDepart } from '/@/utils/ktTool';
import { Modal } from 'ant-design-vue';

const IconFont = createFromIconfontCN({
  scriptUrl: '//at.alicdn.com/t/font_2316098_umqusozousr.js',
});
const { prefixCls } = useDesign('mini-login');
const { notification, createMessage } = useMessage();
const userStore = useUserStore();
const { t } = useI18n();
const localeStore = useLocaleStore();
const showLocale = localeStore.getShowPicker;
const randCodeData = reactive<any>({
  randCodeImage: '',
  requestCodeSuccess: false,
  checkKey: null,
});
const rememberMe = ref<string>('0');
//记住密码(勾选后本机记住帐号+密码,登录页自动填充)
const rememberPassword = ref<boolean>(false);
//记住密码 localStorage 键;密码 base64 编码存储避免明文裸露(仅混淆,不作为安全手段)
const REMEMBER_KEY = 'maotai-login-remember';

function saveRemember(u: string, p: string) {
  localStorage.setItem(REMEMBER_KEY, JSON.stringify({ u, p: btoa(encodeURIComponent(p)) }));
}

function readRemember(): { u: string; p: string } | null {
  try {
    const raw = localStorage.getItem(REMEMBER_KEY);
    if (!raw) return null;
    const obj = JSON.parse(raw);
    return { u: obj.u || '', p: obj.p ? decodeURIComponent(atob(obj.p)) : '' };
  } catch (e) {
    return null;
  }
}

function clearRemember() {
  localStorage.removeItem(REMEMBER_KEY);
}

//忘记密码(平台不开放自助找回,提示联系管理员)
function handleForgotPassword() {
  Modal.info({
    title: '忘记密码',
    content: '请联系管理员修改密码',
    okText: '知道了',
    centered: true,
  });
}
//手机号登录还是账号登录
const activeIndex = ref<string>('accountLogin');
const type = ref<string>('login');
//账号登录表单字段
const formData = reactive<any>({
  inputCode: '',
  username: '',
  password: '',
});
//手机登录表单字段
const phoneFormData = reactive<any>({
  mobile: '',
  smscode: '',
});
const loginRef = ref();
//第三方登录弹窗
const thirdModalRef = ref();
//扫码登录
const codeRef = ref();
//是否显示获取验证码
const showInterval = ref<boolean>(true);
//60s
const timeRuning = ref<number>(60);
//定时器
const timer = ref<any>(null);
//忘记密码
const forgotRef = ref();
const title = ref("MOUTAI DIGITAL PLATFORM");
const curTitle = ref('');
// url协议
const protocol = ref<string>('');
//注册
const registerRef = ref();
const loginLoading = ref<boolean>(false);
const { getIsMobile } = useAppInject();

const settingBgAndTitle = reactive<any>({
  bigBgImageSrc: 'url(/mes3/src/assets/images/login-back.png)',
  fontImgSrc: 'url(/mes3/src/assets/loginmini/icon/jeecg_ad.png)',
  logoSrc: 'url(/mes3/src/assets/loginmini/icon/icon-success.png',
  title: ' ',
  subTitle: `${import.meta.env.VITE_GLOB_APP_TITLE}`,
  // subChildTitle: '致力于生产车间全流程管理',
  // logoText: import.meta.env.VITE_GLOB_APP_TITLE,
});

defineProps({
  sessionTimeout: {
    type: Boolean,
  },
});

/**
 * 获取验证码
 */
function handleChangeCheckCode() {
  formData.inputCode = '';

  randCodeData.checkKey = 1629428467008;
  getCodeInfo(randCodeData.checkKey).then((res) => {
    randCodeData.randCodeImage = res;
    randCodeData.requestCodeSuccess = true;
  });
}

//SSO 登录(演示入口):暂未接入,仅提示
function handleSsoLogin() {
  createMessage.warning('SSO 登录暂不可用，请联系管理员');
}

/**
 * 切换登录方式
 */
function loginClick(type) {
  activeIndex.value = type;
}

/**
 * 账号或者手机登录
 */
async function loginHandleClick() {
  if (unref(activeIndex) === 'accountLogin') {
    accountLogin();
  } else {
    //手机号登录
    phoneLogin();
  }
}

async function accountLogin() {
  if (!formData.username) {
    createMessage.warn(t('language.accountPlaceholder'));
    return;
  }
  if (!formData.password) {
    createMessage.warn(t('language.passwordPlaceholder'));
    return;
  }
  try {
    loginLoading.value = true;
    const { userInfo } = await userStore.login(
      toRaw({
        password: formData.password,
        username: formData.username,
        captcha: formData.inputCode,
        checkKey: randCodeData.checkKey,
        mode: 'none', //不要默认的错误提示
      })
    );
    if (userInfo) {
      notification.success({
        message: t('language.loginSuccessTitle'),
        description: `${t('language.loginSuccessDesc')}: ${userInfo.realname}`,
        duration: 3,
      });
      setStorageDepart(userInfo.orgCode);
      localStorage.setItem('tips-Status', 'true');
      //记住密码:勾选则本机保存,未勾选则清除
      if (unref(rememberPassword)) {
        saveRemember(formData.username, formData.password);
      } else {
        clearRemember();
      }
    }
  } catch (error) {
    notification.error({
      message: t('language.errorTip'),
      description: error.message || t('language.networkExceptionMsg'),
      duration: 3,
    });
    // handleChangeCheckCode();
  } finally {
    loginLoading.value = false;
  }
}

/**
 * 手机号登录
 */
async function phoneLogin() {
  if (!phoneFormData.mobile) {
    createMessage.warn(t('language.mobilePlaceholder'));
    return;
  }
  if (!phoneFormData.smscode) {
    createMessage.warn(t('language.smsPlaceholder'));
    return;
  }
  try {
    loginLoading.value = true;
    const { userInfo }: any = await userStore.phoneLogin({
      mobile: phoneFormData.mobile,
      captcha: phoneFormData.smscode,
      mode: 'none', //不要默认的错误提示
    });
    if (userInfo) {
      notification.success({
        message: t('language.loginSuccessTitle'),
        description: `${t('language.loginSuccessDesc')}: ${userInfo.realname}`,
        duration: 3,
      });
    }
  } catch (error) {
    notification.error({
      message: t('language.errorTip'),
      description: error.message || t('language.networkExceptionMsg'),
      duration: 3,
    });
  } finally {
    loginLoading.value = false;
  }
}

/**
 * 获取手机验证码
 */
async function getLoginCode() {
  if (!phoneFormData.mobile) {
    createMessage.warn(t('language.mobilePlaceholder'));
    return;
  }
  const result = await getCaptcha({
    mobile: phoneFormData.mobile,
    smsmode: SmsEnum.FORGET_PASSWORD,
  });
  if (result) {
    const TIME_COUNT = 60;
    if (!unref(timer)) {
      timeRuning.value = TIME_COUNT;
      showInterval.value = false;
      timer.value = setInterval(() => {
        if (unref(timeRuning) > 0 && unref(timeRuning) <= TIME_COUNT) {
          timeRuning.value = timeRuning.value - 1;
        } else {
          showInterval.value = true;
          clearInterval(unref(timer));
          timer.value = null;
        }
      }, 1000);
    }
  }
}

/**
 * 第三方登录
 * @param type
 */
function onThirdLogin(type) {
  thirdModalRef.value.onThirdLogin(type);
}

/**
 * 忘记密码
 */
function forgetHandelClick() {
  type.value = 'forgot';
  setTimeout(() => {
    forgotRef.value.initForm();
  }, 300);
}

/**
 * 返回登录页面
 */
function goBack() {
  activeIndex.value = 'accountLogin';
  type.value = 'login';
}

/**
 * 忘记密码/注册账号回调事件
 * @param value
 */
function handleSuccess(value) {
  Object.assign(formData, value);
  Object.assign(phoneFormData, { mobile: '', smscode: '' });
  type.value = 'login';
  activeIndex.value = 'accountLogin';
  // handleChangeCheckCode();
}

/**
 * 注册
 */
function registerHandleClick() {
  type.value = 'register';
  setTimeout(() => {
    registerRef.value.initForm();
  }, 300);
}

/**
 * 注册
 */
function codeHandleClick() {
  type.value = 'codeLogin';
  setTimeout(() => {
    codeRef.value.initFrom();
  }, 300);
}

/**
 * 获取验证码
 */
function handleGetLoginBgImageSrc() {
  // getLoginBgImageSrc({}).then((res) => {
  //   const url = location.host;
  //   if (res.backgroundImgSrc) {
  //     settingBgAndTitle.bigBgImageSrc = `url(${protocol.value}${url}/keytech-boot/sys/common/static/${res.backgroundImgSrc})`;
  //     settingBgAndTitle.fontImgSrc = `url(${protocol.value}${url}/keytech-boot/sys/common/static/${res.fontImgSrc})`;
  //     settingBgAndTitle.logoSrc = `${protocol.value}${url}/keytech-boot/sys/common/static/${res.logoSrc}`;
  //     settingBgAndTitle.title = res.orgName;
  //     settingBgAndTitle.subTitle = res.title;
  //     settingBgAndTitle.subChildTitle = res.subTitle;
  //     settingBgAndTitle.logoText = res.logoText;
  //   }
  // });
}

//获取工厂名称
async function requestFactoryName(){
  // curTitle.value='燕京啤酒(中京)有限公司';
  curTitle.value='';
  try {
    const res = await defHttp.get({ url: '/mes3_sys/factoryTitle', params: {tenantId:1006} });
    debugger
    if(res){
      curTitle.value="MOUTAI DIGITAL PLATFORM";
    }
  } catch (error) {

  }

}

onMounted(() => {
  // 定制化http协议
  const isSecureConnection = document.location.protocol === 'https:';
  protocol.value = isSecureConnection ? 'https://' : 'http://';

  //加载验证码
  // handleChangeCheckCode();
  // handleGetLoginBgImageSrc();
  //获取工厂名称
  requestFactoryName();

  //记住密码:本机已保存则自动填充并勾选
  const saved = readRemember();
  if (saved && saved.u) {
    formData.username = saved.u;
    formData.password = saved.p;
    rememberPassword.value = true;
  }
});
</script>

<style lang="less" scoped>
@import '/@/assets/loginmini/style/home.less';
@import '/@/assets/loginmini/style/base.less';

.dashWrap {
  background-image: url('@/assets/images/login/login-bg.png');
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;

  // .container {
  .login-content {
    //position: relative;
    width: 100%;
    height: 100%;
    // min-height: 1080px;


    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(28, 25, 23, 0.62) 0%, rgba(28, 25, 23, 0.35) 100%);
    }


    // 左上 logo 已移入卡片内(.login-logo)
  }
  // }
}

// .dashWrap {
// }

.dashedInfo {
  position: absolute;
  top: 50%;
  left: 140px;
  z-index: 999;
  transform: translateY(-45%);

  .dot {
    font-family: serif;
  }

  h1 {
    font-size: 46px;
    font-family: Source Han Sans CN-Regular, Source Han Sans CN;
    font-weight: 400;
    color: #FFFFFF;
    text-shadow: 0px 2px 2px rgba(0, 0, 0, 0.46);
  }

  h2 {
    color: #fff;
    font-size: 36px;
    font-family: Source Han Sans CN-Regular, Source Han Sans CN;
    font-weight: 400;
    color: #FFFFFF;
    text-shadow: 0px 1px 1px rgba(0, 0, 0, 0.68);
    margin-top: 10px;
  }
}


.login-main {

  z-index: 999;
  right: 162px;
  width: 480px;
  padding: 0 46px !important;
  box-sizing: border-box;
  transform: translateY(-50%);
  top: 50%;
  position: absolute;
  // 玻璃拟态卡片
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(24px) saturate(1.4);
  -webkit-backdrop-filter: blur(24px) saturate(1.4);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);

  .login-logo {
    display: flex;
    justify-content: center;
    width: 100%;
    padding-top: 48px;

    .login-logo-img {
      display: block;
      width: 96px;
      height: 96px;
      border-radius: 14px;
      background: #fff;
      box-shadow: 0 4px 16px rgba(139, 0, 0, 0.3);
      object-fit: cover;
    }
  }

  .login-title {
    padding: 18px 0 20px 0;
    font-size: 30px;
    font-weight: 600;
    color: #fff;
    text-align: center;
    line-height: 1;
    letter-spacing: 2px;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }

  .login-subtitle {
    font-size: 20px;
    font-weight: 400;
    text-align: center;
    margin-bottom: 48px;
    line-height: 1;
    color: rgba(255, 255, 255, 0.85);
  }

  /* 在小屏幕上应用的样式 */
  @media screen and (max-width: 768px) {
    .login-title {
      font-size: 20px;
    }

    .login-subtitle {
      font-size: 16px;
    }
  }

  /* 在大屏幕上应用的样式 */
  @media screen and (min-width: 769px) {
    p {
      font-size: 18px;
      color: red;
    }
  }

}

.user-layout-login {
  label {
    font-size: 14px;
  }

  button.login-button {
    width: 100%;
    height: 56px;
    font-size: 20px;
    font-weight: 600;
    letter-spacing: 4px;
    font-family: Source Han Sans CN-Regular, Source Han Sans CN;
    background: #d97706;
    border-radius: 8px;
    border: none;
    color: #fff;
    transition: all 0.3s ease;

    &:hover,
    &:focus {
      background: #b45309;
      box-shadow: 0 4px 16px rgba(217, 119, 6, 0.4);
      color: #fff;
    }
  }

  button.sso-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 2px;
    font-family: Source Han Sans CN-Regular, Source Han Sans CN;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.45);
    border-radius: 8px;
    color: #fff;
    transition: all 0.3s ease;

    &:hover,
    &:focus {
      border-color: #d97706;
      color: #fbbf24;
      background: rgba(217, 119, 6, 0.12);
      box-shadow: none;
    }
  }

  .user-login-other {
    text-align: left;
    margin-top: 24px;
    line-height: 22px;

    .item-icon {
      font-size: 24px;
      color: rgba(0, 0, 0, 0.2);
      margin-left: 16px;
      vertical-align: middle;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #1890ff;
      }
    }
  }
}

.dashWrap .login-content .logo-title {
    position: absolute;
    top: 60px;
    left: 140px
}

// .dashedInfo {
//     position: absolute;
//     top: 50%;
//     left: 140px;
//     z-index: 999;
//     -webkit-transform: translateY(-45%);
//     transform: translateY(-45%)
// }



.login-account {
  padding-bottom: 20px;
  :deep(.ant-form)  {
    &-item {
      margin-bottom: 0;
    }
    &-item-control {
      &.has-error {
        margin-bottom: 8px;
      }
      margin-bottom: 37px;
    }
    .ant-input {
      height: 52px;
      font-size: 16px;
      width: 100%;
      background-color: rgba(255, 255, 255, 0.08) !important;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 4px;
      box-shadow: none !important;
      color: #f5f5f4;
      transition: all 0.3s ease;

      &:hover {
        border-color: rgba(217, 119, 6, 0.4);
      }
      &:focus {
        background-color: rgba(255, 255, 255, 0.12) !important;
        border-color: rgba(217, 119, 6, 0.5);
        box-shadow: 0 0 0 2px rgba(217, 119, 6, 0.3) !important;
      }
    }
    .ant-form-explain {
      padding: 10px 0 0;
    }
    .ant-input-prefix {
      font-size: 22px;
      color: #a8a29e;
      margin-right: 6px;
    }
    .ant-input-affix-wrapper-lg {
      padding: 0 16px;
    }
    .ant-input-affix-wrapper {
      background-color: rgba(255, 255, 255, 0.08) !important;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 4px;

      &:hover {
        border-color: rgba(217, 119, 6, 0.4);
      }

      // 内层 input 自带边框会与外层 wrapper 边框重叠(双边框),内层去边框去背景
      .ant-input {
        border: none;
        background: transparent;
        box-shadow: none;
        height: 50px;
      }
    }
    .ant-input-affix-wrapper-focused {
      background-color: rgba(255, 255, 255, 0.12) !important;
      border-color: rgba(217, 119, 6, 0.5);
      box-shadow: 0 0 0 2px rgba(217, 119, 6, 0.3) !important;
    }
    .ant-input-affix-wrapper .ant-input:not(:first-child) {
      padding-left: 12px;
    }
    input::placeholder {
      color: rgba(255, 255, 255, 0.5) !important;
      font-size: 16px !important;
    }
    input:-internal-autofill-selected,
    input:-internal-autofill-previewed,
    input:-webkit-autofill,
    input:-webkit-autofill:hover,
    input:-webkit-autofill:focus,
    input:-webkit-autofill:active {
      box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.08) inset !important;
      -webkit-text-fill-color: #f5f5f4 !important;
      transition: background-color 5000s ease-in-out 0s !important;
      -webkit-transition: background-color 5000s ease-in-out 0s !important;
    }
  }
  .login-icon {
    width: 22px;
    height: 22px;
    filter: brightness(0) invert(0.8);
  }
}

.login-account{
    padding-bottom: 20px
}

.login-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -20px;
  padding: 0 2px;

  :deep(.ant-checkbox-wrapper) {
    color: rgba(255, 255, 255, 0.85);
    font-size: 14px;

    .ant-checkbox-inner {
      background-color: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.4);
    }

    .ant-checkbox-checked .ant-checkbox-inner {
      background-color: #d97706;
      border-color: #d97706;
    }

    .ant-checkbox-checked::after {
      border-color: #d97706;
    }
  }

  .forgot-link {
    color: rgba(255, 255, 255, 0.85);
    font-size: 14px;
    transition: color 0.3s;

    &:hover {
      color: #fbbf24;
    }
  }
}
</style>
<style>
.valid-error .ant-select-selection__placeholder {
  color: #f5222d;
}
</style>
