<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1100" 
    :bodyStyle="{height: '60vh'}" :maskClosable="false" @ok="handleSubmit"
    @fullScreen="handleFullScreen"
  >
    <BasicForm @register="registerForm" name="MdmTaskForm">
      <!-- 开始步骤插槽 -->
      <template #startStep="{ model }">
        <a-row>
          <a-col :span="4" class="pr-2">
            <a-select v-model:value="model.startStepType" placeholder="请选择类型" :options="setpTypeArr" :defaultValue="'2'" :disabled="true" />
          </a-col>
          <a-col :span="4" class="pr-2">
            <JDictSelectTag v-model:value="model.startTriggerDevice" 
              placeholder="请选择取数设备" 
              :showChooseOption=false allow-clear
              :getPopupContainer="() => getPopupContainer()"
              :dictCode="`mes3_mdm_device where line_code = '${model.lineCode}' and del_flag = 0, device_name, id`"
              @change="(e) => {model.startTriggerPoint = null;}"
              showSearch
             >
            </JDictSelectTag>
          </a-col>
          <a-col :span="9" class="pr-2">
            <JDictSelectTag v-model:value="model.startTriggerPoint" 
              placeholder="请选择取数点位" 
              :showChooseOption=false allow-clear
              :getPopupContainer="() => getPopupContainer()"
              :dictCode="`mes3_pi_base_point where line_code = '${model.lineCode}' and device_id = '${model.startTriggerDevice}' and del_flag = 0, point_name, point_code`"
              @change="(e) => {model.startTriggerPoint = e;}"
              showSearch
             >
            </JDictSelectTag>
          </a-col>
          <a-col :span="4" class="pr-2">
            <a-select v-model:value="model.startTriggerConditionType" placeholder="请选择条件" allowClear :options="conditionArr" />
          </a-col>
          <a-col :span="3">
            <a-input v-model:value="model.startTriggerConditionValue" placeholder="请输入值" />
          </a-col>
        </a-row>
      </template>
      
      <!-- 结束步骤插槽 -->
      <template #endStep="{ model }">
        <a-row>
          <a-col :span="4" class="pr-2">
            <a-select v-model:value="model.endStepType" placeholder="请选择类型" :options="setpTypeArr" :defaultValue="'2'" :disabled="true" />
          </a-col>
          <a-col :span="4" class="pr-2">
            <JDictSelectTag v-model:value="model.endTriggerDevice" 
              placeholder="请选择取数设备" 
              :showChooseOption=false allow-clear
              :getPopupContainer="() => getPopupContainer()"
              :dictCode="`mes3_mdm_device where line_code = '${model.lineCode}' and del_flag = 0, device_name, id`"
              @change="(e) => {model.endTriggerPoint = null;}"
              showSearch
             >
            </JDictSelectTag>
          </a-col>
          <a-col :span="9" class="pr-2">
            <JDictSelectTag v-model:value="model.endTriggerPoint" 
              placeholder="请选择取数点位" 
              :showChooseOption=false allow-clear
              :getPopupContainer="() => getPopupContainer()"
              :dictCode="`mes3_pi_base_point where line_code = '${model.lineCode}' and device_id = '${model.endTriggerDevice}' and del_flag = 0, point_name, point_code`"
              @change="(e) => {model.endTriggerPoint = e;}"
              showSearch
            >
            </JDictSelectTag>
          </a-col>
          <a-col :span="4" class="pr-2">
            <a-select v-model:value="model.endTriggerConditionType" placeholder="请选择条件" allowClear :options="conditionArr" />
          </a-col>
          <a-col :span="3">
            <a-input v-model:value="model.endTriggerConditionValue" placeholder="请输入值" />
          </a-col>
        </a-row>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive, ref, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../MdmTask.data';
import { saveOrUpdate } from '../MdmTask.api';
import { cloneDeep } from 'lodash-es';
import { CheckItemTypeEnum } from '/@/enums/planEnum';
import { TaskTypeEnum } from '/@/enums/taskEnum';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import { configList } from '../MdmTask.api';
import { getPopupContainer } from '/@/utils';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
const oldTaskKindCode = ref();
const title = ref('');

//取数步骤-值类型
const setpTypeArr = reactive<Record<string, any>>([
  { value: '1', label: '触发时间' },
  { value: '2', label: '步骤号' },
  { value: '3', label: 'PI' }
]);
//计算信号-条件
const conditionArr = reactive<Record<string, any>>([
  { value: '1', label: '进入' },
  { value: '2', label: '结束' }
]);

