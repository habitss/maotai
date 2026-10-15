<template>
  <Header :class="getHeaderClass">
    <!-- left start -->
    <div :class="`${prefixCls}-left`">
      <!-- logo -->
      <AppLogo v-if="getShowHeaderLogo || getIsMobile" :class="`${prefixCls}-logo`" :theme="getHeaderTheme" :style="getLogoWidth" />
      <LayoutTrigger
        v-if="(getShowContent && getShowHeaderTrigger && !getSplit && !getIsMixSidebar) || getIsMobile"
        :theme="getHeaderTheme"
        :sider="false"
      />
      <LayoutBreadcrumb v-if="getShowContent && getShowBread" :theme="getHeaderTheme" />
      <!-- 欢迎语(去括号后缀,16px) -->
      <span v-if="getShowContent && getShowBreadTitle && !getIsMobile" :class="[prefixCls, `${prefixCls}--${getHeaderTheme}`,'headerIntroductionClass']"> {{ t('layout.header.welcomeIn') }} {{ String(title).split('(')[0] }} </span>
    </div>
    <!-- left end -->

    <!-- menu start -->
    <div :class="`${prefixCls}-menu`" v-if="getShowTopMenu && !getIsMobile">
      <LayoutMenu :isHorizontal="true" :theme="getHeaderTheme" :splitType="getSplitType" :menuMode="getMenuMode" />
    </div>
    <!-- menu-end -->

    <!-- action  -->
    <div :class="`${prefixCls}-action`">
      <!-- 切换部门入口(显示当前部门名):茅台项目不开放,注释隐藏 -->
      <!--<div :class="`${prefixCls}__name  `" class="truncate curDept" @click="changeDept">
        <a-input :class="`${prefixCls}-input`" placeholder="暂未选择部门" v-model:value="currentDepartName" readonly style="cursor: pointer"></a-input>
      </div>-->
      <AppSearch :class="`${prefixCls}-action__item `" v-if="getShowSearch" />

      <ErrorAction v-if="getUseErrorHandle" :class="`${prefixCls}-action__item error-action`" />

      <!-- 消息铃铛:茅台项目暂不开放,注释隐藏 -->
      <!--<Notify v-if="getShowNotice" :class="`${prefixCls}-action__item notify-item`" />-->

      <FullScreen v-if="getShowFullScreen" :class="`${prefixCls}-action__item fullscreen-item`" />

      <LockScreen v-if="getUseLockPage" />

      <!-- 中英文切换:茅台项目不开放,注释隐藏 -->
      <!--<AppLocalePicker v-if="getShowLocalePicker" :reload="true" :showText="false" :class="`${prefixCls}-action__item`" />-->

      <UserDropDown :theme="getHeaderTheme" />

      <SettingDrawer v-if="getShowSetting" :class="`${prefixCls}-action__item`" />
      <!-- ai助手 -->
      <!--<Aide></Aide>-->
    </div>
  </Header>
  <LoginSelect ref="loginSelectRef" @success="loginSelectOk"></LoginSelect>
  <!-- 切换部门/租户:茅台项目不开放,注释隐藏 -->
  <!--<DepartSelect ref="deptSelectRef" />-->
