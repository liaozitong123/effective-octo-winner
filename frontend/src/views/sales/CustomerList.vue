<template>
  <div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索客户名称/联系人/电话..."
      @add="openAdd" @edit="openEdit" @delete="handleDelete" />
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { customersAPI } from '../../api/sales'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'name', label: '客户名称' }, { key: 'contact', label: '联系人' },
  { key: 'phone', label: '电话' }, { key: 'email', label: '邮箱' }, { key: 'address', label: '地址' }, { key: 'notes', label: '备注' },
]
const fields = [
  { key: 'name', label: '客户名称', required: true }, { key: 'contact', label: '联系人' },
  { key: 'phone', label: '电话' }, { key: 'email', label: '邮箱' }, { key: 'address', label: '地址' },
  { key: 'notes', label: '备注', type: 'textarea' },
]

function fetchData(p) { return customersAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该客户吗？', '提示', { type: 'warning' })
  await customersAPI.delete(row.id)
  tableRef.value.loadData()
}
async function handleSubmit(form) {
  if (editId.value) await customersAPI.update(editId.value, form)
  else await customersAPI.create(form)
  tableRef.value.loadData()
}
</script>
