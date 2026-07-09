<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="12">
        <el-card>
          <div class="card-title">💳 应付账款分析 (供应商)</div>
          <div class="card-row"><span>应付总额</span><span class="red">¥{{ format(ar.totalPayable) }}</span></div>
          <div class="card-row"><span>已付供应商</span><span class="green">¥{{ format(ar.paidSupplier) }}</span></div>
          <div class="card-row"><span>未付余额</span><span class="red" style="font-weight:700">¥{{ format(ar.unpaidSupplier) }}</span></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div class="card-title">💰 应收账款分析 (客户)</div>
          <div class="card-row"><span>应收总额</span><span class="red">¥{{ format(ar.totalReceivable) }}</span></div>
          <div class="card-row"><span>已收客户</span><span class="green">¥{{ format(ar.receivedCustomer) }}</span></div>
          <div class="card-row"><span>未收余额</span><span class="red" style="font-weight:700">¥{{ format(ar.unreceivedCustomer) }}</span></div>
        </el-card>
      </el-col>
    </el-row>
    <el-card>
      <div class="card-title" style="margin-bottom:16px">📊 利润分析 ({{ year }}年) — 收入 vs 成本</div>
      <div ref="chartRef" style="width:100%;height:350px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { financeAPI } from '../../api/finance'

const year = new Date().getFullYear()
const ar = ref({ totalPayable:0, paidSupplier:0, unpaidSupplier:0, totalReceivable:0, receivedCustomer:0, unreceivedCustomer:0 })
const chartRef = ref(null)

function format(n) { return (n || 0).toLocaleString() }

onMounted(async () => {
  try {
    const [profitRes, arRes] = await Promise.all([
      financeAPI.profitAnalysis({ year }),
      financeAPI.payableReceivable()
    ])
    ar.value = arRes.data.data

    await nextTick()
    const chart = echarts.init(chartRef.value)
    const d = profitRes.data.data.monthly || []
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '成本', '利润'], bottom: 0 },
      grid: { left: 60, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: d.map(m => m.month + '月') },
      yAxis: { type: 'value' },
      series: [
        { name: '收入', type: 'bar', data: d.map(m => m.revenue), itemStyle: { color: '#2563eb' } },
        { name: '成本', type: 'bar', data: d.map(m => m.cost), itemStyle: { color: '#f59e0b' } },
        { name: '利润', type: 'line', data: d.map(m => m.profit), itemStyle: { color: '#22c55e' }, smooth: true },
      ]
    })
  } catch(e) {}
})
</script>

<style scoped>
.card-title { font-size:1rem; font-weight:600; margin-bottom:12px; color:#1e293b; }
.card-row { display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid #e2e8f0; font-size:0.9rem; }
.card-row:last-child { border-bottom:none; }
.red { color:#ef4444; font-weight:600; } .green { color:#22c55e; font-weight:600; }
</style>
