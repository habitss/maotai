<template>
  <div style="width: 100%">
    <a-card :title="titleValue" class="custom-row-spacing" :bordered="false">
      <!-- 机台信息 -->
      <a-form ref="subForm" :model="form" layout="vertical" style="margin-bottom: 24px">
        <a-input hidden="hidden" v-model:value="form.id" />
        <a-row :gutter="24">
          <a-col :span="4" class="title_name">
            清洗标准:
          </a-col>
          <a-col :span="20">
            <a-textarea v-model:value="form.cleaningStandard" disabled />
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="4" class="title_name">
            清洗步骤:
          </a-col>
          <a-col :span="20">
            <a-textarea v-model:value="form.cleaningStep" disabled />
          </a-col>
        </a-row>
        <a-row :gutter="24" style="border: #ebf1fb 1px">
          <a-col :span="4" class="title_name">
            机台:
          </a-col>
          <a-col :span="4">
            <a-input v-model:value="form.copPointName"  disabled />
          </a-col>
          <a-col :span="4" class="title_name">
            清洗人:
          </a-col>
          <a-col :span="4">
            <a-select style="width:160px" v-model:value="form.clearBy" required="true" show-search  placeholder="请选择" :allowClear=true
                      :options="userList" :filter-option="handleFilterOption" :fieldNames="{ label: 'realname', value: 'id' }" :disabled="isReadOnly">
            </a-select>
          </a-col>
          <a-col :span="4" class="title_name">
            清洗日期:
          </a-col>
          <a-col :span="4">
            <a-date-picker  v-model:value="form.clearDate" required="true"  valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                            style="width: 100%;" :disabled="isReadOnly"></a-date-picker>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <template v-for="col in colValue" :key="col.id">
            <a-col v-if="col.fieldType == 4" :span="4" class="upload_name">
              <div class="upload_container">
                <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
                <div style="text-align: center;">
                  <JImageUpload :fileMax="1" v-model:value="form[col.id]"
                                @change="e => handleChange(e, col.id)" :disabled="isReadOnly"></JImageUpload>
                </div>
              </div>
            </a-col>
            <a-col v-if="col.fieldType == 3" :span="4" class="upload_name">
              <div class="upload_container">
                <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
                <div>
                  <a-input v-model:value="form[col.id]" @change="e => handleChange(e, col.id)" :disabled="isReadOnly" placeholder="请输入" />
                </div>
              </div>
            </a-col>
            <a-col v-if="col.fieldType == 2" :span="4" class="upload_name">
              <div class="upload_container">
                <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
                <div>
                  <a-date-picker  v-model:value="form[col.id]" @change="e => handleChange(e, col.id)" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                                  style="width: 100%;" :disabled="isReadOnly"></a-date-picker>
                </div>
              </div>
            </a-col>
            <a-col v-if="col.fieldType == 1" :span="4" class="upload_name">
              <div class="upload_container">
                <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
                <div>
                  <a-date-picker v-model:value="form[col.id]" @change="e => handleChange(e, col.id)" valueFormat="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss"
                    show-time style="width: 100%;" :disabled="isReadOnly" placeholder="请选择时间"></a-date-picker>
                  <!-- <a-input v-model:value="form[col.id]" @change="e => handleChange(e, col.id)" :disabled="isReadOnly" placeholder="请输入" /> -->
                </div>
              </div>
            </a-col>
          </template>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="4" class="title_name">
            检查人:
          </a-col>
          <a-col :span="4">
            <a-select style="width:160px" show-search v-model:value="form.checkBy" placeholder="请选择核查人" :allowClear=true
                      :options="userList" :filter-option="handleFilterOption" :fieldNames="{ label: 'realname', value: 'id' }" :disabled="isReadCheckOnly">
            </a-select>
          </a-col>
          <a-col :span="4" class="title_name">
            审核结果:
          </a-col>
          <a-col :span="4">
            <a-select style="width:160px" v-model:value="form.checkResult" :disabled="isReadCheckOnly" placeholder="请选择审核结果">
              <a-select-option :value="'0'">请选择审核结果</a-select-option>
              <a-select-option :value="'1'">OK</a-select-option>
              <a-select-option :value="'2'">NOK</a-select-option>
            </a-select>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="4" class="title_name">
            备注:
          </a-col>
          <a-col :span="20">
            <a-textarea v-model:value="form.checkRemark" :disabled="isReadCheckOnly" />
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="4" class="title_name">
            修改计划结果:
          </a-col>
          <a-col :span="4">
            <a-select style="width:160px" v-model:value="form.state" :disabled="!isReadOnly"  @change="e => handleSchedule(e)" placeholder="请选择">
              <a-select-option :value="-1">计划</a-select-option>
              <a-select-option :value="0">不合格</a-select-option>
              <a-select-option :value="1">已完成</a-select-option>
              <a-select-option :value="3">延迟完成</a-select-option>
              <a-select-option :value="6">因生产取消</a-select-option>
            </a-select>
          </a-col>
          
          <template v-if="submitData && submitData.createTime">
            <a-col :span="4" class="title_name">
              创建时间:
            </a-col>
            <a-col :span="4" class="pt-5px">
              {{ submitData.createTime }}
            </a-col>
          </template>
          
          <template v-if="submitData && submitData.updateTime">
            <a-col :span="4" class="title_name">
              最后修改时间:
            </a-col>
            <a-col :span="4" class="pt-5px">
              {{ submitData.updateTime }}
            </a-col>
          </template>
        </a-row>
      </a-form>

      <div style="text-align: center; margin-top: 24px">
        <a-button type="primary" size="large" @click="submitResult" :disabled="submitState">保存</a-button>
      </div>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import {reactive, ref} from 'vue';
