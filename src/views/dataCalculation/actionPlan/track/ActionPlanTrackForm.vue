<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"
          style="margin-right: 10px;">新增行动</a-button>
        <a-button v-if="showSubmitBtn" type="primary" @click="handleSubmit" preIcon="ant-design:plus-outlined">提交</a-button>
        <div style="width: 100%;" v-if="isShowTopForm">
          <a-row>
            <a-col span="2" class="text">提出日期:</a-col>
            <a-col span="10" class="components">
              <a-date-picker v-model:value="formData.raiseDate" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                style="width: 100%;" :bordered="false"></a-date-picker>
            </a-col>
            <a-col span="2" class="text">问题分类:</a-col>
            <a-col span="10" class="components">
              <ApiTreeSelect
                v-model:value="formData.problemTypeId" 
                placeholder="请选问题分类"
                :api="queryAllTreeData"
                :showSearch="true"
                :bordered="false"
                :field-names="{children: 'childList', label: 'problemName', value: 'id',}"
                tree-node-filter-prop="problemName"
                style="margin-top: 4px; text-align: left;"
              ></ApiTreeSelect>
            </a-col>
          </a-row>
          <a-row>
            <a-col span="2" class="text">不合格KPI指标:</a-col>
            <a-col span="10" class="components">
              <a-tree-select
                v-model:value="formData.pkiInfo"
                show-search
                style="margin-top: 4px; text-align: left;"
                :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                placeholder="请选择不合格KPI指标"
                allow-clear
                tree-default-expand-all
                :tree-data="kpiTreeData"
                tree-node-filter-prop="kpiName"
                :fieldNames= "{children:'children', label:'kpiName', value: 'kpiCode' }"
                :bordered="false"
              ></a-tree-select>
            </a-col>
            <a-col span="2" class="text">重要性评价:</a-col>
            <a-col span="10" class="components">
              <JDictSelectTag v-model:value="formData.importanceEvaluation" placeholder="请选择重要性评价" :bordered="false"
                :showChooseOption=false allow-clear
                dictCode="mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_IMPORTANCE_EVALUATION'"
                style="margin-top: 4px; text-align: left;" />
            </a-col>
          </a-row>
          <a-row>
            <a-col span="2" class="text" style="height: 60px; line-height: 60px; border-bottom: 1px solid #e5e7eb;">任务/问题描述:</a-col>
            <a-col span="22" class="components">
              <a-textarea v-model:value="formData.problemDescription" placeholder="请输入任务/问题描述，且不少于8个字符" 
                allow-clear style="width: 100%; padding-left: 6px !important;"
                :bordered="false" />
            </a-col>
          </a-row>
        </div>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record }">
        <span v-if="column.dataIndex === 'raiseDate'">
          <a-date-picker v-model:value="record[column.dataIndex]" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
            :allowClear=false style="width: 100%;" :bordered="false" 
            placeholder="请选择提出日期"
            :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false ">
          </a-date-picker>
        </span>
        <span v-if="column.dataIndex === 'problemTypeId'">
          <a-tree-select
            v-model:value="record[column.dataIndex]"
            show-search
            style="margin-top: 4px; text-align: left;"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            placeholder="请选择问题分类"
            allow-clear
            tree-default-expand-all
            :tree-data="problemTreeData"
            tree-node-filter-prop="problemName"
            :fieldNames= "{children:'childList', label:'problemName', value: 'id' }"
            :bordered="false"
            :dropdownMatchSelectWidth = "false"
            :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false "
          ></a-tree-select>
        </span>
        <span v-if="column.dataIndex === 'pkiInfo'">
          <a-tree-select
            v-model:value="record[column.dataIndex]"
            show-search
            style="margin-top: 4px; text-align: left;"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            placeholder="请选择不合格KPI指标"
            allow-clear
            tree-default-expand-all
            :tree-data="kpiTreeData"
            tree-node-filter-prop="kpiName"
            :fieldNames= "{children:'children', label:'kpiName', value: 'kpiCode' }"
            :bordered="false"
            :dropdownMatchSelectWidth = "false"
            :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false "
          ></a-tree-select>
        </span>
        <span v-if="column.dataIndex === 'importanceEvaluation'">
          <JDictSelectTag v-model:value="record[column.dataIndex]" placeholder="请选择重要性评价" :bordered="false"
            :showChooseOption=false allow-clear
            :getPopupContainer="() => getPopupContainer()"
            dictCode="mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_IMPORTANCE_EVALUATION'"
            :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false "
            style="margin-top: 4px; text-align: left;" />
        </span>
        <span v-if="column.dataIndex === 'problemDescription'">
          <a-textarea v-model:value="record[column.dataIndex]" placeholder="请输入问题描述，且不少于8个字符" allow-clear style="width: 100%;"
            :bordered="false" :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false " />
        </span>

        <span v-if="column.dataIndex.includes('UserId')">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search :allowClear=true
            :getPopupContainer="() => getPopupContainer()"
            :maxTagCount="1" :filter-option="filterOption" :options="userOptions" 
            @change="(value) => handleUserChange(value, column.dataIndex, record, true) "
            :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false "
            :fieldNames="{ label: 'realname', value: 'id' }">
          </a-select>
        </span>
        <span v-if="column.dataIndex.includes('DeptId')">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :getPopupContainer="() => getPopupContainer()"
            @change="handleDeptChange(column.dataIndex, record)"
            :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false "
            :filter-option="filterDepartOption" :options="record[column.dataIndex+'_options'] ?? initDeptOptions(record, column)">
          </a-select>
        </span>
        <span v-if="column.dataIndex == 'actionTitle'">
          <a-textarea v-model:value="record[column.dataIndex]" placeholder="请输入行动，且不少于8个字符" allow-clear style="width: 100%;"
            :bordered="false" :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false " />
        </span>
        <span v-if="column.dataIndex == 'actionLevel'">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :getPopupContainer="(node) => getPopupContainer()" :options="actionLevelOptions" :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false ">
          </a-select>
        </span>
        <span v-if="column.dataIndex == 'actionMeasures'">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :allowClear=true :getPopupContainer="() => getPopupContainer()" :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false ">
            <a-select-option value="1">预防</a-select-option>
            <a-select-option value="2">纠正</a-select-option>
          </a-select>
        </span>
        <span v-if="column.dataIndex == 'enterMeeting'">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :getPopupContainer="(node) => getPopupContainer()" :options="meetingOptions" :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false ">
          </a-select>
        </span>
        <span v-if="column.dataIndex == 'planFinishDate'">
          <a-date-picker v-model:value="record[column.dataIndex]" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
            :allowClear=false style="width: 100%;" :bordered="false" :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false "></a-date-picker>
        </span>
        <span v-if="column.dataIndex == 'status'">
          <a-switch v-model:checked="record[column.dataIndex]" 
          :checkedValue="'1'" :unCheckedValue="'0'"
          :disabled="!(record.id+'').includes('temp_') && record.status == 1 ? true : false " />
        </span>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="action.plan-actionPlanTrack-add" setup>
