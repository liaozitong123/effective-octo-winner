<template>
  <div class="production-print-page">
    <div class="print-actions no-print">
      <el-button type="primary" @click="printPage">打印生产单</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <div v-if="order.id" class="print-sheet">
      <header class="sheet-header">
        <h1>生产单</h1>
        <div class="order-no">{{ order.orderNo || '-' }}</div>
      </header>

      <section class="summary-grid">
        <div><span>采购单号</span><strong>{{ value(order.purchaseOrderNo) }}</strong></div>
        <div><span>下单日期</span><strong>{{ value(order.orderDate) }}</strong></div>
        <div><span>客户</span><strong>{{ value(order.customerName) }}</strong></div>
        <div><span>供应商</span><strong>{{ value(order.supplierName) }}</strong></div>
        <div><span>产品名称</span><strong>{{ value(order.productName) }}</strong></div>
        <div><span>生产员</span><strong>{{ value(order.operator) }}</strong></div>
        <div><span>生产备注</span><strong>{{ value(order.productionStatus) }}</strong></div>
      </section>

      <table class="detail-table">
        <tbody>
          <tr>
            <th>规格</th>
            <td>{{ value(order.spec) }}</td>
            <th>客户材质</th>
            <td>{{ value(order.material) }}</td>
          </tr>
          <tr>
            <th>盒式</th>
            <td>{{ value(order.boxType) }}</td>
            <th>钉口</th>
            <td>{{ value(order.stitchType) }}</td>
          </tr>
          <tr>
            <th>下单数量</th>
            <td>{{ value(order.qty) }}</td>
            <th>总面积</th>
            <td>{{ value(order.totalArea) }}</td>
          </tr>
          <tr>
            <th>生产材质</th>
            <td>{{ value(order.productionMaterial) }}</td>
            <th>楞别</th>
            <td>{{ value(order.fluteType) }}</td>
          </tr>
          <tr>
            <th>纸板长度</th>
            <td>{{ value(order.boardLength) }}</td>
            <th>纸板宽度</th>
            <td>{{ value(order.boardWidth) }}</td>
          </tr>
          <tr>
            <th>开数</th>
            <td>{{ value(order.cutCount) }}</td>
            <th>凹压线</th>
            <td>{{ value(order.crease) }}</td>
          </tr>
        </tbody>
      </table>

      <section class="worker-fill-section">
        <div class="worker-fill-item">
          <span>实际数量</span>
          <div class="blank-line"></div>
        </div>
        <div class="worker-fill-item wide">
          <span>验收问题</span>
          <div class="blank-box"></div>
        </div>
        <div class="worker-fill-item wide">
          <span>完成状况</span>
          <div class="blank-box"></div>
        </div>
      </section>

      <section :class="['notes-section', { 'has-image': isImageNote(order.notes) }]">
        <h2>备注</h2>
        <img v-if="isImageNote(order.notes)" :src="order.notes" class="note-image" alt="备注图片" />
        <div v-else class="note-text">{{ value(order.notes) }}</div>
      </section>

      <section class="sign-row">
        <div>生产员签字：</div>
        <div>日期：</div>
      </section>
    </div>

    <el-empty v-else description="未找到生产单" />
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productionOrdersAPI } from '../../api/production'

const route = useRoute()
const router = useRouter()
const order = reactive({})

function value(v) {
  return v !== null && v !== undefined && v !== '' ? v : '-'
}

function isImageNote(v) {
  return typeof v === 'string' && v.startsWith('data:image/')
}

async function printPage() {
  if (order.id && !order.printed) {
    const res = await productionOrdersAPI.markPrinted(order.id)
    Object.assign(order, res.data?.data || {})
  }
  window.print()
}

onMounted(async () => {
  const id = route.query.id
  if (!id) return
  const res = await productionOrdersAPI.get(id)
  Object.assign(order, res.data?.data || {})
})
</script>

<style scoped>
@page {
  size: A4 portrait;
  margin: 6mm;
}

.production-print-page {
  background: #eef2f7;
  min-height: 100vh;
  padding: 18px;
}

