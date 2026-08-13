<template>
  <div class="salary-page">
    <div class="salary-tabs">
      <el-date-picker
        v-model="salaryYear"
        type="year"
        format="YYYY"
        value-format="YYYY"
        placeholder="选择年份"
        class="salary-year-picker"
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
      search-placeholder="搜索姓名/职位/备注..."
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
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { salaryRecordsAPI } from '../../api/finance'

const tableRef = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})
const salaryYear = ref(String(new Date().getFullYear()))
const activeMonth = ref(String(new Date().getMonth() + 1).padStart(2, '0'))
const months = Array.from({ length: 12 }, (_, index) => {
  const value = String(index + 1).padStart(2, '0')
  return { value, label: `${index + 1}月` }
})
const salaryMonth = computed(() => `${salaryYear.value}-${activeMonth.value}`)

const columns = [
  { key: 'employeeName', label: '姓名', minWidth: 100 },
  { key: 'position', label: '职位', minWidth: 110 },
  { key: 'attendanceShifts', label: '考勤班数', minWidth: 100 },
  { key: 'baseSalary', label: '基本工资', minWidth: 110 },
  { key: 'overtimeSalary', label: '加班工资', minWidth: 110 },
  { key: 'allowance', label: '津贴', minWidth: 90 },
  { key: 'socialSecuritySubsidy', label: '社保自行缴纳补贴', minWidth: 150 },
  { key: 'payableSalary', label: '应付工资', minWidth: 110 },
  { key: 'socialSecurityWithheld', label: '社保代缴费用', minWidth: 130 },
  { key: 'netSalary', label: '实发工资', minWidth: 110 },
  { key: 'paymentDate', label: '发放日期', minWidth: 110 },
  { key: 'notes', label: '备注', minWidth: 160 },
]

const fields = [
  { key: 'employeeName', label: '姓名', required: true },
  { key: 'position', label: '职位' },
  { key: 'attendanceShifts', label: '考勤班数', type: 'number' },
  { key: 'baseSalary', label: '基本工资', type: 'number' },
  { key: 'overtimeSalary', label: '加班工资', type: 'number' },
  { key: 'allowance', label: '津贴', type: 'number' },
  { key: 'socialSecuritySubsidy', label: '社保自行缴纳补贴', type: 'number' },
  { key: 'payableSalary', label: '应付工资', type: 'number' },
  { key: 'socialSecurityWithheld', label: '社保代缴费用', type: 'number' },
  { key: 'netSalary', label: '实发工资', type: 'number' },
  { key: 'paymentDate', label: '发放日期', type: 'date' },
  { key: 'notes', label: '备注', type: 'textarea' },
]

function fetchData(params) {
  return salaryRecordsAPI.list({
    ...params,
    month: salaryMonth.value,
  })
}

function handleMonthChange() {
  tableRef.value?.loadData()
}

function openAdd() {
  editId.value = null
  editData.value = {
    paymentDate: `${salaryMonth.value}-01`,
  }
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  editData.value = { ...row }
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除这条工资记录吗？', '提示', { type: 'warning' })
  await salaryRecordsAPI.delete(row.id)
  tableRef.value?.loadData()
}

async function handleSubmit(form) {
  const payload = {
    ...form,
    attendanceShifts: numberValue(form.attendanceShifts),
    baseSalary: numberValue(form.baseSalary),
    overtimeSalary: numberValue(form.overtimeSalary),
    allowance: numberValue(form.allowance),
    socialSecuritySubsidy: numberValue(form.socialSecuritySubsidy),
    payableSalary: numberValue(form.payableSalary),
    socialSecurityWithheld: numberValue(form.socialSecurityWithheld),
    netSalary: numberValue(form.netSalary),
    paymentDate: form.paymentDate || `${salaryMonth.value}-01`,
  }
  if (editId.value) await salaryRecordsAPI.update(editId.value, payload)
  else await salaryRecordsAPI.create(payload)
  tableRef.value?.loadData()
}

function numberValue(value) {
  if (value === null || value === undefined || value === '') return 0
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}
</script>

<style scoped>
.salary-page {
  display: grid;
  gap: 12px;
}

.salary-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: var(--erp-panel);
}

.salary-year-picker {
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
  .salary-tabs {
    align-items: stretch;
    flex-direction: column;
  }

  .salary-year-picker {
    width: 100%;
  }
}
</style>
