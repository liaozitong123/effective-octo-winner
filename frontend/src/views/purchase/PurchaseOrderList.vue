<template>
  <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索采购单号..."
    @add="openAdd" @edit="openEdit" @delete="handleDelete">
    <template #materialType="{ row }">
      <el-tag :type="row.materialType === '纸板' ? '' : 'warning'" size="small">{{ row.materialType }}</el-tag>
    </template>
    <template #status="{ row }">
      <el-tag :type="row.status === '已收货' ? 'success' : ''" size="small">{{ row.status }}</el-tag>
    </template>
  </DataTable>
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" :onChange="onFormChange" />
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { purchaseOrdersAPI, suppliersAPI } from '../../api/purchase'
import { STITCH_OPTIONS, applyBoardCalculation } from '../../utils/boardCalculation'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'orderNo', label: '采购单号' },
  { key: 'customerName', label: '客户' }, { key: 'supplierName', label: '供应商' }, { key: 'productName', label: '产品名称' }, { key: 'spec', label: '规格(cm)' },
  { key: 'material', label: '客户材质' }, { key: 'boxType', label: '盒式' }, { key: 'stitchType', label: '钉口' },
  { key: 'unitPrice', label: '客户平方单价' }, { key: 'qty', label: '数量' },
  { key: 'productionMaterial', label: '生产材质' }, { key: 'fluteType', label: '楞别' },
  { key: 'boardLength', label: '纸板长度' }, { key: 'boardWidth', label: '纸板宽度' },
  { key: 'boardQty', label: '纸板数量' }, { key: 'cutCount', label: '开数' },
  { key: 'boardArea', label: '纸板面积' }, { key: 'totalArea', label: '总面积' },
  { key: 'materialBasePrice', label: '材质基价' }, { key: 'discountRate', label: '折率' },
  { key: 'boardUnitPrice', label: '纸板平方单价' }, { key: 'profitRate', label: '毛利率' },
  { key: 'boardAmount', label: '纸板金额' },
  { key: 'signDate', label: '签收日期' }, { key: 'actualQty', label: '实收数量' },
  { key: 'actualAmount', label: '实收金额' },
]
const fields = [
  { key: 'supplierId', label: '供应商', type: 'select', optionsApi: () => suppliersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'name' },
  { key: 'spec', label: '规格(cm)', type: 'display' },
  { key: 'boxType', label: '盒式', type: 'display' },
  { key: 'stitchType', label: '钉口', type: 'select', options: STITCH_OPTIONS },
  { key: 'productionMaterial', label: '生产材质' },
  { key: 'boardLength', label: '纸板长度', type: 'number' },
  { key: 'boardWidth', label: '纸板宽度', type: 'number' },
  { key: 'boardQty', label: '纸板数量', type: 'number' },
  { key: 'cutCount', label: '开数', type: 'number' },
  { key: 'crease', label: '压线' },
  { key: 'boardArea', label: '纸板面积', type: 'number' },
  { key: 'totalArea', label: '总面积', type: 'number' },
  { key: 'materialBasePrice', label: '材质基价', type: 'number' },
  { key: 'discountRate', label: '折率', type: 'number' },
  { key: 'boardUnitPrice', label: '纸板平方单价', type: 'number' },
  { key: 'profitRate', label: '毛利率', type: 'number' },
  { key: 'boardAmount', label: '纸板金额', type: 'number' },
  { key: 'signDate', label: '签收日期', type: 'date' },
  { key: 'actualQty', label: '实收数量', type: 'number' },
  { key: 'actualAmount', label: '实收金额', type: 'number' },
  { key: 'notes', label: '备注', type: 'textarea' },
]
function calcForm(data) { return applyBoardCalculation(data) }
function onFormChange(data) { return calcForm(data) }
function toApiData(f) { return { ...calcForm(f), supplier: f.supplierId ? { id: Number(f.supplierId) } : null } }
function fetchData(p) { return purchaseOrdersAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row, supplierId: row.supplierId || '' }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await purchaseOrdersAPI.delete(row.id); tableRef.value.loadData()
}
async function handleSubmit(form) {
  const data = toApiData(form)
  editId.value ? await purchaseOrdersAPI.update(editId.value, data) : await purchaseOrdersAPI.create(data); tableRef.value.loadData()
}
</script>