import { onMounted, reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { entryColumns, entryLeftColumns, rules, getDeptOptions, handleDeptChange, handleUserChange } from './ActionPlanTrack.data';
import { buildUUID } from '/@/utils/uuid';
import { getPopupContainer } from '/@/utils';
import { useUserStore } from '/@/store/modules/user';
import { addBatch, departListAll,deleteOne} from './ActionPlanTrack.api';
import { list as kpiList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
import { getDictItems } from '@/api/common/api';
import { userList } from '@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
import { cloneDeep } from 'lodash-es';
import { useMessage } from '/@/hooks/web/useMessage';
import dayjs from 'dayjs';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import ApiTreeSelect from '/@/components/Form/src/components/ApiTreeSelect.vue';
import {queryAllTreeData} from '/@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
const props = defineProps({
  //是否初始化数据
  initRecord: { type: Boolean, default: true },
  //是否显示头部表单
  isShowTopForm: { type: Boolean, default: true },
  //是否显示提交按钮
  isShowSubmitBtn: { type: Boolean, default: true },
});
// Emits声明
const emit = defineEmits(['success']);
//是否显示提交按钮
const showSubmitBtn = ref(props.isShowSubmitBtn);
//注册消息提示组件
const { createMessage } = useMessage();
//获取用户信息
const { userInfo } = useUserStore();
//用户下拉列表数据
const userOptions = ref([] as any);
//部门下拉列表数据
const deptData = ref([] as any);
//会议下拉列表数据
const meetingOptions = ref([]);
//行动级别下拉列表数据
const actionLevelOptions = ref([]);
//KPI下拉列表数据
const kpiTreeData = ref([] as any);
//KPI下拉列表数据
const problemTreeData = ref([] as any);
//保存或返回数据
const isSaveOrResult = ref('save');
//是否在列表显示输入表单
const isInputFormRef = ref(false);
//是否在列表显示实际完成时间
const isShowFinishDateRef = ref(false);
//提交表单数据
const formData = reactive<any>({
  raiseDate: dayjs().format('YYYY-MM-DD'),
  problemTypeId: undefined,
  problemDescription: undefined,
  pkiInfo: undefined,
  importanceEvaluation: undefined,
  createDept: userInfo?.orgCode,
  pathText: undefined,
  fileObj: undefined,
});
//列表默认对象
const record = {
  id: 'temp_' + buildUUID(),
  actionTitle: undefined,
  putUserId: userInfo?.id,
  putDeptId: undefined,
  putDeptId_options: undefined,
  dutyUserId: undefined,
  dutyDeptId: undefined,
  dutyDeptId_options: undefined,
  executeUserId: undefined,
  executeDeptId: undefined,
  executeDeptId_options: undefined,
  verifyUserId: userInfo?.id,
  verifyDeptId: undefined,
  verifyDeptId_options: undefined,
  planFinishDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
  actionLevel: '1',
  actionMeasures: undefined,
  enterMeeting: '0',
  status: '1',
} as any;

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    columns: entryColumns,
    canResize: false,
    pagination: false,
    showTableSetting: false,
    actionColumn: {
      width: 80,
      fixed: 'right'
    },
  },
})

