import { createRouter, createWebHistory } from 'vue-router'
import request from '../api/request'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/production/report', name: 'ScanReport', component: () => import('../views/production/ScanReport.vue') },
  {
    path: '/', component: () => import('../views/Layout.vue'),
    redirect: '/login',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'sales/customers', name: 'Customers', component: () => import('../views/sales/CustomerList.vue') },
      { path: 'sales/orders', name: 'SalesOrders', component: () => import('../views/sales/SalesOrderList.vue') },
      { path: 'sales/delivery', name: 'DeliverySchedule', component: () => import('../views/sales/DeliverySchedule.vue') },
      { path: 'production/orders', name: 'ProductionOrders', component: () => import('../views/production/ProductionOrderList.vue') },
      { path: 'production/orders/print', name: 'ProductionOrderPrint', component: () => import('../views/production/ProductionOrderPrint.vue') },
      { path: 'production/records', name: 'ProductionRecords', component: () => import('../views/production/ProductionRecordList.vue') },
      { path: 'production/progress', name: 'ProductionProgress', component: () => import('../views/production/ProductionProgress.vue') },
      { path: 'production/qrcode', name: 'QRCodePage', component: () => import('../views/production/QRCodePage.vue') },
      { path: 'purchase/suppliers', name: 'Suppliers', component: () => import('../views/purchase/SupplierList.vue') },
      { path: 'purchase/orders', name: 'PurchaseOrders', component: () => import('../views/purchase/PurchaseOrderList.vue') },
      { path: 'warehouse/inventory', name: 'Inventory', component: () => import('../views/warehouse/InventoryList.vue') },
      { path: 'warehouse/receipt', name: 'GoodsReceipt', component: () => import('../views/warehouse/GoodsReceipt.vue') },
      { path: 'warehouse/delivery', name: 'DeliveryNotes', component: () => import('../views/warehouse/DeliveryNoteList.vue') },
      { path: 'warehouse/delivery/print', name: 'DeliveryNotePrint', component: () => import('../views/warehouse/DeliveryNotePrint.vue') },
      { path: 'finance/reconciliation', name: 'Reconciliations', component: () => import('../views/finance/ReconciliationList.vue') },
      { path: 'finance/payments', name: 'Payments', component: () => import('../views/finance/PaymentList.vue') },
      { path: 'finance/profit', name: 'ProfitAnalysis', component: () => import('../views/finance/ProfitAnalysis.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const publicPaths = ['/login', '/production/report']

router.beforeEach(async (to) => {
  if (publicPaths.includes(to.path)) return true

  if (sessionStorage.getItem('erpLoggedIn') !== '1') {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  try {
    await request.get('/current-user', { skipAuthRedirect: true })
    return true
  } catch {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
