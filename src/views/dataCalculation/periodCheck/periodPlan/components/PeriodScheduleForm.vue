<template>
  <div style="width: 100%">
  <a-card :title="titleValue" class="custom-row-spacing" :bordered="false">
    <!-- 机台信息 -->
    <a-form ref="subForm" layout="vertical" style="margin-bottom: 24px">
      <a-input hidden="hidden" v-model:value="formData.id" />
      <a-row :gutter="24">
        <a-col :span="4" class="title_name">
          检查方式:
        </a-col>
        <a-col :span="20">
          <a-textarea v-model:value="formData.verificationMethod" placeholder="检查方式" disabled />
        </a-col>
      </a-row>
      <a-row :gutter="24">
        <a-col :span="4" class="title_name">
          说明:
        </a-col>
        <a-col :span="20">
          <a-textarea v-model:value="formData.explainType" placeholder="检查说明" disabled />
        </a-col>
      </a-row>
      <a-row :gutter="24" style="border: #ebf1fb 1px">
        <a-col :span="4" class="title_name">
          仪器名称:
        </a-col>
        <a-col :span="4">
            <a-input v-model:value="formData.apparatusName"  disabled />
        </a-col>
        <a-col :span="4" class="title_name">
          仪器型号:
        </a-col>
        <a-col :span="4">
          <a-input  v-model:value="formData.apparatusType" disabled />
        </a-col>
        <a-col :span="4" class="title_name">
          允许误差:
        </a-col>
        <a-col :span="4">
            <a-input v-model:value="formData.permissibleError"  disabled />
        </a-col>
      </a-row>
      <a-row :gutter="24" style="border: #ebf1fb 1px">
        <a-col :span="4" class="title_name">
          核查人:
        </a-col>
        <a-col :span="4">
            <a-select style="width:160px" show-search  v-model:value="formData.checkUser" placeholder="请选择" :allowClear=true
                      :rules="[{ required: true, message: '请选择核查人' }]"
                      :options="userList" :filter-option="handleFilterOption" :fieldNames="{ label: 'realname', value: 'id' }" :disabled="isReadOnly">
            </a-select>
        </a-col>
        <a-col :span="4" class="title_name">
          检查日期:
        </a-col>
        <a-col :span="4">
