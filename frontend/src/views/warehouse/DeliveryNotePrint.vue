<template>
  <div class="delivery-print-page">
    <div class="print-actions no-print">
      <el-button type="primary" @click="printPage">打印送货单</el-button>
      <el-button @click="router.back()">返回</el-button>
      <el-checkbox v-model="printRedColumns" class="print-option">打印标红栏</el-checkbox>
    </div>

    <div v-if="note.id" class="print-sheet">
      <div class="paper-note">三联二等分针式打印纸</div>

      <header class="sheet-header">
        <h1>福建泉州琪华工艺品有限公司</h1>
        <h2>华天纸箱送货单</h2>
      </header>

      <section class="meta-grid">
        <div class="meta-item span-2"><span>客户：</span><strong>{{ value(note.customerName) }}</strong></div>
        <div class="meta-item span-2"><span>联系人：</span><strong>{{ value(note.customerContact) }}</strong></div>
        <div class="meta-item span-2"><span>送货单号：</span><strong>{{ value(note.noteNo) }}</strong></div>
        <div class="meta-item span-2"><span>地址：</span><strong>{{ value(note.customerAddress) }}</strong></div>
        <div class="meta-item span-2"><span>电话：</span><strong>{{ value(note.customerPhone) }}</strong></div>
        <div class="meta-item span-2"><span>送货日期：</span><strong>{{ value(note.deliveryDate) }}</strong></div>
      </section>

      <section class="table-with-copy">
        <table class="detail-table">
          <colgroup>
            <col class="col-index" />
            <col class="col-order" />
            <col class="col-product" />
            <col class="col-material" />
            <col class="col-spec" />
            <col class="col-qty" />
            <col class="col-area" />
            <col class="col-price" />
            <col class="col-money" />
            <col v-if="printRedColumns" class="col-stock" />
            <col v-if="printRedColumns" class="col-unit" />
          </colgroup>
          <thead>
            <tr>
              <th>序号</th>
              <th>销售订单号</th>
              <th>产品名称</th>
              <th>客户材质</th>
              <th>规格</th>
              <th>数量</th>
              <th>面积</th>
              <th>纸箱单价</th>
              <th>金额</th>
              <th v-if="printRedColumns" class="red-column">剩余库存</th>
              <th v-if="printRedColumns" class="red-column">客户平方单价</th>
            </tr>
          </thead>
          <tbody>
            <tr class="data-row">
              <td>1</td>
              <td>{{ value(note.salesOrderNo) }}</td>
              <td>{{ value(note.productName) }}</td>
              <td>{{ value(note.customerMaterial) }}</td>
              <td>{{ value(note.spec) }}</td>
              <td>{{ value(note.deliveryQty) }}</td>
              <td>{{ value(note.area) }}</td>
              <td>{{ value(note.boxUnitPrice) }}</td>
              <td>{{ value(note.amount) }}</td>
              <td v-if="printRedColumns">{{ value(note.remainingStock) }}</td>
              <td v-if="printRedColumns">{{ value(note.customerUnitPrice) }}</td>
            </tr>
            <tr v-for="rowNo in blankRows" :key="rowNo" class="blank-row">
              <td>{{ rowNo }}</td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td v-if="printRedColumns"></td>
              <td v-if="printRedColumns"></td>
            </tr>
            <tr class="total-row">
              <td colspan="5">合计人民币总金额：（{{ amountUppercase }}）</td>
              <td>{{ value(note.deliveryQty) }} 个</td>
              <td>{{ value(note.area) }} ㎡</td>
              <td></td>
              <td :colspan="printRedColumns ? 3 : 1">{{ value(note.amount) }} 元</td>
            </tr>
          </tbody>
        </table>

        <aside class="copy-labels">
          <span>一联存根（白）</span>
          <span>二联结算（红）</span>
          <span>三联客户（黄）</span>
        </aside>
      </section>

      <section class="manual-row">
        <div><span>备注：</span><strong>{{ value(note.notes) }}</strong></div>
        <div><span>司机：</span><strong>{{ value(note.driver) }}</strong></div>
        <div><span>开单人：</span><strong>{{ value(note.issuer) }}</strong></div>
        <div><span>业务员：</span><strong>{{ value(note.salesperson) }}</strong></div>
        <div><span>复核计数：</span><strong>{{ value(note.reviewCount) }}</strong></div>
        <div><span>客户签字：</span><strong>{{ value(note.customerSignature) }}</strong></div>
      </section>

      <footer class="terms">
        <p>特此说明：1、对于产品质量问题有异议，应在收货后48小时内向我厂提出，否则视产品为合格；</p>
        <p>2、本送货单，货到验明，收妥实与凭此，客户收货人一经签字即生效；3、收货单位收货后按双方约定结算货款。</p>
      </footer>
    </div>

    <el-empty v-else description="未找到送货单" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { deliveryNotesAPI } from '../../api/sales'

const route = useRoute()
const router = useRouter()
const note = reactive({})
const printRedColumns = ref(true)
const blankRows = Array.from({ length: 7 }, (_, index) => index + 2)

const amountUppercase = computed(() => moneyToChinese(note.amount))

function value(v) {
  return v !== null && v !== undefined && v !== '' ? v : '-'
}

function numberValue(v) {
  const number = Number(v)
  return Number.isFinite(number) ? number : 0
}

