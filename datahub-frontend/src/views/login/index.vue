<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-container">
          <video 
            autoplay 
            loop 
            muted 
            playsinline
            class="logo-video"
          >
            <source src="/src/assets/animation.webm" type="video/webm">
          </video>
        </div>
        <p>企业级一站式数据管理平台</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
// TDesign 风格的登录页面
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 10%;
  background-image: url('https://cloudcache.tencent-cloud.com/qcloud/ui/static/tc_portal/c3f153dd-80a0-40a8-9a54-a3b7d6717ab8.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  overflow: hidden;
  
  // 背景遮罩,让登录框更突出
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.2);
    z-index: 0;
  }
  
  .login-box {
    width: 520px;
    padding: 56px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    position: relative;
    z-index: 1;
    
    .login-header {
      text-align: center;
      margin-bottom: 48px;
      
      .logo-container {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 16px;
        height: 120px;
        overflow: hidden;
        
        .logo-video {
          height: 200px;
          width: auto;
          object-fit: cover;
          transform: scale(1.3);
        }
      }
      
      h1 {
        font-size: 28px;
        font-weight: 600;
        color: #1f2937;
        margin-bottom: 8px;
        letter-spacing: 1px;
      }
      
      p {
        font-size: 14px;
        color: #6b7280;
        font-weight: 400;
      }
    }
    
    .login-form {
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: #374151;
        margin-bottom: 8px;
        font-size: 14px;
      }
      
      :deep(.el-form-item) {
        margin-bottom: 28px;
      }
      
      :deep(.el-input__wrapper) {
        padding: 14px 16px;
        border-radius: 8px;
        box-shadow: 0 0 0 1px #e5e7eb inset;
        
        &:hover {
          box-shadow: 0 0 0 1px #0052d9 inset;
        }
      }
      
      :deep(.el-input__inner) {
        font-size: 14px;
        height: 22px;
      }
      
      .login-button {
        width: 100%;
        height: 44px;
        font-size: 16px;
        font-weight: 500;
        border-radius: 8px;
        background: #0052d9;
        border-color: #0052d9;
        
        &:hover {
          background: #0034b5;
          border-color: #0034b5;
        }
      }
    }
    
    .login-footer {
      text-align: center;
      margin-top: 24px;
      font-size: 13px;
      color: #9ca3af;
      padding: 16px;
      background: #f9fafb;
      border-radius: 8px;
    }
  }
}
</style>

