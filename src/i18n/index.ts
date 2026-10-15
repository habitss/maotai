import { createI18n } from 'vue-i18n';
import { loadLang } from './loader';

export const DEFAULT_LANG = localStorage.getItem('lang') || 'zh-CN';

export const i18n = createI18n({
  legacy: false,
  locale: DEFAULT_LANG,
  messages: {},
});

export async function setupI18n(app) {
  await loadLang(DEFAULT_LANG);
  app.use(i18n);
}

export async function switchLang(lang: string) {
  await loadLang(lang);
  i18n.global.locale.value = lang;
  localStorage.setItem('lang', lang);
}
