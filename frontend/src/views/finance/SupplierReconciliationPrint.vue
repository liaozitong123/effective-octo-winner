<template>
  <div class="supplier-statement-print">
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
        <h2>供应商对账单（{{ displayMonth }}）</h2>
        <p>按签收日期筛选的当月收货对账明细</p>
      </header>

      <section class="month-summary">
        <div><span>供应商数量：</span><strong>{{ groupedRows.length }}</strong></div>
        <div><span>纸板数量：</span><strong>{{ totals.boardQty }}</strong></div>
        <div><span>实收数量：</span><strong>{{ totals.actualQty }}</strong></div>
        <div><span>总面积：</span><strong>{{ totals.totalArea }}</strong></div>
        <div><span>纸板金额：</span><strong>{{ totals.boardAmount }}</strong></div>
        <div><span>实收金额：</span><strong>{{ totals.actualAmount }}</strong></div>
      </section>

      <section
        v-for="(group, groupIndex) in groupedRows"
        :key="group.key"
        class="supplier-section"
        :class="{ 'is-last': groupIndex === groupedRows.length - 1 }"
      >
        <div class="supplier-header">
          <div class="supplier-title">{{ group.supplierName }}</div>
        </div>

        <table class="detail-table">
          <colgroup>
            <col class="col-status" />
            <col class="col-order" />
            <col class="col-customer" />
            <col class="col-spec" />
            <col class="col-qty" />
            <col class="col-date" />
            <col class="col-supplier" />
            <col class="col-material" />
            <col class="col-flute" />
            <col class="col-size" />
            <col class="col-size" />
            <col class="col-qty" />
            <col class="col-cut" />
            <col class="col-area" />
            <col class="col-area" />
            <col class="col-price" />
            <col class="col-rate" />
            <col class="col-price" />
            <col class="col-rate" />
            <col class="col-amount" />
            <col class="col-qty" />
            <col class="col-amount" />
            <col class="col-date" />
            <col class="col-notes" />
            <col class="col-person" />
          </colgroup>
          <thead>
            <tr>
              <th>收货状态</th>
              <th>采购单号</th>
              <th>客户</th>
              <th>规格</th>
              <th>下单数量</th>
              <th>下单日期</th>
              <th>供应商</th>
              <th>生产材质</th>
              <th>楞别</th>
              <th>纸板长度</th>
              <th>纸板宽度</th>
              <th>纸板数量</th>
              <th>开数</th>
              <th>纸板面积</th>
              <th>总面积</th>
              <th>材质基价</th>
              <th>折率</th>
              <th>纸板平方单价</th>
              <th>毛利率</th>
              <th>纸板金额</th>
              <th>实收数量</th>
              <th>实收金额</th>
              <th>签收日期</th>
              <th>验收说明</th>
              <th>签收人</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in group.rows" :key="row.id">
              <td :class="statusClass(row)">{{ value(row.status || '待收货') }}</td>
              <td>{{ value(row.orderNo) }}</td>
              <td>{{ value(row.customerName) }}</td>
              <td>{{ value(row.spec) }}</td>
              <td>{{ value(row.qty) }}</td>
              <td>{{ value(row.orderDate) }}</td>
              <td>{{ value(row.supplierName) }}</td>
              <td>{{ value(row.productionMaterial) }}</td>
              <td>{{ value(row.fluteType) }}</td>
              <td>{{ value(row.boardLength) }}</td>
              <td>{{ value(row.boardWidth) }}</td>
              <td>{{ value(row.boardQty) }}</td>
              <td>{{ value(row.cutCount) }}</td>
              <td>{{ value(row.boardArea) }}</td>
              <td>{{ value(row.totalArea) }}</td>
              <td>{{ value(row.materialBasePrice) }}</td>
              <td>{{ value(row.discountRate) }}</td>
              <td>{{ value(row.boardUnitPrice) }}</td>
              <td>{{ value(row.profitRate) }}</td>
              <td>{{ value(row.boardAmount) }}</td>
              <td>{{ value(row.actualQty) }}</td>
              <td>{{ value(row.actualAmount) }}</td>
              <td>{{ value(row.signDate) }}</td>
              <td>{{ value(row.acceptanceNotes) }}</td>
              <td>{{ value(row.signer) }}</td>
            </tr>
          </tbody>
        </table>

        <div class="group-total">
          <span>小计</span>
          <strong>纸板数量：{{ group.totals.boardQty }}</strong>
          <strong>实收数量：{{ group.totals.actualQty }}</strong>
          <strong>总面积：{{ group.totals.totalArea }}</strong>
          <strong>纸板金额：{{ group.totals.boardAmount }}</strong>
          <strong>实收金额：{{ group.totals.actualAmount }}</strong>
        </div>
      </section>
    </div>

    <el-empty v-else-if="!printError && !loading" description="未找到当月对账明细" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { purchaseOrdersAPI } from '../../api/purchase'

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
    const key = String(row.supplierId || row.supplierName || 'unknown')
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        supplierName: row.supplierName || '未指定供应商',
        rows: [],
      })
    }
    groups.get(key).rows.push(row)
  }
  return Array.from(groups.values())
    .sort((a, b) => a.supplierName.localeCompare(b.supplierName, 'zh-Hans-CN'))
    .map(group => ({
      ...group,
      rows: [...group.rows].sort((a, b) => {
        const dateDiff = String(a.signDate || '').localeCompare(String(b.signDate || ''))
        if (dateDiff !== 0) return dateDiff
        return String(a.orderNo || '').localeCompare(String(b.orderNo || ''))
      }),
      totals: summarize(group.rows),
    }))
})

