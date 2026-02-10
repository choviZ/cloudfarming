<template>
  <a-card class="spu-form-card" title="商品基础信息" :bordered="false">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 3 }"
      :wrapper-col="{ span: 20 }"
    >
      <!-- 商品名称 -->
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

<script setup>
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

// 定义组件的 props
const props = defineProps({
  categoryId: String
})

// 定义组件的 emits
const emit = defineEmits(['validate'])

// 表单ref
const formRef = ref()

// 表单数据
const formData = reactive({
  title: '',
  image: ''
})

// 上传文件列表
const fileList = ref([])

// 预览状态
const previewVisible = ref(false)
const previewUrl = ref('')

// 表单验证规则
const formRules = {
  spuName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在2到50个字符', trigger: 'blur' }
  ]
}

/**
 * 上传前校验
 */
const handleBeforeUpload = (file) => {
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
const handleCustomRequest = ({ file, onSuccess }) => {
  // 这里模拟上传，实际应该调用上传API
  setTimeout(() => {
    // 模拟返回URL
    const mockUrl = URL.createObjectURL(file)
    onSuccess?.({ url: mockUrl })
  }, 1000)
}

/**
 * 预览图片
 */
const handlePreview = async (file) => {
  previewUrl.value = file.url || (file.originFileObj && URL.createObjectURL(file.originFileObj)) || ''
  previewVisible.value = true
}

/**
 * 预览可见性变化
 */
const handleVisibleChange = (visible) => {
  previewVisible.value = visible
}

/**
 * 上传变化处理
 */
const handleUploadChange = ({ fileList: newFileList }) => {
  fileList.value = newFileList

  // 提取所有已上传成功的图片URL，用逗号连接
  const imageUrls = newFileList
    .filter(file => file.status === 'done' && file.url)
    .map(file => file.url)
    .join(',')

  formData.image = imageUrls
}

/**
 * 字段失焦处理
 */
const handleFieldBlur = async (field) => {
  try {
    await formRef.value?.validateFields([field])
  } catch {
    // 验证失败不做处理
  }
}

/**
 * 验证整个表单
 */
const validateForm = async () => {
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
    spuName: formData.title,
    image: formData.image
  }
}

// 暴露方法供父组件调用
defineExpose({
  validateForm,
  resetForm,
  getFormData
})
</script>

<style scoped>
.spu-form-card {
  margin-bottom: 16px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .spu-form-card :deep(.ant-form-item-label) {
    text-align: left;
  }

  .spu-form-card :deep(.ant-col-3) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .spu-form-card :deep(.ant-col-20) {
    flex: 0 0 100%;
    max-width: 100%;
  }
}
</style>
