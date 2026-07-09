<template>
  <el-card shadow="hover" class="stat-card">
    <div class="stat-icon" :style="{ background: iconBg }">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div>
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ icon: String, color: String, value: [String,Number], label: String })
const iconBg = computed(() => (props.color || '#2563eb') + '20')
const iconColor = computed(() => props.color || '#2563eb')
</script>

<style scoped>
.stat-card { display:flex; align-items:center; gap:14px; }
.stat-icon { width:44px; height:44px; border-radius:10px; display:flex; align-items:center; justify-content:center; color:v-bind(iconColor); }
.stat-value { font-size:1.5rem; font-weight:800; line-height:1; margin-bottom:4px; }
.stat-label { font-size:0.82rem; color:#64748b; }
</style>
