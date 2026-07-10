<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-mark">箱</div>
        <h1>华天纸箱管理系统</h1>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" autocomplete="off" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" autocomplete="new-password" show-password @keyup.enter="handleLogin" />
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
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(37,99,235,.18), transparent 30%),
    linear-gradient(180deg, #101928, #0c1320);
}

.login-card {
  width: 100%;
  max-width: 420px;
  border: 1px solid rgba(255,255,255,.16);
  border-radius: 8px;
  padding: 42px 40px 38px;
  background: rgba(255,255,255,.98);
  box-shadow: 0 22px 60px rgba(0,0,0,.28);
}

.login-header {
  display: grid;
  justify-items: center;
  gap: 12px;
  text-align: center;
  margin-bottom: 32px;
}

.login-mark {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: #f59e0b;
  color: #fff;
  font-size: 20px;
  font-weight: 800;
}

.login-header h1 {
  font-size: 1.55rem;
  color: var(--erp-text);
  letter-spacing: 0;
}

.login-btn {
  width: 100%;
  margin-top: 4px;
  box-shadow: 0 10px 20px rgba(37,99,235,.22);
}

@media (max-width: 480px) {
  .login-page {
    padding: 14px;
  }

  .login-card {
    padding: 32px 22px;
  }
}
</style>