const [registerTable, { insertTableDataRecord, deleteTableDataRecord, setTableData, getDataSource, setLoading, setColumns }] = tableContext

/**
 * 新增事件
 */
async function handleAdd() {
  let newRecord = cloneDeep(record);
  if(isInputFormRef.value === true){
    newRecord = {...cloneDeep(formData), ...newRecord};
    newRecord.raiseDate = dayjs().format('YYYY-MM-DD');
  }
  newRecord.id = 'temp_' + buildUUID();
  newRecord.createDept = userInfo?.orgCode,
  handleUserChange(userInfo?.id, 'putUserId', newRecord, false);
  handleUserChange(userInfo?.id, 'verifyUserId', newRecord, false);
  //在第一行插入一条新数据
  await insertTableDataRecord(newRecord, 0);
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  if(!record.id.includes('temp_')){
    await deleteOne({id: record.id}, deleteSuccess);
  }
  await deleteTableDataRecord(record.id);
}
function deleteSuccess(){
  emit('success');
}
/**
 * 提交事件
 */
async function handleSubmit() {
  try {
    if (!validateData()) return false;
    setLoading(true);
    //提交数据
    let saveData = [] as any;
    //解析数据
    const tableData = getDataSource();
    for (const item of tableData) {
      let newItem = cloneDeep(item);
      if((newItem.id+'').indexOf('temp_') > -1){
        delete newItem.id;
      }
      //计算剩余天数
      newItem.surplusDay = calcDay(newItem.planFinishDate, formData.raiseDate);
      //获取提出人相关信息
      const findPutUser = userOptions.value.find(user => user.id === newItem.putUserId);
      newItem.putUserName = findPutUser?.realname;
      //获取执行人相关信息
      const findExecuteUser = userOptions.value.find(user => user.id === newItem.executeUserId);
      newItem.executeUserName = findExecuteUser?.realname;
      //获取确认人相关信息
      const findVerifyUser = userOptions.value.find(user => user.id === newItem.verifyUserId);
      newItem.verifyUserName = findVerifyUser?.realname;
      //获取责任人相关信息
      const findDutyUser = userOptions.value.find(user => user.id === newItem.dutyUserId);
      newItem.dutyUserName = findDutyUser?.realname;

      if(isInputFormRef .value!== true){
        newItem = {
          ...newItem,
          ...formData
        }
      }
      saveData.push(newItem);
    }
    if(isSaveOrResult.value === 'save'){
      await addBatch(saveData);
      handleSuccess();
    } else {
      return saveData;
    }
  } catch (error) {
    console.log('error :>> ', error);
    return;
  } finally {
    setLoading(false);
  }
}
//计算剩余天数
function calcDay(startDate, endDate){
  //将日期转换成最后一秒，避免相差一天计算出来的天数为0
  startDate = dayjs(startDate).endOf('day');
  endDate = dayjs(endDate).endOf('day');
  return startDate.diff(endDate, 'day');
}
//提交数据校验
function validateData() {
  //获取列表数据
  const tableData = getDataSource();
  //提交数据校验
  if (tableData.length === 0) {
    createMessage.error('请先新增行动！');
    return false;
  }
  //非手动在列表输入，单独校验表单
  if(isInputFormRef.value !== true && !validRule(formData)){
    return false;
  }
  //校验列表
  for (const item of tableData) {
    return validRule(item);
  }
  return true;
}

