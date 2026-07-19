<template>
  <div class="data-table-shell">
    <div class="table-toolbar">
      <div class="toolbar-main">
        <el-input v-model="searchText" :placeholder="searchPlaceholder" clearable class="search-input" @clear="doSearch" @keyup.enter="doSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
        <span class="record-count">共 {{ total }} 条</span>
      </div>
      <el-button v-if="!hideAdd" type="primary" :icon="Plus" class="add-btn" @click="$emit('add')">新增</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tableData" border style="width:100%" v-loading="loading" class="desktop-table" :max-height="tableMaxHeight">
        <el-table-column
          v-for="col in columns"
          :key="col.key"
          :prop="col.key"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth || defaultColumnWidth"
          show-overflow-tooltip
        >
          <template v-if="col.slot" #default="scope">
            <slot :name="col.slot" :row="scope.row" :value="scope.row[col.key]" />
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="showPrint ? 200 : 128" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="$emit('edit', scope.row)">编辑</el-button>
            <el-button size="small" type="success" link v-if="showPrint" @click="$emit('print', scope.row)">打印</el-button>
            <el-button size="small" type="danger" link @click="$emit('delete', scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mobile-record-list" v-loading="loading">
        <div v-if="!tableData.length && !loading" class="empty-mobile">暂无数据</div>
        <div v-for="row in tableData" :key="row.id || row.orderNo || JSON.stringify(row)" class="mobile-record-card">
          <div class="mobile-record-title">{{ getPrimaryValue(row) }}</div>
          <div class="mobile-fields">
            <div v-for="col in mobileColumns" :key="col.key" class="mobile-field">
              <span>{{ col.label }}</span>
              <strong>{{ formatDisplayValue(row[col.key]) }}</strong>
            </div>
          </div>
          <div class="mobile-actions">
            <el-button size="small" type="primary" link @click="$emit('edit', row)">编辑</el-button>
            <el-button size="small" type="success" link v-if="showPrint" @click="$emit('print', row)">打印</el-button>
            <el-button size="small" type="danger" link @click="$emit('delete', row)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
        <span class="pagination-summary">第 {{ currentPage }} 页</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10,20,50]"
          :total="total"
          layout="sizes,prev,pager,next"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'

const props = defineProps({
  columns: Array,
  fetchData: Function,
  searchPlaceholder: { type: String, default: '搜索...' },
  searchFields: Array,
  showPrint: { type: Boolean, default: false },
  hideAdd: { type: Boolean, default: false },
  tableMaxHeight: { type: [String, Number], default: 'calc(100vh - 232px)' },
})

const emit = defineEmits(['add', 'edit', 'delete', 'print'])

const tableData = ref([])
const loading = ref(false)
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const defaultColumnWidth = 118
const mobileColumns = computed(() => props.columns.filter(col => col.key !== 'id').slice(0, 4))

function formatDisplayValue(value) {
  if (value === null || value === undefined || value === '') return '-'
  return value
}

function getPrimaryValue(row) {
  const primary = props.columns.find(col => ['orderNo', 'name', 'productName'].includes(col.key)) || props.columns[0]
  return formatDisplayValue(row[primary?.key])
}

async function loadData() {
  loading.value = true
  try {
    const res = await props.fetchData({ page: currentPage.value, perPage: pageSize.value, q: searchText.value })
    tableData.value = res.data.data || []
    total.value = res.data.total || 0
  } catch(e) { tableData.value = [] }
  loading.value = false
}

function doSearch() { currentPage.value = 1; loadData() }

onMounted(loadData)
defineExpose({ loadData, doSearch })
</script>

<style scoped>
.data-table-shell {
  display: grid;
  gap: 12px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: var(--erp-panel);
}

.toolbar-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.search-input {
  width: min(360px, 100%);
}

.record-count,
.pagination-summary {
  color: var(--erp-muted);
  white-space: nowrap;
}

.add-btn {
  box-shadow: 0 7px 15px rgba(37, 99, 235, .18);
}

.table-card {
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: var(--erp-panel);
  overflow: hidden;
}

.desktop-table {
  border-radius: var(--erp-radius) var(--erp-radius) 0 0;
}

.desktop-table :deep(.el-table__header th) {
  height: 42px;
}

.desktop-table :deep(.el-table__row td) {
  height: 44px;
}

.pagination-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 50px;
  padding: 8px 12px;
  border-top: 1px solid var(--erp-border-soft);
  background: #fff;
}

.mobile-record-list {
  display: none;
}

@media (max-width: 720px) {
  .table-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-main {
    flex-direction: column;
    align-items: stretch;
  }

  .record-count {
    font-size: 12px;
  }

  .add-btn {
    width: 100%;
  }

  .desktop-table {
    display: none;
  }

  .mobile-record-list {
    display: grid;
    gap: 10px;
    padding: 10px;
    min-height: 80px;
  }

  .mobile-record-card {
    border: 1px solid var(--erp-border);
    border-radius: var(--erp-radius);
    background: #fbfdff;
    padding: 10px;
  }

  .mobile-record-title {
    color: var(--erp-primary);
    font-weight: 800;
    margin-bottom: 8px;
  }

  .mobile-fields {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .mobile-field span {
    display: block;
    color: var(--erp-muted);
    font-size: 12px;
    margin-bottom: 2px;
  }

  .mobile-field strong {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #273244;
  }

  .mobile-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 10px;
  }

  .pagination-wrap {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .pagination-wrap :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
