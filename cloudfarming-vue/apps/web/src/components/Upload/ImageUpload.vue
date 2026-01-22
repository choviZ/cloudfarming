<script setup lang="ts">
import { ref, watch } from 'vue';
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { message, Upload } from 'ant-design-vue';
import type { UploadChangeParam, UploadProps } from 'ant-design-vue';
import { upload, type UploadType } from '@cloudfarming/core';

interface Props {
  value?: string;
  bizCode: UploadType;
  maxSize?: number; // MB
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 2,
});

const emit = defineEmits<{
  (e: 'update:value', value: string): void;
  (e: 'change', value: string): void;
}>();

const fileList = ref<UploadProps['fileList']>([]);
const loading = ref<boolean>(false);
const previewVisible = ref<boolean>(false);
const previewImage = ref<string>('');
const previewTitle = ref<string>('');

watch(
  () => props.value,
  (val) => {
    if (val) {
      if (fileList.value && fileList.value.length > 0 && fileList.value[0]?.url === val) {
        return;
      }
      fileList.value = [
        {
          uid: '-1',
          name: 'image.png',
          status: 'done',
          url: val,
        },
      ];
    } else {
      fileList.value = [];
    }
  },
  { immediate: true }
);

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'image/webp';
  if (!isJpgOrPng) {
    message.error('You can only upload JPG/PNG/GIF/WEBP file!');
  }
  const isLt2M = file.size / 1024 / 1024 < props.maxSize;
  if (!isLt2M) {
    message.error(`Image must smaller than ${props.maxSize}MB!`);
  }
  return isJpgOrPng && isLt2M;
};

const customRequest = async (options: any) => {
  const { file, onSuccess, onError } = options;
  loading.value = true;
  try {
    const result = await upload(file, { bizCode: props.bizCode });
    if (result.code === '0' || result.code === '200') {
       onSuccess(result.data, file);
       emit('update:value', result.data);
       emit('change', result.data);
    } else {
       onError(new Error(result.message));
       message.error(result.message || 'Upload failed');
    }
  } catch (err: any) {
    onError(err);
    message.error(err.message || 'Upload failed');
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  previewVisible.value = false;
  previewTitle.value = '';
};

const handlePreview = async (file: NonNullable<UploadProps['fileList']>[number]) => {
  if (!file.url && !file.preview) {
    file.preview = (await getBase64(file.originFileObj as File)) as string;
  }
  previewImage.value = file.url || file.preview || '';
  previewVisible.value = true;
  previewTitle.value = file.name || previewImage.value.substring(previewImage.value.lastIndexOf('/') + 1);
};

const handleChange: UploadProps['onChange'] = (info: UploadChangeParam) => {
  fileList.value = info.fileList;
  
  if (info.file.status === 'removed') {
      emit('update:value', '');
      emit('change', '');
  }
};

function getBase64(file: File) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
}
</script>

<template>
  <div class="clearfix">
    <a-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :custom-request="customRequest"
      :before-upload="beforeUpload"
      @preview="handlePreview"
      @change="handleChange"
      accept="image/*"
    >
      <div v-if="fileList && fileList.length < 1">
        <loading-outlined v-if="loading" />
        <plus-outlined v-else />
        <div style="margin-top: 8px">点击上传图片📷</div>
      </div>
    </a-upload>
    <a-modal :open="previewVisible" :title="previewTitle" :footer="null" @cancel="handleCancel">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<style scoped>
.clearfix :deep(.ant-upload-select-picture-card) {
  overflow: hidden;
}
</style>
