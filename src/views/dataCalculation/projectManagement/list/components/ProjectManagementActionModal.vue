<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="'84vw'" 
    :bodyStyle="{height: '70vh'}"
    @ok="handleSubmit" :ok-text="'保存'"
    :show-ok-btn="hasPermission('project.management:mes3_project_management_action:add')" :maskClosable="false"
    @fullScreen="handleFullScreen"
  >
    <!--引用表格-->
    <BasicTable @register="registerTable" @row-dbClick="rowDbClick"  @row-click="rowClick">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'project.management:mes3_project_management_action:add'"
          @click="handleAdd(null, null)" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'project.management:mes3_project_management_action:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'project.management:mes3_project_management_action:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXlsByParams">导入</j-upload-button>
        <a-button type="link" >请双击需要操作的行进行编辑</a-button>
      </template>

      <!-- 关键节点插槽 -->
      <template #nodeSlot="{ column, record, index }">
        <a-row style="height: 100%">
          <a-col :span="21" style="height: 100%">
            <a-textarea v-model:value="record[column.dataIndex]" :placeholder="`请输入${column.customTitle}`"
              style="height: 100%;" @change="handleNodeChange(record)" />
          </a-col>
          <a-col span="3">
            <a-row style="height: 100%" type="flex" justify="center" align="middle">
              <a-col span="24">
                <a-icon class="icon" @click="handleAdd(record, index)" type="plus-square" />
              </a-col>
            </a-row>
          </a-col>
        </a-row>
      </template>

      <!-- 多文本框插槽 -->
      <template #textareaSlot="{ column, record, index }">
        <a-row v-if="isShowSlotId == record.id" style="height: 100%">
          <a-col :span="24" style="height: 100%">
            <a-textarea v-model:value="record[column.dataIndex]" :placeholder="`请输入${column.customTitle}`"
              style="height: 100%;" />
          </a-col>
        </a-row>
      </template>
      <!-- 数字输入框插槽 -->
      <template #sortSlot="{ column, record }">
        <a-input-number v-if="isShowSlotId == record.id || column.dataIndex == 'nodeSort'" v-model:value="record[column.dataIndex]" :placeholder="`请输入`"
          style="min-width: 20px !important;" @change="handleSortChange(record)"></a-input-number>
      </template>
      <!-- 人员插槽 -->
      <template #userSlot="{ column, record }">
        <a-select v-if="isShowSlotId == record.id" v-model:value="record.leaderUserId" @change="(value) => handleUserChange(value, column.dataIndex, record, true)"
          :options="USER_LIST" :fieldNames="{ label: 'realname', value: 'id' }" :showSearch="true" :filterOption>
        </a-select>
        <span v-else>
          {{ record.leaderUserIdVal ?? getListText(USER_LIST, record.leaderUserId, 'id', 'realname') }}
        </span>
      </template>
      <!-- 部门插槽 -->
      <template #deptSlot="{ record }">
        <a-select v-if="isShowSlotId == record.id" v-model:value="record.leaderDeptId" placeholder="请选择" show-search
          :getPopupContainer="() => getPopupContainer()" :options="record.leaderDeptId_options ?? getDeptOptions(record.leaderUserId)">
        </a-select>
        <span v-else>
          {{ record.leaderDeptIdVal ?? getListText(record.leaderDeptId_options ?? getDeptOptions(record.leaderUserId), record.leaderDeptId, 'key', 'label') }}
        </span>
      </template>
      <!-- 计划完成日期插槽 -->
      <template #planCompletionTimeSlot="{ record }">
        <a-date-picker v-if="isShowSlotId == record.id" v-model:value="record.planCompletionTime" @change="() => handleDateChange(record)"
          format="YYYY-MM-DD" :valueFormat="'YYYY-MM-DD'"></a-date-picker>
      </template>
      <!-- 实际完成日期插槽 -->
      <template #realCompletionTimeSlot="{ record }">
        <a-date-picker v-if="isShowSlotId == record.id" v-model:value="record.realCompletionTime" @change="() => handleDateChange(record)"
          :disabled="!record.planCompletionTime" format="YYYY-MM-DD" :valueFormat="'YYYY-MM-DD'"></a-date-picker>
      </template>
      <!-- 完成状态插槽 -->
      <template #actionStateSlot="{ record }">
        <div :style="{ background: getColor(record.actionState), borderRadius: '5px', height: '32px', lineHeight: '32px' }">
          {{ getListText(actionPlanStatusOptions, record.actionState, 'itemVal', 'itemKey') }}
        </div>
      </template>
      <!-- 激励插槽 -->
      <template #rewardSlot="{ record }">
        <a-input-number v-if="isShowSlotId == record.id" v-model:value="record.reward" :min="0" :precision="2"></a-input-number>
      </template>

      <!--操作栏-->
      <template #action="{ record, index }">
        <TableAction :actions="getTableAction(record, index)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { projectActionColumns } from '../ProjectManagement.data';
