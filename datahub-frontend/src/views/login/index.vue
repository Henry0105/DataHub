<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>AllData数据中台</h1>
        <p>企业级一站式数据管理平台</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
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
      
      <div class="login-footer">
        <p>默认账号: admin / admin123</p>
      </div>
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
  justify-content: center;
  background: linear-gradient(135deg, #0052d9 0%, #0034b5 100%);
  position: relative;
  overflow: hidden;
  
  // 背景装饰
  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    top: -200px;
    right: -200px;
    border-radius: 50%;
  }
  
  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
    bottom: -100px;
    left: -100px;
    border-radius: 50%;
  }
  
  .login-box {
    width: 440px;
    padding: 48px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    position: relative;
    z-index: 1;
    
    .login-header {
      text-align: center;
      margin-bottom: 48px;
      
      h1 {
        font-size: 32px;
        font-weight: 600;
        color: #1f2937;
        margin-bottom: 12px;
        letter-spacing: -0.5px;
      }
      
      p {
        font-size: 14px;
        color: #6b7280;
        font-weight: 400;
      }
    }
    
    .login-form {
      :deep(.el-form-item) {
        margin-bottom: 24px;
      }
      
      :deep(.el-input__wrapper) {
        padding: 12px 16px;
        border-radius: 8px;
        box-shadow: 0 0 0 1px #e5e7eb inset;
        
        &:hover {
          box-shadow: 0 0 0 1px #0052d9 inset;
        }
      }
      
      :deep(.el-input__inner) {
        font-size: 14px;
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

