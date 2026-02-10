<template>
  <div class="address-list-container">
    <div class="page-header">
      <h2 class="title">收货地址管理</h2>
      <a-button type="primary" @click="handleAdd">新增收货地址</a-button>
    </div>

    <div class="alert-info">
      <info-circle-outlined /> 
      <span class="text">已保存 {{ list.length }} 条地址，还可以保存 {{ 20 - list.length }} 条地址</span>
    </div>

    <a-table
      :columns="columns"
      :data-source="list"
      :loading="loading"
      :pagination="false"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'receiver'">
          <div>{{ record.receiverName }}</div>
          <div v-if="record.remark" class="remark-tag">{{ record.remark }}</div>
        </template>
        
        <template v-if="column.key === 'area'">
           {{ record.provinceName }} {{ record.cityName }} {{ record.districtName }} {{ record.townName || '' }}
        </template>

        <template v-if="column.key === 'isDefault'">
          <div v-if="record.isDefault === 1" class="default-badge">默认地址</div>
          <a-button 
            v-else 
            type="link" 
            size="small" 
            class="set-default-btn"
            @click="handleSetDefault(record)"
          >
            设为默认
          </a-button>
        </template>

        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定要删除这条地址吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <AddressModal
      v-model:open="modalVisible"
      :edit-data="currentEdit"
      @success="fetchList"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { InfoCircleOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { 
  getCurrentUserReceiveAddresses, 
  deleteReceiveAddress, 
  setDefaultReceiveAddress 
} from '@/api/address';
import AddressModal from './components/AddressModal.vue';

const loading = ref(false);
const list = ref([]);
const modalVisible = ref(false);
const currentEdit = ref(null);

const columns = [
  {
    title: '收货人',
    key: 'receiver',
    width: 200,
  },
  {
    title: '所在地区',
    key: 'area',
    width: 300,
  },
  {
    title: '详细地址',
    dataIndex: 'detailAddress',
    key: 'detailAddress',
  },
  {
    title: '手机/电话',
    dataIndex: 'receiverPhone',
    key: 'phone',
    width: 180,
  },
  {
    title: '状态',
    key: 'isDefault',
    width: 120,
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    align: 'center',
  },
];

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getCurrentUserReceiveAddresses();
    if (res.code === '0' && res.data) {
      list.value = res.data;
    }
  } catch (error) {
    message.error('获取地址列表失败');
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  currentEdit.value = null;
  modalVisible.value = true;
};

const handleEdit = (record) => {
  currentEdit.value = record;
  modalVisible.value = true;
};

const handleDelete = async (id) => {
  try {
    const res = await deleteReceiveAddress(id);
    if (res.code === '0') {
      message.success('删除成功');
      fetchList();
    } else {
      message.error(res.message || '删除失败');
    }
  } catch (error) {
    message.error('删除失败');
  }
};

const handleSetDefault = async (record) => {
  try {
    const res = await setDefaultReceiveAddress({ id: record.id });
    if (res.code === '0') {
      message.success('设置成功');
      fetchList();
    } else {
      message.error(res.message || '设置失败');
    }
  } catch (error) {
    message.error('设置失败');
  }
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.address-list-container {
  background: #fff;
  padding: 24px;
  min-height: 100%;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.alert-info {
  background: #e6f7ff;
  border: 1px solid #91caff;
  padding: 10px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  color: #1890ff;
}

.alert-info .text {
  margin-left: 8px;
  color: #666;
}

.remark-tag {
  display: inline-block;
  background: #f0f0f0;
  color: #999;
  font-size: 12px;
  padding: 1px 4px;
  border-radius: 2px;
  margin-top: 4px;
}

.default-badge {
  background: #ffda44;
  color: #111;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
}

.set-default-btn {
  color: #ff6a00; /* 类似淘宝/闲鱼的操作色 */
}

.set-default-btn:hover {
  color: #ff4400;
}
</style>
