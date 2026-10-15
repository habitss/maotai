<template>
  <BasicModal
    @register="registerModal"
    @ok="handleSubmit"
    v-bind="$attrs"
    :title="title"
    :showOkBtn="!disabledRef"
  >
    <div class="form-pad">
      <BasicForm @register="registerForm" @submit="handleSubmit" />
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { saveOrUpdate } from '../MaotaiEquipmentLedger.api';
import { addFormSchemas } from '../MaotaiEquipmentLedger.data';

// 声明Emits
const emit = defineEmits(['success']);
// 标题
const title = ref('');
// 禁用(详情态)
const disabledRef = ref(false);
// 是否编辑
const isUpdateRef = ref(true);

const { createMessage: msg } = useMessage();

// 注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  // 重置表单数据
  resetFields();
  // 设置表单数据
  setFieldsValue(data.record ?? {});
  if (data.action === 'add') {
    title.value = '新增设备';
    isUpdateRef.value = false;
  } else if (data.action === 'edit') {
    title.value = '编辑设备';
    isUpdateRef.value = true;
  } else if (data.action === 'detail') {
    title.value = '设备详情';
    disabledRef.value = true;
  }
});

/**
 * BasicForm绑定注册
 */
const [registerForm, { resetFields, validate, setFieldsValue }] = useForm({
  schemas: addFormSchemas,
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  disabled: disabledRef,
  baseColProps: { span: 12 },
  // labelWidth 走框架正道(勿用 labelCol.style.width:会拼出 calc(100%-undefined) 无效宽,label 全部塌成上下两行)
  // 110 = 六字 label(96)+冒号+必填星号余量;96 时"功能位置编码"占满容器把冒号挤出不可见
  labelWidth: 110,
  // 覆盖全局 componentSetting 的 wrapperCol sm:18(75%)——满宽后跨行备注的输入框才能右缘到行尾
  wrapperCol: { xs: 24, sm: 24, md: 24, lg: 24, xl: 24, xxl: 24 },
  // 两列紧凑布局:压缩行距保证 16 字段在屏内不滚动
  rowProps: { gutter: [12, 0] },
});

// 表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    await saveOrUpdate(values, isUpdateRef.value);
    closeModal();
    resetFields();
    emit('success');
    msg.success({ content: '操作成功' });
  } catch (error) {
    // 校验失败或接口报错,defHttp 已统一提示
  }
}
</script>

<style scoped>
/** 表单内容与弹窗左右边缘留出间距(jeecg modal body 自身 padding 为 0) */
.form-pad {
  padding: 4px 24px 0;
}
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}
:deep(.ant-picker) {
  width: 100%;
}
/** 下拉框默认文字颜色 */
:deep(.ant-select-selection-placeholder) {
  color: #bebebe !important;
}
/** 备注跨整行(只作用于备注所在的满宽行,span24 列):
 *  wrapper 在 block 容器里默认 inline-flex 收缩成内容宽,强制 flex 拉满;
 *  左缘 0 对齐第一列普通输入框,右收 0 对齐第二列输入框右缘(实测 0 差) */
:deep(.ant-col-24 .ant-input-affix-wrapper) {
  display: flex;
  width: auto;
  margin-left: 0;
  margin-right: 0;
}
</style>
