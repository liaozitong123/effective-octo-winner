<template>
  <DataTable
    ref="tableRef"
    :columns="columns"
    :fetchData="fetchData"
    search-placeholder="搜索送货单号/销售单号/规格/司机..."
    table-max-height="calc(100vh - 232px)"
    showPrint
    hideAdd
    selectable
    :selectableRow="canSelectForMerge"
    @selection-change="handleSelectionChange"
    @edit="openEdit"
    @delete="handleDelete"
    @print="handlePrint"
  >
    <template #toolbar-actions>
      <el-button type="primary" plain :disabled="selectedRows.length < 2" @click="handleMergePrint">合并打印</el-button>
    </template>
    <template #deliveryStatus="{ row }">
      <span :class="['delivery-status', row.deliveryStatus === '已送货' ? 'is-delivered' : 'is-undelivered']">
        {{ row.deliveryStatus }}
      </span>
    </template>
  </DataTable>
  <FormDialog
    v-model="dialogVisible"
    :fields="fields"
    :isEdit="!!editId"
    :initialData="editData"
    :onSubmit="handleSubmit"
    :onChange="onFormChange"
  />
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { deliveryNotesAPI } from '../../api/sales'

const router = useRouter()
const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})
const selectedRows = ref([])
const COMMON_PRINT_FIELDS = [
  { key: 'notes', label: '备注' },
  { key: 'driver', label: '司机' },
  { key: 'deliveryDate', label: '送货日期' },
  { key: 'issuer', label: '开单人' },
  { key: 'salesperson', label: '业务员' },
  { key: 'reviewCount', label: '复核计数' },
  { key: 'customerSignature', label: '客户签字' },
]

