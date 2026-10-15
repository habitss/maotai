import { defHttp, http } from '/@/utils/http/axios';
import { getMenuListResultModel } from './model/menuModel';
import { useLocale } from '/@/locales/useLocale';
import { createLocalStorage, createSessionStorage } from '/@/utils/cache';
const storage = createLocalStorage();

enum Api {
  GetMenuList = '/sys/permission/getUserPermissionByToken',
  SwitchVue3Menu = '/sys/switchVue3Menu',
  RecordPath = '/sys/log/insertLogByMenu',
  RecordLogin = ''
}

/**
 * @description: Get user menu based on id
 */

export const getMenuList = () => {
  // 判断当前语言环境
  const { getLocale } = useLocale();
  const lang = getLocale.value
  return new Promise((resolve) => {
    //为了兼容mock和接口数据
    defHttp.get<getMenuListResultModel>({ url: Api.GetMenuList }).then((res) => {
      const filterRoutes = flattenRoutes(res.menu)
      console.log('转换后的路由', filterRoutes)
      storage.set('KT_ALL_MENUS', filterRoutes);
      if (Array.isArray(res)) {
        resolve(res);
      } else {
        if (lang === 'en-AU' || lang === 'en-BZ' || lang === 'en-CA' || lang === 'en-CB' || lang === 'en-IE' || lang === 'en-JM' || lang === 'en-NZ' || lang === 'en-PH' || lang === 'en-ZA' || lang === 'en-TT' || lang === 'en-GB' || lang === 'en-US' || lang === 'en-ZW' || lang === 'en') {
          // 美国
          updateTitles(res.menu, 'en_US')
        } else if (lang === 'ko' || lang === 'ko-KR') {
          // 韩国
          updateTitles(res.menu, 'ko')
        }
        resolve(res['menu']);
      }
    });
  });
};

/* 路由转换 */
function flattenRoutes(routes) {
  const result = [] as any;

  function recurseFlatten(routes, parentPath = '', depth = 0) {
    routes.forEach(route => {
      const meta = route.meta || {};
      const title = meta.title || '';
      const languageConfig = meta.languageConfig || [];

      // 构建完整的路径，并保留从后往前数的相应数量的 /
      let fullPath = parentPath + route.path;
      if (fullPath.includes('/')) {
        // 从后向前数保留 depth 个 /
        const parts = fullPath.split('/').reverse();
        const preservedParts = parts.slice(0, depth + 1).reverse().join('/');
        fullPath = preservedParts;
      }

      // 找到对应的英文和韩文名称
      const nameEn = languageConfig.find(item => item.languageKeys === 'en_US')?.languageText || '';
      const nameKor = languageConfig.find(item => item.languageKeys === 'ko')?.languageText || '';

      result.push({
        name: title,
        name_en: nameEn,
        name_kor: nameKor,
        path: '/' + fullPath
      });

      // 递归处理子路由
      if (route.children && Array.isArray(route.children) && route.children.length > 0) {
        recurseFlatten(route.children, fullPath, depth + 1);
      }
    });
  }

  recurseFlatten(routes);
  return result;
}

function updateTitles(tree, lang) {
  // 遍历树形结构中的每个节点
  tree.forEach(node => {
    // 如果当前节点有 languageConfig 数组并且包含匹配的 languageKeys，则更新 title
    if (node.meta && node.meta.languageConfig) {
      const match = node.meta.languageConfig.find(config => config.languageKeys === lang);
      if (match) {
        node.meta.title = match.languageText;
        // 如果存在 title 的其他语言版本，也进行更新
        if (node.meta.hasOwnProperty('title_en') && lang === 'en_US') {
          node.meta.title_en = match.languageText;
        }
        if (node.meta.hasOwnProperty('title_ko') && lang === 'ko') {
          node.meta.title_ko = match.languageText;
        }
      }
    }

    // 如果当前节点有子节点，并且子节点是一个数组，递归调用函数处理子节点
    if (Array.isArray(node.children)) {
      updateTitles(node.children, lang);
    }
  });
}

/**
 * 切换成vue3菜单
 */
export const switchVue3Menu = () => {
  return new Promise((resolve) => {
    defHttp.get({ url: Api.SwitchVue3Menu });
  });
};


/**
 * 用户轨迹 - 路由切换
 */
export const toRecordPath = (params) => {
  return http.get({ url: Api.RecordPath, params });
};

/**
 * 用户轨迹 - 登录
 */
export const toRecordLogin = (params) => {
  return http.get({ url: Api.RecordLogin, params });
};
