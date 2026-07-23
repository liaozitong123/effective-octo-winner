<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑' : '新增'"
    width="min(880px, calc(100vw - 28px))"
    top="5vh"
    class="erp-form-dialog"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="erp-form-grid">
      <el-form-item
        v-for="field in fields"
        :key="field.key"
        :label="field.label"
        :prop="field.key"
        :class="fieldClass(field)"
      >
        <span v-if="field.type === 'display'" class="display-value">{{ displayText(form[field.key], field) }}</span>
        <div v-else-if="!field.type || field.type === 'text' || field.type === 'number'" :class="['input-line', { 'has-hint': field.hintKey }]">
          <el-input
            v-model="form[field.key]"
            :type="field.type || 'text'"
            :readonly="field.readonly"
            :disabled="field.disabled"
            @input="markChanged(field.key)"
          />
          <span v-if="field.hintKey" class="field-hint">{{ fieldHintText(field) }}</span>
        </div>
        <el-select v-else-if="field.type === 'select'" v-model="form[field.key]" placeholder="请选择" filterable clearable @change="markChanged(field.key)">
          <el-option v-for="opt in getFieldOptions(field)" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-date-picker v-else-if="field.type === 'date'" v-model="form[field.key]" type="date" value-format="YYYY-MM-DD" style="width:100%" @change="markChanged(field.key)" />
        <el-input v-else-if="field.type === 'textarea'" v-model="form[field.key]" type="textarea" :rows="3" @input="markChanged(field.key)" />
        <div v-else-if="field.type === 'image-note'" class="image-note-editor">
          <div v-if="isImageValue(form[field.key])" class="image-note-preview">
            <img :src="form[field.key]" alt="备注图片" />
          </div>
          <el-input
            v-else
            v-model="form[field.key]"
            type="textarea"
            :rows="4"
            placeholder="输入文字备注，或选择图片"
            @input="markChanged(field.key)"
          />
          <div class="image-note-actions">
            <el-button size="small" @click="triggerImageInput(field.key)">选择图片</el-button>
            <el-button v-if="form[field.key]" size="small" type="danger" plain @click="clearImageNote(field.key)">清空备注</el-button>
          </div>
          <input
            :ref="el => setImageInputRef(field.key, el)"
            class="hidden-file-input"
            type="file"
            accept="image/*"
            @change="handleImageSelect(field.key, $event)"
          />
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-actions">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  fields: Array,
  isEdit: Boolean,
  initialData: Object,
  onSubmit: Function,
  onChange: Function,
})

const emit = defineEmits(['update:modelValue'])
const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({})
const rules = reactive({})
const asyncOptions = reactive({})  // key → [{value, label}]
const lastChangedKey = ref('')
const imageInputRefs = {}
const displayKeys = ['singleArea', 'boxUnitPrice', 'totalAmount', 'boardArea', 'totalArea', 'boardAmount', 'actualAmount', 'orderArea', 'realBoardLength', 'realBoardWidth']

function displayText(value, field = {}) {
  if (value === '' || value === null || value === undefined) return '-'
  return field.suffix ? `${value}${field.suffix}` : value
}

function markChanged(key) {
  lastChangedKey.value = key
}

function fieldHintText(field) {
  const value = form[field.hintKey]
  const text = value !== '' && value !== null && value !== undefined ? value : '-'
  return `${field.hintLabel || '参考'}：${text}`
}

function fieldClass(field) {
  return {
    'wide-field': field.type === 'textarea' || field.type === 'image-note' || field.full,
    'display-field': field.type === 'display',
    'calculated-field': field.type === 'display' && displayKeys.includes(field.key),
  }
}

function isImageValue(value) {
  return typeof value === 'string' && value.startsWith('data:image/')
}

function setImageInputRef(key, el) {
  if (el) imageInputRefs[key] = el
}

function triggerImageInput(key) {
  imageInputRefs[key]?.click()
}

function clearImageNote(key) {
  form[key] = ''
  markChanged(key)
}

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = reject
    image.src = src
  })
}

async function compressImage(file) {
  const dataUrl = await readFileAsDataUrl(file)
  const image = await loadImage(dataUrl)
  const maxSide = 900
  const scale = Math.min(1, maxSide / Math.max(image.width, image.height))
  const canvas = document.createElement('canvas')
  canvas.width = Math.max(1, Math.round(image.width * scale))
  canvas.height = Math.max(1, Math.round(image.height * scale))
  const ctx = canvas.getContext('2d')
  ctx.drawImage(image, 0, 0, canvas.width, canvas.height)
  return canvas.toDataURL('image/jpeg', 0.78)
}

