<template>
  <div class="pay-container">
    <div class="page-content">
      <h2 class="page-title">收银台</h2>

      <div class="pay-card">
        <div class="order-info">
          <div class="info-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ orderId }}</span>
          </div>
          <div class="info-item amount-item">
            <span class="label">应付金额：</span>
            <span class="amount-value">
              <span class="currency">¥</span>
              {{ amount }}
            </span>
          </div>
        </div>

        <div class="divider"></div>

        <div class="payment-methods">
          <h3 class="section-title">选择支付方式</h3>
          
          <a-radio-group v-model:value="paymentMethod" class="method-group">
            <div class="method-item" :class="{ active: paymentMethod === 'alipay' }" @click="paymentMethod = 'alipay'">
              <a-radio value="alipay">
                <div class="method-content">
                  <!-- 暂时没有图片，用文字代替或后续添加图片 -->
                  <span class="method-name">支付宝支付</span>
                  <span class="method-tag">推荐</span>
                </div>
              </a-radio>
            </div>
          </a-radio-group>
        </div>

        <div class="action-area">
          <a-button 
            type="primary" 
            size="large" 
            class="pay-btn"
            :loading="paying"
            @click="handlePay"
          >
            立即支付 ¥{{ amount }}
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';

const route = useRoute();
const router = useRouter();

const orderId = ref('');
const amount = ref('0.00');
const paymentMethod = ref('alipay');
const paying = ref(false);

const handlePay = async () => {
  if (!orderId.value) {
    message.error('订单信息无效');
    return;
  }

  paying.value = true;
  try {
    // TODO: 调用后端支付接口
    // const res = await payOrder({ orderId: orderId.value, channel: paymentMethod.value });
    
    // 模拟支付过程
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    message.success('支付成功！');
    // 跳转到订单列表或详情页
    // router.push('/user/orders');
    router.replace('/adopt/list'); // 暂时跳转到列表
    
  } catch (error) {
    console.error(error);
    message.error('支付发起失败');
  } finally {
    paying.value = false;
  }
};

onMounted(() => {
  const { orderId: id, amount: price } = route.query;
  if (id) {
    orderId.value = id as string;
    amount.value = price as string || '0.00';
  } else {
    message.error('参数缺失');
    router.back();
  }
});
</script>

<style scoped>
.pay-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f6f7f8;
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

.page-content {
  width: 100%;
  max-width: 800px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #111;
}

.pay-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #666;
}

.info-item.amount-item {
  margin-top: 8px;
}

.amount-value {
  color: #ff5000;
  font-size: 24px;
  font-weight: bold;
}

.currency {
  font-size: 16px;
  margin-right: 2px;
}

.divider {
  height: 1px;
  background-color: #eee;
  margin: 30px 0;
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #333;
}

.method-group {
  width: 100%;
}

.method-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.method-item:hover, .method-item.active {
  border-color: #ffda44;
  background-color: #fffdf0;
}

.method-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.method-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.method-tag {
  background: #ff5000;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.action-area {
  margin-top: 40px;
  display: flex;
  justify-content: flex-end;
}

.pay-btn {
  width: 200px;
  background-color: #ffda44;
  border-color: #ffda44;
  color: #333;
  font-weight: 600;
}

.pay-btn:hover {
  background-color: #ffcd00;
  border-color: #ffcd00;
  color: #333;
}
</style>