import { queryActionList, saveAction, deleteAction, getImportUrl, getExportUrl } from '../ProjectManagement.api';
import { dicList } from '/@/views/dataCalculation/mdm/dict/dict.api';
import { usePermission } from '/@/hooks/web/usePermission';
import { buildUUID } from '/@/utils/uuid';
import { getPopupContainer } from '/@/utils';
import { handleUserChange, getDeptOptions, USER_LIST, filterOption } from '/@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data';
import { addFactoryDept } from '/@/utils/common/compUtils';
import {useMessage} from "@/hooks/web/useMessage";
import dayjs from 'dayjs';
//需要编辑行的id
const isShowSlotId = ref('none');
//需要编辑的行
const operateIds = ref(new Set());

//消息提示
const { createMessage: msg } = useMessage();

//操作权限控制
const { hasPermission } = usePermission();
//查询参数
const queryParam = reactive<any>({});
// Emits声明
const emit = defineEmits(['register', 'success']);
//设置标题
const title = ref('');
//父级数据
const parentRecord = ref<any>({});
const projectId = ref<any>({});
//记录是否有重复节点
const hasRepeatNode = ref({});
//行动完成状态
const actionPlanStatusOptions = ref<any>({});

//modal打开的初始化操作
const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
  isShowSlotId.value = 'none';
  operateIds.value = new Set();
  //设置父级数据
  parentRecord.value = data?.record;
  //设置标题
  title.value = data?.record?.projectName + '项目行动计划及跟进表';
  queryParam.projectId = parentRecord.value?.id;
  projectId.value = parentRecord.value?.id;
  actionPlanStatusOptions.value = await dicList({code: 'MES3_ACTION_PLAN_FINISH'});
  reloadData();
});

//注册table数据
const { tableContext, onExportXls, onImportXlsByParams } = useListPage({
  tableProps: {
    title: '项目行动计划及跟进表',
    columns: projectActionColumns,
    pagination: false,
    showTableSetting: false,
    maxColumnWidth: 90,
    canResize: false,
    ellipsis: false,
    immediate: false,
    rowKey: 'id',
    actionColumn: {
      title: '操作',
      width: 60,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.projectName = params.projectName ? `*${params.projectName}*` : '';
      return Object.assign(params, queryParam);
    },

  },
  exportConfig: {
    name: getExportTitle,
    url: getExportUrl,
    params: queryParam,
  },
  importConfigByParams: {
    url: getImportUrl,
    success: handleSuccess,
    params: {
      projectId
    }
  },
})

const [registerTable, { setLoading, insertTableDataRecord, deleteTableDataRecord, getDataSource, setTableData, setColumns, }, { selectedRowKeys }] = tableContext;
/**
   * 操作栏
   */
function getTableAction(record, index) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除？',
        confirm: handleDelete.bind(null, record, index),
        placement: 'topLeft',
      },
      auth: 'project.management:mes3_project_management_action:delete'
    }
  ]
}

/**
 * 设置表格数据
 */
async function reloadData(){
  setLoading(true);
  const { records } = await queryActionList(queryParam);
  let oldItem = {} as any;
  //激励总和
  let rewardTotal = 0;
  //用于累加的合并行数
  let rowSpan = 0;
  records.forEach((item, index) => {
    //先设置默认合并行为0
    item.rowSpan = 0;
    if(!oldItem?.nodeId){
      oldItem.nodeId = item.nodeId;
    }
    //上一个记录不为空，且不等于当前id，设置合并行数
    else if (item.nodeId != oldItem?.nodeId) {
      //设置合并行
      records[index - rowSpan].rowSpan = rowSpan;
      //重置累加
      rowSpan = 0;
      //记录当前id
      oldItem.nodeId = item.nodeId;
    }
    if(index == records.length - 1){
      //设置合并行
      records[index - rowSpan].rowSpan = rowSpan+1;
    }
    rowSpan++;
    rewardTotal += item.reward ?? 0;
  })
  projectActionColumns[projectActionColumns.length - 2].title = `激励(合计：${rewardTotal.toFixed(2)})`
  setColumns(projectActionColumns);
  setTableData(records);
  setLoading(false);
}

/**获取导出名称 */
function getExportTitle(){
  return title.value;
}
/**
 * 新增按钮事件
 */
async function handleAdd(record, index) {
  const tempId = 'temp_' + buildUUID();
  // 创建一个新的记录对象，包含临时ID和项目ID等信息
  const newRecord = {
    id: tempId,
    nodeId: tempId,
    projectId: parentRecord.value.id,
    rowSpan: 1
  } as any;
  //如果有记录则改为记录的值，并增加合并行数
  if (record) {
    newRecord.nodeId = record.nodeId;
    newRecord.nodeSort = record.nodeSort;
    newRecord.nodeName = record.nodeName;
    newRecord.rowSpan = 0;
    index = record.rowSpan + index;
    record.rowSpan++;
  }
  //给对象添加工厂和部门编码
  addFactoryDept(newRecord);
  await insertTableDataRecord(newRecord, index ?? getDataSource().length + 1);
  isShowSlotId.value = tempId;
  operateIds.value.add(tempId);
}
/**
 * 删除事件
 */
