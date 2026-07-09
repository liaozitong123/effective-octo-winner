<template>
  <div>
    <div class="btn-group no-print">
      <el-button type="primary" @click="window.print()">打印送货单</el-button>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <div class="bill-container">
      <div class="bill-header-title">福建泉州琪华工艺品有限公司</div>
      <div class="bill-sub-title">华天纸箱送货单</div>
      <div class="bill-info-row">
        <span>送至：<input type="text" v-model="data.customerName"></span>
        <span>送货单号：<input type="text" v-model="data.noteNo"></span>
      </div>
      <div class="bill-info-row">
        <span>送货人：<input type="text" v-model="data.carrier"></span>
        <span>送货日期：<input type="date" v-model="data.deliveryDate"></span>
      </div>
      <table>
        <thead>
          <tr>
            <th>货号规格</th><th>下单日期</th><th>材质</th><th>单价</th><th>单个面积</th>
            <th>单个价格</th><th>出货数量</th><th>出货金额</th><th>盒式</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in items" :key="idx">
            <td><input type="text" v-model="item.spec"></td>
            <td><input type="date" v-model="item.orderDate"></td>
            <td><input type="text" v-model="item.material"></td>
            <td><input type="number" step="0.01" v-model="item.price"></td>
            <td><input type="number" step="0.01" v-model="item.area"></td>
            <td><input type="number" step="0.01" v-model="item.unitPrice" @change="calcTotal"></td>
            <td><input type="number" v-model.number="item.qty" @change="calcTotal"></td>
            <td><input type="number" step="0.01" v-model="item.amount" readonly></td>
            <td><input type="text" v-model="item.boxType"></td>
          </tr>
          <tr class="total-row">
            <td colspan="6">合计</td>
            <td><input type="number" :value="totalQty" readonly></td>
            <td><input type="number" step="0.01" :value="totalAmount.toFixed(2)" readonly></td>
            <td></td>
          </tr>
        </tbody>
      </table>
      <div class="sign-area">
        <div>收货人（签字）：<span class="sign-name"><input type="text" v-model="data.signName"></span></div>
        <div class="note-text">货到验明：收受属实，如有遗漏，请于两天内告知，否则当正确无异议。存根：白色回单；红色客户；黄色</div>
      </div>
      <div class="btn-group no-print">
        <el-button @click="addRow">新增一行</el-button>
        <el-button @click="calcTotal">自动计算合计</el-button>
        <el-button type="primary" @click="window.print()">打印送货单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { deliveryNotesAPI, salesOrdersAPI, customersAPI } from '../../api/sales'

const route = useRoute()
const data = reactive({ noteNo:'', customerName:'', carrier:'', deliveryDate:'', signName:'' })
const items = reactive([
  { spec:'', orderDate:'', material:'', price:0, area:0, unitPrice:0, qty:0, amount:0, boxType:'' },
  { spec:'', orderDate:'', material:'', price:0, area:0, unitPrice:0, qty:0, amount:0, boxType:'' },
  { spec:'', orderDate:'', material:'', price:0, area:0, unitPrice:0, qty:0, amount:0, boxType:'' },
])
const totalQty = computed(() => items.reduce((s, i) => s + (i.qty || 0), 0))
const totalAmount = computed(() => items.reduce((s, i) => s + (i.amount || 0), 0))

function calcTotal() {
  items.forEach(i => { i.amount = parseFloat(((i.unitPrice || 0) * (i.qty || 0)).toFixed(2)) })
}

function addRow() {
  items.push({ spec:'', orderDate:'', material:'', price:0, area:0, unitPrice:0, qty:0, amount:0, boxType:'' })
}

onMounted(async () => {
  const id = route.query.id
  if (id) {
    try {
      const res = await deliveryNotesAPI.list({ page:1, perPage:100, q:id })
      const dns = res.data.data || []
      const dn = dns.find(d => String(d.id) === String(id))
      if (dn) {
        data.noteNo = dn.noteNo || ''
        data.customerName = dn.customerName || ''
        data.deliveryDate = dn.deliveryDate || ''
        data.carrier = dn.carrier || ''
        // Try loading sales order details for the first item
        try {
          const soRes = await salesOrdersAPI.list({ page:1, perPage:100 })
          const sos = soRes.data.data || []
          const so = sos.find(s => s.customerName === dn.customerName)
          if (so && items.length > 0) {
            items[0].spec = so.spec || ''
            items[0].material = so.material || ''
            items[0].unitPrice = so.unitPrice || 0
            items[0].qty = dn.qty || 0
            items[0].amount = parseFloat(((items[0].unitPrice * items[0].qty).toFixed(2)))
          } else if (items.length > 0) {
            items[0].qty = dn.qty || 0
          }
        } catch(e) {}
      }
    } catch(e) {}
  }
})
</script>

<style scoped>
* { margin:0; padding:0; box-sizing:border-box; font-family:"SimSun","宋体",monospace; }
.bill-container { width:820px; margin:0 auto; background:#fff; border:1px solid #333; padding:20px; position:relative; }
.bill-container::before { content:""; position:absolute; top:-12px; left:0; right:0; height:12px; background-image:radial-gradient(circle,#000 4px,transparent 4px); background-size:45px 12px; background-repeat:repeat-x; }
.bill-header-title { text-align:center; font-size:24px; font-weight:bold; margin-bottom:8px; }
.bill-sub-title { text-align:center; font-size:18px; margin-bottom:20px; }
.bill-info-row { display:flex; justify-content:space-between; margin-bottom:8px; font-size:15px; align-items:center; }
.bill-info-row input { border:1px solid #999; padding:2px 4px; width:140px; }
table { width:100%; border-collapse:collapse; margin:15px 0; font-size:14px; }
th, td { border:1px solid #333; padding:4px; text-align:center; }
td input { width:100%; border:none; outline:none; text-align:center; background:transparent; font-size:13px; }
.total-row { font-weight:bold; }
.sign-area { margin-top:20px; font-size:15px; line-height:1.8; }
.sign-name input { font-size:36px; font-family:"KaiTi","楷体"; border:none; border-bottom:1px solid #000; width:200px; outline:none; }
.note-text { margin-top:15px; font-size:13px; color:#333; }
.btn-group { text-align:center; margin:20px 0; }
@media print {
  .no-print { display:none; }
  .bill-container { width:100%; border:none; }
  .bill-container::before { display:none; }
}
</style>
