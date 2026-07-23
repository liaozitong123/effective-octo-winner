<template>
  <div class="label-print-page">
    <div class="label-actions no-print">
      <el-button type="primary" @click="printLabel">打印标签</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <section v-if="order.id" class="label-sheet">
      <h1>华天纸箱厂</h1>

      <div class="label-row">
        <span>客户：</span>
        <strong>{{ value(order.customerName) }}</strong>
      </div>
      <div class="label-row">
        <span>规格：</span>
        <strong>{{ value(order.spec) }}</strong>
      </div>
      <div class="label-row">
        <span>产品名称：</span>
        <strong>{{ value(order.productName) }}</strong>
      </div>
      <div class="label-row">
        <span>下单数量：</span>
        <strong>{{ value(order.qty) }}</strong>
      </div>
      <div class="label-row handwritten">
        <span>实际数量：</span>
        <strong></strong>
      </div>
      <div class="label-row handwritten">
        <span>生产日期：</span>
        <strong></strong>
      </div>
    </section>

    <el-empty v-else description="未找到生产单" />
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productionOrdersAPI } from '../../api/production'

const route = useRoute()
const router = useRouter()
const order = reactive({})

function value(v) {
  return v !== null && v !== undefined && v !== '' ? v : ''
}

function printLabel() {
  window.print()
}

async function autoPrintIfNeeded() {
  if (route.query.autoPrint !== '1') return
  await nextTick()
  window.setTimeout(printLabel, 300)
}

onMounted(async () => {
  const id = route.query.id
  if (!id) return
  const res = await productionOrdersAPI.get(id)
  Object.assign(order, res.data?.data || {})
  autoPrintIfNeeded()
})
</script>

<style scoped>
@page {
  size: 100mm 80mm;
  margin: 4mm;
}

.label-print-page {
  min-height: 100vh;
  padding: 20px;
  background: #eef2f7;
}

.label-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.label-sheet {
  width: 100mm;
  height: 80mm;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 7mm 8mm 6mm;
  display: flex;
  flex-direction: column;
  gap: 3.4mm;
  background: #fff;
  color: #111827;
  border: 1px solid #111827;
  box-shadow: 0 14px 36px rgba(15, 23, 42, .16);
  font-family: "Microsoft YaHei", "SimHei", Arial, sans-serif;
}

.label-sheet h1 {
  margin: 0 0 1mm;
  text-align: center;
  font-size: 25px;
  line-height: 1;
  letter-spacing: 0;
}

.label-row {
  display: grid;
  grid-template-columns: 25mm minmax(0, 1fr);
  align-items: end;
  gap: 1mm;
  min-height: 7.6mm;
  font-size: 17px;
  line-height: 1.15;
}

.label-row span {
  font-weight: 700;
  white-space: nowrap;
}

.label-row strong {
  min-height: 7mm;
  padding: 0 .8mm .8mm;
  border-bottom: 1.5px solid #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 18px;
  font-weight: 700;
}

.label-row.handwritten strong {
  color: transparent;
}

@media print {
  html,
  body {
    width: 100mm;
    height: 80mm;
  }

  .no-print {
    display: none !important;
  }

  .label-print-page {
    min-height: 0;
    padding: 0;
    background: #fff;
  }

  .label-sheet {
    width: 100%;
    height: 72mm;
    margin: 0;
    padding: 1mm 2mm 0;
    border: none;
    box-shadow: none;
    print-color-adjust: exact;
    -webkit-print-color-adjust: exact;
  }
}
</style>
