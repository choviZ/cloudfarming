<template>
  <a-card class="category-form-card" title="商品基础信息" :bordered="false">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 3 }"
      :wrapper-col="{ span: 20 }"
    >
      <!-- 商品分类 -->
      <a-form-item label="商品分类" name="categoryId" required>
        <a-tree-select
          v-model:value="formData.categoryId"
          :tree-data="categoryTreeData"
          placeholder="请选择商品分类（仅可选择叶子节点）"
          :loading="loading"
          :field-names="{ label: 'name', value: 'id', children: 'children' }"
          tree-default-expand-all
          show-search
          allow-clear
          @change="handleCategoryChange"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
        />
      </a-form-item>

      <!-- 商品标题 -->
      <a-form-item label="商品标题" name="title">
        <a-input
          v-model:value="formData.title"
          placeholder="请输入商品名称"
          :maxlength="50"
          show-count
          @blur="handleFieldBlur('title')"
        />
      </a-form-item>

      <!-- 商品图片 -->
      <a-form-item label="商品图片" name="image">
        <a-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :max-count="5"
          :before-upload="handleBeforeUpload"
          :custom-request="handleCustomRequest"
          @preview="handlePreview"
          @change="handleUploadChange"
        >
          <div v-if="fileList.length < 5">
            <plus-outlined />
            <div style="margin-top: 8px">上传图片</div>
          </div>
        </a-upload>
        <div class="upload-tip">支持jpg/png格式，文件大小不超过2MB，最多上传5张</div>
      </a-form-item>
    </a-form>

    <!-- 图片预览 -->
    <a-image-preview-group :preview="{ visible: previewVisible, onVisibleChange: handleVisibleChange }">
      <a-image v-if="previewUrl" :src="previewUrl" style="display: none" />
    </a-image-preview-group>
  </a-card>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { UploadFile, UploadProps } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { getCategoryTree } from '@cloudfarming/core'
import type { CategoryRespDTO } from '@cloudfarming/core'

// 定义组件的 emits
const emit = defineEmits<{
  categoryChange: [categoryId: string]
}>()

// 表单ref
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表单数据
const formData = reactive({
  categoryId: '',
  title: '',
  image: ''
})

// 分类树数据
const categoryTreeData = ref<Array<CategoryRespDTO & { disabled?: boolean }>>([])

// 上传文件列表
const fileList = ref<UploadFile[]>([])

// 预览状态
const previewVisible = ref(false)
const previewUrl = ref('')

// 表单验证规则
const formRules = {
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在2到50个字符', trigger: 'blur' }
  ]
}

/**
 * 处理分类树数据，标记非叶子节点为禁用状态
 */
const processCategoryTree = (categories: CategoryRespDTO[]): Array<CategoryRespDTO & { disabled?: boolean }> => {
  return categories.map(category => ({
    ...category,
    disabled: category.children && category.children.length > 0,
    children: category.children ? processCategoryTree(category.children) : undefined
  }))
}

/**
 * 加载分类树数据
 */
const loadCategoryTree = async () => {
  loading.value = true
  try {
    const { data } = await getCategoryTree()
    categoryTreeData.value = processCategoryTree(data || [])
  } catch (error) {
    message.error('加载分类数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 分类变化处理
 */
const handleCategoryChange = (categoryId: string) => {
  emit('categoryChange', categoryId)
}

/**
 * 上传前校验
 */
const handleBeforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只能上传JPG/PNG格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过2MB')
    return false
  }
  return true
}

/**
 * 自定义上传请求（模拟上传）
 */
const handleCustomRequest: UploadProps['customRequest'] = ({ file, onSuccess }) => {
  // 这里模拟上传，实际应该调用上传API
  setTimeout(() => {
    // 模拟返回URL
    const mockUrl = URL.createObjectURL(file as File)
    onSuccess?.({ url: mockUrl })
  }, 1000)
}

/**
 * 预览图片
 */
const handlePreview: UploadProps['onPreview'] = async (file) => {
  previewUrl.value = file.url || (file.originFileObj && URL.createObjectURL(file.originFileObj)) || ''
  previewVisible.value = true
}

/**
 * 预览可见性变化
 */
const handleVisibleChange = (visible: boolean) => {
  previewVisible.value = visible
}

/**
 * 上传变化处理
 */
const handleUploadChange: UploadProps['onChange'] = ({ fileList: newFileList }) => {
  fileList.value = newFileList

  // 提取所有已上传成功的图片URL，用逗号连接
  const imageUrls = newFileList
    .filter(file => file.status === 'done' && file.url)
    .map(file => file.url as string)
    .join(',')

  formData.image = imageUrls
}

/**
 * 字段失焦处理
 */
const handleFieldBlur = async (field: keyof typeof formData) => {
  try {
    await formRef.value?.validateFields([field])
  } catch {
    // 验证失败不做处理
  }
}

/**
 * 验证整个表单
 */
const validateForm = async (): Promise<boolean> => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.categoryId = ''
  formData.title = ''
  formData.image = ''
  fileList.value = []
  formRef.value?.resetFields()
}

/**
 * 获取表单数据
 */
const getFormData = () => {
  return {
    categoryId: formData.categoryId,
    spuName: formData.title,
    image: formData.image
  }
}

// 组件挂载时加载分类树
onMounted(() => {
  loadCategoryTree()
})

// 暴露方法供父组件调用
defineExpose({
  validateForm,
  resetForm,
  getFormData
})
</script>

<style scoped>
.category-form-card {
  margin-bottom: 16px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-form-card :deep(.ant-form-item-label) {
    text-align: left;
  }

  .category-form-card :deep(.ant-col-3) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .category-form-card :deep(.ant-col-20) {
    flex: 0 0 100%;
    max-width: 100%;
  }
}
</style>
