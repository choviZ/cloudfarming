<template>
  <div class="farmer-management">
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="农场名称">
          <a-input
            v-model:value="searchForm.farmName"
            placeholder="请输入农场名称"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="养殖品类">
          <a-input
            v-model:value="searchForm.breedingType"
            placeholder="请输入养殖品类"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select
            v-model:value="searchForm.reviewStatus"
            placeholder="请选择审核状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">未通过</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="精选状态">
          <a-select
            v-model:value="searchForm.featuredFlag"
            placeholder="请选择精选状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="1">已精选</a-select-option>
            <a-select-option :value="0">未精选</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="farmerList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 1380 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'reviewStatus'">
            <a-tag :color="getReviewStatusColor(record.reviewStatus)">
              {{ getReviewStatusText(record.reviewStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'featuredFlag'">
            <a-tag :color="record.featuredFlag === 1 ? 'gold' : 'default'">
              {{ record.featuredFlag === 1 ? '已精选' : '未精选' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'farmArea'">
            <span>{{ record.farmArea ? `${record.farmArea} 亩` : '-' }}</span>
          </template>
          <template v-else-if="column.key === 'businessLicensePic'">
            <a-image
              v-if="record.businessLicensePic"
              :src="record.businessLicensePic"
              :width="64"
            />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'environmentImages'">
            <a-image-preview-group v-if="record.environmentImages?.length">
              <div class="image-group">
                <a-image
                  v-for="(image, index) in record.environmentImages.slice(0, 3)"
                  :key="`${record.id}-${index}`"
                  :src="image"
                  :width="56"
                />
                <span v-if="record.environmentImages.length > 3" class="more-count">
                  +{{ record.environmentImages.length - 3 }}
                </span>
              </div>
            </a-image-preview-group>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ formatTime(record.updateTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-popconfirm
                v-if="record.featuredFlag !== 1"
                title="确认设为精选农户？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleFeatured(record, 1)"
              >
                <a-button
                  type="link"
                  size="small"
                  :disabled="!canSetFeatured(record)"
                >
                  设为精选
                </a-button>
              </a-popconfirm>
              <a-popconfirm
                v-else
                title="确认取消精选？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleFeatured(record, 0)"
              >
                <a-button type="link" size="small" danger>
                  取消精选
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <div class="pagination">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-total="(total) => `共 ${total} 条`"
          :show-size-changer="true"
          :show-quick-jumper="true"
          @change="handlePageChange"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { getFarmerPage, updateFarmerFeaturedFlag } from '@/api/farmer'

const loading = ref(false)
const farmerList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  farmName: undefined,
  breedingType: undefined,
  reviewStatus: undefined,
  featuredFlag: undefined
})

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 90
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 90
  },
  {
    title: '农场名称',
    dataIndex: 'farmName',
    key: 'farmName',
    width: 180
  },
  {
    title: '养殖品类',
    dataIndex: 'breedingType',
    key: 'breedingType',
    width: 140
  },
  {
    title: '面积',
    dataIndex: 'farmArea',
    key: 'farmArea',
    width: 110
  },
  {
    title: '审核状态',
    dataIndex: 'reviewStatus',
    key: 'reviewStatus',
    width: 120
  },
  {
    title: '营业执照',
    dataIndex: 'businessLicensePic',
    key: 'businessLicensePic',
    width: 120
  },
  {
    title: '环境图片',
    dataIndex: 'environmentImages',
    key: 'environmentImages',
    width: 220
  },
  {
    title: '精选状态',
    dataIndex: 'featuredFlag',
    key: 'featuredFlag',
    width: 120
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right'
  }
]

const fetchFarmerList = async () => {
  loading.value = true
  try {
    const res = await getFarmerPage({
      current: pagination.current,
      size: pagination.size,
      farmName: searchForm.farmName || undefined,
      breedingType: searchForm.breedingType || undefined,
      reviewStatus: searchForm.reviewStatus,
      featuredFlag: searchForm.featuredFlag
    })
    if (res.code === '0' && res.data) {
      farmerList.value = res.data.records || []
      pagination.total = Number(res.data.total || 0)
      return
    }
    message.error(res.message || '加载农户列表失败')
  } catch (error) {
    console.error('加载农户列表失败', error)
    message.error('加载农户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchFarmerList()
}

const handleReset = () => {
  searchForm.farmName = undefined
  searchForm.breedingType = undefined
  searchForm.reviewStatus = undefined
  searchForm.featuredFlag = undefined
  pagination.current = 1
  fetchFarmerList()
}

const handlePageChange = (page, pageSize) => {
  pagination.current = page
  pagination.size = pageSize
  fetchFarmerList()
}

const canSetFeatured = (record) => {
  return record.reviewStatus === 1
    && !!record.businessLicensePic
    && Array.isArray(record.environmentImages)
    && record.environmentImages.length > 0
}

const handleToggleFeatured = async (record, featuredFlag) => {
  const res = await updateFarmerFeaturedFlag({
    id: record.id,
    featuredFlag
  })
  if (res.code === '0') {
    message.success(featuredFlag === 1 ? '已设为精选农户' : '已取消精选')
    fetchFarmerList()
  } else {
    message.error(res.message || '更新精选状态失败')
  }
}

const getReviewStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '未通过'
  }
  return map[status] || '未知'
}

const getReviewStatusColor = (status) => {
  const map = {
    0: 'processing',
    1: 'success',
    2: 'error'
  }
  return map[status] || 'default'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(() => {
  fetchFarmerList()
})
</script>

<style scoped lang="less">
.farmer-management {
  padding: 24px;
}

.search-card {
  margin-bottom: 16px;
}

.image-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.more-count {
  color: #999;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
