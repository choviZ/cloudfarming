<template>
<!--  <a-form-item label="分类" style="width: 240px">-->
    <a-tree-select
      v-model:value="value"
      placeholder="请选择分类"
      allow-clear
      tree-default-expand-all
      :tree-data="treeData"
    />
<!--  </a-form-item>-->
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import type { TreeSelectProps } from 'ant-design-vue'
import { getCategoryTree, type CategoryRespDTO } from '@cloudfarming/core'

// ===== v-model 支持 =====
const props = defineProps<{modelValue?: string}>()

const emit = defineEmits<{ (e: 'update:modelValue', val?: string): void }>()

const value = ref<string | undefined>(props.modelValue)

watch(() => props.modelValue, v => {
  value.value = v
})

watch(value, v => {
  emit('update:modelValue', v)
})

// ===== Tree 数据 =====
const treeData = ref<TreeSelectProps['treeData']>([])

const initTreeData = async () => {
  const res = await getCategoryTree()
  treeData.value = transformCategoryToTree(res.data)
}

initTreeData()

// ===== DTO → TreeSelect =====
type TreeNode = {
  label: string
  value: string
  key: string
  disabled?: boolean
  children?: TreeNode[]
}
/**
 * 将分类数据转换为树形结构
 * @param data
 */
const transformCategoryToTree = (
  data: CategoryRespDTO[]
): TreeSelectProps['treeData'] => {
  if (!data?.length) return []

  const convert = (node: CategoryRespDTO): TreeNode => {
    const hasChildren = !!node.children && node.children.length > 0
    return {
      label: node.name,
      value: node.id,
      key: node.id,
      disabled: hasChildren,
      children: node.children?.map(convert)
    }
  }
  return data.map(convert)
}
</script>
