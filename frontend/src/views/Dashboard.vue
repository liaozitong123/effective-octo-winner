<template>
  <div>
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6"><StatCard icon="Document" color="#2563eb" :value="data.todayOrders" label="今日订单" /></el-col>
      <el-col :span="6"><StatCard icon="Box" color="#22c55e" :value="data.monthOutput" label="本月产量" /></el-col>
      <el-col :span="6"><StatCard icon="Warning" color="#f59e0b" :value="data.stockAlert" label="库存预警" /></el-col>
      <el-col :span="6"><StatCard icon="User" color="#7c3aed" :value="data.totalCustomers" label="客户总数" /></el-col>
    </el-row>
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="card-title">应付账款</div>
          <div class="card-value red">¥{{ formatNum(data.receivable) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="card-title">应收账款</div>
          <div class="card-value green">¥{{ formatNum(data.payable) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="card-title">供应商总数</div>
          <div class="card-value blue">{{ data.totalSuppliers }}</div>
        </el-card>
      </el-col>
    </el-row>
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
.stat-row { margin-bottom:16px; }
.card-title { font-size:0.85rem; color:#64748b; margin-bottom:8px; }
.card-value { font-size:1.8rem; font-weight:800; }
.card-value.red { color:#ef4444; } .card-value.green { color:#22c55e; } .card-value.blue { color:#2563eb; }
</style>
