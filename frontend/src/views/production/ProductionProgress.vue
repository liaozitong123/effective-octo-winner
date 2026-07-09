<template>
  <div>
    <div v-if="orders.length === 0" style="text-align:center;padding:40px;color:#94a3b8">
      <el-empty description="暂无进行中的生产单" />
    </div>
    <div v-for="order in orders" :key="order.id" class="order-card">
      <div class="order-header">
        <span class="order-no">{{ order.orderNo }}</span>
        <span class="order-product">{{ order.productName }}</span>
        <span style="color:#64748b;font-size:0.85rem">计划: {{ order.qty }} | 产出: {{ order.totalOutput || 0 }}</span>
      </div>
      <div class="pipeline">
        <div v-for="(stage, idx) in stages" :key="stage" class="stage"
          :class="{ done: idx < currentStageIdx(order), active: idx === currentStageIdx(order), pending: idx > currentStageIdx(order) }">
          <div class="stage-dot">
            <span v-if="idx < currentStageIdx(order)">✓</span>
            <span v-else-if="idx === currentStageIdx(order)">●</span>
            <span v-else>{{ idx + 1 }}</span>
          </div>
          <div class="stage-label">{{ stage }}</div>
          <div v-if="idx < stages.length - 1" class="stage-line" :class="{ filled: idx < currentStageIdx(order) }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productionOrdersAPI } from '../../api/production'

const orders = ref([])
const stages = ['待排产', '印刷', '模切', '糊盒', '打包', '质检', '已入库', '已出库', '已送货']
const stageMap = Object.fromEntries(stages.map((s, i) => [s, i]))

function currentStageIdx(order) {
  return stageMap[order.status] ?? 0
}

onMounted(async () => {
  try {
    const res = await productionOrdersAPI.list({ page: 1, perPage: 200 })
    const all = res.data.data || []
    // Load progress for each
    const results = []
    for (const o of all) {
      if (o.status === '已送货') continue  // skip fully completed
      try {
        const p = await productionOrdersAPI.progress(o.id)
        results.push({ ...o, ...p.data.data })
      } catch(e) {
        results.push(o)
      }
    }
    orders.value = results.sort((a, b) => currentStageIdx(b) - currentStageIdx(a))
  } catch(e) {}
})
</script>

<style scoped>
.order-card { background:#fff; border-radius:10px; padding:20px; margin-bottom:16px; box-shadow:0 1px 3px rgba(0,0,0,0.08); }
.order-header { display:flex; gap:16px; align-items:center; margin-bottom:16px; }
.order-no { font-weight:700; font-size:1rem; color:#1e293b; }
.order-product { color:#64748b; font-size:0.9rem; }
.pipeline { display:flex; align-items:flex-start; gap:0; overflow-x:auto; padding:8px 0; }
.stage { display:flex; flex-direction:column; align-items:center; position:relative; min-width:60px; flex:1; }
.stage-dot { width:28px; height:28px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:0.75rem; font-weight:700; z-index:1; }
.stage-done .stage-dot { background:#22c55e; color:#fff; }
.stage-active .stage-dot { background:#2563eb; color:#fff; animation:pulse 1.5s infinite; }
.stage-pending .stage-dot { background:#e2e8f0; color:#94a3b8; }
.stage-label { font-size:0.7rem; color:#64748b; margin-top:4px; text-align:center; white-space:nowrap; }
.stage.done .stage-label { color:#22c55e; font-weight:600; }
.stage.active .stage-label { color:#2563eb; font-weight:700; }
.stage-line { position:absolute; top:14px; left:50%; width:100%; height:2px; background:#e2e8f0; z-index:0; }
.stage-line.filled { background:#22c55e; }
@keyframes pulse { 0%,100%{box-shadow:0 0 0 0 rgba(37,99,235,0.4)} 50%{box-shadow:0 0 0 6px rgba(37,99,235,0)} }
</style>
