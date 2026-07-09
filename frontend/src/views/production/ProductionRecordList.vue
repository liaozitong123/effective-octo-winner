<template>
  <div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索操作员..."
      @add="openAdd" @edit="openEdit" @delete="handleDelete" />
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
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
  { key: 'id', label: 'ID', width: 60 }, { key: 'productionOrderNo', label: '生产单号' },
  { key: 'productName', label: '产品' }, { key: 'outputQty', label: '产量' },
  { key: 'wasteQty', label: '废品数' }, { key: 'operator', label: '操作员' },
  { key: 'shift', label: '班次' }, { key: 'recordDate', label: '日期' },
]
const fields = [
  { key: 'productionOrderId', label: '生产单', type: 'select', optionsApi: () => productionOrdersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'orderNo', required: true },
  { key: 'outputQty', label: '产量', type: 'number', required: true },
  { key: 'wasteQty', label: '废品数', type: 'number' }, { key: 'operator', label: '操作员' },
  { key: 'shift', label: '班次' }, { key: 'recordDate', label: '日期', type: 'date' },
  { key: 'notes', label: '备注', type: 'textarea' },
]

function toApiData(f) {
  return { ...f, productionOrder: f.productionOrderId ? { id: Number(f.productionOrderId) } : null }
}

function fetchData(p) { return productionRecordsAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row, productionOrderId: row.productionOrderId || '' }; dialogVisible.value = true }
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
