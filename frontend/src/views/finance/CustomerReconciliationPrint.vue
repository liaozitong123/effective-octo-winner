<template>
  <div class="customer-statement-print">
    <div class="print-actions no-print">
      <el-button type="primary" @click="printPage">打印</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-alert
      v-if="printError"
      class="print-error no-print"
      type="error"
      :title="printError"
      show-icon
      :closable="false"
    />

    <div v-if="groupedRows.length && !printError" class="print-sheet">
      <header class="sheet-header">
        <h1>华天纸箱厂</h1>
        <h2>客户对账单（{{ displayMonth }}）</h2>
        <p>按送货日期筛选的当月对账明细</p>
      </header>

      <section class="month-summary">
        <div><span>客户数量：</span><strong>{{ groupedRows.length }}</strong></div>
        <div><span>送货数量：</span><strong>{{ totals.deliveryQty }}</strong></div>
        <div><span>面积：</span><strong>{{ totals.area }}</strong></div>
        <div><span>金额：</span><strong>{{ totals.amount }}</strong></div>
      </section>

      <section
        v-for="(group, groupIndex) in groupedRows"
        :key="group.key"
        class="customer-section"
        :class="{ 'is-last': groupIndex === groupedRows.length - 1 }"
      >
        <div class="customer-header">
          <div class="customer-title">{{ group.customerName }}</div>
          <div class="customer-meta">
            <span v-if="group.contact">联系人：{{ group.contact }}</span>
            <span v-if="group.phone">电话：{{ group.phone }}</span>
            <span v-if="group.address">地址：{{ group.address }}</span>
          </div>
        </div>

        <table class="detail-table">
          <colgroup>
            <col class="col-status" />
            <col class="col-sales" />
            <col class="col-date" />
            <col class="col-customer" />
            <col class="col-product" />
            <col class="col-spec" />
            <col class="col-price" />
            <col class="col-material" />
            <col class="col-flute" />
            <col class="col-area-single" />
            <col class="col-qty" />
            <col class="col-area" />
            <col class="col-price" />
            <col class="col-amount" />
            <col class="col-driver" />
            <col class="col-note" />
            <col class="col-date" />
            <col class="col-person" />
            <col class="col-person" />
          </colgroup>
          <thead>
            <tr>
              <th>送货状态</th>
              <th>销售单号</th>
              <th>接单日期</th>
              <th>客户</th>
              <th>产品名称</th>
              <th>规格</th>
              <th>客户平方单价</th>
              <th>客户材质</th>
              <th>楞别</th>
              <th>单个面积</th>
              <th>已送货数量</th>
              <th>面积</th>
              <th>纸箱单价</th>
              <th>金额</th>
              <th>司机</th>
              <th>送货单号</th>
              <th>送货日期</th>
              <th>开单人</th>
              <th>业务员</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in group.rows" :key="row.id">
              <td :class="statusClass(row)">{{ value(row.deliveryStatus) }}</td>
              <td>{{ value(row.salesOrderNo) }}</td>
              <td>{{ value(row.orderDate) }}</td>
              <td>{{ value(row.customerName) }}</td>
              <td>{{ value(row.productName) }}</td>
              <td>{{ value(row.spec) }}</td>
              <td>{{ value(row.customerUnitPrice) }}</td>
              <td>{{ value(row.customerMaterial) }}</td>
              <td>{{ value(row.fluteType) }}</td>
              <td>{{ value(row.singleArea) }}</td>
              <td>{{ value(row.deliveryQty) }}</td>
              <td>{{ value(row.area) }}</td>
              <td>{{ value(row.boxUnitPrice) }}</td>
              <td>{{ value(row.amount) }}</td>
              <td>{{ value(row.driver) }}</td>
              <td>{{ value(row.noteNo) }}</td>
              <td>{{ value(row.deliveryDate) }}</td>
              <td>{{ value(row.issuer) }}</td>
              <td>{{ value(row.salesperson) }}</td>
            </tr>
          </tbody>
        </table>

        <div class="group-total">
          <span>小计</span>
          <strong>送货数量：{{ group.totals.deliveryQty }}</strong>
          <strong>面积：{{ group.totals.area }}</strong>
          <strong>金额：{{ group.totals.amount }}</strong>
        </div>
      </section>
    </div>

    <el-empty v-else-if="!printError && !loading" description="未找到当月对账明细" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deliveryNotesAPI } from '../../api/sales'
import { normalizeCustomerDeliveryRows } from '../../utils/reconciliation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const printError = ref('')
const rows = ref([])

const month = computed(() => normalizeMonth(route.query.month) || currentMonth())
const searchText = computed(() => String(route.query.q || '').trim())
const displayMonth = computed(() => month.value.replace('-', '年') + '月')

