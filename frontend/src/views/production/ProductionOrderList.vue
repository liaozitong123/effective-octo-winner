<template>
  <div class="production-page">
    <div class="production-filter-bar">
      <el-radio-group v-model="printFilter" size="small" @change="applyPrintFilter">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="printed">已打印</el-radio-button>
        <el-radio-button label="unprinted">未打印</el-radio-button>
      </el-radio-group>
    </div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索生产单号/采购单号/客户/供应商..."
      hideAdd showPrint @edit="openEdit" @delete="handleDelete" @print="handlePrint">
      <template #printStatus="{ row }">
        <span :class="['print-status', row.printed ? 'is-printed' : 'is-unprinted']">
          {{ row.printStatus || (row.printed ? '已打印' : '未打印') }}
        </span>
      </template>
      <template #notes="{ row }">
        <img v-if="isImageNote(row.notes)" :src="row.notes" class="note-thumb" alt="备注图片" />
        <span v-else class="note-text">{{ row.notes || '-' }}</span>
      </template>
    </DataTable>
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { productionOrdersAPI } from '../../api/production'

const tableRef = ref(null)
const router = useRouter()
const dialogVisible = ref(false)
const editId = ref(null)
const editData = ref({})
const printFilter = ref('all')

const columns = [
  { key: 'printStatus', label: '状态', slot: 'printStatus', width: 86, minWidth: 86 },
  { key: 'purchaseOrderNo', label: '采购单号', minWidth: 150 },
  { key: 'orderNo', label: '生产单号', minWidth: 150 },
  { key: 'orderDate', label: '下单日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 150 },
  { key: 'supplierName', label: '供应商', minWidth: 150 },
  { key: 'productName', label: '产品名称', minWidth: 140 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'material', label: '客户材质', minWidth: 120 },
  { key: 'boxType', label: '盒式', minWidth: 100 },
  { key: 'stitchType', label: '钉口', minWidth: 100 },
  { key: 'qty', label: '下单数量', minWidth: 100 },
  { key: 'productionMaterial', label: '生产材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'crease', label: '凹压线', minWidth: 170 },
  { key: 'boardLength', label: '纸板长度', minWidth: 110 },
  { key: 'boardWidth', label: '纸板宽度', minWidth: 110 },
  { key: 'cutCount', label: '开数', width: 72, minWidth: 72 },
  { key: 'totalArea', label: '总面积', minWidth: 110 },
  { key: 'operator', label: '生产员', minWidth: 110 },
  { key: 'notes', label: '备注', slot: 'notes', minWidth: 150 },
  { key: 'productionStatus', label: '生产备注', minWidth: 100 },
]

const fields = [
  { key: 'purchaseOrderNo', label: '采购单号', type: 'display' },
  { key: 'orderNo', label: '生产单号', type: 'display' },
  { key: 'orderDate', label: '下单日期', type: 'display' },
  { key: 'customerName', label: '客户', type: 'display' },
  { key: 'supplierName', label: '供应商', type: 'display' },
  { key: 'productName', label: '产品名称', type: 'display' },
  { key: 'spec', label: '规格', type: 'display' },
  { key: 'material', label: '客户材质', type: 'display' },
  { key: 'boxType', label: '盒式', type: 'display' },
  { key: 'stitchType', label: '钉口', type: 'display' },
  { key: 'qty', label: '下单数量', type: 'display' },
  { key: 'productionMaterial', label: '生产材质', type: 'display' },
  { key: 'fluteType', label: '楞别', type: 'display' },
  { key: 'crease', label: '凹压线', type: 'display' },
  { key: 'boardLength', label: '纸板长度', type: 'display' },
  { key: 'boardWidth', label: '纸板宽度', type: 'display' },
  { key: 'cutCount', label: '开数', type: 'display' },
  { key: 'totalArea', label: '总面积', type: 'display' },
  { key: 'notes', label: '备注', type: 'display' },
  { key: 'productionStatus', label: '生产备注', type: 'display' },
  { key: 'operator', label: '生产员' },
]

function isImageNote(value) {
  return typeof value === 'string' && value.startsWith('data:image/')
}

function toApiData(form) { return { operator: form.operator || '' } }
function fetchData(p) { return productionOrdersAPI.list({ ...p, printStatus: printFilter.value }) }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
function handlePrint(row) { router.push(`/production/orders/print?id=${row.id}`) }
function applyPrintFilter() { tableRef.value?.doSearch() }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await productionOrdersAPI.delete(row.id)
  tableRef.value.loadData()
}
async function handleSubmit(form) {
  if (editId.value) await productionOrdersAPI.update(editId.value, toApiData(form))
  tableRef.value.loadData()
}
</script>

<style scoped>
.production-page {
  display: grid;
  gap: 12px;
}

.production-filter-bar {
  display: flex;
  justify-content: flex-end;
  padding: 12px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: var(--erp-panel);
}

.print-status {
  font-weight: 800;
}

.print-status.is-printed {
  color: #16a34a;
}

.print-status.is-unprinted {
  color: #dc2626;
}

.note-thumb {
  width: 54px;
  height: 38px;
  object-fit: cover;
  border: 1px solid var(--erp-border);
  border-radius: 6px;
  display: block;
}

.note-text {
  display: inline-block;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
}
</style>
