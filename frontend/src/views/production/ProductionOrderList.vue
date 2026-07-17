<template>
  <div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索生产单号..."
      hideAdd @edit="openEdit" @delete="handleDelete" />
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" :onChange="onFormChange" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { productionOrdersAPI } from '../../api/production'
import { STITCH_OPTIONS, applyBoardCalculation } from '../../utils/boardCalculation'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'orderNo', label: '生产单号' }, { key: 'customerName', label: '客户' }, { key: 'qty', label: '数量' },
  { key: 'unit', label: '单位' }, { key: 'notes', label: '生产备注' },
  { key: 'supplierName', label: '供应商' }, { key: 'specNotes', label: '纸箱规格备注栏' },
  { key: 'boxType', label: '盒式' }, { key: 'stitchType', label: '钉口' },
  { key: 'productionMaterial', label: '生产材质' }, { key: 'fluteType', label: '楞别' },
  { key: 'boardLength', label: '纸板长度' }, { key: 'boardWidth', label: '纸板宽度' },
  { key: 'boardQty', label: '纸板数量' }, { key: 'cutCount', label: '开数' },
  { key: 'crease', label: '凹压线' }, { key: 'urgent', label: '急单' },
  { key: 'orderArea', label: '下单面积' }, { key: 'signDate', label: '签收日期' },
]

const fields = [
  { key: 'qty', label: '数量', type: 'display' },
  { key: 'unit', label: '单位', type: 'display' },
  { key: 'customerName', label: '客户', type: 'display' },
  { key: 'supplierName', label: '供应商', type: 'display' },
  { key: 'notes', label: '生产备注', type: 'textarea' },
  { key: 'spec', label: '规格(cm)', type: 'display' },
  { key: 'boxType', label: '盒式', type: 'display' },
  { key: 'stitchType', label: '钉口', type: 'select', options: STITCH_OPTIONS },
  { key: 'specNotes', label: '纸箱规格备注栏' },
  { key: 'productionMaterial', label: '生产材质' },
  { key: 'fluteType', label: '楞别' },
  { key: 'cutCount', label: '开数', type: 'number' },
  { key: 'boardLength', label: '纸板长度', type: 'number', hintKey: 'realBoardLength', hintLabel: '真实长度' },
  { key: 'boardWidth', label: '纸板宽度', type: 'number', hintKey: 'realBoardWidth', hintLabel: '真实宽度' },
  { key: 'boardQty', label: '纸板数量', type: 'number' },
  { key: 'crease', label: '凹压线' },
  { key: 'urgent', label: '急单', type: 'select', options: ['否','是'] },
  { key: 'orderArea', label: '下单面积', type: 'number' },
  { key: 'signDate', label: '签收日期', type: 'date' },
]

function calcForm(data) { return applyBoardCalculation(data, { syncOrderArea: true }) }
function onFormChange(data) { return calcForm(data) }
function toApiData(form) {
  const { realBoardLength, realBoardWidth, ...data } = calcForm(form)
  return data
}
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
