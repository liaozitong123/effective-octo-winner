<template>
  <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索供应商..."
    @add="openAdd" @edit="openEdit" @delete="handleDelete" />
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { suppliersAPI } from '../../api/purchase'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'name', label: '供应商名称' }, { key: 'contact', label: '联系人' },
  { key: 'phone', label: '电话' }, { key: 'email', label: '邮箱' }, { key: 'supplyType', label: '供应类型' }, { key: 'address', label: '地址' },
]
const fields = [
  { key: 'name', label: '供应商名称', required: true }, { key: 'contact', label: '联系人' },
  { key: 'phone', label: '电话' }, { key: 'email', label: '邮箱' },
  { key: 'supplyType', label: '供应类型', type: 'select', options: ['纸板','辅料','纸板/辅料'] },
  { key: 'address', label: '地址' }, { key: 'notes', label: '备注', type: 'textarea' },
]
function fetchData(p) { return suppliersAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await suppliersAPI.delete(row.id); tableRef.value.loadData()
}
async function handleSubmit(form) {
  editId.value ? await suppliersAPI.update(editId.value, form) : await suppliersAPI.create(form); tableRef.value.loadData()
}
</script>
