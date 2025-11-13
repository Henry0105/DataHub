<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import useLocale from '@/hooks/useLocale'

const { currentLocale } = useLocale()

// 根据当前语言返回 Element Plus 的语言包
const locale = computed(() => {
  switch (currentLocale.value) {
    case 'zh-CN':
      return zhCn
    case 'en-US':
      return en
    default:
      return zhCn
  }
})

onMounted(() => {
  console.log('DataHub数据中台启动成功！')
})
</script>

<style scoped>
#app {
  width: 100%;
  height: 100%;
}
</style>

