<template>
  <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索对账单..."
    @add="openAdd" @edit="openEdit" @delete="handleDelete">
    <template #partyType="{ row }">
      <el-tag :type="row.partyType === 'customer' ? '' : 'warning'" size="small">{{ row.partyType === 'customer' ? '客户' : '供应商' }}</el-tag>
    </template>
    <template #status="{ row }">
      <el-tag :type="row.status === '已结清' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
    </template>
  </DataTable>
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { reconciliationsAPI } from '../../api/finance'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'partyType', label: '类型', slot: 'partyType' },
  { key: 'partyName', label: '名称' }, { key: 'beginBalance', label: '期初余额' },
  { key: 'currentAmount', label: '本期发生' }, { key: 'paidAmount', label: '已付/已收' },
  { key: 'endBalance', label: '期末余额' }, { key: 'status', label: '状态', slot: 'status' },
]
const fields = [
  { key: 'partyType', label: '类型', type: 'select', options: ['customer','supplier'], required: true },
  { key: 'partyId', label: '往来方ID', type: 'number', required: true }, { key: 'partyName', label: '往来方名称' },
  { key: 'periodStart', label: '期间开始', type: 'date' }, { key: 'periodEnd', label: '期间结束', type: 'date' },
  { key: 'beginBalance', label: '期初余额', type: 'number' }, { key: 'currentAmount', label: '本期发生', type: 'number' },
  { key: 'paidAmount', label: '已付/已收', type: 'number' }, { key: 'endBalance', label: '期末余额', type: 'number' },
  { key: 'status', label: '状态', type: 'select', options: ['未结清','已结清'] },
]
function fetchData(p) { return reconciliationsAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await reconciliationsAPI.delete(row.id); tableRef.value.loadData()
}
async function handleSubmit(form) {
  editId.value ? await reconciliationsAPI.update(editId.value, form) : await reconciliationsAPI.create(form); tableRef.value.loadData()
}
</script>
