<template>
  <div v-for="attr in attributes" :key="attr.key">
    <a-form-item :label="attr.label">
      <a-space>
        <!-- 基本属性：单值 input -->
        <a-input
          v-if="type === 0"
          v-model:value="attr.value"
          placeholder="请输入属性值"
          style="width: 240px"
        />
        <!-- 销售属性：多值 tags -->
        <a-select
          v-else
          v-model:value="attr.values"
          mode="tags"
          style="width: 240px"
          placeholder="输入后回车确认"
          :token-separators="[' ', ',', '，']"
        />
        <a-button
          danger
          type="link"
          @click="removeAttribute(attr.key)"
        >
          删除
        </a-button>
      </a-space>
    </a-form-item>
  </div>
</template>

<script setup>
// 组件通信
import { ref } from 'vue'

const props = defineProps({
  attributes: {
    type: Array,
    default: () => []
  },
  type: Number
})

const emit = defineEmits(['removeAttribute'])

const value = ref('')

const removeAttribute = (id) => {
  emit('removeAttribute', id, props.type)
}
</script>


<style scoped>

</style>