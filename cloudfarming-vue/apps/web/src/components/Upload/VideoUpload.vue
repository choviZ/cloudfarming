<script setup>
import { ref, watch } from 'vue'
import { DeleteOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { uploadMedia } from '@/api/common'

const ACCEPT_TYPES = ['video/mp4', 'video/webm', 'video/quicktime']

const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  bizCode: {
    type: String,
    required: true
  },
  maxSize: {
    type: Number,
    default: 50
  }
})

const emit = defineEmits(['update:value', 'change'])

const loading = ref(false)
const videoUrl = ref('')

watch(
  () => props.value,
  (value) => {
    videoUrl.value = value || ''
  },
  { immediate: true }
)

const beforeUpload = (file) => {
  if (!ACCEPT_TYPES.includes(file.type)) {
    message.error('仅支持上传 MP4、WEBM、MOV 视频')
    return false
  }
  if (file.size / 1024 / 1024 > props.maxSize) {
    message.error(`视频大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

const customRequest = async (options) => {
  const { file, onSuccess, onError } = options
  loading.value = true
  try {
    const result = await uploadMedia(file, { bizCode: props.bizCode })
    if (String(result.code) === '0' || String(result.code) === '200') {
      videoUrl.value = result.data || ''
      emit('update:value', videoUrl.value)
      emit('change', videoUrl.value)
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

const clearVideo = () => {
  videoUrl.value = ''
  emit('update:value', '')
  emit('change', '')
}
</script>

<template>
  <div class="video-upload">
    <div v-if="videoUrl" class="video-preview">
      <video :src="videoUrl" class="video-player" controls preload="metadata" />
      <div class="video-actions">
        <a-button :loading="loading" @click="clearVideo">
          <template #icon>
            <delete-outlined />
          </template>
          删除视频
        </a-button>
      </div>
    </div>

    <a-upload
      v-else
      :show-upload-list="false"
      :before-upload="beforeUpload"
      :custom-request="customRequest"
      accept="video/mp4,video/webm,video/quicktime"
    >
      <a-button :loading="loading">
        <template #icon>
          <upload-outlined />
        </template>
        上传视频
      </a-button>
    </a-upload>

    <p class="video-hint">支持 MP4、WEBM、MOV，最多上传 1 个视频。</p>
  </div>
</template>

<style scoped>
.video-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.video-player {
  width: 100%;
  max-height: 320px;
  border-radius: 16px;
  background: #000;
}

.video-actions {
  display: flex;
  justify-content: flex-end;
}

.video-hint {
  margin: 0;
  color: #7b8b7f;
  font-size: 12px;
}
</style>
