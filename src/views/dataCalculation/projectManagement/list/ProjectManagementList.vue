<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'project.management:mes3_project_management:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'project.management:mes3_project_management:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>

      <!-- 项目名称插槽 -->
      <template #projectNameSlot="{ record, text }">
        <a-button type="link" class="projectName" @click="handleAction(record)"><span>{{ text }}</span></a-button>
      </template>

      <!-- 立项报告插槽 -->
      <template #projectDocumentSlot="{ record, text }">
        <a-button v-if="text" type="link" @click="handleDetail(record)">查看</a-button>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <ProjectManagementModal @register="registerModal" @success="handleSuccess"></ProjectManagementModal>
    <!-- 表单区域 -->
    <ProjectManagementActionModal @register="registerActionModal" @success="handleSuccess"></ProjectManagementActionModal>
  </div>
</template>

<script lang="ts" name="project.management-projectManagement" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import ProjectManagementModal from './components/ProjectManagementModal.vue'
import ProjectManagementActionModal from './components/ProjectManagementActionModal.vue'
import { columns, columnsNode, searchFormSchema } from './ProjectManagement.data';
import { list, batchDelete } from './ProjectManagement.api';
import { pageList as weekList } from '/@//views/dataCalculation/mdm/week/MdmWeek.api';
import {getMonthArr} from '/@/utils/common/YMUtil';
import { cloneDeep } from 'lodash-es';
import { mapTableTotalSummary } from '/@/utils/common/compUtils';
const columnData = cloneDeep([...columns, ...columnsNode]);
//注册model
const [registerModal, { openModal }] = useModal();
//注册model
const [registerActionModal, { openModal: openActionModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '项目管理',
    pagination: false,
    maxColumnWidth: 90,
    ellipsis: false,
    immediate: false,
    showTableSetting: false,
    canResize: false,
    scroll: {
      y: document.body.scrollHeight - 454
    },
    formConfig: {
      labelWidth: '70px',
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      baseColProps: { span: 4 },
      autoAdvancedCol: 10,
      submitFunc: reloadData
    },
    actionColumn: {
      title: '操作',
      width: 80,
      fixed: 'right',
      customCell: (record, index, column) => customCell(index)
    },
  },
})

const [registerTable, { setProps, getColumns, getForm, setColumns, setTableData, setLoading }, { rowSelection, selectedRowKeys }] = tableContext
/**
 * 行动计划及跟进表
 */
function handleAction(record) {
  openActionModal(true, {
    record,
  });
}
/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}
/**
 * 详情
*/
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reloadData();
}
//合计
function onSummary(tableData){
  //行动统计
  let summary = {} as any;
  //周月统计数据
  let weekSummary = {} as any;
  //累加各项数据总和
  tableData.forEach((item, index) => {
    //因为有合并行，且周月数据只统计实际值，所有取偶数行数据统计
    if(index % 2 == 1){
      for(const key in item.summary){
        if(key != 'projectId'){
          summary[key] = (summary[key] ?? 0) + Number(item.summary[key]);
        }
      }
      for(const key in item.weekSummary){
        if(key != 'weekType'){
          console.log('key :>> ', key);
          weekSummary[key] = (weekSummary[key] ?? 0) + Number(item.weekSummary[key]);
        }
      }
    }
  });
  console.log('weekSummary :>> ', weekSummary);
  //返回数据
  let result = {
    _index: "",
    _row: "",
    projectName: '合计',
    palnOrActual: '实际值合计',
    nodeTotal: summary.nodeTotal,
    nodeFinish: summary.nodeFinishTotal,
    nodeDelay: summary.nodeDelayTotal,
    actionFinish: summary.actionFinishTotal,
    actionDelay: summary.actionDelayTotal,
    actionTotal: summary.actionTotal,
    nodeFinishRate: getPercentage(summary.nodeFinishTotal, summary.nodeTotal)+'%',
    nodeDelayRate: getPercentage(summary.nodeDelayTotal, summary.nodeTotal)+'%',
    actionFinishRate: getPercentage(summary.actionFinishTotal, summary.actionTotal)+'%',
    actionDelayRate: getPercentage(summary.actionDelayTotal, summary.actionTotal)+'%',
  } as any;
  return [{...result, ...weekSummary}];
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'project.management:mes3_project_management:edit'
    }
  ]
}
//计算百分比
function getPercentage(correct, total) {
  if (total == 0) return '0.00';
  const percent = (parseInt(correct) / parseInt(total)) * 100;
  return percent.toFixed(2); // 保留两位小数
}
//刷新表格
async function reloadData() {
  setLoading(true);
  //清空数据
  await setTableData([]);
  //获取查询表单数据
  const searchForm = getForm().getFieldsValue();
  const weekAndMon = searchForm.weekAndMon || '';
  const res = await list({
    ...searchForm,
      projectName : searchForm.projectName ? `*${searchForm.projectName}*` : '',
      pageSize : 10000,
      months : searchForm.months ? searchForm.months : getMonthArr(false).map(item => item.value).join(',')
  });

  //右侧月、周列配置
  let monthColumn = [] as any;
  //月、周累复选框不为空时，获取月、周累数据并设置表格列头信息
  if (weekAndMon) {
    //获取月、周累数据列表
    const weekRes = await weekList({ pageSize: 999, year: searchForm.year, month: searchForm.months });
    //用于判断是否添加新的月累表头
    let month = 0;
    for (const item of weekRes.records) {
      //勾选月累，且当前数据月不是上月添加一次月累表头
      if (item.month != month && weekAndMon.includes('mon')) {
        month = item.month;
        monthColumn.push({
          title: month + '月',
          dataIndex: item.year+'-'+month,
          width: 70,
          customRender: ({ record }) => {
            return record.weekSummary[item.year+'-'+item.month] ?? 0;
          },
        })
      }
      //勾选周累，添加周累表头
      if (weekAndMon.includes('week')) {
        monthColumn.push({
          title: `第${item.weekNumber}周(${processingDateStr(item.startDate)}至${processingDateStr(item.endDate)})`,
          dataIndex: item.weekNumber,
          width: '74px',
          customRender: ({ record }) => {
            return record.weekSummary[item.weekNumber];
          },
        })
      }
    }
  }
  await setColumns([...columnData, ...monthColumn]);
  await setTableData(res.records);
  await setProps({ summaryFunc: onSummary });
  setLoading(false);
}

function processingDateStr(dateStr: string) {
  return dateStr.substring(5, 10);
}

function customCell(index){
  return {
    rowSpan: (index+1) % 2 === 0 ? 0 : 2,
  };
}

onMounted(async () => {
  columnData.forEach((item, i) => {
    if(i < 18){
      item.customCell = (record, index, column) => customCell(index);

      if(i > 8 && i < 17){
        item.width = 70;
      }
    }
  })
  reloadData();
})

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

.projectName{
  max-width: 100%; 
  white-space: pre-wrap;
  word-break: break-all;
  padding: 0;
  text-align: left;
  span{
    width: 100%;
    text-decoration: underline;
    color: #D92622;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>