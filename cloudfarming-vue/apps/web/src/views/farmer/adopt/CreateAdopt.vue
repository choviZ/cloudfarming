<template>
  <div class="create-adopt-container">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '编辑认养项目' : '发布认养项目' }}</h2>
      <span class="page-desc">{{ isEdit ? '修改认养项目信息，修改后需要重新审核' : '填写认养项目信息，提交后将进入审核流程' }}</span>
    </div>

    <a-spin :spinning="loadingDetail">
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="onSubmit"
        class="adopt-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <a-row :gutter="24">
            <a-col :span="16">
              <a-form-item label="认养标题" name="title">
                <a-input 
                  v-model:value="formState.title" 
                  placeholder="请输入认养标题（如：生态黑猪认养）" 
                  :maxlength="100" 
                  show-count 
                  size="large"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="动物分类" name="animalCategory">
                <a-select 
                  v-model:value="formState.animalCategory" 
                  placeholder="请选择动物分类"
                  :loading="loadingCategories"
                  size="large"
                >
                  <a-select-option 
                    v-for="cat in categories" 
                    :key="cat.code" 
                    :value="cat.code"
                  >
                    {{ cat.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="封面图片" name="coverImage">
            <div class="upload-placeholder">
              <div class="upload-icon">📷</div>
              <div class="upload-text">点击上传封面图片</div>
              <div class="upload-hint">支持 JPG, PNG 格式，大小不超过 5MB (TODO)</div>
              <a-input v-model:value="formState.coverImage" style="margin-top: 10px; display: none;" />
            </div>
          </a-form-item>
        </div>

        <!-- 认养规则 -->
        <div class="form-section">
          <h3 class="section-title">认养规则与收益</h3>
          <a-row :gutter="24">
            <a-col :span="8">
              <a-form-item label="认养周期 (天)" name="adoptDays">
                <a-input-number 
                  v-model:value="formState.adoptDays" 
                  :min="1" 
                  :precision="0" 
                  style="width: 100%" 
                  placeholder="请输入天数" 
                  size="large"
                  addon-after="天"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="认养价格 (元)" name="price">
                <a-input-number 
                  v-model:value="formState.price" 
                  :min="0.01" 
                  :precision="2" 
                  style="width: 100%" 
                  placeholder="请输入价格" 
                  size="large"
                  prefix="¥"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="预计收益" name="expectedYield">
                <a-input 
                  v-model:value="formState.expectedYield" 
                  placeholder="例如：10kg肉" 
                  size="large"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="认养说明" name="description">
            <a-textarea 
              v-model:value="formState.description" 
              placeholder="请详细描述认养项目的环境、喂养方式、成长过程等信息..." 
              :rows="8" 
              show-count 
              :minlength="20" 
            />
          </a-form-item>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <a-button type="primary" html-type="submit" :loading="loading" size="large" class="submit-btn">
            {{ isEdit ? '保存修改' : '立即发布' }}
          </a-button>
          <a-button size="large" style="margin-left: 12px" @click="router.back()">取消</a-button>
        </div>
      </a-form>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { createAdoptItem, listAnimalCategories, getAdoptItemDetail, updateAdoptItem } from '@cloudfarming/core'
import type { AdoptItemCreateReq, AnimalCategoryResp } from '@cloudfarming/core'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.query.id)
const adoptId = computed(() => route.query.id as string)

// 状态管理
const loading = ref(false)
const loadingCategories = ref(false)
const loadingDetail = ref(false)
const categories = ref<AnimalCategoryResp[]>([])
const formRef = ref<FormInstance>()

// 表单数据
const formState = reactive<AdoptItemCreateReq>({
  title: '',
  animalCategory: '',
  coverImage: 'https://placeholder.com/default-cover.png', // 默认占位图，因接口必填
  adoptDays: 30,
  price: 100,
  expectedYield: '',
  description: '',
})

// 校验规则
const rules: Record<string, Rule[]> = {
  title: [
    { required: true, message: '请输入认养标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过100字', trigger: 'blur' }
  ],
  animalCategory: [
    { required: true, message: '请选择动物分类', trigger: 'change' }
  ],
  adoptDays: [
    { required: true, message: '请输入认养周期', trigger: 'blur' },
    { type: 'integer', min: 1, message: '认养周期必须为正整数', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入认养价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入认养说明', trigger: 'blur' },
    { min: 20, message: '认养说明不少于20字', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  loadingCategories.value = true
  try {
    const res = await listAnimalCategories()
    if (res.code === '0' && res.data) {
      categories.value = res.data
    } else {
      message.error(res.message || '获取动物分类失败')
    }
  } catch (error) {
    console.error('获取分类异常:', error)
    message.error('获取分类异常')
  } finally {
    loadingCategories.value = false
  }
}

// 获取详情
const fetchDetail = async () => {
  if (!adoptId.value) return
  
  loadingDetail.value = true
  try {
    const res = await getAdoptItemDetail(adoptId.value)
    if (res.code === '0' && res.data) {
      // 回显数据
      Object.assign(formState, {
        title: res.data.title,
        animalCategory: res.data.animalCategory,
        coverImage: res.data.coverImage,
        adoptDays: res.data.adoptDays,
        price: res.data.price,
        expectedYield: res.data.expectedYield,
        description: res.data.description,
      })
    } else {
      message.error(res.message || '获取项目详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取详情异常:', error)
    message.error('获取详情异常')
    router.back()
  } finally {
    loadingDetail.value = false
  }
}

// 提交表单
const onSubmit = async (values: AdoptItemCreateReq) => {
  loading.value = true
  try {
    let res
    if (isEdit.value) {
      // 编辑模式
      res = await updateAdoptItem({
        id: adoptId.value,
        ...values
      })
    } else {
      // 创建模式
      res = await createAdoptItem(values)
    }
    
    if (res.code === '0') {
      message.success(isEdit.value ? '修改成功，已重新提交审核' : '提交成功，等待审核')
      // 成功后返回列表页
      if (isEdit.value) {
        router.push('/farmer/adopt/my')
      } else {
        // 创建成功后重置表单
        formRef.value?.resetFields()
        // 或者也可以跳转到列表页
        router.push('/farmer/adopt/my')
      }
    } else {
      message.error(res.message || (isEdit.value ? '修改失败' : '提交失败'))
    }
  } catch (error) {
    console.error('提交异常:', error)
    message.error('系统异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(async () => {
  await fetchCategories()
  if (isEdit.value) {
    fetchDetail()
  }
})
</script>

<style scoped>
.create-adopt-container {
  max-width: 1000px;
  margin: 0 auto;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.page-header {
  margin-bottom: 32px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  color: #1f1f1f;
  margin-bottom: 8px;
}

.page-desc {
  color: #8c8c8c;
  font-size: 14px;
}

.form-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f1f1f;
  margin-bottom: 24px;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background: #1890ff;
  border-radius: 2px;
}

.upload-placeholder {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  background-color: #fafafa;
  height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #1890ff;
  background-color: #f0f7ff;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 16px;
  color: #595959;
  margin-bottom: 4px;
}

.upload-hint {
  font-size: 12px;
  color: #8c8c8c;
}

.form-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
  margin-top: 40px;
}

.submit-btn {
  min-width: 120px;
}
</style>
