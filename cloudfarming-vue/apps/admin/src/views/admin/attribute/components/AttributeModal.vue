<template>
  <a-modal
    :open="open"
    :title="title"
    @ok="handleOk"
    @cancel="handleCancel"
    :confirmLoading="loading"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="属性名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入属性名称" />
      </a-form-item>
      
      <a-form-item label="属性类型" name="attrType">
        <a-radio-group v-model:value="formState.attrType">
          <a-radio :value="0">基本属性</a-radio>
          <a-radio :value="1">销售属性</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="排序" name="sort">
        <a-input-number v-model:value="formState.sort" :min="0" style="width: 100%" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, computed } from 'vue';
import type { FormInstance } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import { createAttribute, updateAttribute } from '@cloudfarming/core/api/attribute';
import type { AttributeRespDTO } from '@cloudfarming/core/api/attribute';

const props = defineProps<{
  open: boolean;
  editData?: AttributeRespDTO | null;
  categoryId: string;
}>();

const emit = defineEmits(['update:open', 'success']);

const loading = ref(false);
const formRef = ref<FormInstance>();

const formState = reactive({
  name: '',
  attrType: 0,
  sort: 0,
});

const rules = {
  name: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  attrType: [{ required: true, message: '请选择属性类型', trigger: 'change' }],
};

const title = computed(() => (props.editData ? '编辑属性' : '新增属性'));

watch(
  () => props.open,
  (val) => {
    if (val) {
      if (props.editData) {
        formState.name = props.editData.name;
        formState.attrType = props.editData.attrType;
        formState.sort = props.editData.sort;
      } else {
        formState.name = '';
        formState.attrType = 0;
        formState.sort = 0;
      }
    }
  }
);

const handleOk = async () => {
  try {
    await formRef.value?.validateFields();
    loading.value = true;
    
    if (props.editData) {
      // 更新
      const res = await updateAttribute({
        id: props.editData.id,
        categoryId: props.categoryId,
        name: formState.name,
        attrType: formState.attrType,
        sort: formState.sort,
      });
      if (res.code === '0') {
        message.success('更新成功');
        emit('success');
        emit('update:open', false);
      } else {
        message.error(res.message || '更新失败');
      }
    } else {
      // 新增
      const res = await createAttribute({
        categoryId: props.categoryId,
        name: formState.name,
        attrType: formState.attrType,
        sort: formState.sort,
      });
      if (res.code === '0') {
        message.success('创建成功');
        emit('success');
        emit('update:open', false);
      } else {
        message.error(res.message || '创建失败');
      }
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  emit('update:open', false);
  formRef.value?.resetFields();
};
</script>
