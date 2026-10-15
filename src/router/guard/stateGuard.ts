import type { Router } from 'vue-router';
import { useAppStore } from '/@/store/modules/app';
import { useMultipleTabStore } from '/@/store/modules/multipleTab';
import { useUserStore } from '/@/store/modules/user';
import { usePermissionStore } from '/@/store/modules/permission';
import { PageEnum } from '/@/enums/pageEnum';
import { removeTabChangeListener } from '/@/logics/mitt/routeChange';
import { Persistent } from '/@/utils/cache/persistent';
import { PROJ_CFG_KEY } from '/@/enums/cacheEnum';

export function createStateGuard(router: Router) {
  router.afterEach((to) => {
    // Just enter the login page and clear the authentication information
    if (to.path === PageEnum.BASE_LOGIN) {
      const tabStore = useMultipleTabStore();
      const userStore = useUserStore();
      const appStore = useAppStore();
      const permissionStore = usePermissionStore();
      // 用户主题配置备份:重新登录不清掉,配置的主题色/深色模式跨会话保留
      const themeBackup = Persistent.getLocal(PROJ_CFG_KEY);
      appStore.resetAllState();
      if (themeBackup) Persistent.setLocal(PROJ_CFG_KEY, themeBackup, true);
      permissionStore.resetState();
      tabStore.resetState();
      userStore.resetState();
      removeTabChangeListener();
    }
  });
}