.print-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.print-sheet {
  width: 210mm;
  max-width: calc(100vw - 36px);
  min-height: 297mm;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  margin: 0 auto;
  background: #fff;
  color: #111827;
  border: 1px solid #111827;
  padding: 10mm;
  box-shadow: 0 14px 36px rgba(15, 23, 42, .16);
}

.sheet-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  border-bottom: 2px solid #111827;
  padding-bottom: 3mm;
  margin-bottom: 3mm;
}

.sheet-header h1 {
  font-size: 23px;
  line-height: 1;
  margin: 0;
}

.order-no {
  font-size: 14px;
  font-weight: 700;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border-left: 1px solid #111827;
  border-top: 1px solid #111827;
  margin-bottom: 3mm;
}

.summary-grid div {
  min-height: 9mm;
  border-right: 1px solid #111827;
  border-bottom: 1px solid #111827;
  padding: 1.4mm 2mm;
}

.summary-grid span {
  display: block;
  color: #4b5563;
  font-size: 10.5px;
  margin-bottom: .6mm;
}

.summary-grid strong {
  font-size: 12.5px;
  line-height: 1.15;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  line-height: 1.15;
}

.detail-table th,
.detail-table td {
  border: 1px solid #111827;
  padding: 1.25mm 2mm;
  text-align: left;
}

.detail-table th {
  width: 23mm;
  background: #f3f4f6;
}

.notes-section {
  flex: 1 1 auto;
  margin-top: 3mm;
  border: 1px solid #111827;
  min-height: 110mm;
  padding: 2mm;
  page-break-inside: avoid;
  overflow: hidden;
}

.notes-section h2 {
  font-size: 12.5px;
  line-height: 1;
  margin: 0 0 1.5mm;
}

.note-text {
  height: calc(100% - 5mm);
  min-height: 12mm;
  overflow: hidden;
  white-space: pre-wrap;
  line-height: 1.35;
  font-size: 12px;
}

.note-image {
  display: block;
  max-width: 100%;
  max-height: 100%;
  margin: 0 auto;
  object-fit: contain;
}

.notes-section.has-image {
  min-height: 158mm;
  display: flex;
  flex-direction: column;
}

.notes-section.has-image .note-image {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.worker-fill-section {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 2mm;
  margin-top: 2mm;
  border: 1px solid #111827;
  padding: 1.2mm;
  page-break-inside: avoid;
}

.worker-fill-item {
  display: grid;
  grid-template-columns: 16mm minmax(0, 1fr);
  align-items: end;
  gap: 1.5mm;
  min-height: 5.5mm;
  font-size: 11.5px;
  font-weight: 700;
}

.worker-fill-item.wide {
  grid-column: auto;
  align-items: end;
}

.blank-line {
  height: 4mm;
  border-bottom: 1px solid #111827;
}

.blank-box {
  min-height: 4mm;
  border-bottom: 1px solid #111827;
}

.sign-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10mm;
  margin-top: auto;
  padding-top: 4mm;
  font-size: 12.5px;
  page-break-inside: avoid;
}

@media print {
  html,
  body {
    width: 210mm;
    min-height: 297mm;
  }

  .no-print {
    display: none;
  }

  .production-print-page {
    background: #fff;
    padding: 0;
    min-height: 0;
  }

  .print-sheet {
    width: 100%;
    max-width: none;
    height: 285mm;
    min-height: 285mm;
    border: none;
    padding: 0;
    box-shadow: none;
    print-color-adjust: exact;
    -webkit-print-color-adjust: exact;
  }

  .sheet-header {
    padding-bottom: 2mm;
    margin-bottom: 2mm;
  }

  .summary-grid {
    margin-bottom: 2mm;
  }

  .summary-grid div {
    min-height: 7.5mm;
    padding: 1mm 1.6mm;
  }

  .detail-table th,
  .detail-table td {
    padding: .9mm 1.6mm;
  }

  .worker-fill-section {
    margin-top: 1.6mm;
  }

  .notes-section {
    min-height: 0;
  }

  .notes-section.has-image {
    min-height: 0;
  }
}
</style>