//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField, getFieldsValue }] = useForm({
  labelWidth: 150,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 },
  wrapperCol: {span: 20}
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter });

  let formData = data?.record || {};
  //如果是新增，将父级数据处理后填入表格
  if (!unref(isUpdate) && !data.isCopy) {
    title.value = '新增';
    formData = cloneDeep(data.parentData);
    formData.deviceId = formData.id;
    formData.deviceId_dictText = formData.deviceName;
    formData.checkTaskType = formData.checkTaskType;
    //如果是交接班类型，默认循环任务
    if(formData.checkTaskType == CheckItemTypeEnum.TYPE_RECORD){
      formData.taskKindCode = '0';
    }
    delete formData.id;
  } else {
    oldTaskKindCode.value = formData.taskKindCode;
    title.value = '编辑';
    formData.isCopy = false;
    if(data.isCopy){
      formData.isCopy = true;
      delete formData.id;
      title.value = '复制';
    }
    //自动触发任务
    if(formData.taskKindCode == 10){
      await getConfig(formData);
    }
    formData.loopTriggerId = formData.loopTriggerId ? String(formData.loopTriggerId) : null;
  }
  //表单赋值
  await setFieldsValue({
    ...formData,
  });
});

async function getConfig(formData){
  //获取配置数据
  const configRes = await configList({taskId: formData.id});
  console.log('formData :>> ', formData);
  if(configRes && configRes.length > 0){
    for(const item of configRes){
      if(!item.triggerDevice){
        item.triggerDevice = formData.deviceId+"";
      }
      let prefix = item.triggerConfigType == 1 ? 'startT' : 'endT';
      for(const key in item){
        if(key == 'id'){
          formData[item.triggerConfigType == 1 ? 'startTriggerId' : 'endTriggerId'] = item[key];
        }else{
          formData[prefix+key.substring(1)] = item[key];
        }
      }
    }
  }
  console.log('formData222 :>> ', formData);
  console.log('configRes :>> ', configRes);
}

//表单提交事件
async function handleSubmit() {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    if(values.loopTriggerId && values.loopTriggerId.label){
      values.loopTriggerName = values.loopTriggerId.label;
      values.loopTriggerId = values.loopTriggerId.value;
    }
    values.pushJson = null;
    for(let key in TaskTypeEnum){ 
      if(TaskTypeEnum[key] == values.loopTriggerId){
        let pushJson = {};
        if(TaskTypeEnum.TASK61 == values.loopTriggerId){  
          pushJson = {'pushDate_day': values['pushDate_day']}
        } else if(TaskTypeEnum.TASK55 == values.loopTriggerId) {
          pushJson = {'pushDate_hour': values['pushDate_hour']}
        } else if(TaskTypeEnum.TASK58 == values.loopTriggerId) {
          pushJson = {'pushDate_week': values['pushDate_week']}
        } else {
          pushJson = {
            pushDate1: values['pushDate1'], 
            pushDate2: values['pushDate2'], 
            pushDate3: values['pushDate3'], 
            pushDate4: values['pushDate4']
          }
        }
        values.pushJson = JSON.stringify(pushJson);
        break;
      }
    }
    //自动触发任务
    if(values.taskKindCode == 10){
      let autoTriggerConfigList = [] as any;
      let start = {
        triggerConfigType: 1, 
        triggerStepType: 2
      };
      let end = {
        triggerConfigType: 2, 
        triggerStepType: 2
      } as any;
      //获取配置数据
      for(const key in values){
        if(key.startsWith('start')){
          if(key == 'startTriggerId'){
            start['id'] = values[key];
          }else{
            start[key.replace('startT', 't')] = values[key];
          }
        }else if(key.startsWith('end')){
          if(key == 'endTriggerId'){
            end['id'] = values[key];
          }else{
            end[key.replace('endT', 't')] = values[key];
          }
        }
      }
      autoTriggerConfigList.push(start);
      autoTriggerConfigList.push(end);
      values.autoTriggerConfig = end.triggerPoint ? 2 : 1;
      values.autoTriggerConfigList = autoTriggerConfigList;
    }else{
      values.autoTriggerRangeTime = null;
    }
    values.autoTriggerLastTime = null;
    //如果从自动触发改为非自动触发任务，则删除自动触发任务记录
    values.delAutoRecord = false;
    if(oldTaskKindCode.value == 10 && values.taskKindCode != 10){
      values.delAutoRecord = true;
    }
    //提交表单
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
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

function handleFullScreen(isFull){
  let height = isFull ? 'calc(100vh - 104px)' : '70vh';
  setModalProps({
    bodyStyle: {height},
  })
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