<template>
  <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索库存物品..."
    @add="openAdd" @edit="openEdit" @delete="handleDelete">
    <template #itemType="{ row }">
      <el-tag :type="row.itemType === '成品' ? 'success' : row.itemType === '纸板' ? '' : 'warning'" size="small">{{ row.itemType }}</el-tag>
    </template>
    <template #qty="{ row }">
      <span :style="{ color: row.qty <= row.safetyStock ? '#ef4444' : 'inherit', fontWeight: row.qty <= row.safetyStock ? '700' : '400' }">{{ row.qty }}</span>
    </template>
  </DataTable>
  <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { inventoryAPI } from '../../api/warehouse'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'itemType', label: '类型', slot: 'itemType' },
  { key: 'itemName', label: '物品名称' }, { key: 'spec', label: '规格' },
  { key: 'qty', label: '数量', slot: 'qty' }, { key: 'unit', label: '单位' },
  { key: 'warehouseLocation', label: '库位' }, { key: 'safetyStock', label: '安全库存' },
]
const fields = [
  { key: 'itemType', label: '类型', type: 'select', options: ['成品','纸板','辅料'], required: true },
  { key: 'itemName', label: '物品名称', required: true }, { key: 'spec', label: '规格' },
  { key: 'qty', label: '数量', type: 'number', required: true }, { key: 'unit', label: '单位' },
  { key: 'warehouseLocation', label: '库位' }, { key: 'safetyStock', label: '安全库存', type: 'number' },
]
function fetchData(p) { return inventoryAPI.list(p) }
function openAdd() { editId.value = null; editData.value = {}; dialogVisible.value = true }
function openEdit(row) { editId.value = row.id; editData.value = { ...row }; dialogVisible.value = true }
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await inventoryAPI.delete(row.id); tableRef.value.loadData()
}
async function handleSubmit(form) {
  editId.value ? await inventoryAPI.update(editId.value, form) : await inventoryAPI.create(form); tableRef.value.loadData()
}
</script>
