import { computed } from 'vue'
import { useAppStore } from '@/store/app'

export default function useTheme() {
  const appStore = useAppStore()
  
  // 是否为暗色主题
  const isDark = computed(() => {
    return appStore.theme === 'dark'
  })
  
  // 当前主题
  const theme = computed(() => {
    return appStore.theme
  })
  
  return {
    isDark,
    theme,
  }
}