function validRule(data){
  let flag = true;
  for (let key in data) {
    if(key === 'executeDeptId'){
      //选择了执行人判断是否选择了执行部门
      if(data['executeUserId'] && !data[key]){
        createMessage.error(rules[key]);
        return false;
      }
    }else if (rules[key]) {
      if(!data[key]){
        createMessage.error(rules[key]);
        flag =  false;
        break;
      } else if(rules[key+'-min-length'] && data[key].length < rules[key+'-min-length'].length){
        createMessage.error(rules[key+'-min-length'].message);
        flag =  false;
        break;
      }
    }
  };
  return flag;
}

/**
 * 成功回调
 */
function handleSuccess() {
  setTableData([record]);
  formData.raiseDate = dayjs().format('YYYY-MM-DD');
  formData.problemTypeId = undefined;
  formData.problemDescription = '';
  formData.pkiInfo = '';
  formData.importanceEvaluation = undefined;
  emit('success');
}
/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
    }
  ]
}

//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.trim().toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

//部门下拉选择过滤方法
function filterDepartOption(input, option) {
  return option.label.trim().toLowerCase().indexOf(input.toLowerCase()) >= 0;
};


//复制添加事件
async function copyAdd(copyRecord, isUpdate, dataId) {
  delete copyRecord.pathText;
  delete copyRecord.fileObj;
  await initData();
  let newRecord =cloneDeep(copyRecord);
  //处理数据，获取顶部所需要的值
  processinFormgData(newRecord);
  if(isUpdate !== true){
    //隐藏提交按钮
    showSubmitBtn.value = false;
    //复制数据到表格中，只保留所需要的数据
    newRecord = cloneDeep(record);
    delete newRecord.planFinishDate
    for (const key in record) {
      if(key != 'id'){
        newRecord[key] = copyRecord[key];
      }
    }
  }
  newRecord.putDeptId_options = await getDeptOptions(newRecord.putUserId);
  newRecord.dutyDeptId_options = await getDeptOptions(newRecord.dutyUserId);
  newRecord.executeDeptId_options = await getDeptOptions(newRecord.executeUserId);
  newRecord.verifyDeptId_options = await getDeptOptions(newRecord.verifyUserId);
  await setTableData([newRecord]);
  record.dataId = dataId;
}

