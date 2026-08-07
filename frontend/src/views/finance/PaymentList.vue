<template>
  <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" :search-placeholder="searchPlaceholder"
    @add="openAdd" @edit="openEdit" @delete="handleDelete" />
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { paymentsAPI } from '../../api/finance'

const route = useRoute()
const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const fixedPaymentType = computed(() => String(route.meta.paymentType || ''))
const fixedPartyType = computed(() => String(route.meta.partyType || ''))
const pageTitle = computed(() => route.meta.pageTitle || '付款/收款单')
const searchPlaceholder = computed(() => `搜索${pageTitle.value}...`)
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'paymentNo', label: '单号' },
  { key: 'partyName', label: '往来方' },
  { key: 'amount', label: '金额' }, { key: 'paymentMethod', label: '付款方式' }, { key: 'paymentDate', label: '日期' },
]
const fields = [
  { key: 'paymentNo', label: '单号', required: true },
  { key: 'partyId', label: '往来方ID', type: 'number', required: true }, { key: 'partyName', label: '往来方名称' },
  { key: 'amount', label: '金额', type: 'number', required: true },
  { key: 'paymentMethod', label: '付款方式', type: 'select', options: ['银行转账','现金','承兑汇票'] },
  { key: 'paymentDate', label: '日期', type: 'date' }, { key: 'notes', label: '备注', type: 'textarea' },
]
function fetchData(p) { return paymentsAPI.list({ ...p, paymentType: fixedPaymentType.value, partyType: fixedPartyType.value }) }
function openAdd() { editId.value = null; editData.value = { paymentType: fixedPaymentType.value, partyType: fixedPartyType.value }; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row, paymentType: fixedPaymentType.value, partyType: fixedPartyType.value }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await paymentsAPI.delete(row.id); tableRef.value.loadData()
}
async function handleSubmit(form) {
  const payload = { ...form, paymentType: fixedPaymentType.value, partyType: fixedPartyType.value }
  editId.value ? await paymentsAPI.update(editId.value, payload) : await paymentsAPI.create(payload); tableRef.value.loadData()
}
</script>