import dayjs from "dayjs";
import {mes3UserListAll} from "@/api/common/api";
import {
  findSubmit,
  getConfigList
} from "@/views/dataCalculation/copManagement/copPlan/copPlanList.api";
import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUploadV2.vue';
import {
  processState,
  saveOrUpdateSubmit
} from "../copPlanList.api";
import {useMessage} from "@/hooks/web/useMessage";

//注册Message
const { createMessage: msg } = useMessage();
const emit = defineEmits(['close',]);
const isReadOnly = ref(); // 填报初始状态为非只读
const isReadCheckOnly =ref(); //审核初始状态为非只读
const submitState = ref();
const scheduleValue = ref();
const titleValue = ref();
const userList = ref([]);
const colValue = ref([]);
const submitData = ref();
const receiveRecord = (msg) => {
  scheduleValue.value = msg;
  let value = scheduleValue.value;
  titleValue.value = value.copPointName;
  setFormValue(value);
}

defineExpose({receiveRecord})

function handleFilterOption(input, option) {
  return option['realname'].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}

const form  = reactive<any>({
  id:undefined,
  clearDate: dayjs().format('YYYY-MM-DD'),
  cleaningStandard: undefined,
  cleaningStep: undefined,
  copPointId: undefined,
  copPointName: undefined,
  checkBy: undefined,
  clearBy: undefined,
  checkRemark: undefined,
  checkResult: undefined,
  copScheduleId:undefined
});

async function setFormValue(records){
  isReadOnly.value = false;
  submitState.value = false;
  isReadCheckOnly.value = false;
  let date = records.date;
  let month = records[date];
  let status = month.state;
  let param = {
    id: form.id,
    copScheduleId : records.id,
    planYear: records.planYear,
    planMonth: month.month,
    planWeek: month.num,
  }
  await processSubmit(records.copPointId);
  console.log(param)
  let submitValue = await findSubmit(param);
  submitData.value = submitValue.result;
  console.log(submitValue)
  if(null != submitValue.result){
    if(submitValue.result.checkResult == 1 || submitValue.result.checkResult == '1'){
      isReadOnly.value = !isReadOnly.value; // 切换只读状态
      isReadCheckOnly.value = !isReadCheckOnly.value;
    } else {
      if(status == '0'){
        isReadCheckOnly.value = true;
      }
    }
    if(null != submitValue.result.dataJson){
      let dataJson = JSON.parse(submitValue.result.dataJson);
      dataJson.forEach(function(value,index){
        let id = value.id
        let path = value.value
        form[id] = path
      })
    }
    form.id = submitValue.result.id;
    form.checkBy = submitValue.result.checkBy;
    form.clearBy = submitValue.result.clearBy;
    form.clearDate = submitValue.result.clearDate
    form.checkResult = submitValue.result.checkResult
    form.checkRemark = submitValue.result.checkRemark
  } else {
    if(status == '5'){
      isReadOnly.value = !isReadOnly.value; // 切换只读状态
    }
    form.checkBy = records.checkBy;
    form.clearBy = records.clearBy;
    form.clearDate = ''
    form.checkResult = ''
    form.checkRemark = ''
    form.state = ''
    form.id = ''
  }
  form.cleaningStandard = records.cleaningStandard;
  form.cleaningStep = records.cleaningStep;
  form.copPointName = records.copPointName;
  form.verifyResult = records.verifyResult;
  form.copPointId = records.copPointId;
  form.copScheduleId = records.id
}

