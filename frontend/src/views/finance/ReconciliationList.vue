<template>
  <DataTable
    ref="tableRef"
    :key="fixedPartyType"
    :columns="statementColumns"
    :fetchData="fetchData"
    :search-placeholder="searchPlaceholder"
    table-max-height="calc(100vh - 232px)"
    hideAdd
    hideActions
    @search-change="handleSearchChange"
  >
    <template #toolbar-actions>
      <el-date-picker
        v-model="statementMonth"
        type="month"
        format="YYYY-MM"
        value-format="YYYY-MM"
        placeholder="选择月份"
        style="width: 140px"
        @change="handleMonthChange"
      />
      <el-button type="primary" plain :icon="Printer" @click="openPrint">打印对账单</el-button>
      <el-button type="success" plain :icon="Download" @click="exportExcel">导出 Excel</el-button>
    </template>

    <template #deliveryStatus="{ row }">
      <span :class="['statement-status', row.deliveryStatus === '已送货' ? 'is-success' : 'is-warning']">
        {{ row.deliveryStatus }}
      </span>
    </template>

    <template #receiptStatus="{ row }">
      <span :class="['statement-status', row.status === '已收货' ? 'is-success' : 'is-warning']">
        {{ row.status || '待收货' }}
      </span>
    </template>
  </DataTable>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Printer } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import DataTable from '../../components/DataTable.vue'
import { deliveryNotesAPI } from '../../api/sales'
import { purchaseOrdersAPI } from '../../api/purchase'
import { normalizeCustomerDeliveryRows } from '../../utils/reconciliation'

const route = useRoute()
const router = useRouter()
const tableRef = ref(null)
const statementMonth = ref(currentMonth())
const statementSearch = ref('')

const fixedPartyType = computed(() => String(route.meta.partyType || ''))
const isCustomerStatement = computed(() => fixedPartyType.value === 'customer')
const pageTitle = computed(() => route.meta.pageTitle || '对账单')
const searchPlaceholder = computed(() => `搜索${pageTitle.value}...`)
const statementColumns = computed(() => isCustomerStatement.value ? customerColumns : supplierColumns)

watch(fixedPartyType, () => {
  statementSearch.value = ''
})

