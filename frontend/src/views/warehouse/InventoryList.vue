<template>
  <DataTable
    ref="tableRef"
    :columns="columns"
    :fetchData="fetchData"
    search-placeholder="搜索销售单号/客户/产品名称/规格..."
    table-max-height="calc(100vh - 232px)"
    hideAdd
    hideActions
  >
    <template #deliveryStatus="{ row }">
      <span :class="['delivery-status', row.deliveryStatus === '已送货' ? 'is-delivered' : 'is-undelivered']">
        {{ row.deliveryStatus }}
      </span>
    </template>
  </DataTable>
</template>

<script setup>
import { ref } from 'vue'
import DataTable from '../../components/DataTable.vue'
import { inventoryAPI } from '../../api/warehouse'

const tableRef = ref(null)

const columns = [
  { key: 'deliveryStatus', label: '送货状态', slot: 'deliveryStatus', width: 92, minWidth: 92 },
  { key: 'salesOrderNo', label: '销售单号', minWidth: 150 },
  { key: 'orderDate', label: '接单日期', minWidth: 110 },
  { key: 'customerName', label: '客户', minWidth: 150 },
  { key: 'productName', label: '产品名称', minWidth: 140 },
  { key: 'spec', label: '规格', minWidth: 170 },
  { key: 'customerUnitPrice', label: '客户平方单价', minWidth: 120 },
  { key: 'customerMaterial', label: '客户材质', minWidth: 120 },
  { key: 'fluteType', label: '楞别', minWidth: 90 },
  { key: 'singleArea', label: '单个面积', minWidth: 110 },
  { key: 'inboundAmount', label: '入库金额', minWidth: 110 },
  { key: 'inboundQty', label: '入库数量', minWidth: 100 },
  { key: 'boxUnitPrice', label: '纸箱单价', minWidth: 110 },
  { key: 'deliveryQty', label: '已送货数量', minWidth: 110 },
  { key: 'amount', label: '金额', minWidth: 100 },
  { key: 'remainingStock', label: '剩余库存', minWidth: 100 },
]

function fetchData(p) {
  return inventoryAPI.list(p)
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
