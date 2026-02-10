<template>
  <div id="apply-farmer">
    <div v-if="!reviewStatus">
      <a-form
        :model="applyFarmerReq"
        :rules="rules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
        labelAlign="left"
        :hide-required-mark="true"
      >
        <a-card v-if="current == 0" class="card">
          <h2 style="text-align: center">主体信息</h2>
          <a-form-item label="姓名" name="realName">
            <a-input placeholder="请输入姓名" v-model:value="applyFarmerReq.realName" />
          </a-form-item>
          <a-form-item label="身份证号" name="idCard">
            <a-input placeholder="请输入身份证号" v-model:value="applyFarmerReq.idCard" />
          </a-form-item>
          <a-form-item label="户籍信息" name="houseAddress">
            <a-input placeholder="请输入户籍信息" v-model:value="applyFarmerReq.houseAddress" />
          </a-form-item>
        </a-card>
        <a-card v-if="current == 1" class="card">
          <h2 style="text-align: center">养殖场信息</h2>
          <a-form-item label="养殖场名称" name="farmName">
            <a-input placeholder="请输入养殖场名称" v-model:value="applyFarmerReq.farmName" />
          </a-form-item>
          <a-form-item label="养殖场地址" name="farmAddress">
            <a-input placeholder="请输入养殖场地址" v-model:value="applyFarmerReq.farmAddress" />
          </a-form-item>
          <a-form-item label="养殖场面积" name="farmArea">
            <a-input-number placeholder="请输入养殖场面积" v-model:value="applyFarmerReq.farmArea" />
          </a-form-item>
          <a-form-item label="养殖品类" name="breedingType">
            <a-input placeholder="请输入养殖品类" v-model:value="applyFarmerReq.breedingType" />
          </a-form-item>
          <a-form-item label="营业执照编号" name="businessLicenseNo">
            <a-input placeholder="请输入营业执照编号" v-model:value="applyFarmerReq.businessLicenseNo" />
          </a-form-item>
          <a-form-item label="养殖场图片">
            <a-upload
              name="avatar"
              list-type="picture-card"
              class="avatar-uploader"
              :show-upload-list="false"
            >
              <img v-if="imageUrl" :src="imageUrl" alt="avatar" />
              <div v-else>
                <loading-outlined v-if="loading"></loading-outlined>
                <plus-outlined v-else></plus-outlined>
                <div class="ant-upload-text">点击或拖拽</div>
              </div>
            </a-upload>
          </a-form-item>
          <a-form-item label="备注" name="remark">
            <a-textarea - placeholder="请输入备注" v-model:value="applyFarmerReq.remark" />
          </a-form-item>
        </a-card>
      </a-form>
      <!-- 步骤条 -->
      <div class="step">
        <a-steps :current="current" :items="items"></a-steps>
        <div class="steps-action">
          <a-space>
            <a-button v-if="current < steps.length - 1" type="primary" @click="next">下一步</a-button>
            <a-button v-if="current > 0" style="margin-left: 8px" @click="prev">上一步</a-button>
            <a-button
              v-if="current == steps.length - 1"
              type="primary"
              @click="submitApply"
            >
              提交审核
            </a-button>
          </a-space>
        </div>
      </div>

    </div>
    <div v-if="reviewStatus">
      <ReviewState :status-data="reviewStatus" :on-re-apply="reApply"/>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { getFarmerReviewStatus, submitFarmerApply } from '@/api/farmer.js'
import { handleResp } from '@/utils/respUtil.js'
import ReviewState from '@/views/farmer/components/ReviewState.vue'

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  farmArea: [
    { required: true, message: '请输入养殖场面积', trigger: 'blur' },
    { type: 'number', min: 0, message: '养殖场面积必须大于0', trigger: 'blur' }
  ],
  businessLicenseNo: [{ required: true, message: '请输入营业执照编号', trigger: 'blur' }]
}


const applyFarmerReq = ref({
  realName: '',
  idCard: '',
  houseAddress: '',
  farmName: '',
  farmAddress: '',
  farmArea: 0,
  breedingType: '',
  businessLicenseNo: 0,
  businessLicensePic: '',
  remark: ''
})

// 提交审核申请
const submitApply = async () => {
  const res = await submitFarmerApply(applyFarmerReq.value)
  handleResp(res, '审核提交成功', '提交失败')
}

const imageUrl = ref()
const loading = ref(false)

// 步骤条
const current = ref(0)
const next = () => {
  current.value++
}
const prev = () => {
  current.value--
}
const steps = [
  {
    title: '主体信息'
  },
  {
    title: '养殖场信息'
  }
]
const items = steps.map(item => ({ key: item.title, title: item.title }))

// 审核状态
const reviewStatus = ref()
// 重新申请
const reApply = () => {
  reviewStatus.value = undefined
}

onMounted(async () => {
  const res = await getFarmerReviewStatus()
  handleResp(res, undefined, undefined,
    () => {
      reviewStatus.value = res.data
    })
})

</script>


<style scoped>
#apply-farmer {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 680px;
  margin: 0 auto;
}

.card {
  width: 680px;
}

.avatar-uploader > .ant-upload {
  width: 128px;
  height: 128px;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}

.step {
  margin-top: 36px;
  text-align: center;
  width: 680px;
}
</style>