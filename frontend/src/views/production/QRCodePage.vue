<template>
  <div style="background:#fff;padding:24px;border-radius:10px">
    <p style="text-align:center;color:#64748b;margin-bottom:20px">
      每个加工区一个固定二维码，打印后贴到对应工位。员工扫码选择生产单、填产量即可报工。
    </p>
    <div style="display:flex;flex-wrap:wrap;gap:20px;justify-content:center">
      <div v-for="area in areas" :key="area" style="text-align:center;border:2px solid #e2e8f0;padding:20px;border-radius:12px;width:180px" class="qr-card">
        <div style="font-weight:700;font-size:1.1rem;margin-bottom:8px;color:#1e293b">{{ area }}</div>
        <canvas :ref="(el) => setQrRef(area, el)" style="width:150px;height:150px"></canvas>
        <el-button size="small" style="margin-top:8px" @click="printSingle(area)">单独打印</el-button>
      </div>
    </div>
    <div style="text-align:center;margin-top:24px;display:flex;gap:12px;justify-content:center">
      <el-button type="primary" @click="window.print()">🖨️ 打印全部二维码</el-button>
      <el-button @click="downloadAll">📥 下载全部二维码</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import QRCode from 'qrcode'

const qrRefs = {}
const areas = ['印刷区', '模切区', '糊盒区', '打包区', '质检区']
const SCAN_URL = window.location.origin

function setQrRef(area, el) { if (el) qrRefs[area] = el }

onMounted(async () => {
  await nextTick()
  setTimeout(() => {
    areas.forEach(async (area) => {
      const canvas = qrRefs[area]
      if (canvas) {
        const url = `${SCAN_URL}/production/report?workshop=${encodeURIComponent(area)}`
        await QRCode.toCanvas(canvas, url, { width: 150, margin: 1 })
      }
    })
  }, 300)
})

function printSingle(area) {
  const canvas = qrRefs[area]
  if (!canvas) return
  const win = window.open('', '_blank')
  win.document.write(`<html><head><title>${area}</title><style>body{text-align:center;padding:20px}canvas{width:250px;height:250px}h3{margin-bottom:10px}@media print{h3{font-size:24px}}</style></head><body><h3>${area}</h3></body></html>`)
  win.document.body.appendChild(canvas.cloneNode(true))
  setTimeout(() => { win.print(); win.close() }, 500)
}

function downloadAll() {
  areas.forEach(area => {
    const canvas = qrRefs[area]
    if (canvas) {
      const link = document.createElement('a')
      link.download = `扫码报工_${area}.png`
      link.href = canvas.toDataURL()
      link.click()
    }
  })
}
</script>

<style scoped>
@media print {
  .qr-card { border:1px solid #ccc !important; page-break-inside:avoid; }
  .el-button { display:none !important; }
}
</style>
