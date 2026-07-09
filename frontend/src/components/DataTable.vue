<template>
  <div>
    <div class="table-toolbar">
      <el-input v-model="searchText" :placeholder="searchPlaceholder" clearable style="width:260px" @clear="doSearch" @keyup.enter="doSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button v-if="!hideAdd" type="primary" :icon="Plus" @click="$emit('add')">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
      <el-table-column v-for="col in columns" :key="col.key" :prop="col.key" :label="col.label" :width="col.width" show-overflow-tooltip>
        <template v-if="col.slot" #default="scope">
          <slot :name="col.slot" :row="scope.row" :value="scope.row[col.key]" />
        </template>
      </el-table-column>
      <el-table-column label="操作" :width="showPrint ? 210 : 150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="$emit('edit', scope.row)">编辑</el-button>
          <el-button size="small" type="success" link v-if="showPrint" @click="$emit('print', scope.row)">打印</el-button>
          <el-button size="small" type="danger" link @click="$emit('delete', scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10,20,50]"
        :total="total"
        layout="total,sizes,prev,pager,next"
        background
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'

const props = defineProps({
  columns: Array,
  fetchData: Function,
  searchPlaceholder: { type: String, default: '搜索...' },
  searchFields: Array,
  showPrint: { type: Boolean, default: false },
  hideAdd: { type: Boolean, default: false },
})

const emit = defineEmits(['add', 'edit', 'delete', 'print'])

const tableData = ref([])
const loading = ref(false)
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

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
.table-toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:14px; gap:12px; }
.pagination-wrap { display:flex; justify-content:flex-end; margin-top:14px; }
</style>
