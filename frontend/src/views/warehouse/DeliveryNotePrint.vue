<template>
  <div class="delivery-print-page">
    <div class="print-actions no-print">
      <el-button type="primary" @click="printPage">打印送货单</el-button>
      <el-button @click="router.back()">返回</el-button>
      <el-checkbox v-model="printRedColumns" class="print-option">打印标红栏</el-checkbox>
    </div>

    <el-alert v-if="printError" class="print-error no-print" type="error" :title="printError" show-icon :closable="false" />

    <div v-if="printRows.length && !printError" class="print-sheet">
      <div class="paper-note">三联二等分针式打印纸</div>

      <header class="sheet-header">
        <h1>福建泉州琪华工艺品有限公司</h1>
        <h2>华天纸箱送货单</h2>
      </header>

      <section class="meta-grid">
        <div class="meta-item span-2"><span>客户：</span><strong>{{ value(note.customerName) }}</strong></div>
        <div class="meta-item span-2"><span>联系人：</span><strong>{{ value(note.customerContact) }}</strong></div>
        <div class="meta-item span-2"><span>送货单号：</span><strong>{{ value(headerNoteNo) }}</strong></div>
        <div class="meta-item span-2"><span>地址：</span><strong>{{ value(note.customerAddress) }}</strong></div>
        <div class="meta-item span-2"><span>电话：</span><strong>{{ value(note.customerPhone) }}</strong></div>
        <div class="meta-item span-2"><span>送货日期：</span><strong>{{ value(note.deliveryDate) }}</strong></div>
      </section>

      <section class="table-with-copy">
        <table class="detail-table">
          <colgroup>
            <col class="col-index" />
            <col class="col-note" />
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
              <th>送货单号</th>
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
            <tr v-for="(row, index) in printRows" :key="row.id" class="data-row">
              <td>{{ index + 1 }}</td>
              <td>{{ value(row.noteNo) }}</td>
              <td>{{ value(row.salesOrderNo) }}</td>
              <td>{{ value(row.productName) }}</td>
              <td>{{ value(row.customerMaterial) }}</td>
              <td>{{ value(row.spec) }}</td>
              <td>{{ value(row.deliveryQty) }}</td>
              <td>{{ value(row.area) }}</td>
              <td>{{ value(row.boxUnitPrice) }}</td>
              <td>{{ value(row.amount) }}</td>
              <td v-if="printRedColumns">{{ value(row.remainingStock) }}</td>
              <td v-if="printRedColumns">{{ value(row.customerUnitPrice) }}</td>
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
              <td></td>
              <td v-if="printRedColumns"></td>
              <td v-if="printRedColumns"></td>
            </tr>
            <tr class="total-row">
              <td colspan="6">合计人民币总金额：（{{ amountUppercase }}）</td>
              <td>{{ value(totals.deliveryQty) }} 个</td>
              <td>{{ value(totals.area) }} ㎡</td>
              <td></td>
              <td :colspan="printRedColumns ? 3 : 1">{{ value(totals.amount) }} 元</td>
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

    <el-empty v-else-if="!printError" description="未找到送货单" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deliveryNotesAPI } from '../../api/sales'

const route = useRoute()
const router = useRouter()
const note = reactive({})
const notes = ref([])
const printError = ref('')
const printRedColumns = ref(true)
const COMMON_PRINT_FIELDS = [
  { key: 'notes', label: '备注' },
  { key: 'driver', label: '司机' },
  { key: 'deliveryDate', label: '送货日期' },
  { key: 'issuer', label: '开单人' },
  { key: 'salesperson', label: '业务员' },
  { key: 'reviewCount', label: '复核计数' },
  { key: 'customerSignature', label: '客户签字' },
]

const printRows = computed(() => notes.value)
const headerNoteNo = computed(() => printRows.value.length > 1 ? '见明细' : note.noteNo)
const blankRows = computed(() => {
  const start = printRows.value.length + 1
  return Array.from({ length: Math.max(0, 8 - printRows.value.length) }, (_, index) => start + index)
})
const totals = computed(() => ({
  deliveryQty: printRows.value.reduce((sum, row) => sum + numberValue(row.deliveryQty), 0),
  area: round4(printRows.value.reduce((sum, row) => sum + numberValue(row.area), 0)),
  amount: round2(printRows.value.reduce((sum, row) => sum + numberValue(row.amount), 0)),
}))
const amountUppercase = computed(() => moneyToChinese(totals.value.amount))

