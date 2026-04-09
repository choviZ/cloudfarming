import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'

const LOCALE_STORAGE_KEY = 'cloudfarming-admin-locale'
const DEFAULT_LOCALE = 'zh-CN'

const messages = {
  'zh-CN': zhCN
}

const resolveLocale = () => {
  const saved = window.localStorage.getItem(LOCALE_STORAGE_KEY)
  if (saved && Object.prototype.hasOwnProperty.call(messages, saved)) {
    return saved
  }
  return DEFAULT_LOCALE
}

export const i18n = createI18n({
  legacy: false,
  locale: resolveLocale(),
  fallbackLocale: DEFAULT_LOCALE,
  messages
})

export const setLocale = (locale) => {
  if (!Object.prototype.hasOwnProperty.call(messages, locale)) {
    return
  }
  i18n.global.locale.value = locale
  window.localStorage.setItem(LOCALE_STORAGE_KEY, locale)
}

export default i18n
