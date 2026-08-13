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
      { path: 'production/orders', name: 'ProductionOrders', component: () => import('../views/production/ProductionOrderList.vue') },
      { path: 'production/orders/print', name: 'ProductionOrderPrint', component: () => import('../views/production/ProductionOrderPrint.vue') },
      { path: 'production/orders/label', name: 'ProductionOrderLabelPrint', component: () => import('../views/production/ProductionOrderLabelPrint.vue') },
      { path: 'production/records', name: 'ProductionRecords', component: () => import('../views/production/ProductionRecordList.vue') },
      { path: 'production/progress', redirect: '/production/orders' },
      { path: 'production/qrcode', redirect: '/production/orders' },
      { path: 'purchase/suppliers', name: 'Suppliers', component: () => import('../views/purchase/SupplierList.vue') },
      { path: 'purchase/orders', name: 'PurchaseOrders', component: () => import('../views/purchase/PurchaseOrderList.vue') },
      { path: 'purchase/receipt', name: 'GoodsReceipt', component: () => import('../views/warehouse/GoodsReceipt.vue') },
      { path: 'warehouse/inventory', name: 'Inventory', component: () => import('../views/warehouse/InventoryList.vue') },
      { path: 'warehouse/inventory/print', name: 'InventoryPrint', component: () => import('../views/warehouse/InventoryPrint.vue') },
      { path: 'warehouse/receipt', redirect: '/purchase/receipt' },
      { path: 'warehouse/delivery', name: 'DeliveryNotes', component: () => import('../views/warehouse/DeliveryNoteList.vue') },
      { path: 'warehouse/delivery/print', name: 'DeliveryNotePrint', component: () => import('../views/warehouse/DeliveryNotePrint.vue') },
      { path: 'finance/customer-reconciliation', name: 'CustomerReconciliations', component: () => import('../views/finance/ReconciliationList.vue'), meta: { pageTitle: '客户对账单', partyType: 'customer' } },
      { path: 'finance/customer-reconciliation/print', name: 'CustomerReconciliationPrint', component: () => import('../views/finance/CustomerReconciliationPrint.vue'), meta: { pageTitle: '客户对账单打印' } },
      { path: 'finance/supplier-reconciliation', name: 'SupplierReconciliations', component: () => import('../views/finance/ReconciliationList.vue'), meta: { pageTitle: '供应商对账单', partyType: 'supplier' } },
      { path: 'finance/supplier-reconciliation/print', name: 'SupplierReconciliationPrint', component: () => import('../views/finance/SupplierReconciliationPrint.vue'), meta: { pageTitle: '供应商对账单打印' } },
      { path: 'finance/receivables', name: 'Receivables', component: () => import('../views/finance/PaymentList.vue'), meta: { pageTitle: '应收款', paymentType: '收款', partyType: 'customer' } },
      { path: 'finance/payables', name: 'Payables', component: () => import('../views/finance/PaymentList.vue'), meta: { pageTitle: '应付款', paymentType: '付款', partyType: 'supplier' } },
      { path: 'finance/salary', name: 'SalarySheet', component: () => import('../views/finance/SalarySheet.vue') },
      { path: 'finance/daily-expenses', name: 'DailyExpenses', component: () => import('../views/finance/DailyExpenseSheet.vue') },
      { path: 'finance/profit', name: 'ProfitAnalysis', component: () => import('../views/finance/ProfitAnalysis.vue') },
      { path: 'finance/reconciliation', redirect: '/finance/customer-reconciliation' },
      { path: 'finance/payments', redirect: '/finance/receivables' },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.onError((error, to) => {
  const message = String(error?.message || error || '')
  const isChunkLoadError = /Failed to fetch dynamically imported module|Importing a module script failed|error loading dynamically imported module|Loading chunk/i.test(message)
  if (!isChunkLoadError) return

  const reloadKey = `erp:chunk-reload:${to.fullPath}`
  if (sessionStorage.getItem(reloadKey) === '1') {
    sessionStorage.removeItem(reloadKey)
    return
  }
  sessionStorage.setItem(reloadKey, '1')
  window.location.assign(to.fullPath)
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
