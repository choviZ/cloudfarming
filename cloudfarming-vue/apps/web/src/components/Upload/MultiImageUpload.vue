<script setup>
import { ref, watch } from 'vue'
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { uploadMedia } from '@/api/common'

const props = defineProps({
  value: {
    type: Array,
    default: () => []
  },
  bizCode: {
    type: String,
    required: true
  },
  maxSize: {
    type: Number,
    default: 2
  },
  maxCount: {
    type: Number,
    default: 6
  }
})

const emit = defineEmits(['update:value', 'change'])

const fileList = ref([])
const loading = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')
const previewTitle = ref('')

const normalizeValues = (values) => {
  return [...new Set((values || []).filter(Boolean).map((item) => item.trim()))]
}

watch(
  () => props.value,
  (values) => {
    const normalized = normalizeValues(values)
    fileList.value = normalized.map((url, index) => ({
      uid: `${index}-${url}`,
      name: `image-${index + 1}.png`,
      status: 'done',
      url
    }))
  },
  { immediate: true }
)

const emitValue = (values) => {
  const normalized = normalizeValues(values).slice(0, props.maxCount)
  emit('update:value', normalized)
  emit('change', normalized)
}

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    message.error('仅支持上传 JPG、PNG、GIF、WEBP 图片')
    return false
  }

  const isLtSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtSize) {
    message.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

const customRequest = async (options) => {
  const { file, onSuccess, onError } = options
  loading.value = true
  try {
    const result = await uploadMedia(file, { bizCode: props.bizCode })
    if (result.code === '0' || result.code === '200') {
      emitValue([...props.value, result.data])
      onSuccess(result.data, file)
      return
    }
    const error = new Error(result.message || '上传失败')
    onError(error)
    message.error(result.message || '上传失败')
  } catch (error) {
    onError(error)
    message.error(error.message || '上传失败')
  } finally {
    loading.value = false
  }
}

const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj)
  }
  previewImage.value = file.url || file.preview || ''
  previewTitle.value = file.name || previewImage.value.substring(previewImage.value.lastIndexOf('/') + 1)
  previewVisible.value = true
}

const handleCancel = () => {
  previewVisible.value = false
  previewTitle.value = ''
}

const handleChange = (info) => {
  fileList.value = info.fileList
  if (info.file.status === 'removed') {
    emitValue(
      info.fileList
        .map((item) => item.url || item.response)
        .filter(Boolean)
    )
  }
}

function getBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}
</script>

<template>
  <div class="multi-image-upload">
    <a-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :multiple="true"
      :custom-request="customRequest"
      :before-upload="beforeUpload"
      @preview="handlePreview"
      @change="handleChange"
      accept="image/*"
    >
      <div v-if="fileList.length < maxCount">
        <loading-outlined v-if="loading" />
        <plus-outlined v-else />
        <div class="upload-text">上传图片</div>
      </div>
    </a-upload>
    <a-modal :open="previewVisible" :title="previewTitle" :footer="null" @cancel="handleCancel">
      <img alt="preview" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<style scoped>
.upload-text {
  margin-top: 8px;
}

.multi-image-upload :deep(.ant-upload-select-picture-card) {
  overflow: hidden;
}
</style>
