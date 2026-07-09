<template>
  <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索付款/收款单..."
    @add="openAdd" @edit="openEdit" @delete="handleDelete">
    <template #paymentType="{ row }">
      <el-tag :type="row.paymentType === '收款' ? 'success' : 'warning'" size="small">{{ row.paymentType }}</el-tag>
    </template>
  </DataTable>
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { paymentsAPI } from '../../api/finance'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'paymentNo', label: '单号' },
  { key: 'paymentType', label: '类型', slot: 'paymentType' }, { key: 'partyName', label: '往来方' },
  { key: 'amount', label: '金额' }, { key: 'paymentMethod', label: '付款方式' }, { key: 'paymentDate', label: '日期' },
]
const fields = [
  { key: 'paymentNo', label: '单号', required: true },
  { key: 'paymentType', label: '类型', type: 'select', options: ['收款','付款'], required: true },
  { key: 'partyType', label: '往来类型', type: 'select', options: ['customer','supplier'], required: true },
  { key: 'partyId', label: '往来方ID', type: 'number', required: true }, { key: 'partyName', label: '往来方名称' },
  { key: 'amount', label: '金额', type: 'number', required: true },
  { key: 'paymentMethod', label: '付款方式', type: 'select', options: ['银行转账','现金','承兑汇票'] },
  { key: 'paymentDate', label: '日期', type: 'date' }, { key: 'notes', label: '备注', type: 'textarea' },
]
function fetchData(p) { return paymentsAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await paymentsAPI.delete(row.id); tableRef.value.loadData()
}
async function handleSubmit(form) {
  editId.value ? await paymentsAPI.update(editId.value, form) : await paymentsAPI.create(form); tableRef.value.loadData()
}
</script>
