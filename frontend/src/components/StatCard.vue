<template>
  <el-card shadow="never" class="stat-card">
    <div class="stat-icon" :style="{ background: iconBg, color: iconColor }">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-main">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value">{{ value }}</div>
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
.stat-card {
  min-height: 96px;
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.stat-main {
  min-width: 0;
}

.stat-value {
  font-size: 1.65rem;
  font-weight: 800;
  line-height: 1.1;
  margin-top: 6px;
  color: var(--erp-text);
  letter-spacing: 0;
}

.stat-label {
  font-size: 0.84rem;
  color: var(--erp-muted);
  white-space: nowrap;
}
</style>
