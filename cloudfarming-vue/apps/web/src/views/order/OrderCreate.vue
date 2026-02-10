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

          <!-- 订单商品信息 -->
          <div class="section-card item-section">
            <div class="section-header">
              <span class="title">商品信息</span>
            </div>
            
            <a-spin :spinning="loading">
              <div v-if="displayItem" class="item-card">
                <div class="item-image">
                  <img :src="displayItem.image" :alt="displayItem.title" />
                </div>
                <div class="item-info">
                  <h3 class="item-title">{{ displayItem.title }}</h3>
                  <div class="item-tags" v-if="displayItem.tags && displayItem.tags.length">
                    <span v-for="(tag, index) in displayItem.tags" :key="index" class="tag">
                      {{ tag }}
                    </span>
                  </div>
                  <div class="item-price-row">
                    <div class="price-box">
                      <span class="currency">¥</span>
                      <span class="amount">{{ displayItem.price }}</span>
                    </div>
                    <div class="quantity-box">
                      <span>x {{ displayItem.quantity }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <a-empty v-else description="商品信息加载失败" />
            </a-spin>
          </div>
        </div>

        <!-- 右侧：结算区 -->
        <div class="side-column">
          <div class="checkout-card">
            <div class="price-detail">
              <div class="detail-row">
                <span>商品金额</span>
                <span class="value">¥{{ totalPrice }}</span>
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
                  {{ totalPrice }}
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
                提交订单即表示同意《云农场用户协议》
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import AddressSelector from '../../components/address/AddressSelector.vue';
import { getAdoptItemDetail } from '@/api/adopt';
import { getSpuDetail } from '@/api/spu';
import { createOrder, ORDER_TYPE } from '@/api/order';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const submitting = ref(false);
const selectedAddressId = ref('');
const selectedAddress = ref(null);

// 统一展示数据
const displayItem = ref(null);

// 从路由参数获取订单信息
const orderType = computed(() => {
  const type = route.query.type;
  return type === 'product' ? ORDER_TYPE.GOODS : ORDER_TYPE.ADOPT;
});

const quantity = computed(() => {
  const q = route.query.quantity;
  return q ? Number(q) : 1;
});

const totalPrice = computed(() => {
  if (!displayItem.value) return '0.00';
  return (displayItem.value.price * displayItem.value.quantity).toFixed(2);
});

const fetchDetail = async () => {
  loading.value = true;
  try {
    if (orderType.value === ORDER_TYPE.ADOPT) {
      // 认养项目
      const id = route.params.id || route.query.id;
      if (!id) throw new Error('缺少参数: id');
      
      const res = await getAdoptItemDetail(id);
      if (res.code == '0' && res.data) {
        const data = res.data;
        displayItem.value = {
          id: data.id,
          bizId: data.id,
          shopId: data.userId, // TODO
          title: data.title,
          image: data.coverImage,
          price: data.price,
          quantity: quantity.value,
          tags: [
            `认养周期 ${data.adoptDays}天`,
            `预计收益 ${data.expectedYield || '以实际收获为准'}`
          ]
        };
      } else {
        message.error(res.message || '获取项目详情失败');
      }
    } else {
      // 农产品
      const spuId = route.query.spuId;
      const skuId = route.query.skuId;
      
      if (!spuId || !skuId) throw new Error('缺少参数: spuId 或 skuId');
      
      const res = await getSpuDetail(Number(spuId));
      if (res.code === '0' && res.data) {
        const spu = res.data;
        // 查找选中的 SKU
        const sku = spu.skuList.find(s => s.id === skuId);
        
        if (sku) {
          // 格式化销售属性
          const specs = Object.values(sku.saleAttrs || {}).join(' ');
          
          displayItem.value = {
            id: String(spu.id),
            skuId: sku.id,
            bizId: sku.id,
            shopId: spu.shopId,
            title: spu.title,
            image: sku.spuImage || spu.images.split(',')[0], // 优先用SKU图片，否则用SPU主图
            price: sku.price,
            quantity: quantity.value,
            tags: specs ? [specs] : []
          };
        } else {
          message.error('未找到指定的商品规格');
        }
      } else {
        message.error(res.message || '获取商品详情失败');
      }
    }
  } catch (error) {
    message.error(error.message || '系统繁忙，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleAddressChange = (addr) => {
  selectedAddress.value = addr;
};

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    message.warning('请选择收货地址');
    return;
  }
  
  if (!displayItem.value) return;

  submitting.value = true;
  try {
    const items = [{
      bizType: orderType.value,
      bizId: displayItem.value.bizId,
      shopId: displayItem.value.shopId || 0,
      quantity: displayItem.value.quantity
    }];

    const res = await createOrder({
      orderType: orderType.value,
      receiveId: selectedAddressId.value,
      items: items
    });

    if (res.code == '0' && res.data) {
      message.success('订单创建成功！即将跳转支付...');
      const { payOrderNo, payAmount } = res.data;
      if (!payOrderNo) {
        message.error('订单创建异常：返回数据缺少订单号');
        return;
      }
      router.push({
        path: '/pay',
        query: {
          payOrderNo: payOrderNo,
          amount: payAmount
        }
      });
    } else {
      message.error(res.message || '创建订单失败');
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped>
.order-create-container {
  width: 100%;
  min-height: 100vh;
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
  background: #10b981;
  border-color: #10b981;
  color: #fff;
  font-weight: 600;
  height: 44px;
  font-size: 16px;
}

.submit-btn:hover {
  background: #047857;
  border-color: #047857;
  color: #fff;
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
