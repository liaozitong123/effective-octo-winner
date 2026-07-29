<template>
  <div>
    <DataTable
      ref="tableRef"
      :columns="columns"
      :fetchData="fetchData"
      search-placeholder="搜索采购单号/销售单号/客户/产品名称/生产员..."
      table-max-height="calc(100vh - 232px)"
      @add="openAdd"
      @edit="openEdit"
      @delete="handleDelete"
    />
    <FormDialog
      v-model="dialogVisible"
      :fields="fields"
      :isEdit="!!editId"
      :initialData="editData"
      :onSubmit="handleSubmit"
      :onChange="onFormChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { productionRecordsAPI, productionOrdersAPI } from '../../api/production'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'receiptStatus', label: '收货状态', minWidth: 92 },
  { key: 'purchaseOrderNo', label: '采购单号', minWidth: 150 },
  { key: 'salesOrderNo', label: '销售单号', minWidth: 150 },
  { key: 'receivedDate', label: '接单日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 150 },
  { key: 'productName', label: '产品名称', minWidth: 140 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'customerUnitPrice', label: '客户平方单价', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'singleArea', label: '单个面积', minWidth: 110 },
  { key: 'inboundAmount', label: '入库金额', minWidth: 110 },
  { key: 'inboundQty', label: '入库数量', minWidth: 100 },
  { key: 'inboundDate', label: '入库日期', minWidth: 110 },
  { key: 'nailer', label: '打钉员', minWidth: 100 },
  { key: 'orderDate', label: '下单日期', minWidth: 110 },
  { key: 'supplierName', label: '供应商', minWidth: 150 },
  { key: 'boardArea', label: '纸板面积', minWidth: 110 },
  { key: 'actualTotalArea', label: '实收总面积', minWidth: 120 },
  { key: 'actualQty', label: '实收数量', minWidth: 100 },
  { key: 'actualAmount', label: '实收金额', minWidth: 110 },
  { key: 'operator', label: '生产员', minWidth: 100 },
  { key: 'productionDate', label: '生产日期', minWidth: 110 },
  { key: 'deliveryQty', label: '送货数量', minWidth: 100 },
  { key: 'remainingStock', label: '剩余库存', minWidth: 100 },
  { key: 'deliveryDate', label: '送货日期', minWidth: 110 },
]

const fields = [
  { key: 'productionOrderId', label: '生产单', type: 'select', optionsApi: () => productionOrdersAPI.list({ page: 1, perPage: 200 }).then(r => r.data.data), labelKey: 'orderNo', required: true },
  { key: 'receiptStatus', label: '收货状态', type: 'display' },
  { key: 'purchaseOrderNo', label: '采购单号', type: 'display' },
  { key: 'salesOrderNo', label: '销售单号', type: 'display' },
  { key: 'receivedDate', label: '接单日期', type: 'display' },
  { key: 'customerName', label: '客户', type: 'display' },
  { key: 'productName', label: '产品名称', type: 'display' },
  { key: 'spec', label: '规格', type: 'display' },
  { key: 'customerUnitPrice', label: '客户平方单价', type: 'display' },
  { key: 'fluteType', label: '楞别', type: 'display' },
  { key: 'singleArea', label: '单个面积', type: 'display' },
  { key: 'inboundAmount', label: '入库金额', type: 'display' },
  { key: 'inboundQty', label: '入库数量', type: 'number' },
  { key: 'inboundDate', label: '入库日期', type: 'date' },
  { key: 'nailer', label: '打钉员' },
  { key: 'orderDate', label: '下单日期', type: 'display' },
  { key: 'supplierName', label: '供应商', type: 'display' },
  { key: 'boardArea', label: '纸板面积', type: 'display' },
  { key: 'actualTotalArea', label: '实收总面积', type: 'display' },
  { key: 'actualQty', label: '实收数量', type: 'display' },
  { key: 'actualAmount', label: '实收金额', type: 'display' },
  { key: 'operator', label: '生产员' },
  { key: 'productionDate', label: '生产日期', type: 'date' },
  { key: 'deliveryQty', label: '送货数量', type: 'number' },
  { key: 'remainingStock', label: '剩余库存', type: 'display' },
  { key: 'deliveryDate', label: '送货日期', type: 'date' },
]

function numberValue(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function calcInboundAmount(data) {
  return Math.round(numberValue(data.customerUnitPrice) * numberValue(data.singleArea) * numberValue(data.inboundQty) * 100) / 100
}

function calcRemainingStock(data) {
  return numberValue(data.inboundQty) - numberValue(data.deliveryQty)
}

function onFormChange(data) {
  return { ...data, inboundAmount: calcInboundAmount(data), remainingStock: calcRemainingStock(data) }
}

function toApiData(form) {
  const inboundQty = numberValue(form.inboundQty)
  const deliveryQty = numberValue(form.deliveryQty)
  return {
    productionOrder: form.productionOrderId ? { id: Number(form.productionOrderId) } : null,
    outputQty: inboundQty,
    recordDate: form.inboundDate || null,
    nailer: form.nailer || '',
    operator: form.operator || '',
    productionDate: form.productionDate || null,
    deliveryQty,
    remainingStock: inboundQty - deliveryQty,
    deliveryDate: form.deliveryDate || null,
  }
}

function fetchData(p) { return productionRecordsAPI.list(p) }
function openAdd() {
  editId.value = null
  editData.value = { inboundQty: 0, deliveryQty: 0, remainingStock: 0 }
  dialogVisible.value = true
}
function openEdit(row) {
  editId.value = row.id
  editData.value = {
    ...row,
    productionOrderId: row.productionOrderId || '',
    inboundAmount: calcInboundAmount(row),
    remainingStock: calcRemainingStock(row),
  }
  dialogVisible.value = true
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await productionRecordsAPI.delete(row.id)
  tableRef.value.loadData()
}
async function handleSubmit(form) {
  const data = toApiData(form)
  if (editId.value) await productionRecordsAPI.update(editId.value, data)
  else await productionRecordsAPI.create(data)
  tableRef.value.loadData()
}
</script>
