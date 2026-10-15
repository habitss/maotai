import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';
import { LAYOUT } from '/@/router/constant';

// import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';

const modules = import.meta.glob('./modules/**/*.ts', { eager: true });

const routeModuleList: AppRouteModule[] = [];

// 加入到路由集合中
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  //新版后台登录，如果想要使用旧版登录放开即可
  // component: () => import('/@/views/sys/login/Login.vue'),
  component: () => import('/@/views/system/loginmini/MiniLogin.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

//update-begin---author:wangshuai ---date:20220629  for：auth2登录页面路由------------
export const Oauth2LoginRoute: AppRouteRecordRaw = {
  path: '/oauth2-app/login',
  name: 'oauth2-app-login',
  //新版钉钉免登录，如果想要使用旧版放开即可
  // component: () => import('/@/views/sys/login/OAuth2Login.vue'),
  component: () => import('/@/views/system/loginmini/OAuth2Login.vue'),
  meta: {
    title: t('routes.oauth2.login'),
  },
};


//update-end---author:wangshuai ---date:20220629  for：auth2登录页面路由------------

/**
 * 【通过token直接静默登录】流程办理登录页面 中转跳转
 */
export const TokenLoginRoute: AppRouteRecordRaw = {
  path: '/tokenLogin',
  name: 'TokenLoginRoute',
  component: () => import('/@/views/sys/login/TokenLoginPage.vue'),
  meta: {
    title: '带token登录页面',
    ignoreAuth: true,
  },
};

export const Oauth2Route: AppRouteRecordRaw = {
  path: '/oauth2-app',
  name: 'oauth2-app',
  //新版钉钉免登录，如果想要使用旧版放开即可
  component: () => import('/@/views/system/loginmini/OAuth2Login.vue'),
  meta: {
    title: t('routes.oauth2.login'),
  },
};
//***********************************MES3放行菜单*************************************************** */
//mes3嵌入燕京平台-报表查询(PI报表)
export const MesQueryTable: AppRouteRecordRaw = {
  path: '/mes3/dataCalculation/pi/allocationTable/QueryTableList/:code/:type',
  name: 'mes3QueryTable',
  component: () => import('/@/views/dataCalculation/pi/allocationTable/QueryTableList.vue'),
  meta: {
    title: '报表查询',
  },
};

export const MesMeetingCheck: AppRouteRecordRaw = {
  path: '/mes3/dataCalculation/meeting/calendar/MeetingSignIn/:id/:meetingDate/:factoryName',
  name: 'mesMeetingCheck',
  component: () => import('/@/views/dataCalculation/meeting/calendar/MeetingSignIn.vue'),
  meta: {
    title: '会议签到',
    ignoreAuth: true
  },
};


//***********************************MES3放行菜单END*************************************************** */

//***********************************maotai 详情页放行路由******************************************** */
// 设备台账详情页:从台账列表「详情」按钮跳转,不依赖后端动态菜单,直接静态注册。
// 用独特父路径 + LAYOUT 让详情页在页面框架内(带侧边栏/顶栏);
// 子路由用绝对路径保持台账跳转 url 不变(/maotai/equipmentLedger/detail?id=),
// 父路径 /_maotai_detail 不与后端动态菜单的 /maotai 冲突。
export const MaotaiEquipmentDetailRoute: AppRouteRecordRaw = {
  path: '/_maotai_detail',
  name: 'MaotaiDetailLayout',
  component: LAYOUT,
  meta: {
    title: '设备详情',
  },
  children: [
    {
      path: '/maotai/equipmentLedger/detail',
      name: 'maotai-equipment-detail',
      component: () => import('/@/views/maotai/equipment/MaotaiEquipmentDetail.vue'),
      meta: {
        title: '设备详情',
        // 详情页不进多标签栏(hideTab),避免每个设备 id 开一个 tab;切换设备由 query.id 变化 + watch 刷新
        hideTab: true,
      },
    },
  ],
};
//***********************************maotai 详情页放行路由 END**************************************** */

//***********************************maotai 全屏驾驶舱路由******************************************** */
// 直接挂载页面组件，不经过 LAYOUT，确保驾驶舱不显示后台顶栏、侧栏和多标签栏。
// 不设置 ignoreAuth，仍由全局权限守卫校验登录状态。
export const MaotaiWorkshopCockpitRoute: AppRouteRecordRaw = {
  path: '/maotai/workshopCockpit',
  name: 'maotai-workshop-cockpit',
  component: () => import('/@/views/maotai/workshopCockpit/index.vue'),
  meta: {
    title: '车间驾驶舱',
    hideTab: true,
    hideMenu: true,
    hideBreadcrumb: true,
  },
};
//***********************************maotai 全屏驾驶舱路由 END**************************************** */


// Basic routing without permission
export const basicRoutes = [
  LoginRoute,
  RootRoute,
  REDIRECT_ROUTE,
  PAGE_NOT_FOUND_ROUTE,
  TokenLoginRoute,
  Oauth2LoginRoute,
  MesQueryTable,
  MesMeetingCheck,
  Oauth2Route,
  MaotaiEquipmentDetailRoute,
  MaotaiWorkshopCockpitRoute,
];