<!--          <a-date-picker  v-model:value="formData.checkDate" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"-->
<!--                          style="width: 100%;" :disabled="isReadOnly"></a-date-picker>-->
          <a-date-picker show-time placeholder="检查日期" v-model:value="formData.checkDate"
                         :rules="[{ required: true, message: '请选择检查日期' }]"
                         valueFormat="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" :disabled="isReadOnly" />
        </a-col>
        <a-col :span="4" class="title_name">
          检查结果:
        </a-col>
        <a-col :span="4">
          <a-select style="width:160px" v-model:value="formData.checkResult" :disabled="isReadOnly" placeholder="请选择检查结果">
            <a-select-option :value="0">请选择检查结果</a-select-option>
            <a-select-option :value="1">OK</a-select-option>
            <a-select-option :value="2">NOK</a-select-option>
          </a-select>
        </a-col>
      </a-row>
      <a-row :gutter="24">
        <template v-for="col in colValue" :key="col.id">
          <a-col v-if="col.fieldType == 5" :span="12" class="upload_name">
            <div class="upload_container">
              <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
              <div style="text-align: center;">
                <JUpload :fileMax="5" v-model:value="formData[col.id]"
                              @change="e => handleChange(e, col.id)" :disabled="isReadOnly"></JUpload>
              </div>
            </div>
          </a-col>
          <a-col v-if="col.fieldType == 4" :span="4" class="upload_name">
            <div class="upload_container">
              <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
              <div style="text-align: center;">
                <JImageUpload :fileMax="1" v-model:value="formData[col.id]"
                              @change="e => handleChange(e, col.id)" :disabled="isReadOnly"></JImageUpload>
              </div>
            </div>
          </a-col>
          <a-col v-if="col.fieldType == 3" :span="4" class="upload_name">
            <div class="upload_container">
              <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
              <div>
                <a-input v-model:value="formData[col.id]" @change="e => handleChange(e, col.id)" :disabled="isReadOnly" placeholder="请输入" />
              </div>
            </div>
          </a-col>
          <a-col v-if="col.fieldType == 2" :span="4" class="upload_name">
            <div class="upload_container">
              <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
              <div>
                <a-date-picker  v-model:value="formData[col.id]" @change="e => handleChange(e, col.id)" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                                style="width: 100%;" :disabled="isReadOnly"></a-date-picker>
              </div>
            </div>
          </a-col>
          <a-col v-if="col.fieldType == 1" :span="4" class="upload_name">
            <div class="upload_container">
              <div style="text-align: center; line-height: 40px">{{ col.fieldName}}</div>
              <div>
                <a-date-picker v-model:value="formData[col.id]" @change="e => handleChange(e, col.id)" valueFormat="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss"
                  show-time style="width: 100%;" :disabled="isReadOnly" placeholder="请选择时间"></a-date-picker>
                <!-- <a-input v-model:value="formData[col.id]" @change="e => handleChange(e, col.id)" :disabled="isReadOnly" placeholder="请输入" /> -->
              </div>
            </div>
          </a-col>
        </template>
      </a-row>
      <a-row :gutter="24">
        <a-col :span="4" class="title_name">
          确认人:
        </a-col>
        <a-col :span="4">
          <a-select style="width:160px" show-search v-model:value="formData.verifyUser" placeholder="请选择核查人" :allowClear=true
                    :options="userList" :filter-option="handleFilterOption" :fieldNames="{ label: 'realname', value: 'id' }" :disabled="isReadCheckOnly">
          </a-select>
        </a-col>
          <a-col :span="4" class="title_name">
            核查结果:
          </a-col>
        <a-col :span="4">
            <a-select style="width:160px" v-model:value="formData.verifyResult" :disabled="isReadCheckOnly" placeholder="请选择核查结果">
              <a-select-option :value="0">请选择核查结果</a-select-option>
              <a-select-option :value="1">OK</a-select-option>
              <a-select-option :value="2">NOK</a-select-option>
            </a-select>
        </a-col>
      </a-row>
      <a-row :gutter="24">
        <a-col :span="4" class="title_name">
          备注:
        </a-col>
        <a-col :span="20">
          <a-textarea v-model:value="formData.verifyRemark" :disabled="isReadCheckOnly"   />
        </a-col>
      </a-row>
      <a-row :gutter="24">
        <a-col :span="4" class="title_name">
          修改计划结果:
        </a-col>
        <a-col :span="4">
          <a-select style="width:160px" v-model:value="formData.state" :disabled="!isReadOnly" @change="e => handleSchedule(e)" placeholder="请选择">
            <a-select-option value="-1">计划</a-select-option>
            <a-select-option value="0">不合格</a-select-option>
            <a-select-option value="1">已完成</a-select-option>
            <a-select-option value="3">延迟完成</a-select-option>
            <a-select-option value="6">因生产取消</a-select-option>
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
      <a-button type="primary" @click="submitResult" :loading="isSaving" :disabled="isSaving">保存</a-button>
      <a-button
        style="background-color: red; color: white; margin-left: 10px;"
        @click="handleOpenConfigModal"
      >历史核查记录</a-button>
    </div>
  </a-card>
  </div>
  <ActionPlanTrackForm ref="actionPlanRef" @success="handleSuccess" :initRecord="false" :isShowTopForm="false" :isShowSubmitBtn="false"></ActionPlanTrackForm>
  <PeriodSubmitHistoryModal @register="registerHistoryModal" @success="handleSuccess"  :destroyOnClose="true"/>
</template>

