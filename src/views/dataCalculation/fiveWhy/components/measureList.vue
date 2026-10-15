<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #action="{ record, index }">
        <TableAction :actions="getTableAction(record, index)" />
      </template>

      <!--插槽:表格内容-->
      <template #bodyCell="{ column, record, index }">
        <a-textarea v-if="column.dataIndex == 'problem' && recordType == '3'"
          v-model:value="record[column.dataIndex]" :placeholder="`请输入问题`" style="height: 100%;"
          :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType" />
        <a-textarea v-if="column.dataIndex == 'reason'"
          v-model:value="record[column.dataIndex]" :placeholder="`请输入${column.customTitle}`" style="height: 100%;"
          :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType" />
        <a-row v-if="column.dataIndex == 'solveProblem'" style="height: 100%">
          <a-col :span="!record.id.includes('temp_') && record.isEnable && record.actionType ? 24 : 21" style="height: 100%">
            <a-textarea v-model:value="record[column.dataIndex]" :placeholder="`请输入${column.customTitle}`"
              style="height: 100%;" maxlength="500" :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType" />
          </a-col>
          <a-col span="3" v-if="record.id.includes('temp_')">
            <a-row style="height: 100%" type="flex" justify="center" align="middle">
              <a-col span="24">
                <!-- 添加图标 -->
                <a-icon class="icon" @click="handleAddRow(null, null, index, record.reason)" type="plus-square" />
              </a-col>
              <a-col span="24">
                <!-- 删除图标 -->
                <a-popconfirm
                    title="是否确认删除"
                    ok-text="确认"
                    cancel-text="取消"
                    @confirm="handleDeleteRow(record)"
                  >
                    <a-icon class="icon" type="minus-square" />
                  </a-popconfirm>
              </a-col>
            </a-row>
          </a-col>
        </a-row>
        <!-- 人员选择 -->
        <span v-if="(column.dataIndex ?? '').endsWith('UserId')">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :allowClear=true :getPopupContainer="() => getPopupContainer()" :maxTagCount="1"
            :filter-option="filterOption" :options="userOptions"
            @change="(value) => handleUserChange(value, column.dataIndex, record, true)"
            :fieldNames="{ label: 'realname', value: 'id' }" :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType">
          </a-select>
        </span>
        <!-- 部门选择 -->
        <span v-if="column.dataIndex.includes('DeptId')">
          <a-select v-model:value="record[column.key]" placeholder="请选择" :bordered="false" show-search
            @change="handleDeptChange(column.key, record)"
            :getPopupContainer="() => getPopupContainer()"
             :filter-option="deptFilterOption" :options="initDeptOptions(record, column)"
             :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType">
          </a-select>
        </span>
        <span v-if="(column.dataIndex ?? '').endsWith('Date')">
          <a-date-picker v-model:value="record[column.dataIndex]" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
            :allowClear=true style="width: 100%;" :bordered="false"
            @change="e => handlePlanDateChange(e, record, column.dataIndex)"
            :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType"></a-date-picker>
        </span>
        <span v-if="column.dataIndex == 'actionLevel'">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :getPopupContainer="() => getPopupContainer()" :options="actionLevelOptions"
            :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType">
          </a-select>
        </span>
        <span v-if="column.dataIndex == 'problemTypeId'">
          <JTreeSelect v-model:value="record[column.dataIndex]" placeholder="请选问题分类" :bordered="false"
            :showChooseOption=false allow-clear
            :getPopupContainer="() => getPopupContainer()"
            :dropdownMatchSelectWidth = false
            :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType"
            dict="mes3_action_plan_problem_type where del_flag = '0', problem_name, id"
            pidField="parent_id"
            hasChildField="has_child"
            style="margin-top: 4px; text-align: left;" />
        </span>
        <span v-if="column.dataIndex == 'isEnable' || column.dataIndex == 'actionType'">
          <a-checkbox v-model:checked="record[column.dataIndex]" :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType"></a-checkbox>
        </span>
        <span v-if="column.dataIndex == 'actionMeasures'">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :getPopupContainer="() => getPopupContainer()" allow-clear :disabled="!record.id.includes('temp_') && record.isEnable && record.actionType">
            <a-select-option value="1">预防</a-select-option>
            <a-select-option value="2">纠正</a-select-option>
          </a-select>
        </span>
        <span v-if="column.dataIndex == 'finishState'">
          <a-select v-model:value="record[column.dataIndex]" placeholder="请选择" :bordered="false" show-search
            :getPopupContainer="() => getPopupContainer()" :options="actionPlanFnishOptions" disabled>
          </a-select>
        </span>
      </template>
    </BasicTable>
    <!-- 行动计划modal -->
    <ActionPlanTrackEditModal @register="registerEditModal" @success="reloadTable"></ActionPlanTrackEditModal>
  </div>
