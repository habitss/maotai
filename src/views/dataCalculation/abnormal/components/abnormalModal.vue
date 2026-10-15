<template>
  <BasicModal :title="title" v-bind="$attrs" @register="registerModal" destroyOnClose width="80vw" :minHeight="300" 
    :bodyStyle="{height: '68vh'}" :maskClosable="false" @fullScreen="handleFullScreen"
    @ok="handleOk">
    <div class="ml-4">
      <a-button v-if="!isUpdate" class="mr-1" type="primary" @click="handleOk(true)" :loading="saveLoading">保存</a-button>
      <a-button v-if="isUpdate" class="mr-1" type="primary" @click="deleteData">删除</a-button>
      <a-button type="primary" @click="handleSchedule">过程记录</a-button>
    </div>
    <BasicForm @register="registerForm" style="margin-top: 20px; padding-right: 20px;">
      <template #evaluate="{ model, field }">
        <a-button type="link" :style="{color : !model[field] ? '#000' : model[field] == 1 ? 'green' : 'red'}" @click="handleEvaluateClick(model, field)">{{ !model[field] ? '评价' : model[field] == 1 ? '合格' : '不合格' }}</a-button>
        <EvaluateModal @register="evaluateModal" @success="evaluateSuccess"></EvaluateModal>
      </template>

      <template #abnormalAnalysis="{ model, field }">
        <a-form-item :name="field">
          <template #label>
            <a-button v-if="!model.id" type="link" @click="handAbnormalClick" style="padding: 0;">异常点</a-button>
            <span v-else>异常点</span>
          </template>
          <div :class="!model.id ? 'textarea_class' : 'textarea_class textarea_disabled_class'">
            <a-textarea v-model:value="model[field]" :bordered="false" :disabled="model.id" :auto-size="true" :placeholder="!model[field] && model.id ? '': '点此处可手动输入异常原因'" />
            <div v-for="(v, i) in abnormalAnalysisTextArr" style="padding: 0 10px;">
              {{ v }}
            </div>
          </div>
        </a-form-item>
      </template>
    </BasicForm>
    <a-divider>5WHY分析</a-divider>
    <FiveWhyList ref="fiveWhyRef" @close="fiveWhyClose"></FiveWhyList>

    <!-- 异常点抽屉组件 -->
    <Drawer @register="register" @closeDrawer="handleCloseDrawer" :showFooter="true" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { nextTick, ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { addFormSchemas } from '../abnormal.data'
import { saveData, queryData, deleteOne } from '../abnormal.api'
import FiveWhyList from '/@/views/dataCalculation/fiveWhy/components/fiveWhyList.vue';
import { useDrawer } from '/@/components/Drawer';
import EvaluateModal from './evaluateModal.vue';
import Drawer from './PointDrawer.vue';
//消息提示
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import { cloneDeep } from 'lodash-es';
//异常点抽屉组件
const [register, { openDrawer }] = useDrawer();
//评价model
const [evaluateModal, { openModal }] = useModal();
//消息组件
const { createConfirm } = useMessage();
//emit组件
const emit = defineEmits(['success', 'openScheduleModal']);
//保存按钮loading
const saveLoading = ref(false);

//色区
const COLOR_ENUM = {
  green: '绿区',
  yellow: '黄区',
  red: '红区',
  grey: '灰区',
};

//5w组件ref
const fiveWhyRef = ref();
const title = ref("");
const isUpdate = ref<boolean>(false);
const dataParams = ref();
const baseData = ref({
  tableName: '',
  tableCode: '',
  fieldName: '',
  fieldCode: '',
  dataId: '',
  actualValue: '',
  piData: '',
  problemType: '',
  abnormalTime: '',
  state: '',
  abnormalDescribe: '',
  abnormalAnalysis: '',
  pointRecordList: [] as any,
  evaluationList: [] as any,
}) as any;

const abnormalAnalysisTextArr = ref([] as any);
let model: Nullable<Recordable> = null;
const [registerForm, { resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  //注册表单列
  schemas: addFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  //表格label宽度
  labelCol: { style: { width: '140px' } },
  //控制组件宽度占比
  wrapperCol: {
    xs: 24,
    sm: 24,
    md: 24,
    lg: 24,
    xl: 24,
    xxl: 24,
  },
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  title.value = data.params?.fieldName;
  baseData.value = cloneDeep(data.params);
  //获取页面数据
  dataParams.value = cloneDeep(data.params);
  //获取页面数据
  await getData();
  //调用5w组件
  initFiveWhy();
  initAbnormalAnalysisText();
});

async function getData(){
  baseData.value.evaluationList = [];
  //重置表单
  await resetFields();
  //获取后台数据
  const res = await queryData({ tableCode: dataParams.value.tableCode, dataId: dataParams.value.dataId, fieldCode: dataParams.value.fieldCode });
  if (res?.id) {
    isUpdate.value = true;
    await setFieldsValue({
      ...dataParams.value,
      ...res,
    });
    model = res
    baseData.value = res
    baseData.value.actualValue = dataParams.value.actualValue
    baseData.value.evaluationList = res.evaluationList
    baseData.value.pointRecordList = res.pointRecordList
;
  } else {
    dataParams.value.abnormalDescribe = `pi名称：${dataParams.value.fieldName}，目标值：${dataParams.value.standardValue??''}，实际值：${dataParams.value.actualValue??''}。颜色为：${COLOR_ENUM[dataParams.value.color] || ''}`;
    await setFieldsValue({
      ...dataParams.value,
    });
    model = dataParams.value;
    isUpdate.value = false;
  }
  setModalProps({ confirmLoading: false, showCancelBtn: true, showOkBtn: false, cancelText: '关闭' });
  setEvaluate();
}

async function handleOk(v) {
  try {
    saveLoading.value = true;
    let values = await validate();
    baseData.value.piData = values.actualValue
    baseData.value.problemType = values.problemType
    baseData.value.abnormalTime = values.abnormalTime
    baseData.value.state = values.state
    baseData.value.abnormalDescribe = values.abnormalDescribe
    baseData.value.abnormalAnalysis = values.abnormalAnalysis
    baseData.value.teamEvaluation = values.teamEvaluation
    baseData.value.workshopEvaluation = values.workshopEvaluation
    baseData.value.abnormalType = values.abnormalType
    setModalProps({ confirmLoading: true });
    // 提交表单
    await saveData(baseData.value);
    getData();
    //刷新列表
    // emit('success');
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    saveLoading.value = false;
  }
}

function deleteData(v) {
  createConfirm({
    title: '删除当前分析',
    content: `确定要删除当前分析吗？`,
    iconType: 'warning',
    onOk: async function () {
      await deleteOne(model?.id);
      abnormalAnalysisTextArr.value = [];
      getData();
      // emit('success');
    },
  });
}

function handleSchedule() {
  emit('openScheduleModal', baseData.value)
}

//评价按钮点击事件
function handleEvaluateClick(model, field) {
  openModal(true, {
    model,
    field,
  });
}

//设置评价
async function evaluateSuccess(evaluate) {
  if(baseData.value.evaluationList){
    baseData.value.evaluationList.push(evaluate);
  }else{
    baseData.value.evaluationList = [evaluate];
  }
  setEvaluate();
}

async function setEvaluate() {
  for (const item of baseData.value.evaluationList ?? []) {
    if (item.evaluationType == '1') {
      await setFieldsValue({
        teamEvaluation: item.evaluationResult,
      })
    } else if (item.evaluationType == '2') {
      await setFieldsValue({
        workshopEvaluation: item.evaluationResult,
      })
    } else if (item.evaluationType == '3') {
      await setFieldsValue({
        employeeEvaluation: item.evaluationResult,
      })
    }
  }
}

//异常点点击事件
function handAbnormalClick() {
  openDrawer(true,{
    queryParams: {
      tableCode: dataParams.value.tableCode,
      fieldCode: dataParams.value.fieldCode,
    }
  });
}

//异常点抽屉关闭事件
function handleCloseDrawer(pointList) {
  baseData.value.pointRecordList = pointList;
  initAbnormalAnalysisText();
}

//初始化异常分析文本内容
function initAbnormalAnalysisText() {
  let pointArr = [] as any;
  for (const item of baseData.value.pointRecordList ?? []) {
    pointArr.push(item.pointName + ' 标准值：' + (item.standardValue ?? '') + '；实际值：' + (item.actualValue ?? '') + '；原因分析：' + (item.reasonAnalysis ?? '')+'。')
  }
  abnormalAnalysisTextArr.value = pointArr;
}

//5w组件保存后的回调
function fiveWhyClose(){
  initFiveWhy();
  emit('success');
}
//初始化5w组件数据
function initFiveWhy(){
  //调用5w组件
  nextTick(() => {
    fiveWhyRef.value.init(dataParams.value);
  })
}

function handleFullScreen(isFull){
  let height = isFull ? 'calc(100vh - 104px)' : '68vh';
  setModalProps({
    bodyStyle: {height},
  })
}
</script>

<style scoped>
.textarea_class {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}
.textarea_disabled_class {
  background: #F7F7F7;
  color: #707272;
}

:deep(.ant-input[disabled]),
:deep(.ant-select-disabled),
:deep(.ant-picker-disabled .ant-picker-input > input) {
  color: #707272 !important;
  -webkit-text-fill-color: #707272 !important;
}
</style>