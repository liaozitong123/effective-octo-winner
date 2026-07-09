<template>
  <div class="scan-page">
    <div class="scan-card" v-if="!submitted">
      <div class="workshop-tag">{{ form.workshop }}</div>
      <el-select v-model="form.productionOrderId" placeholder="选择生产单" size="large" style="width:100%" filterable @change="onOrderChange">
        <el-option v-for="o in orders" :key="o.id" :label="o.orderNo + ' ' + o.productName" :value="o.id" />
      </el-select>
      <div class="order-summary" v-if="selectedOrder">
        {{ selectedOrder.productName }} | 计划{{ selectedOrder.qty }} | 进度{{ selectedOrder.progressPct || 0 }}%
      </div>
      <el-input v-model.number="form.outputQty" type="number" placeholder="本次完成数量" size="large" style="margin-top:12px" />
      <div style="display:flex;gap:8px;margin-top:10px">
        <el-input v-model.number="form.wasteQty" type="number" placeholder="废品数" size="large" style="flex:1" />
        <el-input v-model="form.operator" placeholder="姓名" size="large" style="flex:1" />
      </div>
      <el-button type="primary" size="large" class="submit-btn" @click="handleSubmit" :loading="submitting">提交</el-button>
    </div>
    <div class="scan-card success-card" v-else>
      <div class="success-icon">✅</div>
      <div class="success-msg">报工成功</div>
      <div class="success-detail">+{{ form.outputQty }} | {{ form.workshop }}</div>
      <el-button size="large" style="margin-top:20px;width:100%" @click="resetForm">继续报工</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../api/request'

const route = useRoute()
const orders = ref([])
const selectedOrder = ref(null)
const submitted = ref(false)
const submitting = ref(false)
const form = reactive({ workshop: '', productionOrderId: null, outputQty: null, wasteQty: null, operator: '' })

onMounted(async () => {
  if (route.query.workshop) form.workshop = route.query.workshop
  form.productionOrderId = null; form.outputQty = null; form.wasteQty = null; form.operator = ''
  try {
    const res = await request.get('/public/production-orders')
    orders.value = (res.data.data || []).filter(o => o.status !== '已完成' && o.status !== '已送货' && o.status !== '已出库')
  } catch(e) { orders.value = [] }
})

function onOrderChange(id) { selectedOrder.value = orders.value.find(o => o.id === id) || null }

async function handleSubmit() {
  if (!form.productionOrderId) { alert('请选择生产单'); return }
  if (!form.outputQty || form.outputQty <= 0) { alert('请输入产量'); return }
  submitting.value = true
  try {
    await request.post('/public/report', {
      productionOrderId: form.productionOrderId,
      workshop: form.workshop,
      outputQty: form.outputQty,
      wasteQty: form.wasteQty || 0,
      operator: form.operator || '匿名',
    })
    submitted.value = true
  } catch(e) { alert('提交失败: ' + (e.response?.data?.message || e.message)) }
  submitting.value = false
}

function resetForm() {
  submitted.value = false; form.productionOrderId = null; form.outputQty = null
  form.wasteQty = null; form.operator = ''; selectedOrder.value = null
}
</script>

<style scoped>
.scan-page { min-height:100vh; background:#2563eb; display:flex; align-items:flex-start; justify-content:center; padding:30px 12px; }
.scan-card { background:#fff; border-radius:14px; padding:20px 18px; width:100%; max-width:360px; }
.workshop-tag { text-align:center; font-size:1.1rem; font-weight:700; color:#2563eb; margin-bottom:12px; }
.order-summary { font-size:0.82rem; color:#64748b; margin-top:8px; text-align:center; }
.submit-btn { width:100%; margin-top:14px; height:48px; font-size:1.1rem; }
.success-card { text-align:center; padding:36px 18px; }
.success-icon { font-size:3rem; }
.success-msg { font-size:1.2rem; font-weight:700; margin-top:8px; color:#22c55e; }
.success-detail { font-size:0.9rem; color:#64748b; margin-top:4px; }
</style>