</template>

<script lang="ts" name="dataCalculation-fiveWhy-measureList" setup>
import { onMounted, reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { measureColumns, finishVerifyStateOptions } from '../fiveWhy.data';
import { getDictItems, queryTreeList } from '@/api/common/api';
import { userList } from '@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
import { useUserStore } from '/@/store/modules/user';
import { getExportUrl, detailList } from '../fiveWhy.api';
import { buildUUID } from '/@/utils/uuid';
import { cloneDeep } from 'lodash-es';
import { getPopupContainer } from '/@/utils';
import { useMessage } from '/@/hooks/web/useMessage';
import JTreeSelect from '/@/components/Form/src/jeecg/components/JTreeSelect.vue';
import { exportExcel } from '/@/utils/common/exportToExcelUtils';
import { useModal } from '/@/components/Modal';
import { handlePlanDateChange, handleDeptChange, handleUserChange } from '/@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data';
import ActionPlanTrackEditModal from '/@/views/dataCalculation/actionPlan/track/components/ActionPlanTrackEditModal.vue';
import {list as actionPlanProblemList} from '/@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';

const measureData = ref<any[]>([])
const queryParam = reactive<any>({});
//行动计划编辑modal
const [registerEditModal, { openModal: openEditModal }] = useModal();
//类型
const recordType = ref('');

//获取当前用户信息
const { userInfo } = useUserStore();
//注册消息提示组件
const { createMessage } = useMessage();
//用户下拉列表数据
const userOptions = ref([] as any);
//部门树下拉列表数据
const deptTreeData = ref([] as any);
//所有部门数据
const allDept = ref({} as any);
//行动级别下拉列表数据
const actionLevelOptions = ref([] as any);
//行动完成状态下拉列表数据
const actionPlanFnishOptions = ref([] as any);
//所有问题分类数据
const allProblemType = ref({} as any);
//提交验证规则列表
const rules = {
  solveProblem: '请输入问题解决',
  putUserId: '请选择提出人',
  putDeptId: '请选择提出部门',
  dutyUserId: '请选择责任人',
  dutyDeptId: '请选择责任部门',
  executeDeptId: '请选择执行部门',
  verifyUserId: '请选择确认人',
  verifyDeptId: '请选择确认部门',
  planFinishDate: '请选择计划完成日期',
  actionLevel: '请选择行动级别',
  problemTypeId: '请选择问题分类',
};

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    columns: measureColumns,
    canResize: false,
    showTableSetting: false,
    pagination: false,
    actionColumn: {
      title: '操作',
      width: 80,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "5WHY措施表",
    url: getExportUrl,
    params: queryParam,
  },
})

const [registerTable, { getDataSource, setTableData, getColumns, setColumns, insertTableDataRecord, deleteTableDataRecord, setLoading }] = tableContext;
/**
 * 操作栏
 */
function getTableAction(record, index) {
  return [
    {
      label: '行动计划',
      onClick: handleEdit.bind(null, record),
      disabled: !(record.actionType && record.id.indexOf('temp_') < 0),
      auth: 'action.plan:mes3_action_plan_track:edit'
    },
  ]
}

/**
 * 处理计划事件
 */
function handleEdit(record: Recordable) {
  openEditModal(true, {
    record: {fiveWhyMeasuresId: record.id},
    isUpdate: true,
    showFooter: true,
  });
}

//删除事件
async function handleDeleteRow(record) {
  //删除当前行数据
  await deleteTableDataRecord(record.id);
}

/**
 * 添加事件
 * @param record 行数据
 * @param index 行下标
 * @param column 列数据
 */
async function handleAddRow(fiveWhyId, problem, index, reason) {
  const newRecord = {
    id: 'temp_' + buildUUID(),
    fiveWhyId: fiveWhyId,
    problem: problem,
    reason: reason,
    actionType: true,
    putUserId: userInfo?.id,
    verifyUserId: userInfo?.id,
    createDept: userInfo?.orgCode,
    isEnable: true,
    type: 3
  };
  handleUserChange(userInfo?.id, 'putUserId', newRecord, false);
  handleUserChange(userInfo?.id, 'verifyUserId', newRecord, false);
  await insertTableDataRecord(newRecord, index + 1);
}

//父级调用的添加预防和纠正措施数据，并设置合并规则
async function addMeasure(fiveWhyId, problem, type) {
  recordType.value = type;
  handleAddRow(fiveWhyId, problem, getDataSource().length, null);
  setCustomCell();
}

