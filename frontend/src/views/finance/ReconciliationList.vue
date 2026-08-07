<template>
  <DataTable
    v-if="isCustomerStatement"
    ref="tableRef"
    :columns="customerColumns"
    :fetchData="fetchCustomerData"
    search-placeholder="搜索客户对账单..."
    table-max-height="calc(100vh - 232px)"
    hideAdd
    hideActions
  >
    <template #toolbar-actions>
      <el-date-picker
        v-model="statementMonth"
        type="month"
        format="YYYY-MM"
        value-format="YYYY-MM"
        placeholder="选择月份"
        style="width: 140px"
        @change="handleMonthChange"
      />
      <el-button type="primary" plain :icon="Printer" @click="openPrint">打印对账单</el-button>
    </template>
    <template #deliveryStatus="{ row }">
      <span :class="['delivery-status', row.deliveryStatus === '已送货' ? 'is-delivered' : 'is-undelivered']">
        {{ row.deliveryStatus }}
      </span>
    </template>
  </DataTable>

  <template v-else>
    <DataTable
      ref="tableRef"
      :columns="supplierColumns"
      :fetchData="fetchSupplierData"
      :search-placeholder="searchPlaceholder"
      @add="openAdd"
      @edit="openEdit"
      @delete="handleDelete"
    >
      <template #status="{ row }">
        <el-tag :type="row.status === '已结清' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
      </template>
    </DataTable>
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
  </template>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Printer } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { deliveryNotesAPI } from '../../api/sales'
import { reconciliationsAPI } from '../../api/finance'

const route = useRoute()
const router = useRouter()
const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})
const statementMonth = ref(currentMonth())

const fixedPartyType = computed(() => String(route.meta.partyType || ''))
const isCustomerStatement = computed(() => fixedPartyType.value === 'customer')
const pageTitle = computed(() => route.meta.pageTitle || '对账单')
const searchPlaceholder = computed(() => `搜索${pageTitle.value}...`)

const customerColumns = [
  { key: 'deliveryStatus', label: '送货状态', slot: 'deliveryStatus', width: 92, minWidth: 92 },
  { key: 'salesOrderNo', label: '销售单号', minWidth: 150 },
  { key: 'orderDate', label: '接单日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 130 },
  { key: 'productName', label: '产品名称', minWidth: 140 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'customerUnitPrice', label: '客户平方单价', minWidth: 120 },
  { key: 'customerMaterial', label: '客户材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'singleArea', label: '单个面积', minWidth: 110 },
  { key: 'deliveryQty', label: '已送货数量', minWidth: 110 },
  { key: 'area', label: '面积', minWidth: 100 },
  { key: 'boxUnitPrice', label: '纸箱单价', minWidth: 110 },
  { key: 'amount', label: '金额', minWidth: 100 },
  { key: 'driver', label: '司机', minWidth: 90 },
  { key: 'noteNo', label: '送货单号', minWidth: 160 },
  { key: 'deliveryDate', label: '送货日期', minWidth: 110 },
  { key: 'issuer', label: '开单人', minWidth: 90 },
  { key: 'salesperson', label: '业务员', minWidth: 90 },
]

const supplierColumns = [
  { key: 'id', label: 'ID', width: 60 },
  { key: 'partyName', label: '名称' },
  { key: 'beginBalance', label: '期初余额' },
  { key: 'currentAmount', label: '本期发生' },
  { key: 'paidAmount', label: '已付/已收' },
  { key: 'endBalance', label: '期末余额' },
  { key: 'status', label: '状态', slot: 'status' },
]

const fields = [
  { key: 'partyId', label: '往来方ID', type: 'number', required: true },
  { key: 'partyName', label: '往来方名称' },
  { key: 'periodStart', label: '期间开始', type: 'date' },
  { key: 'periodEnd', label: '期间结束', type: 'date' },
  { key: 'beginBalance', label: '期初余额', type: 'number' },
  { key: 'currentAmount', label: '本期发生', type: 'number' },
  { key: 'paidAmount', label: '已付/已收', type: 'number' },
  { key: 'endBalance', label: '期末余额', type: 'number' },
  { key: 'status', label: '状态', type: 'select', options: ['未结清', '已结清'] },
]

function fetchCustomerData(params) {
  return deliveryNotesAPI.list({ ...params, month: statementMonth.value })
}

function fetchSupplierData(params) {
  return reconciliationsAPI.list({ ...params, partyType: fixedPartyType.value })
}

function handleMonthChange() {
  tableRef.value?.loadData()
}

function openPrint() {
  router.push({
    path: '/finance/customer-reconciliation/print',
    query: { month: statementMonth.value },
  })
}

function openAdd() {
  editId.value = null
  editData.value = { partyType: fixedPartyType.value }
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  editData.value = { ...row, partyType: fixedPartyType.value }
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await reconciliationsAPI.delete(row.id)
  tableRef.value.loadData()
}

async function handleSubmit(form) {
  const payload = { ...form, partyType: fixedPartyType.value }
  if (editId.value) {
    await reconciliationsAPI.update(editId.value, payload)
  } else {
    await reconciliationsAPI.create(payload)
  }
  tableRef.value.loadData()
}

function currentMonth() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}
</script>

<style scoped>
.delivery-status {
  font-weight: 800;
}

.delivery-status.is-delivered {
  color: #16a34a;
}

.delivery-status.is-undelivered {
  color: #dc2626;
}
</style>
