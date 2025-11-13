import { createI18n } from 'vue-i18n'
import en from './en-US'
import cn from './zh-CN'

// 语言选项
export const LOCALE_OPTIONS = [
  { label: '中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
]

// 从本地存储获取默认语言，没有则使用中文
const defaultLocale = localStorage.getItem('datahub-locale') || 'zh-CN'

// 创建 i18n 实例
const i18n = createI18n({
  locale: defaultLocale,
  fallbackLocale: 'en-US',
  legacy: false,
  allowComposition: true,
  messages: {
    'en-US': en,
    'zh-CN': cn,
  },
})

export default i18n

