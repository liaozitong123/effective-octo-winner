<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>📦 华天纸箱管理系统</h1>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  loading.value = true
  try {
    await login(form.username, form.password)
    sessionStorage.setItem('erpLoggedIn', '1')
    router.push(route.query.redirect || '/dashboard')
  } catch (e) {}
  loading.value = false
}
</script>

<style scoped>
.login-page { display:flex; align-items:center; justify-content:center; min-height:100vh; background:linear-gradient(135deg,#0f172a,#1e293b,#0f172a); }
.login-card { background:#fff; border-radius:16px; padding:48px 40px; width:100%; max-width:420px; box-shadow:0 20px 60px rgba(0,0,0,0.3); }
.login-header { text-align:center; margin-bottom:36px; }
.login-header h1 { font-size:1.6rem; color:#1e293b; margin-bottom:6px; }
.login-header p { font-size:0.85rem; color:#64748b; }
.login-btn { width:100%; }
</style>
