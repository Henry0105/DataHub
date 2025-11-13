<template>
  <div class="iframe-container">
    <iframe
      :src="iframeUrl"
      frameborder="0"
      width="100%"
      height="100%"
      @load="handleIframeLoad"
    ></iframe>
    <div v-if="loading" class="loading-overlay">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>正在加载 大模型应用开发平台...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const iframeUrl = ref('')
const loading = ref(true)

onMounted(() => {
  // TODO: 从环境变量或配置中获取 BISHENG 服务地址
  // iframeUrl.value = import.meta.env.VITE_BISHENG_URL || 'http://localhost:3001'
  
  // 暂时显示提示信息
  loading.value = false
  ElMessage.info('大模型应用开发平台服务尚未部署，请先在 Docker 中启动 BISHENG 服务')
})

const handleIframeLoad = () => {
  loading.value = false
}
</script>

<style scoped lang="scss">
.iframe-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  
  iframe {
    display: block;
  }
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  z-index: 10;
  
  p {
    margin-top: 16px;
    font-size: 14px;
    color: #6b7280;
  }
}
</style>
