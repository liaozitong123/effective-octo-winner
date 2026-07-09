<template>
  <div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索送货单号..."
      showPrint @add="openAdd" @edit="openEdit" @delete="handleDelete" @print="handlePrint" />
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { deliveryNotesAPI, customersAPI, salesOrdersAPI } from '../../api/sales'

const router = useRouter()
const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'noteNo', label: '送货单号' },
  { key: 'customerName', label: '客户' }, { key: 'qty', label: '数量' },
  { key: 'deliveryDate', label: '送货日期' }, { key: 'status', label: '状态' },
  { key: 'carrier', label: '承运方' }, { key: 'trackingNo', label: '物流单号' },
]
const fields = [
  { key: 'noteNo', label: '送货单号', required: true }, { key: 'salesOrderId', label: '销售订单', type: 'select', optionsApi: () => salesOrdersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'orderNo' },
  { key: 'customerId', label: '客户', type: 'select', optionsApi: () => customersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'name', required: true }, { key: 'qty', label: '数量', type: 'number', required: true },
  { key: 'deliveryDate', label: '送货日期', type: 'date' }, { key: 'carrier', label: '承运方' },
  { key: 'trackingNo', label: '物流单号' }, { key: 'notes', label: '备注', type: 'textarea' },
]

function toApiData(f) {
  return { ...f, salesOrder: { id: Number(f.salesOrderId) }, customer: { id: Number(f.customerId) } }
}

function fetchData(p) { return deliveryNotesAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await deliveryNotesAPI.delete(row.id)
  tableRef.value.loadData()
}
function handlePrint(row) { router.push(`/warehouse/delivery/print?id=${row.id}`) }
async function handleSubmit(form) {
  const data = toApiData(form)
  if (editId.value) await deliveryNotesAPI.update(editId.value, data)
  else await deliveryNotesAPI.create(data)
  tableRef.value.loadData()
}
</script>
