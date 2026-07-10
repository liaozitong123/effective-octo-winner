<template>
  <div class="dashboard-page">
    <section class="stat-grid">
      <StatCard icon="Document" color="#2563eb" :value="data.todayOrders" label="今日订单" />
      <StatCard icon="Box" color="#16a34a" :value="data.monthOutput" label="本月产量" />
      <StatCard icon="Warning" color="#d97706" :value="data.stockAlert" label="库存预警" />
      <StatCard icon="User" color="#7c3aed" :value="data.totalCustomers" label="客户总数" />
    </section>

    <section class="finance-grid">
      <el-card shadow="never" class="finance-card">
        <div class="card-title">应付账款</div>
        <div class="card-value red">¥{{ formatNum(data.receivable) }}</div>
      </el-card>
      <el-card shadow="never" class="finance-card">
        <div class="card-title">应收账款</div>
        <div class="card-value green">¥{{ formatNum(data.payable) }}</div>
      </el-card>
      <el-card shadow="never" class="finance-card">
        <div class="card-title">供应商总数</div>
        <div class="card-value blue">{{ data.totalSuppliers }}</div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '../api/auth'
import StatCard from '../components/StatCard.vue'

const data = ref({ todayOrders:0, monthOutput:0, stockAlert:0, receivable:0, payable:0, totalCustomers:0, totalSuppliers:0 })

onMounted(async () => {
  try { const res = await getDashboard(); data.value = res.data.data; } catch(e) {}
})

function formatNum(n) { return (n || 0).toLocaleString() }
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 14px;
}

.stat-grid,
.finance-grid {
  display: grid;
  gap: 12px;
}

.stat-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.finance-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.finance-card :deep(.el-card__body) {
  min-height: 108px;
  display: grid;
  align-content: center;
  gap: 10px;
  padding: 18px;
}

.card-title {
  font-size: 0.86rem;
  color: var(--erp-muted);
  font-weight: 700;
}

.card-value {
  font-size: 1.9rem;
  font-weight: 800;
  letter-spacing: 0;
}

.card-value.red { color: var(--erp-danger); }
.card-value.green { color: var(--erp-success); }
.card-value.blue { color: var(--erp-primary); }

@media (max-width: 1080px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .stat-grid,
  .finance-grid {
    grid-template-columns: 1fr;
  }
}
</style>
