<template>
  <div>
    <p style="margin-bottom:14px;color:#64748b;font-size:0.9rem;">收货/退货通过采购单的状态进行管理。修改采购单状态为"已收货"表示收货，改为"已退货"表示退货。</p>
    <DataTable ref="tableRef" :columns="columns" :fetchData="fetchData" search-placeholder="搜索采购单..."
      @add="openAdd" @edit="openEdit" @delete="handleDelete">
      <template #status="{ row }">
        <el-tag :type="row.status === '已收货' ? 'success' : row.status === '已退货' ? 'danger' : ''" size="small">{{ row.status }}</el-tag>
      </template>
    </DataTable>
    <FormDialog v-model="dialogVisible" :fields="fields" :isEdit="!!editId" :initialData="editData" :onSubmit="handleSubmit" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import DataTable from '../../components/DataTable.vue'
import FormDialog from '../../components/FormDialog.vue'
import { purchaseOrdersAPI, suppliersAPI } from '../../api/purchase'

const tableRef = ref(null), dialogVisible = ref(false), editId = ref(null), editData = ref({})
const columns = [
  { key: 'id', label: 'ID', width: 60 }, { key: 'orderNo', label: '采购单号' }, { key: 'supplierName', label: '供应商' },
  { key: 'materialType', label: '类型' }, { key: 'materialName', label: '材料名称' }, { key: 'qty', label: '数量' },
  { key: 'status', label: '状态', slot: 'status' }, { key: 'orderDate', label: '日期' },
]
const fields = [
  { key: 'orderNo', label: '采购单号', required: true }, { key: 'supplierId', label: '供应商', type: 'select', optionsApi: () => suppliersAPI.list({ page:1, perPage:200 }).then(r => r.data.data), labelKey: 'name' },
  { key: 'materialType', label: '材料类型', type: 'select', options: ['纸板','辅料'] },
  { key: 'materialName', label: '材料名称' }, { key: 'qty', label: '数量', type: 'number' },
  { key: 'status', label: '状态', type: 'select', options: ['待收货','已收货','已退货'] },
  { key: 'notes', label: '备注', type: 'textarea' },
]
function toApiData(f) { return { ...f, supplier: f.supplierId ? { id: Number(f.supplierId) } : null } }
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