<script lang="ts" setup>
import {reactive, ref} from 'vue';
import dayjs from "dayjs";
import {mes3UserListAll} from "@/api/common/api";
import {getConfigList} from "../PeriodScheduleList.api";
import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUploadV2.vue';
import JUpload from '/@/components/Form/src/jeecg/components/JUpload/JUpload.vue';
import {
  processState,
  saveOrUpdateSubmit
} from "@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.api";
import {findSubmit} from "@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.api";
import { list as actionList } from '@/views/dataCalculation/actionPlan/track/ActionPlanTrack.api';
import { useUserStore } from '/@/store/modules/user';
import ActionPlanTrackForm from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackForm.vue';
import {useMessage} from "@/hooks/web/useMessage";
import {useModal} from "@/components/Modal";
import PeriodSubmitHistoryModal
  from "@/views/dataCalculation/periodCheck/periodPlan/components/PeriodSubmitHistoryModal.vue";

const emit = defineEmits(['close']);

const isReadOnly = ref(); // 填报初始状态为非只读
const isReadCheckOnly =ref(); //审核初始状态为非只读
const subForm = ref();
const scheduleValue = ref();
const titleValue = ref();
const userList = ref([]);
const colValue = ref([]);
const userInfo = useUserStore();
const submitData = ref();
// 保存状态
const isSaving = ref(false);
//行动计划
const actionPlanRef = ref();
//注册Message
const { createMessage: msg } = useMessage();

const formData = reactive<any>({
  checkDate: dayjs().format('YYYY-MM-DD'),
  verificationMethod: undefined,
  explainType: undefined,
  apparatusId: undefined,
  apparatusName: undefined,
  apparatusType: undefined,
  checkUser: undefined,
  verifyUser: undefined,
  permissibleError: undefined,
  verifyResult: undefined,
  verifyRemark: undefined,
  checkResult: undefined,
});
const receiveRecord = (msg) => {
  scheduleValue.value = msg;
  let value = scheduleValue.value;
  formData.state = '';
  titleValue.value = value.apparatusName;
  setFormValue(value);
}

defineExpose({receiveRecord})

const [registerHistoryModal, { openModal}] = useModal();


async function initAction(data){
  console.log('------======',data)
  //获取数据
  let datas = [];
  if(data.id){
    const {records} = await actionList({pageSize: 999, dataId: data?.id });
    console.log(records)
    datas = records;
  }
  //初始化
  await actionPlanRef.value.otherPageInit({}, datas, true);
}


function handleFilterOption(input, option) {
  return option['realname'].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}
async function setFormValue(records){
  isReadOnly.value = false;
  isReadCheckOnly.value = false;
  await initAction(records);
  let date = records.date;
  let month = records[date];
  let status = month.state;
  let param = {
    id: formData.id,
    periodScheduleId : records.id,
    planYear: month.year,
    planMonth: month.month,
    planWeek: month.num,
  }
  await processSubmit(records.apparatusId);
  let submitValue = await findSubmit(param);
  submitData.value = submitValue.result;
  if(null != submitValue.result){
    if(submitValue.result.verifyResult == 1){
      isReadOnly.value = !isReadOnly.value; // 切换只读状态
      isReadCheckOnly.value = !isReadCheckOnly.value;
    } else {
      if(status == '0'){
        isReadCheckOnly.value = true;
      }
    }
    let dataJson = JSON.parse(submitValue.result.dataJson);
    dataJson.forEach(function(value,index){
      let id = value.id
      let path = value.value
      formData[id] = path
    })
    formData.checkUser = submitValue.result.checkUser;
    formData.verifyUser = submitValue.result.verifyUser;
    console.log(submitValue.result.checkDate)
    formData.checkDate = submitValue.result.checkDate
    formData.checkResult = submitValue.result.checkResult
    formData.checkRemark = submitValue.result.checkRemark
    formData.verifyResult = submitValue.result.verifyResult
    formData.id = submitValue.result.id
  } else {
    if(status == '5'){
      isReadOnly.value = !isReadOnly.value; // 切换只读状态
    }
    formData.checkDate = dayjs().format('YYYY-MM-DD HH:mm:ss')
    formData.checkResult = 0
    formData.checkRemark = ''
    formData.verifyResult = ''
    formData.id = ''
    formData.checkUser = records.checkUser;
    formData.verifyUser = records.verifyUser;
  }
  formData.verificationMethod = records.verificationMethod;
  formData.explainType = records.explainType;
  formData.apparatusName = records.apparatusName;
  formData.apparatusType = records.apparatusType;
  formData.permissibleError = records.permissibleError;
  formData.apparatusId = records.apparatusId;
  formData.state = ''
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
    formData[id] = ''
  })
  await getUserList();
}

