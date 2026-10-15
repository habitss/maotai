<template>
  <BasicModal
    @register="registerModal"
    @ok="handleSubmit"
    v-bind="$attrs"
    wrapClassName="wo-form-modal"
    :title="title"
    :showOkBtn="!disabledRef"
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
import { addOrder, editOrder, getPaths, getTanks, type TransferPath } from '../WorkOrder.api';
import { buildFormSchemas } from '../WorkOrder.data';

// 声明Emits
const emit = defineEmits(['success']);
// 标题
const title = ref('');
// 禁用(详情态)
const disabledRef = ref(false);
// 是否编辑
const isUpdateRef = ref(false);
// 转酒路径候选(车间+工艺+源/目标罐联动刷新,全部来自配置表)
const pathOptions = ref<TransferPath[]>([]);
// 车间罐清单(设备台账 tank_config_id 非空,车间联动刷新)
const tankOptions = ref<{ label: string; value: string }[]>([]);

const { createMessage: msg } = useMessage();

// 车间选定后加载该车间罐清单
async function loadTanks(workshopCode?: string) {
  tankOptions.value = [];
  if (!workshopCode) return;
  try {
    tankOptions.value = (await getTanks(workshopCode)) || [];
  } catch (e) {
    console.error('罐清单加载失败', e);
  }
}

// 转酒路径联动:车间/工艺确定后(卸车/装车)或源罐+目标罐选齐后(倒罐/灌装)拉取配置候选。
// 无兜底:无候选即空,唯一候选也不自动选中,必须人工选择;已选值不在新候选中才清空。
async function refreshPaths() {
  await nextTick();
  const values = getFieldsValue() as Recordable;
  pathOptions.value = [];
  if (!values?.workshopCode || !values?.processName) return;
  if (values.processName !== '卸车工艺' && values.processName !== '装车工艺' && (!values.sourceTank || !values.targetTank)) return;
  try {
    pathOptions.value = (await getPaths({
      workshopCode: values.workshopCode,
      processName: values.processName,
      sourceTank: values.sourceTank,
      targetTank: values.targetTank,
    })) || [];
    if (values.transferPath && pathOptions.value.length && !pathOptions.value.some((p) => p.code === values.transferPath)) {
      setFieldsValue({ transferPath: undefined });
    }
  } catch (e) {
    pathOptions.value = [];
  }
}

const schemas = buildFormSchemas(pathOptions, tankOptions, {
  onTankChange: refreshPaths,
  onProcessChange: async () => {
    await nextTick();
    // 工艺切换后原路径不再匹配,清空重选
    setFieldsValue({ transferPath: undefined });
    refreshPaths();
  },
  onWorkshopChange: async () => {
    await nextTick();
    // 车间切换:罐清单/工艺/罐/路径全部重置
    const values = getFieldsValue() as Recordable;
    setFieldsValue({ processName: undefined, sourceTank: undefined, targetTank: undefined, transferPath: undefined });
    pathOptions.value = [];
    loadTanks(values?.workshopCode);
  },
});

// 注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  pathOptions.value = [];
  // 重置表单数据
  resetFields();
  // 设置表单数据(时间字段转展示格式,后端返回 ISO 带 T)
  const record = data.record ? { ...data.record } : {};
  if (record.startTime) record.startTime = String(record.startTime).replace('T', ' ').slice(0, 19);
  if (record.endTime) record.endTime = String(record.endTime).replace('T', ' ').slice(0, 19);
  setFieldsValue(record);
  if (data.action === 'add') {
    title.value = '新增工单';
    isUpdateRef.value = false;
  } else if (data.action === 'edit') {
    title.value = '编辑工单';
    isUpdateRef.value = true;
    loadTanks(record.workshopCode);
    refreshPaths();
  } else if (data.action === 'detail') {
    title.value = '工单详情';
    isUpdateRef.value = true;
    disabledRef.value = true;
    loadTanks(record.workshopCode);
    refreshPaths();
  }
});

/**
 * BasicForm绑定注册
 */
const [registerForm, { resetFields, validate, setFieldsValue, getFieldsValue }] = useForm({
  schemas,
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  disabled: disabledRef,
  baseColProps: { span: 12 },
  // labelWidth/两列紧凑布局与设备台账表单一致(勿用 labelCol.style.width)
  labelWidth: 110,
  wrapperCol: { xs: 24, sm: 24, md: 24, lg: 24, xl: 24, xxl: 24 },
  rowProps: { gutter: [12, 0] },
});

// 表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    if (isUpdateRef.value) {
      await editOrder(values.id, values);
      msg.success({ content: `工单 ${values.orderNo} 信息保存成功` });
    } else {
      const created = await addOrder(values);
      msg.success({ content: `工单 ${created.orderNo} 已创建` });
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
/** 表单内容与弹窗左右边缘留出间距(jeecg modal body 自身 padding 为 0) */
.form-pad {
  padding: 4px 24px 0;
}
/** 行距压缩:槽车工艺全字段(约10行)在屏内放完,不出现弹窗内滚动 */
:deep(.ant-form-item) {
  margin-bottom: 10px;
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
/** 备注跨整行输入框撑满 */
:deep(.ant-col-24 .ant-input-affix-wrapper) {
  display: flex;
  width: auto;
  margin-left: 0;
  margin-right: 0;
}
</style>
<style lang="less">
/** 弹窗容器上提:antd 默认 top:100px 会把 body 可用高度压到 ~400px(视口-2×top-头尾)触发滚动。
 *  modal 被 teleport 到 body,scoped deep 命不中,须全局块;wrapClassName 落在 .ant-modal-wrap 上 */
.wo-form-modal .ant-modal {
  top: 30px;
}
/** ModalWrapper 的 setModalHeight 以打开瞬间(字段少)算出的 max-height 钉死内联样式,
 *  选中工艺字段变多后不重算 → 隐藏滚动。直接解除该层高度限制,让内容自然撑开(整弹窗距顶30px不会出屏) */
.wo-form-modal .ant-modal-body .scrollbar__view > div[style*="max-height"] {
  max-height: none !important;
}
</style>
