const STITCH_OPTIONS = ['一片成', '两片成', '四片成']

function toNumber(value, fallback = 0) {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

function round(value, digits) {
  const base = 10 ** digits
  return Math.round((value + Number.EPSILON) * base) / base
}

function parseSpec(spec) {
  if (!spec) return []
  const parts = String(spec).trim().split(/[*xX×]/).map(v => Number(v.trim()))
  return parts.every(v => Number.isFinite(v) && v > 0) ? parts : []
}

function calcBoardLength(length, width, boxType, stitchType) {
  if (boxType === '纸板') return length
  if (stitchType === '一片成') return (length + width) * 2 + 3
  if (stitchType === '两片成') return length + width + 3
  if (stitchType === '四片成') return (length + width + 7) / 2
  return null
}

function calcBoardWidth(width, height, boxType, cutCount) {
  const cuts = toNumber(cutCount)
  if (boxType === '纸板') return width
  if (boxType === '中封箱') return 2 + height + 2 * width + 1.8
  if (boxType === '全盖箱' && cuts > 0) return (2 * width + height) * cuts - 1
  if (boxType === '平口箱' && cuts > 0) return (width + height) * cuts + 1.8
  return null
}

function formatSegment(value) {
  return String(round(value, 4))
}

function calcReferenceCrease(width, height, boxType) {
  if (boxType === '中封箱') {
    return [width / 2, height, width, height, width / 2].map(formatSegment).join('+')
  }
  if (boxType === '平口箱') {
    return [width / 2 + 0.3, height, width / 2 + 0.3].map(formatSegment).join('+')
  }
  if (boxType === '全盖箱') {
    return [width, height, width].map(formatSegment).join('+')
  }
  return ''
}

function calcReferenceBoardQty(qty, cutCount, stitchType) {
  const orderQty = toNumber(qty)
  const cuts = toNumber(cutCount)
  if (orderQty <= 0 || cuts <= 0) return ''
  if (stitchType === '一片成') return round(orderQty / cuts, 4)
  if (stitchType === '两片成') return round(orderQty * 2 / cuts, 4)
  if (stitchType === '四片成') return round(orderQty * 4 / cuts, 4)
  return ''
}

export function applyBoardCalculation(data, options = {}) {
  const next = { ...data }
  const specParts = parseSpec(next.spec)
  next.realBoardLength = ''
  next.realBoardWidth = ''
  next.referenceCrease = ''
  next.referenceBoardQty = calcReferenceBoardQty(next.qty, next.cutCount, next.stitchType)

  if (next.boxType && next.stitchType && specParts.length >= 2) {
    const [length, width, height = 0] = specParts
    const realBoardLength = calcBoardLength(length, width, next.boxType, next.stitchType)
    const realBoardWidth = calcBoardWidth(width, height, next.boxType, next.cutCount)
    next.realBoardLength = realBoardLength === null ? '' : round(realBoardLength, 4)
    next.realBoardWidth = realBoardWidth === null ? '' : round(realBoardWidth, 4)
    if (specParts.length >= 3) {
      next.referenceCrease = calcReferenceCrease(width, height, next.boxType)
    }
  }

  const manualBoardLength = toNumber(next.boardLength)
  const manualBoardWidth = toNumber(next.boardWidth)
  const boardArea = manualBoardLength > 0 && manualBoardWidth > 0
    ? round(manualBoardLength * manualBoardWidth / 10000, 6)
    : 0
  const totalArea = round(boardArea * toNumber(next.boardQty), 6)
  const boardUnitPrice = round(toNumber(next.materialBasePrice) * toNumber(next.discountRate, 1), 4)
  const boardAmount = round(totalArea * boardUnitPrice, 2)

  next.boardArea = boardArea
  next.totalArea = totalArea
  next.boardUnitPrice = boardUnitPrice
  next.boardAmount = boardAmount
  if (options.syncOrderArea) next.orderArea = totalArea

  return next
}

export { STITCH_OPTIONS }
