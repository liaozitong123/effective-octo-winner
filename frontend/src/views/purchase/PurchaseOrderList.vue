<template>
  <div class="purchase-page">
    <div class="purchase-filter-bar">
      <div class="unsigned-reminder">
        当前有 <span class="danger-number">{{ unsignedCount }}</span> 条未签收采购单！
        当前未签收总面积 <span class="danger-number">{{ formatArea(unsignedTotalArea) }}</span>，
        已签收总面积 <span class="success-number">{{ formatArea(signedTotalArea) }}</span>。
      </div>
      <el-radio-group v-model="signFilter" size="small" @change="applySignFilter">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="signed">已签收</el-radio-button>
        <el-radio-button label="unsigned">未签收</el-radio-button>
      </el-radio-group>
    </div>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索采购单号/客户/产品名..."
      @add="openAdd" @edit="openEdit" @delete="handleDelete">
      <template #signStatus="{ row }">
        <span :class="['sign-status', row.signDate ? 'is-signed' : 'is-unsigned']">
          {{ row.signDate ? '已签收' : '未签收' }}
        </span>
      </template>
      <template #materialType="{ row }">
        <el-tag :type="row.materialType === '纸板' ? '' : 'warning'" size="small">{{ row.materialType }}</el-tag>
      </template>
      <template #status="{ row }">
        <el-tag :type="row.status === '已收货' ? 'success' : ''" size="small">{{ row.status }}</el-tag>
      </template>
    </DataTable>
  </div>
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" :onChange="onFormChange" />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { purchaseOrdersAPI, suppliersAPI } from '../../api/purchase'
import { STITCH_OPTIONS, applyBoardCalculation } from '../../utils/boardCalculation'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const signFilter = ref('all')
const unsignedCount = ref(0)
const unsignedTotalArea = ref(0)
const signedTotalArea = ref(0)
const columns = [
  { key: 'signStatus', label: '状态', slot: 'signStatus', width: 82 }, { key: 'orderNo', label: '采购单号' }, { key: 'salesOrderNo', label: '销售订单号' },
  { key: 'customerName', label: '客户' }, { key: 'productName', label: '产品名称' },
  { key: 'supplierName', label: '供应商' }, { key: 'orderDate', label: '下单日期' }, { key: 'spec', label: '规格(cm)', minWidth: 170 },
  { key: 'productionMaterial', label: '生产材质' }, { key: 'fluteType', label: '楞别' },
  { key: 'boardLength', label: '纸板长度' }, { key: 'boardWidth', label: '纸板宽度' },
  { key: 'boardQty', label: '纸板数量' }, { key: 'cutCount', label: '开数', width: 72, minWidth: 72 }, { key: 'crease', label: '凹压线', minWidth: 170 },
  { key: 'notes', label: '备注' },
  { key: 'material', label: '客户材质' }, { key: 'boxType', label: '盒式' }, { key: 'stitchType', label: '钉口' },
  { key: 'unitPrice', label: '客户平方单价' }, { key: 'qty', label: '下单数量' },
  { key: 'boardArea', label: '纸板面积' }, { key: 'totalArea', label: '总面积' },
  { key: 'materialBasePrice', label: '材质基价' }, { key: 'discountRate', label: '折率(%)' },
  { key: 'boardUnitPrice', label: '纸板平方单价' }, { key: 'profitRate', label: '毛利率(%)' },
  { key: 'boardAmount', label: '纸板金额' },
  { key: 'signDate', label: '签收日期' }, { key: 'actualQty', label: '实收数量' },
  { key: 'actualAmount', label: '实收金额' },
]
const fields = [
  { key: 'unitPrice', label: '客户平方单价', type: 'display' },
  { key: 'supplierId', label: '供应商', type: 'select', optionsApi: () => suppliersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'name' },
  { key: 'orderDate', label: '下单日期', type: 'date', required: true },
  { key: 'qty', label: '下单数量', type: 'display' },
  { key: 'spec', label: '规格(cm)', type: 'display' },
  { key: 'boxType', label: '盒式', type: 'display' },
  { key: 'stitchType', label: '钉口', type: 'select', options: STITCH_OPTIONS },
  { key: 'productionMaterial', label: '生产材质' },
  { key: 'cutCount', label: '开数', type: 'number' },
  { key: 'boardLength', label: '纸板长度', type: 'number', hintKey: 'realBoardLength', hintLabel: '参考长度' },
  { key: 'boardWidth', label: '纸板宽度', type: 'number', hintKey: 'realBoardWidth', hintLabel: '参考宽度' },
  { key: 'boardQty', label: '纸板数量', type: 'number', hintKey: 'referenceBoardQty', hintLabel: '参考数量' },
  { key: 'crease', label: '凹压线', hintKey: 'referenceCrease', hintLabel: '参考凹压线' },
  { key: 'boardArea', label: '纸板面积', type: 'number' },
  { key: 'totalArea', label: '总面积', type: 'number' },
  { key: 'materialBasePrice', label: '材质基价', type: 'number' },
  { key: 'discountRate', label: '折率(%)', type: 'number' },
  { key: 'boardUnitPrice', label: '纸板平方单价', type: 'number' },
  { key: 'profitRate', label: '毛利率(%)', type: 'number', readonly: true },
  { key: 'boardAmount', label: '纸板金额', type: 'number' },
  { key: 'signDate', label: '签收日期', type: 'date' },
  { key: 'actualQty', label: '实收数量', type: 'number' },
  { key: 'actualAmount', label: '实收金额', type: 'number', readonly: true },
  { key: 'notes', label: '备注', type: 'textarea' },
]
function shouldAutoBoardUnitPrice(data, changedKey) {
  const currentPrice = Number(data.boardUnitPrice)
  return ['materialBasePrice', 'discountRate'].includes(changedKey)
    || data.boardUnitPrice === ''
    || data.boardUnitPrice === null
    || data.boardUnitPrice === undefined
    || (Number.isFinite(currentPrice) && currentPrice <= 0)
}
function calcForm(data, changedKey) {
  return applyBoardCalculation(data, { autoBoardUnitPrice: shouldAutoBoardUnitPrice(data, changedKey) })
}
function onFormChange(data, changedKey) { return calcForm(data, changedKey) }
function toApiData(f) {
  const { realBoardLength, realBoardWidth, referenceCrease, referenceBoardQty, ...data } = calcForm(f)
  return { ...data, supplier: f.supplierId ? { id: Number(f.supplierId) } : null }
}
async function fetchData(p) {
  const res = await purchaseOrdersAPI.list({ ...p, signStatus: signFilter.value })
  if (Array.isArray(res.data?.data)) {
    res.data.data = res.data.data.map(row => ({
      ...row,
      signStatus: row.signDate ? '已签收' : '未签收',
    }))
  }
  return res
}
function formatArea(value) {
  const n = Number(value)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}