const columns = [
  { key: 'deliveryStatus', label: '送货状态', slot: 'deliveryStatus', width: 92, minWidth: 92 },
  { key: 'salesOrderNo', label: '销售单号', minWidth: 150 },
  { key: 'orderDate', label: '下单日期', minWidth: 110 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'customerUnitPrice', label: '客户平方单价', minWidth: 120 },
  { key: 'customerMaterial', label: '客户材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'singleArea', label: '单个面积', minWidth: 110 },
  { key: 'inboundAmount', label: '入库金额', minWidth: 110 },
  { key: 'inboundQty', label: '入库数量', minWidth: 100 },
  { key: 'area', label: '面积', minWidth: 100 },
  { key: 'boxUnitPrice', label: '纸箱单价', minWidth: 110 },
  { key: 'deliveryQty', label: '送货数量', minWidth: 100 },
  { key: 'amount', label: '金额', minWidth: 100 },
  { key: 'remainingStock', label: '剩余库存', minWidth: 100 },
  { key: 'notes', label: '备注', minWidth: 150 },
  { key: 'driver', label: '司机', minWidth: 90 },
  { key: 'noteNo', label: '送货单号', minWidth: 160 },
  { key: 'deliveryDate', label: '送货日期', minWidth: 110 },
  { key: 'issuer', label: '开单人', minWidth: 90 },
  { key: 'salesperson', label: '业务员', minWidth: 90 },
  { key: 'reviewCount', label: '复核计数', minWidth: 100 },
  { key: 'customerSignature', label: '客户签字', minWidth: 110 },
]

const fields = [
  { key: 'deliveryStatus', label: '送货状态', type: 'display' },
  { key: 'salesOrderNo', label: '销售单号', type: 'display' },
  { key: 'orderDate', label: '下单日期', type: 'display' },
  { key: 'spec', label: '规格', type: 'display' },
  { key: 'customerUnitPrice', label: '客户平方单价', type: 'display' },
  { key: 'customerMaterial', label: '客户材质', type: 'display' },
  { key: 'fluteType', label: '楞别', type: 'display' },
  { key: 'singleArea', label: '单个面积', type: 'display' },
  { key: 'inboundAmount', label: '入库金额', type: 'display' },
  { key: 'inboundQty', label: '入库数量', type: 'display' },
  { key: 'area', label: '面积', type: 'display' },
  { key: 'boxUnitPrice', label: '纸箱单价', type: 'display' },
  { key: 'deliveryQty', label: '送货数量', type: 'number' },
  { key: 'amount', label: '金额', type: 'display' },
  { key: 'remainingStock', label: '剩余库存', type: 'display' },
  { key: 'notes', label: '备注', type: 'textarea' },
  { key: 'driver', label: '司机' },
  { key: 'noteNo', label: '送货单号', required: true },
  { key: 'deliveryDate', label: '送货日期', type: 'date' },
  { key: 'issuer', label: '开单人' },
  { key: 'salesperson', label: '业务员' },
  { key: 'reviewCount', label: '复核计数' },
  { key: 'customerSignature', label: '客户签字' },
]

function numberValue(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function round2(value) {
  return Math.round(value * 100) / 100
}

function round4(value) {
  return Math.round(value * 10000) / 10000
}

function calculateForm(data) {
  const deliveryQty = numberValue(data.deliveryQty)
  const area = round4(numberValue(data.singleArea) * deliveryQty)
  const amount = round2(area * numberValue(data.boxUnitPrice))
  const remainingStock = numberValue(data.inboundQty) - numberValue(data.otherDeliveredQty) - deliveryQty
  return { ...data, area, amount, remainingStock }
}

function onFormChange(form) {
  return calculateForm(form)
}

function toApiData(f) {
  return {
    productionOrder: f.productionOrderId ? { id: Number(f.productionOrderId) } : null,
    noteNo: f.noteNo || '',
    qty: numberValue(f.deliveryQty),
    notes: f.notes || '',
    driver: f.driver || '',
    deliveryDate: f.deliveryDate || null,
    issuer: f.issuer || '',
    salesperson: f.salesperson || '',
    reviewCount: f.reviewCount || '',
    customerSignature: f.customerSignature || '',
  }
}

function fetchData(p) { return deliveryNotesAPI.list(p) }

function openEdit(row) {
  editId.value = row.id
  editData.value = calculateForm({ ...row, deliveryQty: row.deliveryQty ?? row.qty ?? 0 })
  dialogVisible.value = true
}

function handlePrint(row) { router.push(`/warehouse/delivery/print?id=${row.id}`) }

function canSelectForMerge(row) {
  return !row.printed && row.deliveryStatus !== '已送货'
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function normalizeCommonValue(value) {
  return value === null || value === undefined ? '' : String(value).trim()
}

function sameCustomer(rows) {
  const first = rows[0]
  return rows.every(row => {
    if (first.customerId || row.customerId) return Number(row.customerId) === Number(first.customerId)
    return normalizeCommonValue(row.customerName) === normalizeCommonValue(first.customerName)
  })
}

function differentCommonFields(rows) {
  return COMMON_PRINT_FIELDS.filter(field => {
    const firstValue = normalizeCommonValue(rows[0]?.[field.key])
    return rows.some(row => normalizeCommonValue(row[field.key]) !== firstValue)
  })
}

function validateMergeRows(rows) {
  if (rows.length < 2) {
    ElMessage.warning('请至少勾选两张未送货送货单')
    return false
  }
  const printedRows = rows.filter(row => row.printed || row.deliveryStatus === '已送货')
  if (printedRows.length) {
    ElMessage.error('已送货的送货单不能参与合并打印')
    return false
  }
  if (!sameCustomer(rows)) {
    ElMessage.error('只能合并打印同一个客户的送货单')
    return false
  }
  const emptyQtyRows = rows.filter(row => numberValue(row.deliveryQty) <= 0)
  if (emptyQtyRows.length) {
    ElMessage.error(`无法打印：${emptyQtyRows.map(row => row.noteNo).join('、')} 的送货数量为空或为0`)
    return false
  }
  const diffFields = differentCommonFields(rows)
  if (diffFields.length) {
    ElMessage.error(`无法打印：${diffFields.map(field => field.label).join('、')}填写不同`)
    return false
  }
  return true
}

function handleMergePrint() {
  const rows = selectedRows.value
  if (!validateMergeRows(rows)) return
  router.push(`/warehouse/delivery/print?ids=${rows.map(row => row.id).join(',')}`)
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await deliveryNotesAPI.delete(row.id)
  tableRef.value.loadData()
}

async function handleSubmit(form) {
  const data = toApiData(form)
  await deliveryNotesAPI.update(editId.value, data)
  tableRef.value.loadData()
}
</script>

<style scoped>
.delivery-status {
  font-weight: 800;
}

.delivery-status.is-delivered {
  color: #16a34a;
}

.delivery-status.is-undelivered {
  color: #dc2626;
}
</style>
