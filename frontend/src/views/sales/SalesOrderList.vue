<template>
  <div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索订单号/产品名..."
      @add="openAdd" @edit="openEdit" @delete="handleDelete">
      <template #status="{ row }">
        <el-tag :type="row.status === '已完成' ? 'success' : row.status === '生产中' ? 'warning' : ''" size="small">{{ row.status }}</el-tag>
      </template>
    </DataTable>
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" :onChange="onFormChange" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { salesOrdersAPI, customersAPI } from '../../api/sales'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})

const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'orderNo', label: '订单号' },
  { key: 'customerName', label: '客户' }, { key: 'productName', label: '产品名称' },
  { key: 'spec', label: '规格(cm)' }, { key: 'material', label: '客户材质' },
  { key: 'boxType', label: '盒式' }, { key: 'fluteType', label: '楞别' }, { key: 'singleArea', label: '单个面积' }, { key: 'qty', label: '下单数量' },
  { key: 'unitPrice', label: '客户平方单价' }, { key: 'boxUnitPrice', label: '纸箱单价' },
  { key: 'totalAmount', label: '总金额' },
]

const fields = [
  { key: 'customerId', label: '客户', type: 'select', optionsApi: () => customersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'name', required: true },
  { key: 'productName', label: '产品名称', required: true },
  { key: 'boxType', label: '盒式', type: 'select', options: ['平口箱','全盖箱','无底半翼','无底齐翼','中封箱','隔板','天地箱'] },
  { key: 'spec', label: '规格(cm)' },
  { key: 'material', label: '客户材质' },

  { key: 'fluteType', label: '楞别' },
  { key: 'singleArea', label: '单个面积(㎡)', type: 'display' },
  { key: 'unitPrice', label: '客户平方单价', type: 'number' },
  { key: 'qty', label: '下单数量', type: 'number', required: true },
  { key: 'boxUnitPrice', label: '纸箱单价(元)', type: 'display' },
  { key: 'totalAmount', label: '总金额(元)', type: 'display' },
  { key: 'notes', label: '备注', type: 'textarea' },
]

function calcForm(data) {
  // Auto-calculate singleArea from boxType and spec
  if (data.spec) {
    const parts = data.spec.split('*').map(Number)
    if (parts.length === 3 && parts.every(n => !isNaN(n) && n > 0)) {
      const [l, w, h] = parts  // 长, 宽, 高
      if (data.boxType === '平口箱') {
        data.singleArea = (2 * (l + w + 5) * (w + h + 3) / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      } else if (data.boxType === '全盖箱') {
        data.singleArea = ((l + w + 5) * (2 * w + h + 3) * 2 / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      } else if (data.boxType === '中封箱') {
        data.singleArea = ((2 * h + 2 * w + 3) * (2 * h + l + 3) / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      } else if (data.boxType === '无底齐翼') {
        data.singleArea = (2 * (l + w + 5) * (1.5 * w + h + 3) / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      } else if (data.boxType === '无底半翼') {
        data.singleArea = (2 * (l + w + 5) * (0.5 * w + h + 3) / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      } else if (data.boxType === '天地箱') {
        data.singleArea = ((l + 2 * h + 5) * (w + 2 * h + 3) * 2 / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      } else if (data.boxType === '隔板') {
        data.singleArea = ((l + 3) * (w + 3) / 10000).toFixed(6).replace(/0+$/, '').replace(/\.$/, '')
      }
    }
  }
  const sa = parseFloat(data.singleArea) || 0
  const up = parseFloat(data.unitPrice) || 0
  const qty = parseInt(data.qty) || 0
  const boxPrice = parseFloat((sa * up).toFixed(2))
  data.boxUnitPrice = boxPrice > 0 ? parseFloat(boxPrice.toFixed(2)) : 0
  data.totalAmount = parseFloat((qty * data.boxUnitPrice).toFixed(2)) || 0
  return data
}

function onFormChange(data) { return calcForm(data) }

function fetchData(p) { return salesOrdersAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row, customerId: row.customerId || '' }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await salesOrdersAPI.delete(row.id)
  tableRef.value.loadData()
}
async function handleSubmit(form) {
  const data = calcForm({ ...form, customer: form.customerId ? { id: Number(form.customerId) } : null })
  if (editId.value) await salesOrdersAPI.update(editId.value, data)
  else await salesOrdersAPI.create(data)
  tableRef.value.loadData()
}
</script>