/**
 * 其他页面引入调用方法
 * @param formRecord 头部表单数据
 * @param recordList 行动计划表格数据
 * @param isInputForm 是否需要手动输入表单数据
 * @param isShowFinishDate 是否显示实际完成日期
 */
async function otherPageInit(formRecord, recordList, isInputForm, isShowFinishDate) {
  isInputFormRef.value = false;
  isInputFormRef.value = isInputForm;
  isShowFinishDateRef.value = isShowFinishDate;
  //复制一份数据，并设置指定列显示
  let newColumns = cloneDeep(entryColumns);
  for(const item of newColumns){
    if(item.dataIndex === 'status'){
      item.ifShow = true;
    }else if(item.dataIndex === 'practicalFinishDate' && isShowFinishDate === true){
      item.ifShow = true;
    } 
  }
  if(isInputForm === true){
    newColumns = [...entryLeftColumns, ...newColumns]
  }
  setColumns(newColumns);
  //初始化数据
  await initData();
  //设置表单数据
  processinFormgData(formRecord);
  //设置列表数据
  await setTableData(recordList);
}

//获取已保存数据的单位选择项
function initDeptOptions(record, column){
  const prefix = column.dataIndex.substr(0, column.dataIndex.indexOf('DeptId'));
  let deptOptions = record[prefix+'DeptId_options'];

  //设置下拉数据，等于undefined说明未初始化过，调用一遍handleUserChange方法初始化下拉数据
  if(deptOptions == undefined){
    handleUserChange(record[prefix+'UserId'], prefix+'UserId', record, false);
  } else {
    return deptOptions;
  }
}

/**
 * 设置表单数据
 * @param data 表单数据
 * @param saveOrResult 是否需要保存数据, 不保存则返回数据
 */
async function setFormDataAndResultData(data, saveOrResult){
  //判断列表数据是否为空，不为空则提交数据或返回数据
  if(getDataSource().length > 0){
    isSaveOrResult.value = saveOrResult ?? 'result';
    //处理formData所需数据
    await processinFormgData(data);
    return handleSubmit();
  }
}

//处理formData所需数据
function processinFormgData(data){
  if(!data){
    return;
  }
  for (const key in formData) {
    formData[key] = data[key];
  }
}

//初始化数据
async function initData(){
  setLoading(true);
  //获取用户下拉选项数据
  userOptions.value = await userList({});
  //获取部门下拉选项数据
  deptData.value = await departListAll({});
  //获取会议下拉选项数据
  meetingOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_MEETING'");
  //获取行动级别下拉选项数据
  actionLevelOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_LEVEL'");
  //问题分类下拉
  problemTreeData.value = await queryAllTreeData({});
  //获取KPI下拉选项数据
  let kpiRes = await kpiList({pageNo: 1,pageSize: 9999});
  kpiTreeData.value = kpiRes.records;
  setLoading(false);
}

//暴露方法
defineExpose({
  handleSubmit,
  copyAdd,
  otherPageInit,
  setFormDataAndResultData
})

onMounted(async () => {
  isInputFormRef.value = false;
  if(props.initRecord){
    await initData();
    handleAdd();
  }
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

:deep(.jeecg-basic-table-header__tableTitle) {
  margin-bottom: -12px !important;
}

:deep(.jeecg-basic-table-header__tableTitle > *) {
  margin-right: 0;
}

.text {
  height: 40px;
  line-height: 40px;
  background-color: #FBFBFB;
  border: 1px solid #e5e7eb;
  border-bottom: 0;
  text-align: center;
}

.components {
  border-top: 1px solid #e5e7eb;
  text-align: center;

  >* {
    height: 100%;
  }
}
.components:last-child {
  border-right: 1px solid #e5e7eb;
}
</style>