</template>
<script lang="ts">
  import { defineComponent, unref, computed, ref, onMounted, toRaw } from 'vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { propTypes } from '/@/utils/propTypes';

  import { Layout } from 'ant-design-vue';
  import { AppLogo } from '/@/components/Application';
  import LayoutMenu from '../menu/index.vue';
  import LayoutTrigger from '../trigger/index.vue';

  import { AppSearch } from '/@/components/Application';

  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';

  import { MenuModeEnum, MenuSplitTyeEnum } from '/@/enums/menuEnum';
  import { SettingButtonPositionEnum } from '/@/enums/appEnum';
  import { AppLocalePicker } from '/@/components/Application';

  import { UserDropDown, LayoutBreadcrumb, FullScreen, Notify, ErrorAction, LockScreen } from './components';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useDesign } from '/@/hooks/web/useDesign';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useLocale } from '/@/locales/useLocale';

  import LoginSelect from '/@/views/sys/login/LoginSelect.vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import Aide from "@/views/dashboard/ai/components/aide/index.vue"
  import { createLocalStorage } from '/@/utils/cache';
  import { getUserDeparts } from '/@/views/system/depart/depart.api';
  const departStorage = createLocalStorage();

  import {getDepartName, departListAll} from "/@/api/common/api"
  const { t } = useI18n();

  export default defineComponent({
    name: 'LayoutHeader',
    components: {
      Header: Layout.Header,
      AppLogo,
      LayoutTrigger,
      LayoutBreadcrumb,
      LayoutMenu,
      UserDropDown,
      AppLocalePicker,
      FullScreen,
      Notify,
      AppSearch,
      ErrorAction,
      LockScreen,
      LoginSelect,
      SettingDrawer: createAsyncComponent(() => import('/@/layouts/default/setting/index.vue'), {
        loading: true,
      }),
      Aide,
      DepartSelect: createAsyncComponent(() => import('./components/user-dropdown/DepartSelect.vue')),
    },
    props: {
      fixed: propTypes.bool,
    },
    setup(props) {
      const { prefixCls } = useDesign('layout-header');
      const userStore = useUserStore();
      const { getShowTopMenu, getShowHeaderTrigger, getSplit, getIsMixMode, getMenuWidth, getIsMixSidebar } = useMenuSetting();
      const { getUseErrorHandle, getShowSettingButton, getSettingButtonPosition } = useRootSetting();
      const title = ref(useGlobSetting().title);
      const {
        getHeaderTheme,
        getShowFullScreen,
        getShowNotice,
        getShowContent,
        getShowBread,
        getShowHeaderLogo,
        getShowHeader,
        getShowSearch,
        getUseLockPage,
        getShowBreadTitle,
      } = useHeaderSetting();

      const { getShowLocalePicker } = useLocale();

      const { getIsMobile } = useAppInject();

      const getHeaderClass = computed(() => {
        const theme = unref(getHeaderTheme);
        return [
          prefixCls,
          {
            [`${prefixCls}--fixed`]: props.fixed,
            [`${prefixCls}--mobile`]: unref(getIsMobile),
            [`${prefixCls}--${theme}`]: theme,
          },
        ];
      });

      const getShowSetting = computed(() => {
        if (!unref(getShowSettingButton)) {
          return false;
        }
        const settingButtonPosition = unref(getSettingButtonPosition);

        if (settingButtonPosition === SettingButtonPositionEnum.AUTO) {
          return unref(getShowHeader);
        }
        return settingButtonPosition === SettingButtonPositionEnum.HEADER;
      });

      // logo 容器不再占满菜单宽度，让右侧标题/菜单紧跟 logo（缩小间距）
      const getLogoWidth = computed(() => {
        return {};
      });

      const getSplitType = computed(() => {
        return unref(getSplit) ? MenuSplitTyeEnum.TOP : MenuSplitTyeEnum.NONE;
      });

      const getMenuMode = computed(() => {
        return unref(getSplit) ? MenuModeEnum.HORIZONTAL : null;
      });

      /**
       * 首页多租户部门弹窗逻辑
       */
      const loginSelectRef = ref();

      function showLoginSelect() {
        //update-begin---author:liusq  Date:20220101  for：判断登录进来是否需要弹窗选择租户----
        //判断是否是登陆进来
        const loginInfo = toRaw(userStore.getLoginInfo) || {};
        if (!!loginInfo.isLogin) {
          loginSelectRef.value.show(loginInfo);
        }
        //update-end---author:liusq  Date:20220101  for：判断登录进来是否需要弹窗选择租户----
      }

      function loginSelectOk() {
        console.log('成功。。。。。');
      }

      const deptSelectRef = ref();
      const currentDepartName = ref();
      //设置顶部用户选择部门
      async function getUserDept(){
        const result = await getUserDeparts();
        if (!result.list || result.list.length == 0) {
          currentDepartName.value = '暂未选择部门';
          return;
        }
        let currentDepart = result.list.filter((item) => item.orgCode == result.orgCode);
        currentDepartName.value = currentDepart && currentDepart.length > 0 ? currentDepart[0].departName : '';
      }
      //切换部门
      function changeDept(){
        deptSelectRef.value.show();
      }

      onMounted(async () => {
        showLoginSelect();
        // //设置顶部工厂名称
        if(userStore.getUserInfo.orgCode){
          const deptData = await departListAll(null);
          departStorage.set(userStore.getUserInfo.orgCode+'_depart', deptData);
          departStorage.set(userStore?.factoryCode+'_depart', deptData);
          let orgCode = userStore.getUserInfo.orgCode ? userStore.getUserInfo.orgCode.length > 9 ? userStore.getUserInfo.orgCode.substring(0, 9) : userStore.getUserInfo.orgCode : '';
          for(const item of deptData){
            if(item.orgCode == orgCode){
              title.value += `(${item.departName})`;
              break;
            }
          }
        }
        //设置顶部用户选择部门
        getUserDept();
      });

      return {
        prefixCls,
        getHeaderClass,
        getShowHeaderLogo,
        getHeaderTheme,
        getShowHeaderTrigger,
        getIsMobile,
        getShowBreadTitle,
        getShowBread,
        getShowContent,
        getSplitType,
        getSplit,
        getMenuMode,
        getShowTopMenu,
        getShowLocalePicker,
        getShowFullScreen,
        getShowNotice,
        getUseErrorHandle,
        getLogoWidth,
        getIsMixSidebar,
        getShowSettingButton,
        getShowSetting,
        getShowSearch,
        getUseLockPage,
        loginSelectOk,
        loginSelectRef,
        title,
        currentDepartName,
        deptSelectRef,
        changeDept,
        t
      };
    },
  });
</script>
<style lang="less">
  @import './index.less';
  //update-begin---author:scott ---date:2022-09-30  for：默认隐藏顶部菜单面包屑-----------
  //顶部欢迎语展示样式
  @prefix-cls: ~'@{namespace}-layout-header';
  
  .ant-layout .@{prefix-cls} {
    display: flex;
    padding: 0 8px;
    // update-begin--author:liaozhiyang---date:20240407---for：【QQYUN-8762】顶栏高度
    height: @header-height;
    // update-end--author:liaozhiyang---date:20240407---for：【QQYUN-8762】顶栏高度
    align-items: center;
    
    .headerIntroductionClass {
      margin-right: 4px;
      margin-bottom: 2px;
      border-bottom: 0px;
      border-left: 0px;
      font-size: 16px;
    }
    
    &--light {
      .headerIntroductionClass {
        color: #000;
      }
    }

    &--dark {
      .headerIntroductionClass {
        color: rgba(255, 255, 255, 1);
      }
      .anticon, .truncate {
        color: rgba(255, 255, 255, 1);
      }
    }
    //update-end---author:scott ---date::2022-09-30  for：默认隐藏顶部菜单面包屑--------------
  }

  .curDept{
    color: #fff;
    font-size: 14px;
    margin-right: 20px;
    cursor: pointer;
  }
</style>