async function getUserList(){
  userList.value = await mes3UserListAll();
}

function handleChange(index, name){
  formData[index] = name;
  Object.assign({}, formData[index], formData);
}

async function submitResult(){
  if (isSaving.value) return;
  
  isSaving.value = true;
  
  try {
    let scheduleSubmit ={
      id: '',
      factoryCode: '',
      deptCode: '',
      lineCode: '',
      periodScheduleId : '',
      planYear: '',
      planMonth: '',
      planWeek: '',
      checkUser: '',
      verifyUser : '',
      checkDate : '',
      dataJson: '',
      checkResult: '',
      verifyResult: '',
      verifyRemark: '',
      actionPlanTrackList: []
    }
    if(null != formData.checkDate && '' != formData.checkDate) {
      let date = scheduleValue.value.date;
      let month = scheduleValue.value[date];
      scheduleSubmit.periodScheduleId = scheduleValue.value.id;
      scheduleSubmit.factoryCode = scheduleValue.value.factoryCode;
      scheduleSubmit.deptCode = scheduleValue.value.deptCode;
      scheduleSubmit.lineCode = scheduleValue.value.lineCode;
      scheduleSubmit.planYear = month.year;
      scheduleSubmit.planMonth = month.month;
      scheduleSubmit.planWeek = month.num;
      scheduleSubmit.checkUser = formData.checkUser;
      scheduleSubmit.verifyUser = formData.verifyUser;
      scheduleSubmit.checkResult = formData.checkResult;
      scheduleSubmit.checkDate = formData.checkDate;
      scheduleSubmit.verifyResult = formData.verifyResult;
      scheduleSubmit.verifyRemark = formData.verifyRemark;
      scheduleSubmit.id = formData.id;
      let result = [];
      colValue.value.forEach(function (value, index) {
        let json = {
          id: '',
          name: '',
          type: '',
          value: ''
        }
        json.id = value.id;
        json.value = formData[value.id];
        json.name = value.fieldName;
        json.type = value.fieldType;
        result.push(json);
      })
      scheduleSubmit.dataJson = JSON.stringify(result).toString();

      //组装行动计划所需数据
      const actionForm = {
        raiseDate: formData.checkDate,
        problemDescription: formData.apparatusName,
        createDept: userInfo?.orgCode,
        problemTypeId: '20',
      }
      //获取行动计划数据
      const actionResult = await actionPlanRef.value?.setFormDataAndResultData(actionForm, false);
      console.log(actionResult)
      //行动计划数据校验
      if (false === actionResult) return;
      scheduleSubmit.actionPlanTrackList = actionResult;

      await saveOrUpdateSubmit(scheduleSubmit);
      //刷新外部表格
      emit('close');
    } else {
      msg.error({ content: '请选择核查时间！', key: 'saving' });
    }
  } catch (error) {
    msg.error({ content: '保存失败！', key: 'saving' });
  } finally {
    isSaving.value = false;
  }
}

async function handleSchedule(value){
  let date = scheduleValue.value.date;
  let month = scheduleValue.value[date];
  let resultValue = {
    id: formData.id,
    periodScheduleId: scheduleValue.value.id,
    verifyResult: value,
    planMonth : month.num
  }
  await processState(resultValue);
  emit('close');
}

function handleOpenConfigModal(){
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': scheduleValue.value.id});
}

function handleSuccess(){
  emit('close');
}

</script>

<style scoped>
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
