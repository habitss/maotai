<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn="!isConfirmed" :confirmLoading="confirmLoading">
    <BasicForm @register="registerForm" @submit="handleSubmit" style="padding: 20px">
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import { updateConfirmStatus } from "@/views/dataCalculation/serviceAgreement/ServiceAgreementException.api";

const { t } = useI18n();

const isAdd = ref(false);
const isDetail = ref(false);
const recordId = ref('');
const currentConfirmStatus = ref(0);
const title = ref<string>('确认状态');
const width = ref<number>(600);
const confirmLoading = ref(false);

const isConfirmed = computed(() => currentConfirmStatus.value === 1);

const emit = defineEmits(['success']);

const { createMessage: msg } = useMessage();

const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  await resetFields();
  isAdd.value = data.isAdd || false;
  isDetail.value = data.isDetail || false;
  
  if (data.record) {
    recordId.value = data.record.id;
    currentConfirmStatus.value = data.record.confirmStatus || 0;
    await setFieldsValue({
      confirmStatus: data.record.confirmStatus || 0,
      remark: data.record.remark || '',
      confirmFile: data.record.confirmFile || ''
    });
  }
  
  if (isConfirmed.value) {
    title.value = '确认状态（已确认）';
    updateSchema({
      field: 'confirmStatus',
      dynamicDisabled: () => true
    });
    updateSchema({
      field: 'confirmFile',
      dynamicDisabled: () => true,
      required: false
    });
    updateSchema({
      field: 'remark',
      dynamicDisabled: () => true
    });
  } else {
    title.value = '确认状态';
    updateSchema({
      field: 'confirmStatus',
      dynamicDisabled: () => false
    });
    updateSchema({
      field: 'confirmFile',
      dynamicDisabled: () => false,
      required: true
    });
    updateSchema({
      field: 'remark',
      dynamicDisabled: () => false
    });
  }
});

const confirmStatusFormSchemas = [
  {
    field: 'confirmStatus',
    component: 'RadioGroup',
    componentProps: {
      options: [
        { label: '未确认', value: 0 },
        { label: '已确认', value: 1 },
      ],
    },
    required: true,
    label: '确认状态',
    colProps: { span: 20 },
  },
  {
    field: 'confirmFile',
    component: 'JUpload',
    componentProps: {
      fileType: 'all',
    },
    required: true,
    helpMessage: '无限制上传',
    label: '附件',
    colProps: { span: 20 },
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    colProps: { span: 20 },
  },
];

const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  schemas: confirmStatusFormSchemas,
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  labelCol: { style: { width: '100px' } },
});

async function handleSubmit() {
  try {
    confirmLoading.value = true;
    const values = await validate();
    values.id = recordId.value;
    await updateConfirmStatus(values);
    closeModal();
    await resetFields();
    emit('success');
    msg.success({ content: '保存成功', key: 'saving' });
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
  } finally {
    confirmLoading.value = false;
  }
}
</script>

<style>
.jee-hidden {
  display: none !important;
}
</style>