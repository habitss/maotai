import { i18nHttp } from './i18nHttp';

/**
 * 获取i18n版本
 */
export function getI18nVersions() {
  return i18nHttp.get('/i18n/mdmI18nEntryLang/version').then((res) => res.data);
}

/**
 * 下载语言包
 */
export function downloadLangZip(lang: string) {
  return i18nHttp.get('/i18n/mdmI18nEntryLang/zip', {
    params: { lang },
    responseType: 'blob',
  });
}