async function processSubmit(value){
  let params = {
    dataId: value,
    column : 'sort',
    order : 'asc',
    pageNo:1,
    pageSize:999
  }
  let configData = await getConfigList(params);
  colValue.value = configData.records;
  colValue.value.forEach(function (value,index){
    let id = value.id
    form[id] = ''
  })
  await getUserList();
}

async function getUserList(){
  userList.value = await mes3UserListAll();
}

function handleChange(index, name){
  form[index] = name;
  Object.assign({}, form[index], form);
}

async function submitResult(){
  submitState.value = true;
  let scheduleSubmit ={
    id: '',
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    copScheduleId : '',
    planYear: '',
    planMonth: '',
    planWeek: '',
    checkBy: '',
    clearBy : '',
    clearDate : '',
    checkDate : '',
    dataJson: '',
    checkResult: '',
    checkRemark: '',
  }
  console.log(form.clearDate)
if(null != form.clearDate && '' != form.clearDate) {
    let date = scheduleValue.value.date;
    let month = scheduleValue.value[date];
    scheduleSubmit.copScheduleId = scheduleValue.value.id;
    scheduleSubmit.factoryCode = scheduleValue.value.factoryCode;
    scheduleSubmit.deptCode = scheduleValue.value.deptCode;
    scheduleSubmit.lineCode = scheduleValue.value.lineCode;
    scheduleSubmit.planYear = scheduleValue.value.planYear;
    scheduleSubmit.planMonth = month.month;
    scheduleSubmit.planWeek = month.num;
    scheduleSubmit.checkBy = form.checkBy;
    scheduleSubmit.clearBy = form.clearBy;
    scheduleSubmit.checkResult = form.checkResult;
    scheduleSubmit.clearDate = form.clearDate;
    scheduleSubmit.checkRemark = form.checkRemark;
    scheduleSubmit.id = form.id;
    let result = [];
    colValue.value.forEach(function (value, index) {
      let json = {
        id: '',
        name: '',
        type: '',
        value: ''
      }
      json.id = value.id;
      json.value = form[value.id];
      json.name = value.fieldName;
      json.type = value.fieldType;
      result.push(json);
    })
    scheduleSubmit.dataJson = JSON.stringify(result).toString();
    await saveOrUpdateSubmit(scheduleSubmit);
    //刷新外部表格
    emit('close');
  } else {
    submitState.value = false;
    msg.error({ content: '请选择清洗时间！', key: 'saving' });
  }
}

async function handleSchedule(value){
  let date = scheduleValue.value.date;
  let month = scheduleValue.value[date];
  let resultValue = {
    id: scheduleValue.value.id,
    result: value,
    num : month.num
  }
  await processState(resultValue);
  emit('close');
}

</script>

<style>
.ant-card {
  margin: 24px;
}
.ant-form-item {
  margin-bottom: 16px;
}
.ant-row {
  margin-bottom: 10px;
}
.title_name{
  text-align: center;
  padding: 5px;
  background-color: #ebf1fb;
  border: 1px solid #ccc;
}

.upload_name{ border: 1px solid #ccc;display: flex; justify-content: center; align-items: center; }
.upload_container{
  display: flex;
  flex-direction: column;
  margin-left: 10px;
}
</style>
