<template>
  <a-modal
    :open="props.open"
    :title="modalTitle"
    :confirm-loading="modalLoading"
    @ok="handleModalOk"
    @update:open="handleUpdateOpen"
    width="600px"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
      style="margin-top: 24px"
    >
      <a-form-item label="分类名称" name="name">
        <a-input
          v-model:value="formData.name"
          placeholder="请输入分类名称"
        />
      </a-form-item>
      <a-form-item v-if="props.mode !== 'create'" label="父级分类" name="parentId">
        <a-tree-select
          v-model:value="formData.parentId"
          :tree-data="categoryTreeSelectData"
          placeholder="请选择父级分类"
          allow-clear
          tree-default-expand-all
          :field-names="{ label: 'name', value: 'id', children: 'children' }"
          :disabled="props.mode === 'create-child'"
        />
      </a-form-item>
      <a-form-item label="排序权重" name="sortOrder">
        <a-input-number
          v-model:value="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="请输入排序权重"
          style="width: 100%"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  createCategory,
  updateCategory
} from '@/api/category'

// 定义组件的 props
const props = defineProps({
  open: { type: Boolean, required: true },
  mode: { type: String, required: true },
  initialData: { type: Object, default: null },
  categoryTree: { type: Array, required: true }
})

// 定义组件的 emits
const emit = defineEmits(['update:open', 'success'])

const modalLoading = ref(false)
const formRef = ref()

const formData = reactive({
  id: undefined,
  name: '',
  parentId: null,
  sortOrder: 0
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序权重', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '排序权重范围 0-9999', trigger: 'blur' }
  ]
}

const modalTitle = computed(() => {
  if (props.mode === 'create-child') return '新增子分类'
  if (props.mode === 'edit') return '编辑分类'
  return '新增顶级分类'
})

const categoryTreeSelectData = computed(() => {
  return props.categoryTree.map(item => ({
    ...item,
    disabled: props.mode === 'edit' && item.id === formData.id
  }))
})

// 监听 props.open 的变化，当弹窗打开时，根据模式初始化表单数据
watch(() => props.open, (newVal) => {
  if (newVal) {
    resetForm()
    if (props.mode === 'edit' && props.initialData) {
      Object.assign(formData, {
        id: props.initialData.id,
        name: props.initialData.name,
        parentId: props.initialData.parentId,
        sortOrder: props.initialData.sortOrder
      })
    } else if (props.mode === 'create-child' && props.initialData) {
      formData.parentId = props.initialData.id
    }
  }
})

const handleModalOk = async () => {
  try {
    await formRef.value?.validate()
    modalLoading.value = true

    if (props.mode === 'edit') {
      await updateCategory(formData)
      message.success('更新成功')
    } else {
      await createCategory(formData)
      message.success('创建成功')
    }

    emit('success')
    emit('update:open', false)
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单填写是否正确')
    } else {
      message.error(props.mode === 'edit' ? '更新失败' : '创建失败')
    }
  } finally {
    modalLoading.value = false
  }
}

const handleUpdateOpen = (value) => {
  emit('update:open', value)
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    name: '',
    parentId: null,
    sortOrder: 0
  })
  formRef.value?.resetFields()
}
</script>
