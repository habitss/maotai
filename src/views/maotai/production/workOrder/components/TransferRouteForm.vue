<template>
  <BasicModal
    @register="registerModal"
    @ok="handleSubmit"
    v-bind="$attrs"
    wrapClassName="wo-form-modal"
    :title="title"
  >
    <div class="form-pad">
      <BasicForm @register="registerForm" @submit="handleSubmit" />
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
import { nextTick, ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { addRoute, editRoute, getTanks, type TransferRouteItem } from '../WorkOrder.api';
import { defHttp } from '/@/utils/http/axios';
import { buildRouteSchemas } from '../TransferRoute.data';

// 声明Emits
const emit = defineEmits(['success']);
// 标题
const title = ref('');
// 是否编辑
const isUpdateRef = ref(false);
// 车间罐清单(设备台账,车间联动)
const tankOptions = ref<{ label: string; value: string }[]>([]);
// 卸车撬清单(MES3_SKID 字典实时查询,不走缓存——撬管理新增后立即可见)
const skidOptions = ref<{ label: string; value: string }[]>([]);

async function loadSkids() {
  try {
    const res = await defHttp.get({ url: '/mes3_mdm/dict/queryPageList', params: { groupCode: 'MES3_SKID', pageNo: 1, pageSize: 100 } });
    const rows = res?.records || res || [];
    skidOptions.value = rows.map((r: any) => ({ label: `${r.itemVal}(${r.itemKey})`, value: r.itemKey }));
  } catch (e) {
    console.error('撬清单加载失败', e);
  }
}

const { createMessage: msg } = useMessage();

async function loadTanks(workshopCode?: string) {
  tankOptions.value = [];
  if (!workshopCode) return;
  try {
    tankOptions.value = (await getTanks(workshopCode)) || [];
  } catch (e) {
    console.error('罐清单加载失败', e);
  }
}

const schemas = buildRouteSchemas(tankOptions, skidOptions, {
  onWorkshopChange: async () => {
    await nextTick();
    const values = getFieldsValue() as Recordable;
    // 车间切换:工艺/罐/撬全部重置,重载罐清单
    setFieldsValue({ processType: undefined, sourceTank: undefined, targetTank: undefined, skidCode: undefined });
    loadTanks(values?.workshopCode);
  },
  onProcessChange: async () => {
    await nextTick();
    // 工艺切换:罐/撬按新工艺显隐,清空已选
    setFieldsValue({ sourceTank: undefined, targetTank: undefined, skidCode: undefined });
  },
});

// 注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  const record: TransferRouteItem = data.record ? { ...data.record } : {};
  title.value = data.action === 'edit' ? '编辑转酒路径' : '新增转酒路径';
  isUpdateRef.value = data.action === 'edit';
  resetFields();
  setFieldsValue(record);
  loadTanks(record.workshopCode);
  loadSkids();
});

/**
 * BasicForm绑定注册
 */
const [registerForm, { resetFields, validate, setFieldsValue, getFieldsValue }] = useForm({
  schemas,
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  baseColProps: { span: 12 },
  labelWidth: 110,
  wrapperCol: { xs: 24, sm: 24, md: 24, lg: 24, xl: 24, xxl: 24 },
  rowProps: { gutter: [12, 0] },
});

// 表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    if (isUpdateRef.value) {
      await editRoute(values);
      msg.success({ content: '路径配置保存成功' });
    } else {
      await addRoute(values);
      msg.success({ content: '路径配置新增成功' });
    }
    closeModal();
    resetFields();
    emit('success');
  } catch (error) {
    // 校验失败或接口报错,defHttp 已统一提示
  }
}
</script>

<style scoped lang="less">
.form-pad {
  padding: 4px 24px 0;
}
:deep(.ant-form-item) {
  margin-bottom: 10px;
}
:deep(.ant-input-number) {
  width: 100%;
}
:deep(.ant-select-selection-placeholder) {
  color: #bebebe !important;
}
</style>
<style lang="less">
/** 同工单弹窗:上提+解除高度限制 */
.wo-form-modal .ant-modal {
  top: 30px;
}
.wo-form-modal .ant-modal-body .scrollbar__view > div[style*="max-height"] {
  max-height: none !important;
}
</style>
