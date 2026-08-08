<template>
  <DataTable
    ref="tableRef"
    :columns="columns"
    :fetchData="fetchData"
    :search-placeholder="searchPlaceholder"
    :hide-add="isAutoPayment"
    :hide-delete="isAutoPayment"
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
    :onChange="handleFormChange"
  />
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { paymentsAPI } from '../../api/finance'

const route = useRoute()
const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const fixedPaymentType = computed(() => String(route.meta.paymentType || ''))
const fixedPartyType = computed(() => String(route.meta.partyType || ''))
const isReceivable = computed(() => fixedPaymentType.value === '收款' && fixedPartyType.value === 'customer')
const isPayable = computed(() => fixedPaymentType.value === '付款' && fixedPartyType.value === 'supplier')
const isAutoPayment = computed(() => isReceivable.value || isPayable.value)
const pageTitle = computed(() => route.meta.pageTitle || '付款/收款单')
const searchPlaceholder = computed(() => `搜索${pageTitle.value}...`)
const columns = computed(() => {
  if (isReceivable.value) return receivableColumns
  if (isPayable.value) return payableColumns
  return paymentColumns
})
const fields = computed(() => {
  if (isReceivable.value) return receivableFields
  if (isPayable.value) return payableFields
  return paymentFields
})

const receivableColumns = [
  { key: 'deliveryDate', label: '送货日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 140 },
  { key: 'deliveryNoteNo', label: '送货单号', minWidth: 150 },
  { key: 'receivableAmount', label: '应收金额', minWidth: 110 },
  { key: 'amount', label: '已收金额', minWidth: 110 },
  { key: 'unreceivedAmount', label: '未收金额', minWidth: 110 },
  { key: 'paymentDate', label: '收款日期', minWidth: 110 },
  { key: 'registrar', label: '登记员', minWidth: 100 },
  { key: 'reviewer', label: '复核人', minWidth: 100 },
]

const receivableFields = [
  { key: 'deliveryDate', label: '送货日期', type: 'display' },
  { key: 'customerName', label: '客户', type: 'display' },
  { key: 'deliveryNoteNo', label: '送货单号', type: 'display' },
  { key: 'receivableAmount', label: '应收金额', type: 'display' },
  { key: 'amount', label: '已收金额', type: 'number' },
  { key: 'unreceivedAmount', label: '未收金额', type: 'display' },
  { key: 'paymentDate', label: '收款日期', type: 'date' },
  { key: 'registrar', label: '登记员' },
  { key: 'reviewer', label: '复核人' },
]

const payableColumns = [
  { key: 'signDate', label: '签收日期', minWidth: 110 },
  { key: 'supplierName', label: '供应商', minWidth: 150 },
  { key: 'purchaseOrderNo', label: '采购单号', minWidth: 150 },
  { key: 'payableAmount', label: '应付金额', minWidth: 110 },
  { key: 'amount', label: '已付金额', minWidth: 110 },
  { key: 'unpaidAmount', label: '未付金额', minWidth: 110 },
  { key: 'paymentDate', label: '付款日期', minWidth: 110 },
  { key: 'registrar', label: '登记员', minWidth: 100 },
  { key: 'reviewer', label: '复核人', minWidth: 100 },
]

const payableFields = [
  { key: 'signDate', label: '签收日期', type: 'display' },
  { key: 'supplierName', label: '供应商', type: 'display' },
  { key: 'purchaseOrderNo', label: '采购单号', type: 'display' },
  { key: 'payableAmount', label: '应付金额', type: 'display' },
  { key: 'amount', label: '已付金额', type: 'number' },
  { key: 'unpaidAmount', label: '未付金额', type: 'display' },
  { key: 'paymentDate', label: '付款日期', type: 'date' },
  { key: 'registrar', label: '登记员' },
  { key: 'reviewer', label: '复核人' },
]

const paymentColumns = [
  { key: 'id', label: 'ID', width: 60 },
  { key: 'paymentNo', label: '单号' },
  { key: 'partyName', label: '往来方' },
  { key: 'amount', label: '金额' },
  { key: 'paymentMethod', label: '付款方式' },
  { key: 'paymentDate', label: '日期' },
]

const paymentFields = [
  { key: 'paymentNo', label: '单号', required: true },
  { key: 'partyId', label: '往来方ID', type: 'number', required: true },
  { key: 'partyName', label: '往来方名称' },
  { key: 'amount', label: '金额', type: 'number', required: true },
  { key: 'paymentMethod', label: '付款方式', type: 'select', options: ['银行转账', '现金', '承兑汇票'] },
  { key: 'paymentDate', label: '日期', type: 'date' },
  { key: 'notes', label: '备注', type: 'textarea' },
]

function fetchData(params) {
  return paymentsAPI.list({
    ...params,
    paymentType: fixedPaymentType.value,
    partyType: fixedPartyType.value,
  })
}

function openAdd() {
  if (isAutoPayment.value) return
  editId.value = null
  editData.value = { paymentType: fixedPaymentType.value, partyType: fixedPartyType.value }
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  editData.value = {
    ...row,
    amount: toMoney(row.amount),
    receivableAmount: toMoney(row.receivableAmount),
    unreceivedAmount: toMoney(row.unreceivedAmount),
    payableAmount: toMoney(row.payableAmount),
    unpaidAmount: toMoney(row.unpaidAmount),
    paymentType: fixedPaymentType.value,
    partyType: fixedPartyType.value,
  }
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await paymentsAPI.delete(row.id)
  tableRef.value.loadData()
}

async function handleSubmit(form) {
  const payload = isAutoPayment.value
    ? {
        amount: toNumber(form.amount),
        paymentDate: form.paymentDate || null,
        registrar: form.registrar || '',
        reviewer: form.reviewer || '',
      }
    : { ...form, paymentType: fixedPaymentType.value, partyType: fixedPartyType.value }

  if (editId.value) {
    await paymentsAPI.update(editId.value, payload)
  } else {
    await paymentsAPI.create(payload)
  }
  tableRef.value.loadData()
}

function handleFormChange(form) {
  if (isReceivable.value) {
    return {
      unreceivedAmount: toMoney(toNumber(form.receivableAmount) - toNumber(form.amount)),
    }
  }
  if (isPayable.value) {
    return {
      unpaidAmount: toMoney(toNumber(form.payableAmount) - toNumber(form.amount)),
    }
  }
  return null
}

function toNumber(value) {
  if (value === null || value === undefined || value === '') return 0
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function toMoney(value) {
  return Math.round(toNumber(value) * 100) / 100
}

watch(() => route.path, () => {
  dialogVisible.value = false
  editId.value = null
  editData.value = {}
  tableRef.value?.loadData()
})
</script>