//父级调用的修改对应原因
async function changeReason(fiveWhyId, value) {
  for (const item of getDataSource()) {
    if (item.fiveWhyId === fiveWhyId) {
      item.reason = item.id.includes('temp_') ? value : item.reason;
      return;
    }
  }
}

//设置合并
function setCustomCell() {
  const newColumnArr = cloneDeep(getColumns());
  newColumnArr.forEach((item, colIndex) => {
    if (item.dataIndex != 'finishState') {
      item.customCell = (record, index, column) => {
        let rowSpan = 1;
        if (column?.dataIndex === 'problem') {
          if (index === 0) {
            rowSpan = getDataSource().length + 1;
          } else {
            rowSpan = 0;
          }
        }
        //用当前行列坐标获取合并规则，如果当前列为0则不渲染，否则渲染合并行数
        return {
          rowSpan: rowSpan,
          style: {
            'height': '80px'
          }
        };
      }
    }
  });
  //重新设置表格columns数据
  setColumns(newColumnArr);
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

//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

//部门下拉选择过滤方法
function deptFilterOption(input, option) {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

//设置措施列表数据
function setMeasureData(data) {
  measureData.value = data;
  setTableData(data);
  setCustomCell();
}

//获取措施列表数据
function getMeasureData() {
  if (!validateData()) return;
  //提交数据
  let saveData = [] as any;
  //解析数据
  const tableData = getDataSource();
  for (const item of tableData) {
    let newItem = cloneDeep(item);
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

    saveData.push(newItem);
  }
  return saveData;
}

//提交数据校验
function validateData() {
  //获取列表数据
  const tableData = getDataSource();
  //校验列表
  for (const item of tableData) {
    for (let key in rules) {
      if(key === 'executeDeptId'){
        //选择了执行人判断是否选择了执行部门
        if(item['executeUserId'] && !item[key]){
          createMessage.error(rules[key]);
          return false;
        }
      }else if (!item[key]) {
        createMessage.error(rules[key]);
        return false;
      }
    }
  }
  return true;
}

function onExportXlsss() {
  const columnMap = {} as any;
  for (const item of measureColumns) {
    columnMap[item.dataIndex as string] = item.title;
  }
  let newArr = [] as any;
  const tableData = cloneDeep(measureData.value);
  for (const item of tableData) {
    //获取提出人相关信息
    const findPutUser = userOptions.value.find(user => user.id === item.putUserId);
    if(findPutUser){
      item.putUserId = findPutUser?.realname;
      if(item.putDeptId && allDept.value[item.putDeptId]){
        item.putDeptId = allDept.value[item.putDeptId].departName;
      }
    }
    //获取确认人相关信息
    const findVerifyUser = userOptions.value.find(user => user.id === item.verifyUserId);
    if(findVerifyUser){
      item.verifyUserId = findVerifyUser?.realname;
      if(item.verifyDeptId && allDept.value[item.verifyDeptId]){
        item.verifyDeptId = allDept.value[item.verifyDeptId].departName;
      }
    }
    //获取责任人相关信息
    const findDutyUser = userOptions.value.find(user => user.id === item.dutyUserId);
    if(findDutyUser){
      item.dutyUserId = findDutyUser?.realname;
      if(item.dutyDeptId && allDept.value[item.dutyDeptId]){
        item.dutyDeptId = allDept.value[item.dutyDeptId].departName;
      }
    }
    //获取执行人相关信息
    const findExecuteUserId = userOptions.value.find(user => user.id === item.executeUserId);
    if(findExecuteUserId){
      item.executeUserId = findExecuteUserId?.realname;
      if(item.executeDeptId && allDept.value[item.executeDeptId]){
        item.executeDeptId = allDept.value[item.executeDeptId].departName;
      }
    }
    //行动级别
    const findActionLevel = actionLevelOptions.value.find(al => al.value === item.actionLevel);
    if(findActionLevel){
      item.actionLevel = findActionLevel?.text;
    }
    //完成情况
    const findFinishState = actionPlanFnishOptions.value.find(al => al.value === item.finishState);
    if(findFinishState){
      item.finishState = findFinishState?.text;
    }
    //完成情况确认
    const findFinishVerifyState = finishVerifyStateOptions.find(al => al.value === item.finishVerifyState);
    if(findFinishVerifyState){
      item.finishVerifyState = findFinishVerifyState?.label;
    }
    //问题分类
    const findProblemType = allProblemType.value.find(ele => ele.id === item.problemTypeId);
    if(findProblemType){
      item.problemTypeId = findProblemType.problemName;
    }

    //行动性质
    if(item.actionMeasures){
      item.actionMeasures = item.actionMeasures == 1 ? '预防' : '纠正';
    }
    item.isEnable = item.isEnable ? '是' : '否';
    item.actionType = item.actionType ? '是' : '否';
    let obj = {} as any;
    for (const key in columnMap) {
      if (key != 'id') {
        obj[columnMap[key]] = item[key];
      }
    }
    newArr.push(obj);
  }
  //导出列表
  exportExcel('5W措施', newArr, null);
}

// 获取导出数据（供父组件调用）
function getExportData() {
  const columnMap = {} as any;
  for (const item of measureColumns) {
    columnMap[item.dataIndex as string] = item.title;
  }
  const header = Object.values(columnMap);
  const data = [] as any;
  const tableData = cloneDeep(measureData.value);
  for (const item of tableData) {
    //获取提出人相关信息
    const findPutUser = userOptions.value.find(user => user.id === item.putUserId);
    if(findPutUser){
      item.putUserId = findPutUser?.realname;
      if(item.putDeptId && allDept.value[item.putDeptId]){
        item.putDeptId = allDept.value[item.putDeptId].departName;
      }
    }
    //获取确认人相关信息
    const findVerifyUser = userOptions.value.find(user => user.id === item.verifyUserId);
    if(findVerifyUser){
      item.verifyUserId = findVerifyUser?.realname;
      if(item.verifyDeptId && allDept.value[item.verifyDeptId]){
        item.verifyDeptId = allDept.value[item.verifyDeptId].departName;
      }
    }
    //获取责任人相关信息
    const findDutyUser = userOptions.value.find(user => user.id === item.dutyUserId);
    if(findDutyUser){
      item.dutyUserId = findDutyUser?.realname;
      if(item.dutyDeptId && allDept.value[item.dutyDeptId]){
        item.dutyDeptId = allDept.value[item.dutyDeptId].departName;
      }
    }
    //获取执行人相关信息
    const findExecuteUserId = userOptions.value.find(user => user.id === item.executeUserId);
    if(findExecuteUserId){
      item.executeUserId = findExecuteUserId?.realname;
      if(item.executeDeptId && allDept.value[item.executeDeptId]){
        item.executeDeptId = allDept.value[item.executeDeptId].departName;
      }
    }
    //行动级别
    const findActionLevel = actionLevelOptions.value.find(al => al.value === item.actionLevel);
    if(findActionLevel){
      item.actionLevel = findActionLevel?.text;
    }
    //完成情况
    const findFinishState = actionPlanFnishOptions.value.find(al => al.value === item.finishState);
    if(findFinishState){
      item.finishState = findFinishState?.text;
    }
    //完成情况确认
    const findFinishVerifyState = finishVerifyStateOptions.find(al => al.value === item.finishVerifyState);
    if(findFinishVerifyState){
      item.finishVerifyState = findFinishVerifyState?.label;
    }
    //问题分类
    const findProblemType = allProblemType.value.find(ele => ele.id === item.problemTypeId);
    if(findProblemType){
      item.problemTypeId = findProblemType.problemName;
    }

    //行动性质
    if(item.actionMeasures){
      item.actionMeasures = item.actionMeasures == 1 ? '预防' : '纠正';
    }
    item.isEnable = item.isEnable ? '是' : '否';
    item.actionType = item.actionType ? '是' : '否';
    const obj = {} as any;
    for (const key in columnMap) {
      obj[columnMap[key]] = item[key];
    }
    data.push(obj);
  }
  return { header, data };
}

function flattenTree(array) {
  let obj = {};

  function traverse(nodes) {
    for (const node of nodes) {
      obj[node.id ?? node.key] = node; // 使用节点的 id 作为键

      if (node.children && node.children.length > 0) {
        traverse(node.children);
      }
    }
  }
  traverse(array);
  return obj;
}

async function reloadTable(){
  const { records } = await detailList({recordId: measureData.value[0].recordId, pageSize: 100 });
  setTableData(records);
}

onMounted(async () => {
  setLoading(true);
  const {records} = await actionPlanProblemList({pageSize: 9999});
  allProblemType.value = records;
  //获取用户下拉选项数据
  userOptions.value = await userList({});
  //获取部门下拉选项数据
  deptTreeData.value = await queryTreeList({});
  allDept.value = flattenTree(deptTreeData.value);
  //获取行动级别下拉选项数据
  actionLevelOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_LEVEL'");
  //完成情况确认
  actionPlanFnishOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_FINISH'");
  setLoading(false);
})

//暴露方法
defineExpose({
  addMeasure,
  changeReason,
  getMeasureData,
  setMeasureData,
  onExportXlsss,
  getExportData,
  reloadTable
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-table-cell .ant-table-cell-ellipsis) {
  min-height: 100px !important;
}

.icon {
  cursor: pointer;
}
</style>