function value(v) {
  return v !== null && v !== undefined && v !== '' ? v : '-'
}

function numberValue(v) {
  const number = Number(v)
  return Number.isFinite(number) ? number : 0
}

function round2(value) {
  return Math.round(value * 100) / 100
}

function round4(value) {
  return Math.round(value * 10000) / 10000
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

function parseIds() {
  if (route.query.ids) {
    return String(route.query.ids).split(',').map(id => Number(id)).filter(Number.isFinite)
  }
  const id = Number(route.query.id)
  return Number.isFinite(id) ? [id] : []
}

function normalizeCommonValue(value) {
  return value === null || value === undefined ? '' : String(value).trim()
}

function sameCustomer(rows) {
  const first = rows[0]
  return rows.every(row => {
    if (first.customerId || row.customerId) return Number(row.customerId) === Number(first.customerId)
    return normalizeCommonValue(row.customerName) === normalizeCommonValue(first.customerName)
  })
}

function differentCommonFields(rows) {
  return COMMON_PRINT_FIELDS.filter(field => {
    const firstValue = normalizeCommonValue(rows[0]?.[field.key])
    return rows.some(row => normalizeCommonValue(row[field.key]) !== firstValue)
  })
}

function validatePrintableNotes(rows) {
  if (rows.length <= 1) return ''
  if (!sameCustomer(rows)) return '无法打印：只能合并打印同一个客户的送货单'
  const printedRows = rows.filter(row => row.printed || row.deliveryStatus === '已送货')
  if (printedRows.length) return `无法打印：${printedRows.map(row => row.noteNo).join('、')} 已送货`
  const emptyQtyRows = rows.filter(row => numberValue(row.deliveryQty) <= 0)
  if (emptyQtyRows.length) return `无法打印：${emptyQtyRows.map(row => row.noteNo).join('、')} 的送货数量为空或为0`
  const diffFields = differentCommonFields(rows)
  if (diffFields.length) return `无法打印：${diffFields.map(field => field.label).join('、')}填写不同`
  return ''
}

async function loadNote() {
  const ids = parseIds()
  if (!ids.length) return
  const rows = []
  for (const id of ids) {
    const res = await deliveryNotesAPI.get(id)
    const row = res.data?.data
    if (row?.id) rows.push(row)
  }
  printError.value = validatePrintableNotes(rows)
  notes.value = rows
  Object.assign(note, rows[0] || {})
}

async function printPage() {
  if (printError.value) {
    ElMessage.error(printError.value)
    return
  }
  const unprintedRows = printRows.value.filter(row => row.id && !row.printed)
  if (unprintedRows.length > 1) {
    const res = await deliveryNotesAPI.markPrintedBatch(unprintedRows.map(row => row.id))
    const updatedRows = res.data?.data || []
    notes.value = printRows.value.map(row => updatedRows.find(updated => updated.id === row.id) || row)
    Object.assign(note, notes.value[0] || {})
  } else if (note.id && !note.printed) {
    const res = await deliveryNotesAPI.markPrinted(note.id)
    Object.assign(note, res.data?.data || {})
    notes.value = [res.data?.data || note]
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

.print-error {
  width: min(960px, calc(100vw - 36px));
  margin: 0 auto 14px;
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
  font-size: 12px;
  background: #fff;
}

.detail-table th,
.detail-table td {
  border: 1px solid #000;
  padding: 0 .7mm;
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
  font-size: 12px;
  font-weight: 800;
}

.red-column {
  color: #f00;
}

.col-index { width: 10mm; }
.col-note { width: 28mm; }
.col-order { width: 28mm; }
.col-product { width: 25mm; }
.col-material { width: 20mm; }
.col-spec { width: 24mm; }
.col-qty { width: 15mm; }
.col-area { width: 15mm; }
.col-price { width: 17mm; }
.col-money { width: 17mm; }
.col-stock { width: 15mm; }
.col-unit { width: 16mm; }

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
