import type { App } from 'vue';
import type { I18n, I18nOptions } from 'vue-i18n';

import { createI18n } from 'vue-i18n';
import { setHtmlPageLang, setLoadLocalePool } from './helper';
import { localeSetting } from '/@/settings/localeSetting';
import { useLocaleStoreWithOut } from '/@/store/modules/locale';

import JSZip from 'jszip';
import { getI18nVersions, downloadLangZip } from '/@/i18n/api';

const { fallback, availableLocales } = localeSetting;

export let i18n: ReturnType<typeof createI18n>;

async function createI18nOptions(): Promise<I18nOptions> {
  const localeStore = useLocaleStoreWithOut();
  const locale = localeStore.getLocale;
  //本地语言（兜底）
  const defaultLocal = await import(`./lang/${locale}.ts`);
  const localMmessages = defaultLocal.default?.message ?? {};
  //后端语言（可热更新）
  const map: Record<string, string> = {
    zh_CN: 'zh-CN',
    en: 'en-US',
    ko: 'ko-KR',
    ja: 'ja-JP',
    ru: 'ru-RU',
    fr: 'fr-FR',
  };
  const remoteMessages = await loadLangMessage(map[locale]);
  // 合并（后端优先）
  const message = {
    ...localMmessages,
    ...remoteMessages,
  };

  setHtmlPageLang(locale);
  setLoadLocalePool((loadLocalePool) => {
    loadLocalePool.push(locale);
  });

  return {
    legacy: false,
    locale,
    fallbackLocale: fallback,
    messages: {
      [locale]: message,
    },
    availableLocales: availableLocales,
    sync: true, //If you don’t want to inherit locale from global scope, you need to set sync of i18n component option to false.
    silentTranslationWarn: true, // true - warning off
    missingWarn: false,
    silentFallbackWarn: true,
  };
}

// setup i18n instance with glob
export async function setupI18n(app: App) {
  const options = await createI18nOptions();
  i18n = createI18n(options) as I18n;
  app.use(i18n);
}

/**
 * 从后端拉取语言包
 * @author rocye
 * @since 2025-12-22
 */
async function loadLangMessage(locale: string) {
  // 1 先查 localStorage 版本
  const localVer = Number(localStorage.getItem(`i18n-ver-${locale}`) || 0);

  // 2 查后端 Redis 版本
  const verRes = await getI18nVersions();
  const versions = verRes.result;
  console.log('versions', versions, locale);
  const serverVer = versions[locale] || 0;
  console.log('serverVer', serverVer);

  if (serverVer === localVer) {
    const cached = localStorage.getItem(`i18n-cache-${locale}`);
    if (cached) {
      return JSON.parse(cached);
    }
  }

  // 3 拉 ZIP
  const zipBlob = await downloadLangZip(locale);
  const zip = await JSZip.loadAsync(zipBlob.data);
  const json = await zip.file(`${locale}.json`)!.async('string');
  const messages = JSON.parse(json);

  localStorage.setItem(`i18n-cache-${locale}`, JSON.stringify(messages));
  localStorage.setItem(`i18n-ver-${locale}`, String(serverVer));

  return messages;
}
