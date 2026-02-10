<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑收货地址' : '新增收货地址'"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirmLoading="loading"
    width="600px"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="收货人" name="receiverName">
        <a-input v-model:value="formState.receiverName" placeholder="请输入收货人姓名" />
      </a-form-item>

      <a-form-item label="手机号码" name="receiverPhone">
        <a-input v-model:value="formState.receiverPhone" placeholder="请输入手机号码" />
      </a-form-item>

      <a-form-item label="所在地区" name="area">
        <a-cascader
          v-model:value="formState.area"
          :options="areaOptions"
          placeholder="请选择省/市/区"
          :field-names="{ label: 'label', value: 'label', children: 'children' }"
        />
      </a-form-item>

      <a-form-item label="详细地址" name="detailAddress">
        <a-textarea
          v-model:value="formState.detailAddress"
          placeholder="请输入街道、小区、楼栋号等详细信息"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="地址标签" name="remark">
        <a-radio-group v-model:value="formState.remark" @change="customTagVisible = false">
          <a-radio value="家">家</a-radio>
          <a-radio value="公司">公司</a-radio>
          <a-radio value="学校">学校</a-radio>
        </a-radio-group>
        <div style="margin-top: 8px">
            <a-input 
                v-if="customTagVisible" 
                v-model:value="formState.remark" 
                placeholder="请输入标签名称" 
                style="width: 120px"
                :maxlength="10"
            />
             <a-button v-else type="link" size="small" @click="enableCustomTag">自定义</a-button>
        </div>
      </a-form-item>

      <a-form-item label="默认地址" name="isDefault">
        <a-switch
          v-model:checked="isDefaultBool"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { addReceiveAddress, updateReceiveAddress } from '@/api/address';
import { message } from 'ant-design-vue';
import { regionData } from 'element-china-area-data';

const props = defineProps({
  open: { type: Boolean, required: true },
  editData: { type: Object, default: null },
});

const emit = defineEmits(['update:open', 'success']);

const loading = ref(false);
const formRef = ref();
const customTagVisible = ref(false);
const predefinedTags = ['家', '公司', '学校'];

// 使用 element-china-area-data 的数据
const areaOptions = regionData;

const formState = reactive({
  receiverName: '',
  receiverPhone: '',
  area: [],
  detailAddress: '',
  isDefault: 0,
  remark: '家',
});

const isDefaultBool = computed({
  get: () => formState.isDefault === 1,
  set: (val) => {
    formState.isDefault = val ? 1 : 0;
  },
});

const isEdit = computed(() => !!props.editData);

// 监听编辑数据变化
watch(
  () => props.editData,
  (val) => {
    if (val) {
      formState.receiverName = val.receiverName;
      formState.receiverPhone = val.receiverPhone;
      formState.area = [val.provinceName, val.cityName, val.districtName];
      formState.detailAddress = val.detailAddress;
      formState.isDefault = val.isDefault;
      formState.remark = val.remark;
      
      // 判断是否为自定义标签
      if (!predefinedTags.includes(val.remark)) {
        customTagVisible.value = true;
      } else {
        customTagVisible.value = false;
      }
    } else {
      resetForm();
    }
  },
  { immediate: true }
);

// 监听 modal 打开/关闭，关闭时重置表单
watch(
    () => props.open,
    (val) => {
        if(!val) {
             // 可以在这里做一些清理工作，但通常 formState 的重置在 watch editData 或者提交成功后做
        } else if (!props.editData) {
            resetForm();
        }
    }
)

function resetForm() {
  formState.receiverName = '';
  formState.receiverPhone = '';
  formState.area = [];
  formState.detailAddress = '';
  formState.isDefault = 0;
  formState.remark = '家';
  customTagVisible.value = false;
}

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号码格式不正确', trigger: 'blur' },
  ],
  area: [{ required: true, message: '请选择所在地区', trigger: 'change', type: 'array' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
};

const handleCancel = () => {
  emit('update:open', false);
  formRef.value?.resetFields();
  customTagVisible.value = false;
};

const enableCustomTag = () => {
  customTagVisible.value = true;
  if (predefinedTags.includes(formState.remark)) {
    formState.remark = '';
  }
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    // 提取省市区街道信息
    let provinceName = '', cityName = '', districtName = '', townName = '';
    
    if (formState.area && formState.area.length > 0) {
      provinceName = formState.area[0] || '';
      cityName = formState.area[1] || '';
      districtName = formState.area[2] || '';
    }

    const commonData = {
      receiverName: formState.receiverName,
      receiverPhone: formState.receiverPhone,
      provinceName,
      cityName,
      districtName,
      townName,
      detailAddress: formState.detailAddress,
      isDefault: formState.isDefault,
      remark: formState.remark,
    };

    let res;
    if (isEdit.value && props.editData) {
      const updateData = {
        id: props.editData.id,
        ...commonData,
      };
      res = await updateReceiveAddress(updateData);
    } else {
      const addData = {
        ...commonData,
      };
      res = await addReceiveAddress(addData);
    }

    if (res.code === '0') {
      message.success(isEdit.value ? '修改成功' : '添加成功');
      emit('success');
      emit('update:open', false);
    } else {
      message.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>
