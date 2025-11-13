import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

export default function useLocale() {
  const i18 = useI18n()
  
  // 当前语言
  const currentLocale = computed(() => {
    return i18.locale.value
  })
  
  // 切换语言
  const changeLocale = (value: string) => {
    if (i18.locale.value === value) {
      return
    }
    i18.locale.value = value
    localStorage.setItem('datahub-locale', value) // 保存到本地存储
    ElMessage.success(i18.t('settings.languageChanged'))
  }
  
  return {
    currentLocale,
    changeLocale,
  }
}

