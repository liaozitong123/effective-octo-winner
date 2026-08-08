export function normalizeCustomerDeliveryRows(rows) {
  return (Array.isArray(rows) ? rows : []).map(normalizeCustomerDeliveryRow)
}

export function normalizeCustomerDeliveryRow(row) {
  const source = row || {}
  const deliveryQty = firstNumber(source.deliveryQty, source.qty, source.deliveredQty)
  const rawArea = firstNumber(source.area, source.deliveryArea)
  let singleArea = firstNumber(source.singleArea, source.unitArea)
  if (singleArea <= 0 && rawArea > 0 && deliveryQty > 0) {
    singleArea = round4(rawArea / deliveryQty)
  }

  const customerUnitPrice = firstNumber(
    source.customerUnitPrice,
    source.unitPrice,
    source.customerSquarePrice,
    source.squareUnitPrice,
  )
  const boxUnitPrice = firstNumber(
    source.boxUnitPrice,
    source.cartonUnitPrice,
    source.paperBoxUnitPrice,
  )
  const area = rawArea > 0 ? rawArea : round4(singleArea * deliveryQty)
  const rawAmount = firstNumber(source.amount, source.deliveryAmount)
  const amount = rawAmount > 0 ? rawAmount : round2(deliveryQty * boxUnitPrice)

  return {
    ...source,
    deliveryStatus: firstNonBlank(
      source.deliveryStatus,
      source.status,
      source.printed === true ? '已送货' : '未送货',
    ),
    customerUnitPrice,
    customerMaterial: firstNonBlank(
      source.customerMaterial,
      source.material,
      source.customerMaterialName,
    ),
    singleArea,
    deliveryQty,
    qty: deliveryQty,
    area,
    boxUnitPrice,
    amount,
    driver: firstNonBlank(source.driver, source.carrier),
    noteNo: firstNonBlank(source.noteNo, source.deliveryNoteNo, source.deliveryOrderNo),
    deliveryDate: firstNonBlank(source.deliveryDate, source.shipDate),
    issuer: firstNonBlank(source.issuer, source.billingPerson),
    salesperson: firstNonBlank(source.salesperson, source.salesPerson),
  }
}

function firstNonBlank(...values) {
  return values.find(value => value !== null && value !== undefined && String(value).trim() !== '') || ''
}

function firstNumber(...values) {
  let fallback = 0
  for (const value of values) {
    const number = Number(value)
    if (!Number.isFinite(number)) continue
    fallback = number
    if (number > 0) return number
  }
  return fallback
}

function round2(value) {
  return Math.round(value * 100) / 100
}

function round4(value) {
  return Math.round(value * 10000) / 10000
}
