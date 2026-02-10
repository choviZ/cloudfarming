<template>
  <a-result :status="resultStatus" :title="title" :sub-title="subTitle">
    <template #icon>
      <component :is="iconComponent" :style="{ color: iconColor }" />
    </template>

    <!-- 仅在“未通过”时显示备注和重新申请按钮 -->
    <template v-if="props.statusData.status === '未通过'" #extra>
      <a-space direction="vertical" style="width: 100%">
        <a-alert
          v-if="props.statusData.reviewRemark"
          type="error"
          :description="props.statusData.reviewRemark"
        />
        <a-button danger v-if="onReApply" type="primary" @click="onReApply">
          修改信息并重新申请
        </a-button>
      </a-space>
    </template>
  </a-result>
</template>

<script setup>
import { computed } from 'vue';
import {
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue';

const props = defineProps({
  statusData: Object,
  onReApply: Function
});

const title = computed(() => {
  switch (props.statusData.status) {
    case '待审核':
      return '入驻申请已提交';
    case '已通过':
      return '恭喜！您已成为认证农户';
    case '未通过':
      return '审核未通过';
    default:
      return '未知审核状态';
  }
});

const subTitle = computed(() => {
  switch (props.statusData.status) {
    case '待审核':
      return '您的申请正在审核中，请耐心等待。审核结果将通过站内通知告知您。';
    case '已通过':
      return '您现在可以管理农场、发布农产品等操作。';
    case '未通过':
      return ''; // 原因由 Alert 单独展示
    default:
      return `当前状态：${props.statusData.status}`;
  }
});

const resultStatus = computed(() => {
  switch (props.statusData.status) {
    case '待审核':
      return 'info';
    case '已通过':
      return 'success';
    case '未通过':
      return 'error';
    default:
      return 'warning';
  }
});

const iconComponent = computed(() => {
  switch (props.statusData.status) {
    case '待审核':
      return ClockCircleOutlined;
    case '已通过':
      return CheckCircleOutlined;
    case 'REJECTED':
      return CloseCircleOutlined;
    default:
      return CloseCircleOutlined;
  }
});

const iconColor = computed(() => {
  switch (props.statusData.status) {
    case '待审核':
      return '#faad14';
    case '已通过':
      return '#52c41a';
    case '未通过':
      return '#ff4d4f';
    default:
      return '#fa8c16';
  }
});
</script>