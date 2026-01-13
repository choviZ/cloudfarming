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

<script setup lang="ts">
// 组件通信
import { ref } from 'vue'

// 动态区域已添加的属性
interface Attribute {
  key: string;
  label: string;
  value?: string;
  // 销售属性：values（可选，因为基本属性没有这个字段）
  values?: string[]
}

interface Props {
  attributes: Attribute[],
  type: number
}

const props = withDefaults(defineProps<Props>(), {
  attributes: () => []
})

const emit = defineEmits<{
  (e: 'removeAttribute', key: string, type: number): void
}>()

const value = ref<string>('')

const removeAttribute = (id: string) => {
  emit('removeAttribute', id, props.type)
}
</script>


<style scoped>

</style>