function moneyToChinese(value) {
  const amount = Math.round(numberValue(value) * 100)
  if (amount <= 0) return '零元整'

  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const units = ['分', '角', '元', '拾', '佰', '仟', '万', '拾', '佰', '仟', '亿', '拾', '佰', '仟']
  let text = ''
  let zeroPending = false

  String(amount).split('').reverse().forEach((char, index) => {
    const digit = Number(char)
    const unit = units[index] || ''
    if (digit === 0) {
      if (['元', '万', '亿'].includes(unit)) {
        text = unit + text
        zeroPending = false
      } else {
        zeroPending = true
      }
      return
    }
    text = `${zeroPending ? '零' : ''}${digits[digit]}${unit}${text}`
    zeroPending = false
  })

  text = text
    .replace(/零+/g, '零')
    .replace(/零(万|亿)/g, '$1')
    .replace(/亿万/g, '亿')
    .replace(/零元/g, '元')
  if (!text.includes('角') && !text.includes('分')) text += '整'
  return text
}

async function loadNote() {
  const id = route.query.id
  if (!id) return
  const res = await deliveryNotesAPI.get(id)
  Object.assign(note, res.data?.data || {})
}

async function printPage() {
  if (note.id && !note.printed) {
    const res = await deliveryNotesAPI.markPrinted(note.id)
    Object.assign(note, res.data?.data || {})
  }
  window.print()
}

onMounted(loadNote)
</script>

<style scoped>
@page {
  size: A4 landscape;
  margin: 7mm;
}

.delivery-print-page {
  min-height: 100vh;
  padding: 18px;
  background: #eef2f7;
  color: #000;
  font-family: SimSun, "Songti SC", serif;
}

.print-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.print-option {
  margin-left: 8px;
  font-weight: 700;
}

.print-sheet {
  position: relative;
  width: 297mm;
  max-width: calc(100vw - 36px);
  min-height: 210mm;
  margin: 0 auto;
  padding: 5mm 11mm 7mm 11mm;
  background:
    linear-gradient(#e5e7eb 1px, transparent 1px),
    linear-gradient(90deg, #e5e7eb 1px, transparent 1px),
    #fff;
  background-size: 18.5mm 7.4mm;
  border: 1px solid transparent;
  box-shadow: 0 14px 36px rgba(15, 23, 42, .16);
}

.paper-note {
  margin-left: 18mm;
  font-size: 13px;
  line-height: 1.2;
}

.sheet-header {
  text-align: center;
  margin-top: 10mm;
  margin-bottom: 9mm;
}

.sheet-header h1 {
  margin: 0 0 5mm;
  font-size: 23px;
  line-height: 1;
  letter-spacing: 0;
  font-weight: 800;
}

.sheet-header h2 {
  margin: 0;
  font-size: 17px;
  line-height: 1;
  letter-spacing: 0;
  font-weight: 800;
}

.meta-grid {
  display: grid;
  grid-template-columns: 29mm 47mm 29mm 47mm 35mm 57mm;
  row-gap: 6mm;
  align-items: end;
  width: 244mm;
  margin-left: 9mm;
  margin-bottom: 2mm;
  font-size: 14px;
  font-weight: 800;
}

.meta-item {
  display: flex;
  align-items: flex-end;
  min-width: 0;
}

.span-2 {
  grid-column: span 2;
}

.meta-item span {
  flex: 0 0 auto;
}

.meta-item strong {
  min-width: 0;
  flex: 1;
  min-height: 5mm;
  border-bottom: 1px solid transparent;
  font-size: 14px;
  font-weight: 700;
  overflow-wrap: anywhere;
}

.table-with-copy {
  display: grid;
  grid-template-columns: 244mm 16mm;
  column-gap: 5mm;
  align-items: stretch;
  margin-left: 9mm;
}

.detail-table {
  width: 244mm;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 13px;
  background: #fff;
}

.detail-table th,
.detail-table td {
  border: 1px solid #000;
  padding: 0 1mm;
  text-align: center;
  vertical-align: middle;
  overflow-wrap: anywhere;
}

.detail-table th {
  height: 6.5mm;
  font-weight: 800;
}

.detail-table td {
  height: 10.6mm;
}

.detail-table .blank-row td {
  height: 7.2mm;
}

.detail-table .total-row td {
  height: 9.6mm;
  font-size: 13px;
  font-weight: 800;
}

.red-column {
  color: #f00;
}

.col-index { width: 15mm; }
.col-order { width: 30mm; }
.col-product { width: 25mm; }
.col-material { width: 23mm; }
.col-spec { width: 15mm; }
.col-qty { width: 20mm; }
.col-area { width: 15mm; }
.col-price { width: 22mm; }
.col-money { width: 11mm; }
.col-stock { width: 15mm; }
.col-unit { width: 15mm; }

.copy-labels {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  border-left: 1px dashed #777;
  padding-left: 5mm;
  font-size: 13px;
  font-weight: 800;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.copy-labels span {
  display: block;
}

.manual-row {
  display: grid;
  grid-template-columns: 2fr repeat(5, 1fr);
  gap: 2mm;
  width: 244mm;
  margin: 3mm 0 0 9mm;
  font-size: 13px;
  font-weight: 700;
}

.manual-row div {
  min-height: 8mm;
  border-bottom: 1px solid #000;
  display: flex;
  align-items: flex-end;
  min-width: 0;
}

.manual-row span {
  flex: 0 0 auto;
}

.manual-row strong {
  min-width: 0;
  overflow-wrap: anywhere;
}

.terms {
  width: 244mm;
  margin: 12mm 0 0 9mm;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.7;
}

.terms p {
  margin: 0;
}

@media print {
  .no-print {
    display: none;
  }

  .delivery-print-page {
    min-height: 0;
    padding: 0;
    background: #fff;
  }

  .print-sheet {
    width: 100%;
    max-width: none;
    min-height: 0;
    padding: 0;
    border: none;
    box-shadow: none;
    print-color-adjust: exact;
    -webkit-print-color-adjust: exact;
  }
}
</style>