const customerColumns = [
  { key: 'deliveryStatus', label: '送货状态', slot: 'deliveryStatus', width: 92, minWidth: 92 },
  { key: 'salesOrderNo', label: '销售单号', minWidth: 150 },
  { key: 'orderDate', label: '接单日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 130 },
  { key: 'productName', label: '产品名称', minWidth: 140 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'customerUnitPrice', label: '客户平方单价', minWidth: 120 },
  { key: 'customerMaterial', label: '客户材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'singleArea', label: '单个面积', minWidth: 110 },
  { key: 'deliveryQty', label: '已送货数量', minWidth: 110 },
  { key: 'area', label: '面积', minWidth: 100 },
  { key: 'boxUnitPrice', label: '纸箱单价', minWidth: 110 },
  { key: 'amount', label: '金额', minWidth: 100 },
  { key: 'driver', label: '司机', minWidth: 90 },
  { key: 'noteNo', label: '送货单号', minWidth: 160 },
  { key: 'deliveryDate', label: '送货日期', minWidth: 110 },
  { key: 'issuer', label: '开单人', minWidth: 90 },
  { key: 'salesperson', label: '业务员', minWidth: 90 },
]

const supplierColumns = [
  { key: 'status', label: '收货状态', slot: 'receiptStatus', width: 92, minWidth: 92 },
  { key: 'orderNo', label: '采购单号', minWidth: 150 },
  { key: 'customerName', label: '客户', minWidth: 130 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'qty', label: '下单数量', minWidth: 100 },
  { key: 'orderDate', label: '下单日期', minWidth: 110 },
  { key: 'supplierName', label: '供应商', minWidth: 150 },
  { key: 'productionMaterial', label: '生产材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'boardLength', label: '纸板长度', minWidth: 110 },
  { key: 'boardWidth', label: '纸板宽度', minWidth: 110 },
  { key: 'boardQty', label: '纸板数量', minWidth: 110 },
  { key: 'cutCount', label: '开数', width: 72, minWidth: 72 },
  { key: 'boardArea', label: '纸板面积', minWidth: 110 },
  { key: 'totalArea', label: '总面积', minWidth: 110 },
  { key: 'materialBasePrice', label: '材质基价', minWidth: 110 },
  { key: 'discountRate', label: '折率', minWidth: 90 },
  { key: 'boardUnitPrice', label: '纸板平方单价', minWidth: 130 },
  { key: 'profitRate', label: '毛利率', minWidth: 100 },
  { key: 'boardAmount', label: '纸板金额', minWidth: 110 },
  { key: 'actualQty', label: '实收数量', minWidth: 100 },
  { key: 'actualAmount', label: '实收金额', minWidth: 110 },
  { key: 'signDate', label: '签收日期', minWidth: 110 },
  { key: 'acceptanceNotes', label: '验收说明', minWidth: 160 },
  { key: 'signer', label: '签收人', minWidth: 100 },
]

async function fetchData(params) {
  if (isCustomerStatement.value) {
    const response = await deliveryNotesAPI.list({ ...params, month: statementMonth.value })
    return {
      ...response,
      data: {
        ...response.data,
        data: normalizeCustomerDeliveryRows(response.data?.data),
      },
    }
  }
  return purchaseOrdersAPI.list({ ...params, month: statementMonth.value, signStatus: 'signed' })
}

function handleMonthChange() {
  tableRef.value?.loadData()
}

function handleSearchChange(value) {
  statementSearch.value = String(value || '').trim()
}

function openPrint() {
  router.push({
    path: isCustomerStatement.value ? '/finance/customer-reconciliation/print' : '/finance/supplier-reconciliation/print',
    query: {
      month: statementMonth.value,
      ...(statementSearch.value ? { q: statementSearch.value } : {}),
    },
  })
}

async function exportExcel() {
  try {
    const rows = await loadExportRows()
    if (!rows.length) {
      ElMessage.warning('当前筛选条件下没有可导出的数据')
      return
    }

    const headers = statementColumns.value.map(column => column.label)
    const data = rows.map(row => statementColumns.value.map(column => exportValue(row[column.key])))
    const worksheet = XLSX.utils.aoa_to_sheet([headers, ...data])
    worksheet['!cols'] = statementColumns.value.map(column => ({
      wch: Math.max(10, Math.min(24, String(column.label || '').length * 2 + 4)),
    }))

    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, isCustomerStatement.value ? '客户对账单' : '供应商对账单')
    XLSX.writeFile(workbook, `${pageTitle.value}_${statementMonth.value}${statementSearch.value ? `_${sanitizeFileName(statementSearch.value)}` : ''}.xlsx`)
    ElMessage.success(`已导出 ${rows.length} 条明细`)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '导出 Excel 失败')
  }
}

async function loadExportRows() {
  const params = {
    q: statementSearch.value,
    month: statementMonth.value,
    page: 1,
    perPage: 9999,
  }
  const res = isCustomerStatement.value
    ? await deliveryNotesAPI.list(params)
    : await purchaseOrdersAPI.list({ ...params, signStatus: 'signed' })
  return isCustomerStatement.value
    ? normalizeCustomerDeliveryRows(res.data?.data)
    : res.data?.data || []
}

function exportValue(value) {
  return value === null || value === undefined ? '' : value
}

function sanitizeFileName(value) {
  return String(value).replace(/[\\/:*?"<>|]/g, '_').slice(0, 40)
}

function currentMonth() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}
</script>

<style scoped>
.statement-status {
  font-weight: 800;
}

.statement-status.is-success {
  color: #16a34a;
}

.statement-status.is-warning {
  color: #dc2626;
}
</style>
