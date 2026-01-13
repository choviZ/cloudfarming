<template>
  <a-space style="margin-bottom: 12px">
    <a-select
      v-model:value="selectedAttrId"
      :options="attrOptions"
      style="width: 240px"
      placeholder="请选择属性"
    />
    <a-button type="primary" @click="addAttribute">添加</a-button>
  </a-space>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { SelectProps } from 'ant-design-vue'
import type { AttributeRespDTO } from '@cloudfarming/core'

const selectedAttrId = ref<string>('')

// 组件通信
const props = defineProps<{
  // 所有属性
  attributes: AttributeRespDTO[]
  // 已选的属性
  selectedAttrIds: string[]
  // 类型过滤
  typeFilter?: number
}>()

const emit = defineEmits<{
  (e: 'add', id: string, type: number): void
}>()

/**
 * 计算选择器可用的选项
 */
const attrOptions = computed<SelectProps['options']>(() => {
  const addedIds = new Set(props.selectedAttrIds)
  return props.attributes
    .filter(attr => {
      if (props.typeFilter !== undefined && attr.attrType !== props.typeFilter) {
        return false
      }
      return !addedIds.has(String(attr.id))
    })
    .map(attr => ({
      label: attr.name,
      value: String(attr.id)
    }))
})

const addAttribute = () => {
  if (!selectedAttrId.value) return
  emit('add', selectedAttrId.value, props.typeFilter ?? 0)
  selectedAttrId.value = ''
}
</script>

<style scoped>

</style>