async function handleDelete(record, index) {
  try{
    if(record.id && !record.id.startsWith('temp_')){
      await deleteAction({id: record.id, nodeId: record.nodeId});
    }
    //如果删除行的合并行数大于1，则给他下一行数据赋值，避免删除第一条数据后显示错误
    if (record.rowSpan > 1) {
      let rowSpan = record.rowSpan - 1;
      getDataSource()[index + 1].nodeSort = record.nodeSort;
      getDataSource()[index + 1].nodeName = record.nodeName;
      getDataSource()[index + 1].rowSpan = rowSpan;
    } else {
      let findData = getDataSource().find((item) => item.nodeId == record.nodeId && item.rowSpan > 0);
      if (findData?.rowSpan) {
        findData.rowSpan--;
      }
    }

    await deleteTableDataRecord(record.id);
  } catch(error) {

  }
}
/**
 * 保存事件
 */
async function handleSubmit() {
  try {
    setModalProps({ confirmLoading: true });
    if(Object.keys(hasRepeatNode.value).length > 0){
      msg.warn('数据中包含重复节点');
      return;
    }
    //需要保存的数据
    let saveData = [] as any;
    //遍历数据，找出操作过的数据进行保存
    operateIds.value.forEach((value) => {
      const findData = getDataSource().find(item => item.id == value) ?? {};
      saveData.push(findData);
    });
    //提交表单
    await saveAction(saveData);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch (error) {
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reloadData();
}

/**
 * 日期改变事件
 * @param record 列表记录
 */
function handleDateChange(record) {
  const curDate = dayjs().endOf('day');
  let planDate = record.planCompletionTime;
  let realDate = record.realCompletionTime;
  //如果计划完成时间为空，则实际完成时间为空，完成状态为空
  if (!planDate) {
    record.realCompletionTime = undefined;
    record.actionState = undefined;
    return;
  }
  let actionState = 0;
  //计划完成时间转换为当天结束时间
  planDate = dayjs(planDate).endOf('day');
  //实际完成时间为空的处理
  if (!realDate) {
    //计算计划完成时间是否在今天的日期之前
    const diffDay = planDate.diff(curDate, 'day');
    //小于0为延迟，大于0为进行中
    if (diffDay < 0) {
      actionState = 2;
    }
  } else {
    //实际完成时间非空的处理
    //实际完成时间转换为当天结束时间
    realDate = dayjs(realDate).endOf('day');
    //计算计划完成时间是否在实际完成时间之前
    const diffDay = planDate.diff(realDate, 'day');
    //小于0为延迟完成，大于0为完成
    if (diffDay < 0) {
      actionState = 4;
    } else {
      actionState = 1;
    }
  }
  record.actionState = actionState;
}
/**
 * 获取文本
 * @param list 数据源
 * @param value 值
 * @param key 比对key
 * @param resultField 返回key
 */
function getListText(list, value, key, resultField){
  let findObj = list.find(item=>item[key] == value);
  return findObj ? findObj[resultField] : '';
}

/**
 * 获取完成状态颜色
 * @param value 值
 */
function getColor(value) {
  let color = '';
  if (value == 1) {
    color = '#00FF00'
  } else if (value == 2 || value == 4) {
    color = '#FE0000'
  } else if (value == 0) {
    color = 'yellow'
  } else if (value == 3) {
    color = '#666666'
  }
  return color;
}

/**
 * 关键节点框改变事件
 * @param record 
 */
let debounceTimeout;
function handleNodeChange(record){
  clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    const findNode = getDataSource().find(item => item.nodeName == record.nodeName && item.nodeId != record.nodeId && item.rowSpan > 0);
    if(findNode){
      hasRepeatNode.value[record.nodeId] = true;
      msg.warn('节点名称重复，请重新输入');
    } else {
      delete hasRepeatNode.value[record.nodeId];
    }
    operateIds.value.add(record.id);
  }, 500);
}

function handleSortChange(record){
  operateIds.value.add(record.id);
}

//表格行双击
function rowDbClick(record){
  isShowSlotId.value = 'none';
  isShowSlotId.value = record.id;
  operateIds.value.add(record.id);
}

function handleFullScreen(isFull){
  let height = isFull ? 'calc(100vh - 104px)' : '70vh';
  setModalProps({
    bodyStyle: {height},
  })
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

.actionState {
  :deep .ant-select-disabled .ant-select-selection {
    background-color: rgba(0, 0, 0, 0) !important;
    color: black !important;
  }

  :deep .ant-select-selection-item {
    padding: 0 !important;
  }

  :deep .ant-select-arrow {
    display: none !important;
  }
}

.icon {
  cursor: pointer;
  margin-left: 4px;
}

.flex-center {
  display: flex;
  justify-content: center;
  /* 水平居中 */
  align-items: center;
  /* 垂直居中 */
  height: 100%;
  /* 确保有高度，以便垂直居中生效 */
}
</style>