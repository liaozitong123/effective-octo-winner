<template>
  <div class="inventory-print-page">
    <div class="print-actions no-print">
      <el-button type="primary" @click="printPage">打印文档</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <section class="print-sheet">
      <header class="sheet-header">
        <h1>库存汇总未送货明细</h1>
        <div class="filter-line">
          <span>送货状态：未送货</span>
          <span>客户：{{ customerLabel }}</span>
          <span>打印日期：{{ today }}</span>
        </div>
      </header>

      <table class="inventory-table">
        <thead>
          <tr>
            <th class="col-index">序号</th>
            <th>客户</th>
            <th>送货状态</th>
            <th>接单日期</th>
            <th>产品名称</th>
            <th>规格</th>
            <th>已送货数量</th>
            <th>剩余库存</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="row.id || index">
            <td>{{ index + 1 }}</td>
            <td>{{ value(row.customerName) }}</td>
            <td class="status-cell">{{ value(row.deliveryStatus) }}</td>
            <td>{{ value(row.orderDate) }}</td>
            <td>{{ value(row.productName) }}</td>
            <td>{{ value(row.spec) }}</td>
            <td class="number-cell">{{ value(row.deliveryQty) }}</td>
            <td class="number-cell">{{ value(row.remainingStock) }}</td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="8" class="empty-cell">暂无未送货库存</td>
          </tr>
        </tbody>
        <tfoot v-if="rows.length">
          <tr>
            <td colspan="6">合计</td>
            <td class="number-cell">{{ totalDeliveryQty }}</td>
            <td class="number-cell">{{ totalRemainingStock }}</td>
          </tr>
        </tfoot>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { inventoryAPI } from '../../api/warehouse'

const route = useRoute()
const router = useRouter()
const rows = ref([])
const today = new Date().toISOString().slice(0, 10)

const customer = computed(() => String(route.query.customer || '').trim())
const customerLabel = computed(() => customer.value || '全部客户')
const totalDeliveryQty = computed(() => rows.value.reduce((sum, row) => sum + numberValue(row.deliveryQty), 0))
const totalRemainingStock = computed(() => rows.value.reduce((sum, row) => sum + numberValue(row.remainingStock), 0))

function value(v) {
  return v !== null && v !== undefined && v !== '' ? v : '-'
}

function numberValue(v) {
  const number = Number(v)
  return Number.isFinite(number) ? number : 0
}

async function loadRows() {
  const res = await inventoryAPI.list({
    page: 1,
    perPage: 10000,
    customer: customer.value,
    deliveryStatus: 'undelivered',
  })
  rows.value = res.data?.data || []
}

function printPage() {
  window.print()
}

onMounted(loadRows)
</script>

<style scoped>
@page {
  size: A4 landscape;
  margin: 10mm;
}

.inventory-print-page {
  min-height: 100vh;
  padding: 18px;
  background: #eef2f7;
  color: #000;
  font-family: SimSun, "Songti SC", serif;
}

.print-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 14px;
}

.print-sheet {
  width: 297mm;
  max-width: calc(100vw - 36px);
  min-height: 210mm;
  margin: 0 auto;
  padding: 12mm;
  background: #fff;
  box-shadow: 0 14px 36px rgba(15, 23, 42, .16);
}

.sheet-header {
  margin-bottom: 8mm;
  text-align: center;
}

.sheet-header h1 {
  margin: 0 0 5mm;
  font-size: 24px;
  line-height: 1;
  font-weight: 800;
}

.filter-line {
  display: flex;
  justify-content: space-between;
  gap: 8mm;
  font-size: 14px;
  font-weight: 700;
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 12px;
}

.inventory-table th,
.inventory-table td {
  border: 1px solid #000;
  padding: 2mm 1.5mm;
  text-align: center;
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.inventory-table th {
  height: 9mm;
  font-weight: 800;
}

.inventory-table td {
  height: 8mm;
}

.inventory-table tfoot td {
  font-weight: 800;
}

.col-index {
  width: 12mm;
}

.status-cell {
  color: #dc2626;
  font-weight: 800;
}

.number-cell {
  font-weight: 800;
}

.empty-cell {
  height: 32mm;
  color: #666;
}

@media print {
  .no-print {
    display: none;
  }

  .inventory-print-page {
    min-height: 0;
    padding: 0;
    background: #fff;
  }

  .print-sheet {
    width: 100%;
    max-width: none;
    min-height: 0;
    padding: 0;
    box-shadow: none;
  }
}
</style>
