<template>
  <div class="profit-page">
    <div class="profit-toolbar">
      <div>
        <h2>利润分析</h2>
        <p>按送货月份汇总，纸板成本计入销售单第一次送货月份。</p>
      </div>
      <el-date-picker
        v-model="yearValue"
        type="year"
        value-format="YYYY"
        format="YYYY"
        placeholder="选择年份"
        class="year-picker"
        @change="loadData"
      />
    </div>

    <div class="summary-grid">
      <div class="summary-item">
        <span>销售收入</span>
        <strong>{{ formatMoney(summary.revenue) }}</strong>
      </div>
      <div class="summary-item">
        <span>纸板成本</span>
        <strong>{{ formatMoney(summary.cost) }}</strong>
      </div>
      <div class="summary-item">
        <span>毛利润</span>
        <strong :class="summary.profit >= 0 ? 'is-profit' : 'is-loss'">{{ formatMoney(summary.profit) }}</strong>
      </div>
      <div class="summary-item">
        <span>毛利率</span>
        <strong :class="summary.profit >= 0 ? 'is-profit' : 'is-loss'">{{ formatRate(totalProfitRate) }}</strong>
      </div>
    </div>

    <section class="analysis-section">
      <div class="section-head">
        <h3>{{ yearValue }}年月度利润汇总</h3>
      </div>
      <el-table :data="monthlyRows" border stripe class="profit-table" max-height="420">
        <el-table-column prop="monthLabel" label="月份" width="90" fixed="left" />
        <el-table-column prop="revenue" label="销售收入" min-width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.revenue) }}</template>
        </el-table-column>
        <el-table-column prop="cost" label="纸板成本" min-width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.cost) }}</template>
        </el-table-column>
        <el-table-column prop="profit" label="毛利润" min-width="130" align="right">
          <template #default="{ row }">
            <span :class="row.profit >= 0 ? 'is-profit' : 'is-loss'">{{ formatMoney(row.profit) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="profitRate" label="毛利率%" min-width="110" align="right">
          <template #default="{ row }">
            <span :class="row.profit >= 0 ? 'is-profit' : 'is-loss'">{{ formatRate(row.profitRate) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="analysis-section">
      <div class="section-head">
        <h3>收入、成本、毛利润趋势</h3>
      </div>
      <div ref="chartRef" class="profit-chart"></div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { financeAPI } from '../../api/finance'

const yearValue = ref(String(new Date().getFullYear()))
const summary = ref({ revenue: 0, cost: 0, profit: 0 })
const monthlyRows = ref([])
const chartRef = ref(null)
let chart = null

const totalProfitRate = computed(() => {
  const revenue = Number(summary.value.revenue) || 0
  if (revenue <= 0) return 0
  return Math.round(((Number(summary.value.profit) || 0) / revenue) * 10000) / 100
})

function formatMoney(value) {
  return (Number(value) || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function formatRate(value) {
  return `${(Number(value) || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}%`
}

async function loadData() {
  const year = Number(yearValue.value) || new Date().getFullYear()
  const res = await financeAPI.profitAnalysis({ year })
  const data = res.data?.data || {}
  summary.value = {
    revenue: Number(data.revenue) || 0,
    cost: Number(data.cost) || 0,
    profit: Number(data.profit) || 0,
  }
  monthlyRows.value = (data.monthly || []).map(row => ({
    ...row,
    monthLabel: `${row.month}月`,
  }))
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const rows = monthlyRows.value
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售收入', '纸板成本', '毛利润'], bottom: 0 },
    grid: { left: 64, right: 24, top: 24, bottom: 48 },
    xAxis: { type: 'category', data: rows.map(row => row.monthLabel) },
    yAxis: { type: 'value' },
    series: [
      { name: '销售收入', type: 'bar', data: rows.map(row => row.revenue), itemStyle: { color: '#2563eb' } },
      { name: '纸板成本', type: 'bar', data: rows.map(row => row.cost), itemStyle: { color: '#f59e0b' } },
      { name: '毛利润', type: 'line', data: rows.map(row => row.profit), itemStyle: { color: '#16a34a' }, smooth: true },
    ],
  })
}

onMounted(loadData)

onBeforeUnmount(() => {
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.profit-page {
  display: grid;
  gap: 14px;
}

.profit-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: #fff;
}

.profit-toolbar h2,
.section-head h3 {
  margin: 0;
  color: #172033;
}

.profit-toolbar p {
  margin: 6px 0 0;
  color: var(--erp-muted);
  font-size: 13px;
}

.year-picker {
  width: 132px;
  flex: 0 0 auto;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  display: grid;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: #fff;
}

.summary-item span {
  color: var(--erp-muted);
  font-size: 13px;
}

.summary-item strong {
  color: #172033;
  font-size: 24px;
  line-height: 1;
}

.analysis-section {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius);
  background: #fff;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profit-table :deep(.el-table__header th) {
  height: 42px;
}

.profit-chart {
  width: 100%;
  height: 360px;
}

.is-profit {
  color: #16a34a;
  font-weight: 800;
}

.is-loss {
  color: #dc2626;
  font-weight: 800;
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .profit-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
}

@media (max-width: 560px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