const groupedRows = computed(() => {
  const groups = new Map()
  for (const row of rows.value) {
    const key = String(row.customerId || row.customerName || 'unknown')
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        customerName: row.customerName || '未指定客户',
        contact: row.customerContact || '',
        phone: row.customerPhone || '',
        address: row.customerAddress || '',
        rows: [],
      })
    }
    groups.get(key).rows.push(row)
  }
  return Array.from(groups.values())
    .sort((a, b) => a.customerName.localeCompare(b.customerName, 'zh-Hans-CN'))
    .map(group => ({
      ...group,
      rows: [...group.rows].sort((a, b) => {
        const dateDiff = String(a.deliveryDate || '').localeCompare(String(b.deliveryDate || ''))
        if (dateDiff !== 0) return dateDiff
        return String(a.noteNo || a.salesOrderNo || '').localeCompare(String(b.noteNo || b.salesOrderNo || ''))
      }),
      totals: {
        deliveryQty: roundNumber(group.rows.reduce((sum, row) => sum + numberValue(row.deliveryQty), 0)),
        area: round4(group.rows.reduce((sum, row) => sum + numberValue(row.area), 0)),
        amount: round2(group.rows.reduce((sum, row) => sum + numberValue(row.amount), 0)),
      },
    }))
})

const totals = computed(() => ({
  deliveryQty: roundNumber(rows.value.reduce((sum, row) => sum + numberValue(row.deliveryQty), 0)),
  area: round4(rows.value.reduce((sum, row) => sum + numberValue(row.area), 0)),
  amount: round2(rows.value.reduce((sum, row) => sum + numberValue(row.amount), 0)),
}))

function value(v) {
  return v !== null && v !== undefined && v !== '' ? v : '-'
}

function numberValue(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

function round2(v) {
  return Math.round(v * 100) / 100
}

function round4(v) {
  return Math.round(v * 10000) / 10000
}

function roundNumber(v) {
  return Math.round(v * 100) / 100
}

function statusClass(row) {
  return row.deliveryStatus === '已送货' ? 'is-delivered' : 'is-undelivered'
}

function normalizeMonth(value) {
  if (!value) return ''
  const text = String(value).trim()
  return /^\d{4}-\d{2}$/.test(text) ? text : ''
}

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

async function loadRows() {
  loading.value = true
  printError.value = ''
  try {
    const res = await deliveryNotesAPI.list({
      q: searchText.value,
      month: month.value,
      page: 1,
      perPage: 9999,
    })
    rows.value = normalizeCustomerDeliveryRows(res.data?.data)
  } catch (error) {
    printError.value = error?.response?.data?.message || '加载对账明细失败'
  }
  loading.value = false
}

function printPage() {
  if (!groupedRows.value.length) {
    ElMessage.warning('没有可打印的数据')
    return
  }
  window.print()
}

onMounted(loadRows)
</script>

<style scoped>
.customer-statement-print {
  padding: 16px;
}

.print-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.print-error {
  margin-bottom: 12px;
}

.print-sheet {
  width: 100%;
}

.sheet-header {
  text-align: center;
  margin-bottom: 10px;
}

.sheet-header h1 {
  font-size: 20px;
  margin: 0;
}

.sheet-header h2 {
  font-size: 16px;
  margin: 4px 0 0;
}

.sheet-header p {
  margin: 4px 0 0;
  color: #5b6472;
}

.month-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  margin-bottom: 12px;
  padding: 8px 10px;
  border: 1px solid #d7dee8;
  border-radius: 6px;
  background: #f8fbff;
  font-size: 12px;
}

.customer-section {
  margin-bottom: 12px;
  break-after: page;
}

.customer-section.is-last {
  break-after: auto;
}

.customer-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 6px;
}

.customer-title {
  font-size: 14px;
  font-weight: 800;
  color: #111827;
}

.customer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  justify-content: flex-end;
  color: #475569;
  font-size: 11px;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 7px;
}

.detail-table th,
.detail-table td {
  border: 1px solid #000;
  padding: 1.2mm 0.8mm;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

.detail-table th {
  background: #f3f4f6;
  font-weight: 700;
}

.is-delivered {
  color: #16a34a;
  font-weight: 700;
}

.is-undelivered {
  color: #dc2626;
  font-weight: 700;
}

.group-total {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  align-items: center;
  margin-top: 4px;
  font-size: 12px;
}

@page {
  size: A4 landscape;
  margin: 8mm;
}

@media print {
  .no-print {
    display: none !important;
  }

  .customer-statement-print {
    padding: 0;
  }

  .customer-section {
    break-after: page;
  }

  .customer-section.is-last {
    break-after: auto;
  }
}

.col-status { width: 14mm; }
.col-sales { width: 20mm; }
.col-date { width: 16mm; }
.col-customer { width: 16mm; }
.col-product { width: 18mm; }
.col-spec { width: 22mm; }
.col-price { width: 16mm; }
.col-material { width: 16mm; }
.col-flute { width: 10mm; }
.col-area-single { width: 14mm; }
.col-qty { width: 12mm; }
.col-area { width: 12mm; }
.col-amount { width: 12mm; }
.col-driver { width: 12mm; }
.col-note { width: 18mm; }
.col-person { width: 12mm; }
</style>
