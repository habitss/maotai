import JSZip from 'jszip';
import { getI18nVersions, downloadLangZip } from './api';
import { i18n } from '@/i18n';

const versionKey = (lang: string) => `i18n-ver-${lang}`;
const cacheKey = (lang: string) => `i18n-cache-${lang}`;

export async function loadLang(lang: string) {
  const versions = await getI18nVersions();
  const serverVer = versions[lang] || 0;
  const localVer = Number(localStorage.getItem(versionKey(lang)) || 0);

  // 命中缓存
  if (serverVer === localVer) {
    const cached = localStorage.getItem(cacheKey(lang));
    if (cached) {
      i18n.global.setLocaleMessage(lang, JSON.parse(cached));
      return;
    }
  }

  // 拉 ZIP
  const zipBlob = await downloadLangZip(lang);
  const zip = await JSZip.loadAsync(zipBlob.data);
  const json = await zip.file(`${lang}.json`)!.async('string');
  const messages = JSON.parse(json);

  i18n.global.setLocaleMessage(lang, messages);
  localStorage.setItem(cacheKey(lang), JSON.stringify(messages));
  localStorage.setItem(versionKey(lang), String(serverVer));
}
