<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1024" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="BestPracticeFillForm">
      <!-- 评审结果插槽 -->
      <template #shiftGroupResult="{ model }">
        <a-button :disabled = "!hasPermission('practice:mes3_best_practice_fill:shift_group_score')"
          type="link" @click="handleResultClick(model, 1, model.shiftGroupReportId, model.shiftGroupResult)">{{ model.shiftGroupAvgScore ?? '点击' }}</a-button>
      </template>
      <template #deptResult="{ model }">
        <a-button :disabled = "!hasPermission('practice:mes3_best_practice_fill:dept_score')"
          type="link" @click="handleResultClick(model, 2, model.deptReportId, model.deptResult)">{{ model.deptAvgScore ?? '点击' }}</a-button>
      </template>
      <template #factoryResult="{ model }">
        <a-button :disabled="model.deptResult != 1 || model.deptAvgScore < 18 || !hasPermission('practice:mes3_best_practice_fill:factory_score')"
          type="link" @click="handleResultClick(model, 3, model.factoryReportId, model.factoryResult)">{{ model.factoryAvgScore ?? '点击' }}</a-button>
      </template>

      <!--  部门插槽    -->
      <template #deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="'请选择提报部门'"
        />
      </template>

      <!--  改善类型插槽    -->
      <template #improvementTypeSlot="{ model, field }">
        <JDictSelectTag v-model:value="model[field]" 
          placeholder="请选择改善类型" 
          dictCode="mes3_best_practice_type where del_flag = 0, type_name, id" 
          @change="(e) => handleImprovementTypeChange(e, model)"
        />
      </template>
    </BasicForm>
    <!-- 评审 -->
    <BestPracticeConfigModal @register="registerConfigModal" @success="handleSuccess"></BestPracticeConfigModal>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../BestPracticeFill.data';
import { saveOrUpdate, deleteByFillId } from '../BestPracticeFill.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import BestPracticeConfigModal from '/@/views/dataCalculation/practice/config/components/BestPracticeConfigModal.vue'
import { useModal } from '/@/components/Modal';
import { useUserStore } from '/@/store/modules/user';
import dayjs from 'dayjs';
import {useMessage} from "@/hooks/web/useMessage";
import { usePermission } from '/@/hooks/web/usePermission'
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
const { hasPermission } = usePermission();

const userStore = useUserStore();
//消息提示组件注册
const { createMessage: msg, createConfirm } = useMessage();
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
// 临时id，处理评审数据绑定
const tempId = ref(dayjs().valueOf())
//历史数据
const oldImprovementType = ref();
//注册评审model
const [registerConfigModal, { openModal: openConfigModal }] = useModal();
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 8 },
  labelWidth: '120px',
  wrapperCol: {
    span: 24,
  },
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  oldImprovementType.value = data.record.improvementType;
  if (unref(isUpdate)) {
    //表单赋值
    await setFieldsValue({
      ...data.record,
    });
  }
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    if(!unref(isUpdate)){
      values.id = tempId.value;
    }
    values.factoryCode = userStore.factoryCode;
    //提交表单
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    handleSuccess(null, null, null);
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}

async function handleSuccess(fillType, avgScore, fillReportId) {
  if(fillType == 1){
    await setFieldsValue({ shiftGroupResult: '1', shiftGroupAvgScore: avgScore, shiftGroupReportId: fillReportId });
  } else if(fillType == 2){
    await setFieldsValue({ deptResult: '1', deptAvgScore: avgScore, deptReportId: fillReportId });
  } else if(fillType == 3){
    await setFieldsValue({ factoryResult: '1', factoryAvgScore: avgScore, factoryReportId: fillReportId });
  }
  emit('success');
}

//改善类型改变事件
function handleImprovementTypeChange(value, model){
  if(oldImprovementType.value != value && (model.shiftGroupReportId || model.deptReportId)){
    createConfirm({
      title: '提示',
      content: `即将修改改善类型，继续操作将会删除已有评审分数，是否继续？`,
      iconType: 'warning',
      okText: '删除',
      onOk: async () => {
        oldImprovementType.value = value;
        await deleteByFillId({fillId: unref(isUpdate) ? model.id : tempId.value, improvementType: value, isUpdate: isUpdate.value}, () => {
          model.shiftGroupReportId = null;
          model.shiftGroupResult = null;
          model.shiftGroupAvgScore = null;
          model.shiftGroupReview = null;
          model.deptReportId = null;
          model.deptResult = null;
          model.deptAvgScore = null;
          model.deptReview = null;
          model.factoryReportId = null;
          model.factoryResult = null;
          model.factoryAvgScore = null;
          model.factoryReview = null;
          handleSuccess(null, null, null);
        });
      },
      onCancel: () => {model.improvementType = oldImprovementType.value;},
    });
  } else {
    oldImprovementType.value = value;
  }
}
//评审结果点击事件
function handleResultClick(record, fillType, fillReportId, result){
  if(!record.improvementType){
    msg.warning('请先选择改善类型')
    return;
  }
  openConfigModal(true, {
    typeId: record.improvementType,
    fillType,
    fillReportId,
    fillId: unref(isUpdate) ? record.id : tempId.value,
    isFill: true,
    isUpdate: result == 1 ? true : false,
    showFooter: true,
  });
}
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>