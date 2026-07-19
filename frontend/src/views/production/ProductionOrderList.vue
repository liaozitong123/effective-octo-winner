<template>
  <div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索生产单号/采购单号/客户/供应商..."
      hideAdd @edit="openEdit" @delete="handleDelete" />
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { productionOrdersAPI } from '../../api/production'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'purchaseOrderNo', label: '采购单号', minWidth: 150 },
  { key: 'orderNo', label: '生产单号', minWidth: 150 },
  { key: 'orderDate', label: '下单日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 150 },
  { key: 'supplierName', label: '供应商', minWidth: 150 },
  { key: 'productName', label: '产品名称', minWidth: 140 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'material', label: '客户材质', minWidth: 120 },
  { key: 'boxType', label: '盒式', minWidth: 100 },
  { key: 'stitchType', label: '钉口', minWidth: 100 },
  { key: 'qty', label: '下单数量', minWidth: 100 },
  { key: 'productionMaterial', label: '生产材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'crease', label: '凹压线', minWidth: 170 },
  { key: 'boardLength', label: '纸板长度', minWidth: 110 },
  { key: 'boardWidth', label: '纸板宽度', minWidth: 110 },
  { key: 'cutCount', label: '开数', width: 72, minWidth: 72 },
  { key: 'totalArea', label: '总面积', minWidth: 110 },
  { key: 'operator', label: '生产员', minWidth: 110 },
]

const fields = [
  { key: 'purchaseOrderNo', label: '采购单号', type: 'display' },
  { key: 'orderNo', label: '生产单号', type: 'display' },
  { key: 'orderDate', label: '下单日期', type: 'display' },
  { key: 'customerName', label: '客户', type: 'display' },
  { key: 'supplierName', label: '供应商', type: 'display' },
  { key: 'productName', label: '产品名称', type: 'display' },
  { key: 'spec', label: '规格', type: 'display' },
  { key: 'material', label: '客户材质', type: 'display' },
  { key: 'boxType', label: '盒式', type: 'display' },
  { key: 'stitchType', label: '钉口', type: 'display' },
  { key: 'qty', label: '下单数量', type: 'display' },
  { key: 'productionMaterial', label: '生产材质', type: 'display' },
  { key: 'fluteType', label: '楞别', type: 'display' },
  { key: 'crease', label: '凹压线', type: 'display' },
  { key: 'boardLength', label: '纸板长度', type: 'display' },
  { key: 'boardWidth', label: '纸板宽度', type: 'display' },
  { key: 'cutCount', label: '开数', type: 'display' },
  { key: 'totalArea', label: '总面积', type: 'display' },
  { key: 'operator', label: '生产员' },
]

function toApiData(form) { return { operator: form.operator || '' } }
function fetchData(p) { return productionOrdersAPI.list(p) }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await productionOrdersAPI.delete(row.id)
  tableRef.value.loadData()
}
async function handleSubmit(form) {
  if (editId.value) await productionOrdersAPI.update(editId.value, toApiData(form))
  tableRef.value.loadData()
}
</script>
