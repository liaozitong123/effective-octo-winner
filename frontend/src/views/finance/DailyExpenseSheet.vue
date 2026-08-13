<template>
  <div class="expense-page">
    <div class="expense-tabs">
      <el-date-picker
        v-model="expenseYear"
        type="year"
        format="YYYY"
        value-format="YYYY"
        placeholder="选择年份"
        class="expense-year-picker"
        @change="handleMonthChange"
      />
      <el-tabs v-model="activeMonth" type="card" class="month-tabs" @tab-change="handleMonthChange">
        <el-tab-pane v-for="month in months" :key="month.value" :name="month.value" :label="month.label" />
      </el-tabs>
    </div>

    <DataTable
      ref="tableRef"
      :columns="columns"
      :fetchData="fetchData"
      search-placeholder="搜索费用分类/明细/经办人/备注..."
      table-max-height="calc(100vh - 286px)"
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
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { dailyExpensesAPI } from '../../api/finance'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})
const expenseYear = ref(String(new Date().getFullYear()))
const activeMonth = ref(String(new Date().getMonth() + 1).padStart(2, '0'))
const months = Array.from({ length: 12 }, (_, index) => {
  const value = String(index + 1).padStart(2, '0')
  return { value, label: `${index + 1}月` }
})
const expenseMonth = computed(() => `${expenseYear.value}-${activeMonth.value}`)

const columns = [
  { key: 'expenseDate', label: '日期', minWidth: 110 },
  { key: 'category', label: '费用分类', minWidth: 120 },
  { key: 'details', label: '费用明细', minWidth: 180 },
  { key: 'quantity', label: '数量', minWidth: 90 },
  { key: 'unitPrice', label: '单价', minWidth: 90 },
  { key: 'amount', label: '金额', minWidth: 100 },
  { key: 'handler', label: '经办人', minWidth: 100 },
  { key: 'notes', label: '备注', minWidth: 160 },
  { key: 'settlementDate', label: '结算日期', minWidth: 110 },
]

const fields = [
  { key: 'expenseDate', label: '日期', type: 'date' },
  { key: 'category', label: '费用分类', required: true },
  { key: 'details', label: '费用明细', type: 'textarea' },
  { key: 'quantity', label: '数量', type: 'number' },
  { key: 'unitPrice', label: '单价', type: 'number' },
  { key: 'amount', label: '金额', type: 'display' },
  { key: 'handler', label: '经办人' },
  { key: 'notes', label: '备注', type: 'textarea' },
  { key: 'settlementDate', label: '结算日期', type: 'date' },
]

function fetchData(params) {
  return dailyExpensesAPI.list({
    ...params,
    month: expenseMonth.value,
  })
}

function handleMonthChange() {
  tableRef.value?.loadData()
}

function openAdd() {
  editId.value = null
  editData.value = {
    recordMonth: expenseMonth.value,
    expenseDate: `${expenseMonth.value}-01`,
    amount: 0,
  }
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  editData.value = normalizeRecord({ ...row })
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除这条日常支出记录吗？', '提示', { type: 'warning' })
  await dailyExpensesAPI.delete(row.id)
  tableRef.value?.loadData()
}

async function handleSubmit(form) {
  const payload = normalizeRecord({
    ...form,
    recordMonth: expenseMonth.value,
  })
  if (editId.value) await dailyExpensesAPI.update(editId.value, payload)
  else await dailyExpensesAPI.create(payload)
  tableRef.value?.loadData()
}

function handleFormChange(form) {
  return normalizeRecord(form)
}

function normalizeRecord(record) {
  const quantity = numberValue(record.quantity)
  const unitPrice = numberValue(record.unitPrice)
  return {
    ...record,
    recordMonth: record.recordMonth || expenseMonth.value,
    quantity,
    unitPrice,
    amount: round2(quantity * unitPrice),
  }
}

function numberValue(value) {
  if (value === null || value === undefined || value === '') return 0
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function round2(value) {
  return Math.round(value * 100) / 100
}
</script>

<style scoped>
.expense-page {
  display: grid;
  gap: 12px;
}

.expense-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: var(--erp-panel);
}

.expense-year-picker {
  width: 132px;
  flex: 0 0 auto;
}

.month-tabs {
  flex: 1;
  min-width: 0;
}

.month-tabs :deep(.el-tabs__header) {
  margin: 0;
}

@media (max-width: 720px) {
  .expense-tabs {
    align-items: stretch;
    flex-direction: column;
  }

  .expense-year-picker {
    width: 100%;
  }
}
</style>
