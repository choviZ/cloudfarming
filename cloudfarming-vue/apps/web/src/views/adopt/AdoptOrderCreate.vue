<template>
  <div class="order-create-container">
    <div class="page-content">
      <h2 class="page-title">确认订单</h2>

      <div class="layout-grid">
        <!-- 左侧：主要信息区 -->
        <div class="main-column">
          <!-- 收货地址 -->
          <div class="section-card address-section">
            <AddressSelector
              v-model="selectedAddressId"
              @change="handleAddressChange"
            />
          </div>

          <!-- 认养项目信息 -->
          <div class="section-card item-section">
            <div class="section-header">
              <span class="title">认养项目信息</span>
            </div>
            
            <a-spin :spinning="loading">
              <div v-if="detail" class="item-card">
                <div class="item-image">
                  <img :src="detail.coverImage" :alt="detail.title" />
                </div>
                <div class="item-info">
                  <h3 class="item-title">{{ detail.title }}</h3>
                  <div class="item-tags">
                    <span class="tag">认养周期 {{ detail.adoptDays }}天</span>
                    <span class="tag">预计收益 {{ detail.expectedYield || '以实际收获为准' }}</span>
                  </div>
                  <div class="item-price-row">
                    <div class="price-box">
                      <span class="currency">¥</span>
                      <span class="amount">{{ detail.price }}</span>
                    </div>
                    <div class="quantity-box">
                      <span>x 1</span>
                    </div>
                  </div>
                </div>
              </div>
            </a-spin>
          </div>
        </div>

        <!-- 右侧：结算区 -->
        <div class="side-column">
          <div class="checkout-card">
            <div class="price-detail">
              <div class="detail-row">
                <span>认养金额</span>
                <span class="value">¥{{ detail ? detail.price : 0 }}</span>
              </div>
              <div class="detail-row">
                <span>运费</span>
                <span class="value">¥0.00</span>
              </div>
              <div class="divider"></div>
              <div class="total-row">
                <span>合计</span>
                <span class="total-price">
                  <span class="currency">¥</span>
                  {{ detail ? detail.price : 0 }}
                </span>
              </div>
            </div>

            <div class="action-area">
              <a-button 
                type="primary" 
                size="large" 
                block 
                class="submit-btn"
                :loading="submitting"
                @click="handleSubmit"
              >
                提交订单
              </a-button>
              <div class="agreement-text">
                提交订单即表示同意《云农场认养协议》
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import AddressSelector from '../../components/address/AddressSelector.vue';
import { getAdoptItemDetail, createAdoptOrder } from '@cloudfarming/core/api/adopt';
import type { ReceiveAddressResp } from '@cloudfarming/core/api/address';
import type { AdoptItemResp } from '@cloudfarming/core/api/adopt'

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const submitting = ref(false);
const detail = ref<AdoptItemResp | null>(null);
const selectedAddressId = ref('');
const selectedAddress = ref<ReceiveAddressResp | null>(null);

const fetchDetail = async (id: string) => {
  loading.value = true;
  try {
    const res = await getAdoptItemDetail(id);
    if (res.code === '0' && res.data) {
      detail.value = res.data;
    } else {
      message.error(res.message || '获取项目详情失败');
    }
  } catch (error) {
    console.error(error);
    message.error('系统繁忙，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleAddressChange = (addr: ReceiveAddressResp) => {
  selectedAddress.value = addr;
};

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    message.warning('请选择收货地址');
    return;
  }
  
  if (!detail.value) return;

  submitting.value = true;
  try {
    // TODO: 目前API只定义了 adoptItemId，后续可能需要传入 addressId
    const res = await createAdoptOrder({
      adoptItemId: detail.value.id,
      receiveId: selectedAddressId.value
    });

    if (res.code === '0' && res.data) {
      message.success('订单创建成功！即将跳转支付...');
      // TODO: 跳转到支付页面，携带 orderId
      // router.push(`/pay?orderId=${res.data.id}`);
      console.log('Order created:', res.data);
    } else {
      message.error(res.message || '创建订单失败');
    }
  } catch (error) {
    console.error(error);
    message.error('创建订单异常');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  const id = route.params.id as string;
  if (id) {
    fetchDetail(id);
  } else {
    message.error('参数错误');
    router.push('/adopt/list');
  }
});
</script>

<style scoped>
.order-create-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f6f7f8;
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

.page-content {
  width: 100%;
  max-width: 1200px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #111;
}

.layout-grid {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.main-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-column {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

/* 认养项目信息样式 */
.section-header {
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
}

.section-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.item-card {
  display: flex;
  gap: 16px;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  background: #f4f4f4;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #111;
  margin-bottom: 8px;
  line-height: 1.4;
}

.item-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.tag {
  background: #f7f8fa;
  color: #666;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.item-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-box {
  color: #111;
  font-weight: 600;
}

.price-box .currency {
  font-size: 12px;
}

.price-box .amount {
  font-size: 18px;
}

.quantity-box {
  color: #999;
  font-size: 14px;
}

/* 结算卡片样式 */
.checkout-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.price-detail {
  margin-bottom: 24px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
}

.detail-row .value {
  color: #111;
  font-weight: 500;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 16px 0;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.total-row span:first-child {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.total-price {
  color: #ff4400;
  font-weight: bold;
  font-size: 24px;
  line-height: 1;
}

.total-price .currency {
  font-size: 14px;
  margin-right: 2px;
}

.submit-btn {
  background: #ffda44;
  border-color: #ffda44;
  color: #111;
  font-weight: 600;
  height: 44px;
  font-size: 16px;
}

.submit-btn:hover {
  background: #ffcd11;
  border-color: #ffcd11;
  color: #111;
}

.agreement-text {
  margin-top: 12px;
  font-size: 12px;
  color: #999;
  text-align: center;
}

@media (max-width: 768px) {
  .layout-grid {
    flex-direction: column;
  }
  
  .side-column {
    width: 100%;
    position: static;
  }
}
</style>
