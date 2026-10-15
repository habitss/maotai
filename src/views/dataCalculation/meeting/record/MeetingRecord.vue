<template>
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="rowSelection">
        <!--插槽:table标题-->
        <template #tableTitle>
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
<!--          <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>-->
<!--          <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>-->

        </template>
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </BasicTable>
      <RecordAddModal @register="registerAddModal" @success="handleSuccess" />
</template>

<script lang="ts" setup>
import {reactive, ref} from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import {useModal} from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import {useI18n} from "@/hooks/web/useI18n";
import {useUserStore} from "@/store/modules/user";
import RecordAddModal from "./components/RecordAddModal.vue"
import {
  batchDelete,
  deleteOne,
  getExportUrl,
  getImportUrl,
  getList
} from "@/views/dataCalculation/meeting/record/MeetingRecord.api";
import {columns} from "@/views/dataCalculation/meeting/record/MeetingRecord.data";


const { t } = useI18n();
const { createMessage: msg } = useMessage();
const userStore = useUserStore();
const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})
const record = ref();
const title = ref();
const queryParam = reactive<any>({});


//注册table数据
const { tableContext ,onExportXls, onImportXls } = useListPage({
  tableProps: {
    columns: columns,
    api: getList,
    showActionColumn: true,
    actionColumn: {
      fixed: 'right',
      width: 150,
    },
    beforeFetch: (params) => {
      params.column = 'recordType,sort';
      params.order = 'asc';
      params.pageSize = 200;
      return Object.assign(params, {});
    },
    afterFetch:  (records) => {
      records.forEach(function (value ,index){
        if(value.recordType == 1 ){
          value.recordType = "会议内容"
        } else if(value.recordType == 2){
          value.recordType = "会议效果"
        }
      })
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
const [registerTable, { reload ,setColumns, setShowPagination,setTableData, getForm }, { rowSelection, selectedRowKeys }] = tableContext;

//新增/编辑modal
const [registerAddModal, { openModal ,setModalProps}] = useModal();
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
      popConfirm: {
        title: '是否删除？',
        confirm: handleDelete.bind(null, record),
      },
    },
  ];
}

/**
 * 新增事件
 */
function onAdd() {
  openModal(true, {'isAdd': true, 'isDetail': false,'depData': record})
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