const totals = computed(() => summarize(rows.value))

function summarize(list) {
  return {
    boardQty: roundNumber(list.reduce((sum, row) => sum + numberValue(row.boardQty), 0)),
    actualQty: roundNumber(list.reduce((sum, row) => sum + numberValue(row.actualQty), 0)),
    totalArea: round4(list.reduce((sum, row) => sum + numberValue(row.totalArea), 0)),
    boardAmount: round2(list.reduce((sum, row) => sum + numberValue(row.boardAmount), 0)),
    actualAmount: round2(list.reduce((sum, row) => sum + numberValue(row.actualAmount), 0)),
  }
}

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
  return row.status === '已收货' ? 'is-received' : 'is-pending'
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
    const res = await purchaseOrdersAPI.list({
      q: searchText.value,
      month: month.value,
      signStatus: 'signed',
      page: 1,
      perPage: 9999,
    })
    rows.value = res.data?.data || []
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
.supplier-statement-print {
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

.sheet-header {
  text-align: center;
  margin-bottom: 8px;
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
  gap: 10px 18px;
  margin-bottom: 10px;
  padding: 8px 10px;
  border: 1px solid #d7dee8;
  border-radius: 6px;
  background: #f8fbff;
  font-size: 12px;
}

.supplier-section {
  margin-bottom: 12px;
  break-after: page;
}

.supplier-section.is-last {
  break-after: auto;
}

.supplier-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 5px;
}

.supplier-title {
  font-size: 14px;
  font-weight: 800;
  color: #111827;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 6px;
}

.detail-table th,
.detail-table td {
  border: 1px solid #000;
  padding: 1mm 0.6mm;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

.detail-table th {
  background: #f3f4f6;
  font-weight: 700;
}

.is-received {
  color: #16a34a;
  font-weight: 700;
}

.is-pending {
  color: #dc2626;
  font-weight: 700;
}

.group-total {
  display: flex;
  gap: 14px;
  justify-content: flex-end;
  align-items: center;
  margin-top: 4px;
  font-size: 11px;
}

@page {
  size: A4 landscape;
  margin: 7mm;
}

@media print {
  .no-print {
    display: none !important;
  }

  .supplier-statement-print {
    padding: 0;
  }

  .supplier-section {
    break-after: page;
  }

  .supplier-section.is-last {
    break-after: auto;
  }
}

.col-status { width: 13mm; }
.col-order { width: 18mm; }
.col-customer { width: 15mm; }
.col-spec { width: 22mm; }
.col-qty { width: 11mm; }
.col-date { width: 15mm; }
.col-supplier { width: 18mm; }
.col-material { width: 15mm; }
.col-flute { width: 9mm; }
.col-size { width: 11mm; }
.col-cut { width: 8mm; }
.col-area { width: 11mm; }
.col-price { width: 13mm; }
.col-rate { width: 10mm; }
.col-amount { width: 12mm; }
.col-notes { width: 18mm; }
.col-person { width: 10mm; }
</style>