async function handleImageSelect(key, event) {
  const input = event.target
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  try {
    form[key] = await compressImage(file)
    markChanged(key)
  } catch {
    ElMessage.error('图片读取失败')
  }
}

watch(() => props.modelValue, (v) => {
  visible.value = v
  if (v) {
    Object.keys(form).forEach(k => delete form[k])
    props.fields.forEach(f => {
      form[f.key] = props.initialData?.[f.key] ?? ''
      if (f.hintKey && !(f.hintKey in form)) form[f.hintKey] = props.initialData?.[f.hintKey] ?? ''
      rules[f.key] = f.required ? [{ required: true, message: `请${f.type === 'select' ? '选择' : '输入'}${f.label}`, trigger: 'blur' }] : []
      // Load async options
      if (f.type === 'select' && f.optionsApi && !asyncOptions[f.key]) {
        loadAsyncOptions(f)
      }
    })
  }
})

async function loadAsyncOptions(field) {
  try {
    const res = await field.optionsApi()
    asyncOptions[field.key] = res.map(item => {
      const labelKey = field.labelKey || 'name'
      return { value: item.id || item, label: item[labelKey] || item }
    })
  } catch(e) {
    asyncOptions[field.key] = []
  }
}

function getFieldOptions(field) {
  if (field.type === 'select') {
    if (asyncOptions[field.key]) return asyncOptions[field.key]
    if (field.options) return field.options.map(o => typeof o === 'string' ? { value: o, label: o } : o)
  }
  return []
}

// Real-time field change callback
watch(form, (newForm) => {
  if (props.onChange) {
    const changedKey = lastChangedKey.value
    lastChangedKey.value = ''
    const updated = props.onChange({ ...newForm }, changedKey)
    if (updated) {
      Object.keys(updated).forEach(k => {
        if (k in form && form[k] !== updated[k]) form[k] = updated[k]
      })
    }
  }
}, { deep: true })

watch(visible, (v) => emit('update:modelValue', v))

async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    await props.onSubmit({ ...form })
    visible.value = false
  } catch(e) {
    ElMessage.error(e?.response?.data?.message || '保存失败，请检查后端服务是否正常')
  }
  submitting.value = false
}

function handleClose() { formRef.value?.resetFields() }
</script>

<style scoped>
:global(.erp-form-dialog) {
  border-radius: var(--erp-radius);
}

:global(.erp-form-dialog .el-dialog__header) {
  padding: 18px 22px 14px;
  border-bottom: 1px solid var(--erp-border);
  margin-right: 0;
}

:global(.erp-form-dialog .el-dialog__title) {
  font-weight: 800;
  color: var(--erp-text);
}

:global(.erp-form-dialog .el-dialog__body) {
  max-height: min(66vh, 620px);
  overflow: auto;
  padding: 16px 22px 8px;
}

:global(.erp-form-dialog .el-dialog__footer) {
  position: sticky;
  bottom: 0;
  padding: 12px 22px 16px;
  border-top: 1px solid var(--erp-border);
  background: linear-gradient(180deg, rgba(255,255,255,.88), #fff);
}

.erp-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.erp-form-grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.erp-form-grid :deep(.el-form-item__label) {
  color: #506079;
  font-weight: 700;
  line-height: 20px;
  margin-bottom: 6px;
}

.erp-form-grid :deep(.el-select),
.erp-form-grid :deep(.el-input),
.erp-form-grid :deep(.el-date-editor) {
  width: 100%;
}

.input-line {
  width: 100%;
}

.input-line.has-hint {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-line.has-hint :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.field-hint {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 0.82rem;
  line-height: 1;
  white-space: nowrap;
}

.wide-field {
  grid-column: 1 / -1;
}

.display-value {
  width: 100%;
  min-height: 36px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  border-radius: 7px;
  background: #f7fbff;
  border: 1px solid var(--erp-border);
  font-size: 1rem;
  font-weight: 800;
  color: var(--erp-primary);
}

.calculated-field .display-value {
  min-height: 48px;
  background: var(--erp-primary-soft);
  border-color: #cfe0ff;
  font-size: 1.12rem;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 720px) {
  :global(.erp-form-dialog .el-dialog__body) {
    max-height: 70vh;
    padding: 14px;
  }

  :global(.erp-form-dialog .el-dialog__footer) {
    padding: 12px 14px;
  }

  .erp-form-grid {
    grid-template-columns: 1fr;
  }

  .dialog-actions :deep(.el-button) {
    flex: 1;
  }

  .input-line.has-hint {
    align-items: flex-start;
    flex-direction: column;
    gap: 5px;
  }
}
</style>
