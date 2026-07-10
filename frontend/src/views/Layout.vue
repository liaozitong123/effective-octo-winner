<template>
  <div class="layout" :class="{ 'is-collapsed': isSidebarCollapsed }">
    <el-container class="layout-container">
      <el-aside :width="asideWidth" class="sidebar">
        <div class="sidebar-logo">
          <span class="brand-mark">箱</span>
          <span class="brand-text">华天纸箱</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          :collapse="isSidebarCollapsed"
          :collapse-transition="false"
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
          <div class="topbar-left">
            <el-button class="collapse-btn" :icon="Menu" text @click="toggleSidebar" />
            <span class="topbar-title">{{ pageTitle }}</span>
          </div>
          <div class="topbar-right">
            <span class="system-badge">ERP 工作台</span>
            <span class="user-pill">管理员</span>
            <el-button type="danger" plain size="small" @click="handleLogout">退出</el-button>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'
import { logout } from '../api/auth'

const router = useRouter()
const route = useRoute()
const isSidebarCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const asideWidth = computed(() => isSidebarCollapsed.value ? '72px' : '232px')
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

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

function syncSidebarByViewport() {
  if (window.innerWidth <= 900) isSidebarCollapsed.value = true
}

onMounted(() => {
  syncSidebarByViewport()
  window.addEventListener('resize', syncSidebarByViewport)
})

onBeforeUnmount(() => window.removeEventListener('resize', syncSidebarByViewport))

async function handleLogout() {
  try {
    await logout()
  } catch {}
  sessionStorage.removeItem('erpLoggedIn')
  router.push('/login')
}
</script>

<style scoped>
.layout,
.layout-container {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, var(--erp-sidebar), var(--erp-sidebar-deep));
  overflow-x: hidden;
  overflow-y: auto;
  transition: width .18s ease;
  box-shadow: 8px 0 24px rgba(15, 23, 42, .08);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 20px;
  color: #fff;
  font-size: 1.15rem;
  font-weight: 800;
  border-bottom: 1px solid rgba(255,255,255,0.07);
  white-space: nowrap;
}

.brand-mark {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f59e0b;
  color: #fff;
  font-size: 15px;
  flex: none;
}

.brand-text {
  overflow: hidden;
}

.sidebar-menu {
  border-right: none;
  padding: 12px 8px;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
  border-radius: 7px;
  margin: 3px 0;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--erp-primary);
  box-shadow: 0 8px 18px rgba(37, 99, 235, .25);
}

.is-collapsed .brand-text {
  display: none;
}

.is-collapsed .sidebar-logo {
  justify-content: center;
  padding: 0;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255,255,255,.9);
  border-bottom: 1px solid var(--erp-border);
  height: 58px;
  padding: 0 22px;
  backdrop-filter: blur(8px);
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.collapse-btn {
  color: #526178;
}

.topbar-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--erp-text);
  white-space: nowrap;
}

.topbar-right {
  font-size: 0.9rem;
  color: var(--erp-muted);
}

.system-badge,
.user-pill {
  height: 30px;
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--erp-border);
  border-radius: 999px;
  background: #fff;
  padding: 0 10px;
}

.main-content {
  background: var(--erp-bg);
  min-height: calc(100vh - 58px);
  padding: 18px 20px 22px;
}

@media (max-width: 720px) {
  .topbar {
    padding: 0 12px;
  }

  .system-badge {
    display: none;
  }

  .topbar-right {
    gap: 8px;
  }

  .main-content {
    padding: 12px;
  }
}
</style>
