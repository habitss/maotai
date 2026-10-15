<template>
  <BasicModal
    @register="registerModal"
    title="会议模版配置"
    v-bind="$attrs"
    :showOkBtn="false"
    :showCancelBtn="false"
    destroyOnClose
    :maskClosable="false"
  >
  <div style="margin: 0">
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" @click="onSchedule"> 自动排期</a-button>
        <a-button type="primary" @click="onAdd"> 新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>

      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <TemplateAddModal @register="registerAddModal" @success="handleSuccess" />
    <ScheduleModal @register="registerScheduleModal" @success="handleSuccess" />
  </div>
  </BasicModal>
</template>

<script lang="ts" name="dataCalculation-meeting-template-list" setup>
import {defineEmits, reactive, ref} from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import {BasicModal,useModal, useModalInner} from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import {setBaseColumns} from "./MeetingTemplate.data";
import { useI18n } from '/@/hooks/web/useI18n';
import { mapTableTotalSummary } from '/@/utils/common/compUtils';
import {
  getList,
  batchDelete,
  deleteOne,
  getExportUrl,
  getImportUrl
} from "./MeetingTemplate.api";
import TemplateAddModal from './components/TemplateAddModal.vue'
import { useUserStore } from '/@/store/modules/user';
import ScheduleModal from "@/views/dataCalculation/meeting/template/components/ScheduleModal.vue";

const { t } = useI18n();
const { createMessage: msg } = useMessage();
const userStore = useUserStore();
const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

const emit = defineEmits(['custom-event']); // 声明事件名称和类型（可选）

const record = ref();
const title = ref();
const queryParam = reactive<any>({});

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  record.value = data.record
  title.value =record.value.meetingName + '会议内容及行动跟踪记录';
  console.log(data.record)
  queryParam.factoryCode = data.record.factoryCode
  queryParam.deptCode = data.record.deptCode
  queryParam.workshop = data.record.workshop
  queryParam.lineCode = data.record.lineCode
  queryParam.meetingType = data.record.id
  Object.assign(queryParam, {dataId: record.value.id});
  setModalProps({ confirmLoading: false});
});


//注册table数据
const { tableContext , onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: title.value,
    api: getList,
    showActionColumn: true,
    expandRowByClick: true,
    actionColumn: {
      fixed: 'right',
      width: 150,
    },
    showSummary: true,
    summaryFunc: onSummary,
    beforeFetch: (params) => {
      params.column = 'sort';
      params.order = 'asc';
      params.pageSize = 50;
      return Object.assign(params, {dataId: record.value.id});
    },
    afterFetch:  (records) => {
      setColumns(setBaseColumns(title.value));
      setShowPagination(false);

    },
  },
  exportConfig: {
    name: "会议模版",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})
const [registerTable, { reload ,setShowPagination,setColumns, setTableData, getForm }, { rowSelection, selectedRowKeys }] = tableContext;

//新增/编辑modal
const [registerAddModal, { openModal ,setModalProps}] = useModal();


//新增/编辑modal
const [registerScheduleModal, { openModal: openScheduleModal}] = useModal();

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
    {
      label: '删除',
      onClick: handleDelete.bind(null, record),
    },
  ];
}

function onSummary(tableData: Recordable[]){
  const totals = mapTableTotalSummary(tableData, ['time']);
  return [totals];
}

/**
 * 自动排期事件
 */
function onSchedule(){
  openScheduleModal(true, {'isAdd': true, 'isDetail': false,'depData': record.value})
}


/**
 * 新增事件
 */
function onAdd() {
  openModal(true, {'isAdd': true, 'isDetail': false,'depData': record.value})
}

/**
 * 编辑事件
 */
function handleEdit(record) {
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 详情
 */
function handleDetail(record) {
  openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
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
  (selectedRowKeys.value = []) && reload();
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await getForm().setFieldsValue({
    lineCode: '',
  });
}

//设备下拉列表联动
async function handleTypeChange(value) {
  await getForm().updateSchema([
    {
      field: 'meetingType',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: "mes3_meeting_config, meeting_type, id, del_flag = 0 and line_code='" + value + "'",
      },
    },
  ]);
}

</script>

<style scoped>

</style>
