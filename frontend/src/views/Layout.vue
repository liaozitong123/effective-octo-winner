<template>
  <div class="layout">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="sidebar-logo">
          <span>📦 华天纸箱</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#0f172a"
          text-color="#94a3b8"
          active-text-color="#fff"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
          </el-menu-item>
          <el-sub-menu index="sales">
            <template #title><el-icon><Sell /></el-icon><span>销售管理</span></template>
            <el-menu-item index="/sales/customers">客户资料</el-menu-item>
            <el-menu-item index="/sales/orders">销售订单</el-menu-item>
            <el-menu-item index="/sales/delivery">送货排程</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="purchase">
            <template #title><el-icon><ShoppingCart /></el-icon><span>采购管理</span></template>
            <el-menu-item index="/purchase/suppliers">供应商资料</el-menu-item>
            <el-menu-item index="/purchase/orders">采购单</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="production">
            <template #title><el-icon><Setting /></el-icon><span>生产管理</span></template>
            <el-menu-item index="/production/orders">生产单</el-menu-item>
            <el-menu-item index="/production/records">产量登记</el-menu-item>
            <el-menu-item index="/production/progress">生产进度</el-menu-item>
            <el-menu-item index="/production/qrcode">加工区二维码</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="warehouse">
            <template #title><el-icon><Box /></el-icon><span>仓库管理</span></template>
            <el-menu-item index="/warehouse/receipt">收货/退货</el-menu-item>
            <el-menu-item index="/warehouse/inventory">库存管理</el-menu-item>
            <el-menu-item index="/warehouse/delivery">送货/退货单</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="finance">
            <template #title><el-icon><Money /></el-icon><span>财务管理</span></template>
            <el-menu-item index="/finance/reconciliation">对账单</el-menu-item>
            <el-menu-item index="/finance/payments">付款/收款单</el-menu-item>
            <el-menu-item index="/finance/profit">利润分析</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="topbar">
          <span class="topbar-title">{{ pageTitle }}</span>
          <div class="topbar-right">
            <span>管理员</span>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </el-header>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { logout } from '../api/auth'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => {
  const m = {
    '/dashboard': '仪表盘', '/sales/customers': '客户资料', '/sales/orders': '销售订单',
    '/sales/delivery': '送货排程', '/production/orders': '生产单', '/production/records': '产量登记',
    '/production/progress': '生产进度', '/production/qrcode': '加工区二维码', '/purchase/suppliers': '供应商资料', '/purchase/orders': '采购单',
    '/warehouse/receipt': '收货/退货', '/warehouse/inventory': '库存管理', '/warehouse/delivery': '送货/退货单',
    '/finance/reconciliation': '对账单', '/finance/payments': '付款/收款单', '/finance/profit': '利润分析',
  }
  return m[route.path] || ''
})

async function handleLogout() {
  try {
    await logout()
  } catch {}
  sessionStorage.removeItem('erpLoggedIn')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar { background: #0f172a; overflow-y: auto; }
.sidebar-logo { display:flex; align-items:center; justify-content:center; padding:20px; color:#fff; font-size:1.15rem; font-weight:700; border-bottom:1px solid rgba(255,255,255,0.06); }
.sidebar-menu { border-right: none; }
.topbar { display:flex; align-items:center; justify-content:space-between; background:#fff; border-bottom:1px solid #e2e8f0; height:56px; }
.topbar-title { font-size:1.1rem; font-weight:700; color:#1e293b; }
.topbar-right { display:flex; align-items:center; gap:12px; font-size:0.9rem; color:#64748b; }
.main-content { background:#f1f5f9; min-height:calc(100vh - 56px); padding:20px; }
</style>
