<template>
  <a-modal
    :open="props.open"
    :title="modalTitle"
    :confirm-loading="submitLoading"
    width="960px"
    @ok="handleSubmit"
    @update:open="handleUpdateOpen"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 19 }"
      class="article-form"
    >
      <a-form-item label="文章标题" name="title">
        <a-input
          v-model:value="formState.title"
          :maxlength="100"
          show-count
          placeholder="请输入文章标题"
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="文章类型" name="articleType">
            <a-select
              v-model:value="formState.articleType"
              placeholder="请选择文章类型"
              :options="articleTypeOptions"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="置顶设置" name="isTop">
            <a-switch
              v-model:checked="formState.isTop"
              checked-children="置顶"
              un-checked-children="普通"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item v-if="isEditMode" label="发布状态" name="status">
        <a-radio-group v-model:value="formState.status">
          <a-radio :value="1">已发布</a-radio>
          <a-radio :value="0">已下线</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="封面图片" name="coverImage">
        <ImageUpload v-model:value="formState.coverImage" biz-code="article-cover" />
        <div v-if="formState.coverImage" class="cover-preview">
          <img :src="formState.coverImage" alt="cover" class="cover-preview__image" />
        </div>
      </a-form-item>

      <a-form-item label="文章摘要" name="summary">
        <a-textarea
          v-model:value="formState.summary"
          :maxlength="300"
          show-count
          :auto-size="{ minRows: 3, maxRows: 5 }"
          placeholder="请输入文章摘要，不填则根据正文自动生成"
        />
      </a-form-item>

      <a-form-item label="正文内容" name="content">
        <a-textarea
          v-model:value="formState.content"
          :auto-size="{ minRows: 10, maxRows: 18 }"
          placeholder="请输入文章正文，支持 HTML 内容"
        />
      </a-form-item>

      <a-form-item label="内容预览">
        <div class="article-preview" v-html="previewContent"></div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import ImageUpload from '@/components/Upload/ImageUpload.vue'
import { publishArticle, updateArticle } from '@/api/article'

const defaultFormState = () => ({
  id: undefined,
  title: '',
  summary: '',
  coverImage: '',
  content: '',
  articleType: undefined,
  isTop: false,
  status: 1
})

const props = defineProps({
  open: { type: Boolean, required: true },
  mode: { type: String, default: 'create' },
  initialData: { type: Object, default: null }
})

const emit = defineEmits(['update:open', 'success'])

const formRef = ref()
const submitLoading = ref(false)
const formState = reactive(defaultFormState())

const articleTypeOptions = [
  { label: '平台公告', value: 1 },
  { label: '农业政策', value: 2 },
  { label: '养殖知识', value: 3 }
]

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 100, message: '文章标题长度为 2-100 个字符', trigger: 'blur' }
  ],
  articleType: [
    { required: true, message: '请选择文章类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入正文内容', trigger: 'blur' }
  ],
  summary: [
    { max: 300, message: '文章摘要最多 300 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择发布状态', trigger: 'change' }
  ]
}

const isEditMode = computed(() => props.mode === 'edit')

const modalTitle = computed(() => (isEditMode.value ? '编辑文章资讯' : '发布文章资讯'))

const previewContent = computed(() => {
  if (formState.content && formState.content.trim()) {
    return formState.content
  }
  return '<div class="article-preview__empty">请输入正文内容后可在这里预览</div>'
})

const resetForm = () => {
  Object.assign(formState, defaultFormState())
  formRef.value?.resetFields()
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    resetForm()
    if (props.initialData) {
      Object.assign(formState, {
        id: props.initialData.id,
        title: props.initialData.title ?? '',
        summary: props.initialData.summary ?? '',
        coverImage: props.initialData.coverImage ?? '',
        content: props.initialData.content ?? '',
        articleType: props.initialData.articleType,
        isTop: Boolean(props.initialData.isTop),
        status: props.initialData.status ?? 1
      })
    }
  }
)

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const payload = {
      title: formState.title?.trim(),
      summary: formState.summary?.trim(),
      coverImage: formState.coverImage?.trim(),
      content: formState.content,
      articleType: formState.articleType,
      isTop: formState.isTop
    }

    let res
    if (isEditMode.value) {
      res = await updateArticle({
        ...payload,
        id: formState.id,
        status: formState.status
      })
    } else {
      res = await publishArticle(payload)
    }

    if (res.code === '0') {
      message.success(isEditMode.value ? '文章更新成功' : '文章发布成功')
      emit('success')
      emit('update:open', false)
      return
    }
    message.error(res.message || (isEditMode.value ? '文章更新失败' : '文章发布失败'))
  } catch (error) {
    if (!error?.errorFields) {
      message.error(isEditMode.value ? '文章更新失败' : '文章发布失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleUpdateOpen = (value) => {
  emit('update:open', value)
}
</script>

<style scoped>
.article-form {
  margin-top: 20px;
}

.cover-preview {
  margin-top: 12px;
}

.cover-preview__image {
  width: 220px;
  max-width: 100%;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.article-preview {
  min-height: 220px;
  max-height: 420px;
  overflow: auto;
  width: 100%;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.article-preview :deep(img) {
  max-width: 100%;
  height: auto;
}

.article-preview :deep(.article-preview__empty) {
  color: #999;
}
</style>
