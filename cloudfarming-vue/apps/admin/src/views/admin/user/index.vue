<template>
  <div class="user-management">
    <!-- 查询表单 -->
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchForm.username"
            placeholder="请输入用户名"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="用户类型">
          <a-select
            v-model:value="searchForm.userType"
            placeholder="请选择用户类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="0">普通用户</a-select-option>
            <a-select-option :value="1">农户</a-select-option>
            <a-select-option :value="2">系统管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="账号状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择账号状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="0">正常</a-select-option>
            <a-select-option :value="1">禁用</a-select-option>
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

    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-button type="primary" @click="handleAdd">
        <template #icon>
          <PlusOutlined />
        </template>
        新增用户
      </a-button>
    </div>

    <!-- 用户列表 -->
    <a-card>
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userType'">
            <a-tag :color="getUserTypeColor(record.userType)">
              {{ getUserTypeText(record.userType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 0 ? 'green' : 'red'">
              {{ record.status === 0 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'avatar'">
            <a-avatar :src="record.avatar" :size="32">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该用户吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="() => handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 分页 -->
      <div class="pagination">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-total="(total: any) => `共 ${total} 条`"
          :show-size-changer="true"
          :show-quick-jumper="true"
          @change="handlePageChange"
        />
      </div>
    </a-card>

    <!-- 新增/编辑用户弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="用户名" name="username">
          <a-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password
            v-model:value="formData.password"
            :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'"
          />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input
            v-model:value="formData.phone"
            placeholder="请输入手机号"
          />
        </a-form-item>
        <a-form-item label="头像" name="avatar">
          <a-input
            v-model:value="formData.avatar"
            placeholder="请输入头像URL"
          />
        </a-form-item>
        <a-form-item label="用户类型" name="userType">
          <a-select
            v-model:value="formData.userType"
            placeholder="请选择用户类型"
          >
            <a-select-option :value="0">普通用户</a-select-option>
            <a-select-option :value="1">农户</a-select-option>
            <a-select-option :value="2">系统管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="账号状态" name="status">
          <a-select
            v-model:value="formData.status"
            placeholder="请选择账号状态"
          >
            <a-select-option :value="0">正常</a-select-option>
            <a-select-option :value="1">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser
} from '@cloudfarming/core'
import type {
  UserRespDTO,
  UserCreateReqDTO,
  UserUpdateReqDTO,
  UserPageQueryReqDTO
} from '@cloudfarming/core'
import type { FormInstance } from 'ant-design-vue'

const loading = ref(false)
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const userList = ref<UserRespDTO[]>([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive<UserPageQueryReqDTO>({
  username: undefined,
  phone: undefined,
  userType: undefined,
  status: undefined
})

const formData = reactive<Partial<UserCreateReqDTO & UserUpdateReqDTO>>({
  username: '',
  password: '',
  phone: '',
  avatar: '',
  userType: 2,
  status: 0
})

const modalTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'))

// 表单规则（使用 computed 响应 isEdit 的变化）
const formRules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 6, max: 18, message: '用户名长度为6-18位', trigger: 'blur' }
  ],
  password: [
    { required: !isEdit.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择账号状态', trigger: 'change' }
  ]
}))

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 120
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 120
  },
  {
    title: '头像',
    dataIndex: 'avatar',
    key: 'avatar',
    width: 100
  },
  {
    title: '用户类型',
    dataIndex: 'userType',
    key: 'userType',
    width: 120
  },
  {
    title: '账号状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
]
// 用户类型文本映射
const getUserTypeText = (type: number) => {
  const typeMap: Record<number, string> = {
    0: '普通用户',
    1: '农户',
    2: '系统管理员'
  }
  return typeMap[type] || '未知'
}
// 用户类型显示颜色映射
const getUserTypeColor = (type: number) => {
  const colorMap: Record<number, string> = {
    0: 'blue',
    1: 'green',
    2: 'orange'
  }
  return colorMap[type] || 'default'
}

const fetchUserList = async () => {
  loading.value = true
  const params: UserPageQueryReqDTO = {
    current: pagination.current,
    size: pagination.size,
    username: searchForm.username || undefined,
    phone: searchForm.phone || undefined,
    userType: searchForm.userType,
    status: searchForm.status
  }
  const res = await getUserPage(params)
  loading.value = false
  if (res.code === '0' && res.data) {
    userList.value = res.data.records
    pagination.total = res.data.total
  } else {
    message.error(res.message || '获取用户列表失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.username = undefined
  searchForm.phone = undefined
  searchForm.userType = undefined
  searchForm.status = undefined
  pagination.current = 1
  fetchUserList()
}

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.size = pageSize
  fetchUserList()
}

const handleAdd = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm(true)
}

const handleEdit = (record: UserRespDTO) => {
  isEdit.value = true
  modalVisible.value = true
  formData.id = record.id
  formData.username = record.username
  formData.password = undefined
  formData.phone = record.phone
  formData.avatar = record.avatar
  formData.userType = record.userType
  formData.status = record.status
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validate()
  } catch (error) {
    return
  }

  modalLoading.value = true
  if (isEdit.value) {
    const updateData: UserUpdateReqDTO = {
      id: formData.id!,
      username: formData.username,
      password: formData.password || undefined,
      phone: formData.phone,
      avatar: formData.avatar,
      userType: formData.userType,
      status: formData.status
    }
    const res = await updateUser(updateData)
    modalLoading.value = false
    if (res.code === '0') {
      message.success('更新用户成功')
      modalVisible.value = false
      fetchUserList()
    } else {
      message.error(res.message || '更新用户失败')
    }
  } else {
    const createData: UserCreateReqDTO = {
      username: formData.username!,
      password: formData.password!,
      phone: formData.phone!,
      avatar: formData.avatar!,
      userType: formData.userType!,
      status: formData.status!
    }
    const res = await createUser(createData)
    modalLoading.value = false
    if (res.code === '0') {
      message.success('新增用户成功')
      modalVisible.value = false
      fetchUserList()
    } else {
      message.error(res.message || '新增用户失败')
    }
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
}

const handleDelete = async (id: string) => {
  const res = await deleteUser(id)
  if (res.code === '0') {
    message.success('删除用户成功')
    fetchUserList()
  } else {
    message.error(res.message || '删除用户失败')
  }
}

const resetForm = (isAdd: boolean = false) => {
  if (isAdd) {
    formData.username = ''
    formData.password = ''
    formData.phone = ''
    formData.avatar = ''
    formData.userType = 2
    formData.status = 0
  }
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="less">
.user-management {
  padding: 24px;

  .search-card {
    margin-bottom: 16px;
  }

  .action-bar {
    margin-bottom: 16px;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
