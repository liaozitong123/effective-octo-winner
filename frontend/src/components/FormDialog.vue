<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑' : '新增'" width="600px" :close-on-click-modal="false" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item v-for="field in fields" :key="field.key" :label="field.label" :prop="field.key">
<span v-if="field.type === 'display'" class="display-value">{{ form[field.key] || '-' }}</span>
        <el-input v-else-if="!field.type || field.type === 'text' || field.type === 'number'" v-model="form[field.key]" :type="field.type || 'text'" :readonly="field.readonly" :disabled="field.disabled" />
        <el-select v-else-if="field.type === 'select'" v-model="form[field.key]" placeholder="请选择" filterable clearable>
          <el-option v-for="opt in getFieldOptions(field)" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-date-picker v-else-if="field.type === 'date'" v-model="form[field.key]" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        <el-input v-else-if="field.type === 'textarea'" v-model="form[field.key]" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

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

watch(() => props.modelValue, (v) => {
  visible.value = v
  if (v) {
    Object.keys(form).forEach(k => delete form[k])
    props.fields.forEach(f => {
      form[f.key] = props.initialData?.[f.key] ?? ''
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
    const updated = props.onChange({ ...newForm })
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
  } catch(e) {}
  submitting.value = false
}

function handleClose() { formRef.value?.resetFields() }
</script>

<style scoped>
.display-value { font-size:1rem; font-weight:700; color:#2563eb; line-height:40px; }
</style>