async function refreshSignSummary() {
  const res = await purchaseOrdersAPI.signSummary()
  const summary = res.data?.data || {}
  unsignedCount.value = Number(summary.unsignedCount) || 0
  unsignedTotalArea.value = Number(summary.unsignedTotalArea) || 0
  signedTotalArea.value = Number(summary.signedTotalArea) || 0
}
function applySignFilter() {
  tableRef.value?.doSearch()
}
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function displayDiscountRate(rate) {
  const value = Number(rate)
  return Number.isFinite(value) && value > 0 && value <= 2 ? value * 100 : rate
}
function openEdit(row) {
  editId.value = row.id
  editData.value = { ...row, discountRate: displayDiscountRate(row.discountRate), supplierId: row.supplierId || '' }
  dialogVisible.value = true
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await purchaseOrdersAPI.delete(row.id); await refreshSignSummary(); tableRef.value.loadData()
}
async function handleSubmit(form) {
  const data = toApiData(form)
  editId.value ? await purchaseOrdersAPI.update(editId.value, data) : await purchaseOrdersAPI.create(data); await refreshSignSummary(); tableRef.value.loadData()
}
onMounted(refreshSignSummary)
</script>

<style scoped>
.purchase-page {
  display: grid;
  gap: 12px;
}

.purchase-filter-bar {
  align-items: center;
  background: var(--erp-panel);
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
}

.unsigned-reminder {
  color: #334155;
  font-weight: 800;
}

.unsigned-reminder .danger-number {
  color: #dc2626;
  font-size: 1.16rem;
}

.unsigned-reminder .success-number {
  color: #16a34a;
  font-size: 1.16rem;
}

.sign-status {
  font-weight: 800;
}

.sign-status.is-signed {
  color: #16a34a;
}

.sign-status.is-unsigned {
  color: #dc2626;
}

@media (max-width: 720px) {
  .purchase-filter-bar {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
