<template>
  <div class="receipt-page">
    <DataTable
      ref="tableRef"
      :columns="columns"
      :fetchData="fetchData"
      search-placeholder="搜索采购单号/客户/供应商/规格..."
      table-max-height="calc(100vh - 246px)"
      hideAdd
      @edit="openEdit"
      @delete="handleDelete"
    >
      <template #status="{ row }">
        <el-tag :type="statusTagType(row.status)" size="small">{{ row.status || '待收货' }}</el-tag>
      </template>
      <template #profitRate="{ row }">
        <span>{{ displayProfitRate(row) }}</span>
      </template>
    </DataTable>
  </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { purchaseOrdersAPI } from '../../api/purchase'
import { applyBoardCalculation } from '../../utils/boardCalculation'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'status', label: '收货状态', slot: 'status', width: 92, minWidth: 92 },
  { key: 'orderNo', label: '采购单号', minWidth: 150 },
  { key: 'customerName', label: '客户', minWidth: 150 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'qty', label: '下单数量', minWidth: 100 },
  { key: 'supplierName', label: '供应商', minWidth: 150 },
  { key: 'productionMaterial', label: '生产材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'boardLength', label: '纸板长度', minWidth: 110 },
  { key: 'boardWidth', label: '纸板宽度', minWidth: 110 },
  { key: 'boardQty', label: '纸板数量', minWidth: 110 },
  { key: 'cutCount', label: '开数', width: 72, minWidth: 72 },
  { key: 'crease', label: '凹压线', minWidth: 170 },
  { key: 'boardArea', label: '纸板面积', minWidth: 110 },
  { key: 'totalArea', label: '总面积', minWidth: 110 },
  { key: 'materialBasePrice', label: '材质基价', minWidth: 110 },
  { key: 'discountRate', label: '折率%', minWidth: 90 },
  { key: 'boardUnitPrice', label: '纸板平方单价', minWidth: 130 },
  { key: 'profitRate', label: '毛利率%', slot: 'profitRate', minWidth: 100 },
  { key: 'boardAmount', label: '纸板金额', minWidth: 110 },
  { key: 'actualQty', label: '实收数量', minWidth: 100 },
  { key: 'actualAmount', label: '实收金额', minWidth: 110 },
  { key: 'signDate', label: '签收日期', minWidth: 110 },
  { key: 'acceptanceNotes', label: '验收说明', minWidth: 160 },
  { key: 'signer', label: '签收人', minWidth: 100 },
]

const fields = [
  { key: 'orderNo', label: '采购单号', type: 'display' },
  { key: 'customerName', label: '客户', type: 'display' },
  { key: 'spec', label: '规格', type: 'display' },
  { key: 'qty', label: '下单数量', type: 'display' },
  { key: 'supplierName', label: '供应商', type: 'display' },
  { key: 'productionMaterial', label: '生产材质', type: 'display' },
  { key: 'fluteType', label: '楞别', type: 'display' },
  { key: 'boardLength', label: '纸板长度', type: 'display' },
  { key: 'boardWidth', label: '纸板宽度', type: 'display' },
  { key: 'boardQty', label: '纸板数量', type: 'display' },
  { key: 'cutCount', label: '开数', type: 'display' },
  { key: 'crease', label: '凹压线', type: 'display' },
  { key: 'boardArea', label: '纸板面积', type: 'display' },
  { key: 'totalArea', label: '总面积', type: 'display' },
  { key: 'materialBasePrice', label: '材质基价', type: 'display' },
  { key: 'discountRate', label: '折率%', type: 'display' },
  { key: 'boardUnitPrice', label: '纸板平方单价', type: 'display' },
  { key: 'profitRate', label: '毛利率%', type: 'display', suffix: '%' },
  { key: 'boardAmount', label: '纸板金额', type: 'display' },
  { key: 'status', label: '收货状态', type: 'select', options: ['待收货', '已收货', '已退货'] },
  { key: 'actualQty', label: '实收数量', type: 'number' },
  { key: 'actualAmount', label: '实收金额', type: 'display' },
  { key: 'signDate', label: '签收日期', type: 'date' },
  { key: 'acceptanceNotes', label: '验收说明', type: 'textarea', full: true },
  { key: 'signer', label: '签收人' },
]

function statusTagType(status) {
  if (status === '已收货') return 'success'
  if (status === '已退货') return 'danger'
  return 'warning'
}

function hasValue(value) {
  return value !== '' && value !== null && value !== undefined
}

function round(value, digits = 2) {
  const base = 10 ** digits
  return Math.round((value + Number.EPSILON) * base) / base
}

function displayProfitRate(row) {
  if (hasValue(row.profitRate)) return `${row.profitRate}%`
  const unitPrice = Number(row.unitPrice)
  const boardUnitPrice = Number(row.boardUnitPrice)
  if (Number.isFinite(unitPrice) && unitPrice > 0 && Number.isFinite(boardUnitPrice)) {
    return `${round((unitPrice - boardUnitPrice) / unitPrice * 100)}%`
  }
  return '-'
}

function fetchData(params) {
  return purchaseOrdersAPI.list(params)
}

function onFormChange(data) {
  return applyBoardCalculation({ ...editData.value, ...data }, { autoBoardUnitPrice: false })
}

function openEdit(row) {
  editId.value = row.id
  editData.value = applyBoardCalculation({ ...row }, { autoBoardUnitPrice: false })
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除这张采购单吗？', '提示', { type: 'warning' })
  await purchaseOrdersAPI.delete(row.id)
  tableRef.value.loadData()
}

async function handleSubmit(form) {
  const calculated = applyBoardCalculation({ ...editData.value, ...form }, { autoBoardUnitPrice: false })
  const data = {
    status: calculated.status || '待收货',
    actualQty: calculated.actualQty === '' ? 0 : Number(calculated.actualQty),
    actualAmount: calculated.actualAmount,
    signDate: calculated.signDate || null,
    acceptanceNotes: calculated.acceptanceNotes || '',
    signer: calculated.signer || '',
  }
  const res = await purchaseOrdersAPI.updateReceipt(editId.value, data)
  const message = res.data?.message || '收货信息已更新'
  if (message.includes('未完全完成')) {
    ElMessage.warning(message)
  } else {
    ElMessage.success(message)
  }
  tableRef.value.loadData()
}
</script>

<style scoped>
.receipt-page {
  display: grid;
  gap: 12px;
}
</style>
